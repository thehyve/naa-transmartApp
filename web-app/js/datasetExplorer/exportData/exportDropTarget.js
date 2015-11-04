ExportDropTarget = (function() {

    var _dialogService;

    var exportDropTarget = {
        dialog : {}
    };

    /**
     * Check if node can be dropped onto export data drop zones
     * @param gridRow
     * @param data
     * @returns {*|boolean}
     * @private
     */
    var _isWrongNode = function(gridRow, data) {
        var _visualAttr = data.node.attributes.visualattributes;
        var _dropHDToClinical =  _visualAttr.indexOf('HIGH_DIMENSIONAL') >= 0 && gridRow.dataTypeId === 'CLINICAL';
        var _dropClinicalToHD =  _visualAttr.indexOf('HIGH_DIMENSIONAL') < 0 && gridRow.dataTypeId !== 'CLINICAL';
        //console.log(_visualAttr);
        //console.log( gridRow.dataTypeId);
        //console.log(_dropClinicalToHD);
        return _dropClinicalToHD || _dropHDToClinical;
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

    exportDropTarget.notifyDropF = function (source, e, data) {

        var _dropTarget = this; // this is the drop target

        // create dialog box instance in the initialisation
        exportDropTarget.dialog = _dialogService.createIdentifierDialog('#dialog-form');

        // 1st checking
        if (_isWrongNode(_dropTarget.recordData, data)) {
            return false;
        }

        //
        if (data.node.attributes.visualattributes.indexOf('HIGH_DIMENSIONAL') >= 0 &&
            _dropTarget.dataTypeId !== 'CLINICAL') {

            _dropTarget.dropData = data;

            exportDropTarget.dialog.dropTarget = _dropTarget;

            console.log(exportDropTarget.dialog);

            exportDropTarget.dialog.dialog("open");

            jQuery('#filterForm').hide();
            jQuery('#loadingPleaseWait').show();
            jQuery('#dialog-apply-btn').button('disable');
            jQuery('#dialog-cancel-btn').button('disable');

            _getHDNodeInfo(data)
                .done(function (d) {
                    console.log('Done with response ', d);
                    _dropTarget.dropData.details = d;

                    if (_isCorrectHD(d, _dropTarget.recordData)) { // TODO refactor to match dropped & drop zone
                        jQuery('#filterForm').show();
                        jQuery('#loadingPleaseWait').hide();
                        jQuery('#dialog-apply-btn').button('enable');
                        jQuery('#dialog-cancel-btn').button('enable');
                    } else {
                        exportDropTarget.dialog.dialog("close");
                    }
                })
                .fail(function (msg) {
                    console.error('Something wrong when checking the node ...', msg);
                    exportDropTarget.dialog.dialog("close");
                });
        } else {
            _dialogService.dropOntoVariableSelection(data, _dropTarget.el);
        }

        // mark as checked
        jQuery(_dropTarget.el.dom).find('input[name="SubsetDataTypeFileType"]').prop('checked', true);
        // ---------------------------------------

        return true;
    };

    /**
     *
     * @param target
     * @param rd
     * @returns {Function}
     */
    exportDropTarget.getEnterHandler = function(target, rd) {
        return function(dd, e, data) {
            if(target.overClass){
                target.el.addClass(target.overClass);
            }
            return _isWrongNode(rd, data) ? target.dropNotAllowed : target.dropAllowed;
        };
    };

    /**
     *
     * @param target
     * @param rd
     * @returns {Function}
     */
    exportDropTarget.getOverHandler = function(target, rd) {
        return function(dd, e, data) {
            return _isWrongNode(rd, data) ? target.dropNotAllowed : target.dropAllowed;
        };
    };

    /**
     * Initialize
     * @param dialogService
     */
    exportDropTarget.init = function (dialogService) {
        _dialogService = dialogService;
    };

    return exportDropTarget;
})();

ExportDropTarget.init(HighDimDialog);
