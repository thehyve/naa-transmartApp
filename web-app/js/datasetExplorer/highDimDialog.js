HighDimDialog = (function() {

    var highDimDialog = {};

    /**
     * get filter type & keywords selected by user
     * @returns {{type: *, data: Array}}
     * @private
     */
    var _getFilter = function () {
        var _separator = /[,]+/; // identifiers are separated by comma (,)

        var _tokenizeChrPosition = function(strChrPos) {
            var _strChrPos = strChrPos.split(':'),
                _pos = _strChrPos[1].split('-');

            return {
                'chr': _strChrPos[0],
                'start': _pos[0],
                'end': _pos[1]
            }
        };

        var _filterKeyword = jQuery("#filterKeyword").val(),
            _filter = {
                type: jQuery("#filterType").val(),
                data: []
            }; // init filter obj
        if (_filter.type === 'genes') {
            _filterKeyword.split(_separator).forEach(function(d) {
                _filter.data.push(d.trim());
            });
            _filter.data.pop();
        } else if (_filter.type === 'chromosome_segment') {
            _filterKeyword.split(_separator).forEach(function(chrPos) {
                _filter.data.push(_tokenizeChrPosition(chrPos.trim()));
            });
        }
        return _filter;
    };

    /**
     * Stuff to be invoked when a node is dropped into a drop zone.
     * @param data
     * @param el
     * @private
     */
    highDimDialog.dropOntoVariableSelection = function (data, el, filter) {
        data.node.attributes.oktousevalues = "N";
        highDimDialog.createPanelItemNew(el, convertNodeToConcept(data.node), filter);
    };

    /**
     * Generate jQuery UI High Dimensional filter by identifier dialog
     * @param id
     * @private
     */
    highDimDialog.createIdentifierDialog = function (id) {

        var _dialog =  jQuery(id).dialog({
            autoOpen: false,
            width: 350,
            modal: true,
            buttons: [
                {
                    id : 'dialog-apply-btn',
                    text : 'Apply',
                    click : function () {
                        _dialog.dropTarget.filter = _getFilter();
                        _dialog.dialog("close");
                    }
                },
                {
                    id : 'dialog-cancel-btn',
                    text : 'Cancel',
                    click : function () {
                        _dialog.dialog( "close" );
                    }
                }
            ],
            close: function() {
                var _dt = _dialog.dropTarget;

                if (Object.keys(_dt.dropData.details).indexOf(_dt.recordData.dataTypeId) >= 0 ) {
                    highDimDialog.dropOntoVariableSelection(_dt.dropData, _dt.el, _dt.filter);
                }
            }
        });

        console.log('HighDimDialog', _dialog);
        return _dialog;
    };


    highDimDialog.createPanelItemNew = function (panel, concept, filter) {

        var li = document.createElement('div'); //was li
        //convert all object attributes to element attributes so i can get them later (must be a way to keep them in object?)
        li.setAttribute('conceptname', concept.name);
        li.setAttribute('conceptid', concept.key);
        li.setAttribute('conceptlevel', concept.level);
        li.setAttribute('concepttooltip', concept.tooltip);
        li.setAttribute('concepttablename', concept.tablename);
        li.setAttribute('conceptdimcode', concept.dimcode);
        li.setAttribute('conceptcomment', concept.comment);
        li.setAttribute('normalunits', concept.normalunits);
        li.setAttribute('setvaluemode', concept.value.mode);
        li.setAttribute('setvalueoperator', concept.value.operator);
        li.setAttribute('setvaluehighlowselect', concept.value.highlowselect);
        li.setAttribute('setvaluehighvalue', concept.value.highvalue);
        li.setAttribute('setvaluelowvalue', concept.value.lowvalue);
        li.setAttribute('setvalueunits', concept.value.units);
        li.setAttribute('oktousevalues', concept.oktousevalues);
        li.setAttribute('setnodetype', concept.nodeType);
        li.setAttribute('visualattributes', concept.visualattributes);
        li.className = "conceptUnselected";

        //Create a shortname
        var splits = concept.key.split("\\");
        var shortname = "";
        if (splits.length > 1) {
            shortname = "...\\" + splits[splits.length - 2] + "\\" + splits[splits.length - 1];
        }
        else shortname = splits[splits.length - 1];
        li.setAttribute('conceptshortname', shortname);

        //Create a setvalue description
        var valuetext = "";

        if (typeof(concept.value.mode) != "undefined") {
            valuetext = getSetValueText(
                concept.value.mode,
                concept.value.operator,
                concept.value.highlowselect,
                concept.value.highvalue,
                concept.value.lowvalue,
                concept.value.units
            );
            li.setAttribute('conceptsetvaluetext', valuetext);
        }
        else {
            li.setAttribute('conceptsetvaluetext', '');
        }
        //Create the node
        var text = document.createTextNode(shortname + " " + valuetext + '  (' +filter.data.toString() + ')'); //used to be name
        li.appendChild(text);
        panel.appendChild(li);
        Ext.get(li).addListener('click', conceptClick);
        Ext.get(li).addListener('contextmenu', conceptRightClick);
        new Ext.ToolTip({target: li, html: concept.key, dismissDelay: 10000});
        li.concept = concept;
        //return the node

        // Invalidate only when something dropped to the subset panel
        if (panel.id.indexOf("queryCriteriaDiv") > -1) {
            var subset = getSubsetFromPanel(panel);
            invalidateSubset(subset);
        }

        return li;
    };



    highDimDialog.createGeneAutocomplete = function (el) {

        console.log('highDimDialog.createGeneAutocomplete', el);

        var split = function ( val ) {
            return val.split( /,\s*/ );
        };

        var extractLast = function ( term ) {
            return split( term ).pop();
        };

        var _displayExportAutocomplete = function (d) {
            //console.log('_displayExportAutocomplete', d);
            return d.rows;
        };

        var _autocomplete = function(type,request,response) {
        	var search = extractLast(request.term)
        	console.log('_geneAutocomplete: type = ' + type + ', search = ' + search);
            jQuery.get(pageInfo.basePath + "/filterAutocomplete/autocomplete/" + type, {
                callback: '_displayExportAutocomplete',
                search: search
            }, function (data) {
            	console.log('_geneAutocomplete: data = ' + data);
                response(data);
            });
        	/*
            jQuery.get(pageInfo.basePath + "/search/loadSearchPathways", {
                callback: '_displayExportAutocomplete',
                query: extractLast(request.term)
            }, function (data) {
                response(eval(data));

            });
            */
        };
        
        var _typedAutocomplete = function(type) {
        	console.log('creating autocompleter for type ' + type);
        	return function(request,response) {
        		return _autocomplete(type,request,response);
        	}
        }

        // Convert input text into autocomplete when user select filter type gene
        // ----
        console.log('filter for ' + el.value)
        //if (el.value === 'genes' ) {
            jQuery("#filterKeyword")
                //don't navigate away from the field on tab when selecting an item
                .bind( "keydown", function( event ) {
                    if ( event.keyCode === jQuery.ui.keyCode.TAB &&
                        jQuery( this ).autocomplete( "instance" ).menu.active ) {
                        event.preventDefault();
                    }
                })
                .autocomplete({
                    source: _typedAutocomplete(el.value),
                    minLength: 2,
                    search: function() {
                        // custom minLength
                        var term = extractLast( this.value );
                        if ( term.length < 2 ) {
                            return false;
                        }
                    },
                    focus: function() {
                        // prevent value inserted on focus
                        return false;
                    },
                    select: function( event, ui ) {

                        var terms = split( this.value );
                        // remove the current input
                        terms.pop();
                        // add the selected item
                        terms.push( ui.item.value );
                        // add placeholder to get the comma-and-space at the end
                        terms.push( "" );
                        this.value = terms.join( ", " );
                        return false;
                    }
                })
                .data('autocomplete')._renderItem = function(ul, item) {
                console.log(item);
                return jQuery('<li></li>')
                    .data('item.autocomplete', item)
                    .append('<a><span style="color: #0000FF"> ' + item.label + '</span> &raquo; <strong>' +
                    item.keyword + '</strong> ' + item.synonyms + '</a>')
                    .appendTo(ul);
            };
        //} else {
        //    jQuery("#filterKeyword").empty();
        //}
    };

    return highDimDialog;
})();

