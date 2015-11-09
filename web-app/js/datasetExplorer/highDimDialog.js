HighDimDialog = (function() {

    // init
    var highDimDialog = {
        filterType: {},
        filterKeyword: {}
    };

    /**
     * get filter type & keywords selected by user
     * @returns {{type: *, data: Array}}
     * @private
     */
    var _getFilter = function (filterType, filterKeyword) {
        var _separator = /[,]+/; // identifiers are separated by comma (,)

        var _tokenizeChrPosition = function(strChrPos) {
            var _strChrPos = strChrPos.split(':'),
                _pos = _strChrPos[1].split('-');
            return {
                'chromosome': _strChrPos[0],
                'start': _pos[0],
                'end': _pos[1]
            }
        };

        var _filter = {
                type: filterType,
                data: [],
                inString: ''
            }; // init filter obj

        if (filterType === 'genes' || filterType === 'snps') {
            filterKeyword.split(_separator).forEach(function(d) {
                _filter.data.push(d.trim());
            });
            _filter.data.pop();
            _filter.inString = _filter.data.toString();
        } else if (filterType === 'chromosome_segment' ) {
            filterKeyword.split(_separator).forEach(function(chrPos) {
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
        console.log(filter);
        data.node.attributes.oktousevalues = "N";
        highDimDialog.createPanelItemNew(el, convertNodeToConcept(data.node), filter);
    };

    /**
     * Generate jQuery UI High Dimensional filter by identifier dialog
     * @param id
     * @private
     */
    highDimDialog.createIdentifierDialog = function (id) {

        /**
         * Get high dimensional node info.
         * @param data
         * @returns {{}}
         * @private
         */
        var _getHDNodeInfo = function( data) {
            return jQuery.ajax({
                url : pageInfo.basePath + "/HighDimension/nodeDetails",
                method : 'POST',
                data : 'conceptKeys=' + encodeURIComponent(data.node.attributes.id)
            });
        };

        /**
         *
         * @param data
         * @param gridRow
         * @returns {boolean}
         * @private
         */
        var _isCorrectHD = function (data, gridRow) {
            //console.log(data)
            //console.log(gridRow)
            var _keys = Object.keys(data);
            //console.log( _keys);
            // return _keys.indexOf('snp_lz') < 0 ? false : true;
            return _keys[0] === gridRow.dataTypeId;
        };

        var _dialog =  jQuery(id).dialog({
            autoOpen: false,
            width: 350,
            modal: true,
            open : function () {

                var _this = this;

                highDimDialog.filterType =  jQuery('#filterType');
                highDimDialog.filterKeyword = jQuery('#filterKeyword');

                jQuery('#filterForm').hide();
                jQuery('#loadingPleaseWait').show();
                jQuery('#dialog-apply-btn').button('disable');
                jQuery('#dialog-cancel-btn').button('disable');
                jQuery('#filterKeyword').val('');

                _getHDNodeInfo( _dialog.dropTarget.dropData)
                    .done(function (d) {
                        console.log('Done with response ', d);
                        _dialog.dropTarget.dropData.details = d;

                        if (_isCorrectHD(d, _dialog.dropTarget.recordData)) { // TODO refactor to match dropped & drop zone
                            jQuery('#filterForm').show();
                            jQuery('#loadingPleaseWait').hide();
                            jQuery('#dialog-apply-btn').button('enable');
                            jQuery('#dialog-cancel-btn').button('enable');
                            highDimDialog.createAutocompleteInput();
                        } else {
                            _this.dialog("close");
                        }
                    })
                    .fail(function (msg) {
                        console.error('Something wrong when checking the node ...', msg);
                        _this.dialog("close");
                    });
            },
            buttons: [
                {
                    id : 'dialog-apply-btn',
                    text : 'Apply',
                    click : function () {
                        _dialog.dropTarget.filter = _getFilter(
                            highDimDialog.filterType.val(),
                            highDimDialog.filterKeyword.val()
                        );

                        _dialog.dropTarget.recordData.subset1.each(function (subset) {
                            subset.jajal =  _dialog.dropTarget.filter ;
                        });
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

        return _dialog;
    };


    highDimDialog.createPanelItemNew = function (panel, concept, filter) {

        var li = document.createElement('div'); // was li
        var _strFilterValue = '';

        if ( typeof filter !== 'undefined') {
            if (filter.data[0] instanceof Array) {
                _strFilterValue = filter.data.toString();
            } else {
                _strFilterValue = JSON.stringify(filter.data);
            }
            li.setAttribute('conceptfiltertype', filter.type);
            li.setAttribute('conceptfiltervalues', _strFilterValue);
        }

        // convert all object attributes to element attributes so i can get them later
        // (must be a way to keep them in object?)

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
        var text = document.createTextNode(shortname + " " + valuetext + _strFilterValue ); //used to be name

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

    highDimDialog.createAutocompleteInput = function (el) {

        var _DEFAULT_TYPE = 'snps';

        var split = function ( val ) {
            return val.split( /,\s*/ );
        };

        var extractLast = function ( term ) {
            return split( term ).pop();
        };


        var _createAutocomplete = function (htmlId) {
                // Convert input text into autocomplete when user select filter type gene
                jQuery(htmlId)
                    //don't navigate away from the field on tab when selecting an item
                    .bind( "keydown", function( event ) {
                        if ( event.keyCode === jQuery.ui.keyCode.TAB &&
                            jQuery( this ).autocomplete( "instance" ).menu.active ) {
                            event.preventDefault();
                        }
                    })
                    .autocomplete({
                        source : function (req, res) {
                            var _type = highDimDialog.filterType.val();
                            if (typeof highDimDialog.filterType.val() === "undefined") {
                                _type=_DEFAULT_TYPE;
                            }
                            var search = extractLast(req.term);
                            jQuery.get(pageInfo.basePath + "/filterAutocomplete/autocomplete/" + _type, {
                                search: search
                            }, function (data) {
                                res(data);
                            });
                        },

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
                    return jQuery('<li></li>')
                        .data('item.autocomplete', item)
                        .append('<a style="color: #0000FF"> ' + item.label + '</a>')
                        .appendTo(ul);
                };
        };

        if (highDimDialog.filterType.val() === 'chromosome_segment') {
            highDimDialog.filterKeyword.autocomplete('disable');
        } else {
            if (highDimDialog.filterKeyword.is('.ui-autocomplete-input')){
                // UI draggable is loaded
                highDimDialog.filterKeyword.autocomplete('enable');
            } else {
                _createAutocomplete('#filterKeyword');
            }

        }

    };

    return highDimDialog;
})();

