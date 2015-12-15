AutocompleteInput = (function() {


    var _splitF = function ( val ) {
        return val.split( /,\s*/ );
    };

    var _extractLastF = function ( term ) {
        return _splitF( term ).pop();
    };

    var service = {
        uri : '',
        uiComponent : {
            minLength: 2,
            search: function() {
                // custom minLength
                var term = _extractLastF( this.value );
                if (term.length < 2) {
                    return false;
                }
            },
            focus: function() {
                // prevent value inserted on focus
                return false;
            },
            select: function( event, ui ) {
                var terms = _splitF( this.value );

                // remove the current input
                terms.pop();

                // add the selected item
                terms.push( ui.item.value );

                // add placeholder to get the comma-and-space at the end
                terms.push( "" );

                this.value = terms.join( ", " );
                return false;
            }
        }
    };

    service.create = function (htmlId, uri) {

        var _ui = service.uiComponent;

        if (typeof uri !== 'undefined') {
            service.uri = uri;
        }

        _ui.source = function (req, res) {
            jQuery.get(service.uri, {
                search: _extractLastF(req.term)
            }, function (data) {
                res(data);
            });
        };

        return jQuery('#'+ htmlId)
            // don't navigate away from the field on tab when selecting an item
            .bind( "keydown", function( event ) {
                if ( event.keyCode === jQuery.ui.keyCode.TAB &&
                    jQuery( this ).autocomplete( "instance" ).menu.active ) {
                    event.preventDefault();
                }
            })
            .autocomplete(_ui)
            .data('autocomplete')._renderItem = function(ul, item) {
            return jQuery('<li></li>')
                .data('item.autocomplete', item)
                .append('<a style="color: #0000FF"> ' + item.label + '</a>')
                .appendTo(ul);
        };

    };

    return service;
})();
