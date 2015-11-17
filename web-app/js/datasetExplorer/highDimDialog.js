HighDimensionDialogService = (function(autocompleteInp) {

    var _autocompleteInp = autocompleteInp;

    /**
     * Get filter type & keywords selected by user
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
            data: []
        }; // init filter obj

        if (filterType === 'genes' || filterType === 'snps') {
            filterKeyword.split(_separator).forEach(function(d) {
                _filter.data.push(d.trim());
            });
            _filter.data.pop();
            _filter.names = _filter.data;
        } else if (filterType === 'chromosome_segment' ) {
            filterKeyword.split(_separator).forEach(function(chrPos) {
                _filter.data.push(_tokenizeChrPosition(chrPos.trim()));
            });
        }

        return _filter;
    };

    /**
     * Check if dropped data has same type as the row data
     * @param data
     * @param gridRow
     * @returns {boolean}
     * @private
     */
    var _isCorrectHD = function (data, gridRow) {
        var _keys = Object.keys(data);
        console.log(_keys);
        return _keys[0] === gridRow.dataTypeId;
    };

    /**
     * Define some form elements
     * @param obj
     * @private
     */
    var _defineFormElements = function (obj) {
        obj.filterForm = jQuery('#filterForm');
        obj.filterType =  jQuery('#filterType');
        obj.filterKeyword = jQuery('#filterKeyword');
        obj.loadingEl = jQuery('#loadingPleaseWait');
        obj.applyBtn = jQuery('#dialog-apply-btn');
        obj.cancelBtn = jQuery('#dialog-cancel-btn');
    };


    /**
     * Initialize service object
     * @type {{filterType: {}, filterKeyword: {}, dialogEl, uiComponent: {autoOpen: boolean, width: number, modal: boolean, buttons: *[]}}}
     */
    var service =
    {
        filterType: {},
        filterKeyword: {},
        dialogEl: jQuery('#dialog-form'),
        uiComponent : {
            autoOpen: false,
            width: 350,
            modal: true,
            buttons: [
                {
                    id : 'dialog-apply-btn',
                    text : 'Apply',
                    click : function () {
                        var _f = _getFilter(
                            service.filterType.val(),
                            service.filterKeyword.val()
                        );
                        console.log(_f);
                        service.filter = _f;
                        if (typeof service.dropTarget !== 'undefined') {
                            service.dropTarget.filter = _f;
                        }
                        jQuery('#dialog-form').dialog("close");
                    }
                },
                {
                    id : 'dialog-cancel-btn',
                    text : 'Cancel',
                    click : function () {
                        jQuery('#dialog-form').dialog("close");
                    }
                }
            ]
        }
    };

    /**
     * void create auto complete input depends on the filter
     */
    service.createAutocompleteInput = function () {
        if (service.filterType.val() === 'chromosome_segment') {
            service.filterKeyword.autocomplete('disable');
        } else {
            var _uri = pageInfo.basePath + "/filterAutocomplete/autocomplete/" + service.filterType.val();

            if (service.filterKeyword.is('.ui-autocomplete-input')){
                _autocompleteInp.uri = _uri;
                // UI draggable is loaded
                service.filterKeyword.autocomplete('enable');
            } else {
                _autocompleteInp.create(
                    service.filterKeyword.attr('id'),
                    _uri
                );
            }
        }
    };

    /**
     * Stuff to be invoked when a node is dropped into a drop zone.
     * @param data
     * @param el
     * @private
     */
    service.dropOntoVariableSelection = function (data, el, filter) {
        if (Object.keys(data.details).indexOf(data.dataTypeId) >= 0 ) {
            data.node.attributes.oktousevalues = "N";
            service.createPanelItemNew(el, convertNodeToConcept(data.node), filter);
        }
    };

    service.createSummaryStatDialog = function (node) {

        var _s = service;

        // assign open dialog handler
        _s.uiComponent.open = function () {

            _defineFormElements(_s);
            _s.loadingEl.hide();
            _s.filterType.prop('disabled', true);

            if (service.filterKeyword.is('.ui-autocomplete-input')){
                //console.log('autocomplete already initiated, so now enabling it');
                service.filterKeyword.autocomplete('enable');
            } else {
                //console.log('autocomplete not yet initiated, so now creating it');
                _s.createAutocompleteInput();
            }

        };

        // assign close dialog handler
        _s.uiComponent.close = function () {
            // todo
            console.log('Close Dialog UI and disabling autocomplete');
            service.filterKeyword.autocomplete('disable');
        };

        // =============== //
        // dialog creation //
        // =============== //
        return jQuery('#dialog-form').dialog(_s.uiComponent);
    };

    /**
     * Generate jQuery UI High Dimensional filter by identifier dialog
     * @param id
     * @private
     */
    service.createIdentifierDialog = function (dropzone) {

        var _s = service;

        _s.dropTarget = dropzone;

        // assign open dialog handler
        _s.uiComponent.open = function () {

            _defineFormElements(_s);

            _s.filterForm.hide();
            _s.loadingEl.show();
            _s.applyBtn.button('disable');
            _s.cancelBtn.button('disable');
            _s.filterKeyword.val('');

            jQuery.ajax({
                url : pageInfo.basePath + "/HighDimension/nodeDetails",
                method : 'POST',
                data : 'conceptKeys=' + encodeURIComponent(dropzone.dropData.node.attributes.id)
            })
                .done(function (d) {
                    dropzone.dropData.details = d;
                    if (_isCorrectHD(d, dropzone.recordData)) {
                        _s.filterForm.show();
                        _s.loadingEl.hide();
                        _s.applyBtn.button('enable');
                        _s.cancelBtn.button('enable');
                        _s.createAutocompleteInput();
                    } else {
                        _s.dialogEl.dialog("close");
                    }
                })
                .fail(function (msg) {
                    console.error('Something wrong when checking the node ...', msg);
                    _s.dialogEl.dialog("close");
                });
        };

        // assign close dialog handler
        _s.uiComponent.close = function () {
            if (Object.keys(dropzone.dropData.details).indexOf(dropzone.recordData.dataTypeId) >= 0 ) {
                dropzone.dropData.node.attributes.oktousevalues = "N";
                _s.createPanelItemNew(dropzone.el, convertNodeToConcept(dropzone.dropData.node), dropzone.filter);
            }
        };

        // =============== //
        // dialog creation //
        // =============== //
       return jQuery('#dialog-form').dialog(_s.uiComponent);
    };


    service.createPanelItemNew = function (panel, concept, filter) {
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

    return service;

})(AutocompleteInput);

