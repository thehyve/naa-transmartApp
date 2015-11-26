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
        return _dropClinicalToHD || _dropHDToClinical;
    };

    exportDropTarget.notifyDropF = function (source, e, data) {

        var _dropTarget = this; // this is the drop target

        // create dialog box instance in the initialisation
        exportDropTarget.dialog = _dialogService.createIdentifierDialog( _dropTarget);

        // 1st checking
        if (_isWrongNode(_dropTarget.recordData, data)) {
            return false;
        }

        //
        if (data.node.attributes.visualattributes.indexOf('HIGH_DIMENSIONAL') >= 0 &&
            _dropTarget.dataTypeId !== 'CLINICAL') {

            _dropTarget.dropData = data;

            exportDropTarget.dialog.dropTarget = _dropTarget;
            exportDropTarget.dialog.dialog("open");

        } else {
            _dialogService.dropOntoVariableSelection(data, _dropTarget.el);
        }

        // mark as checked
        var priority = 0;
        var toCheck = [];
        ['subset1', 'subset2'].each(function(subset) {
            _dropTarget.recordData[subset].each(function(exp) {
                var prio = exp.displayAttributes.selectOnFilterPriority;
                if (prio < priority) return;
                if (prio > priority) {
                    toCheck = [];
                    priority = prio;
                }
                exp.platforms.each(function(platform) {
                    toCheck.push('#' +
                        [subset, _dropTarget.recordData.dataTypeId, exp.fileType, platform.gplId]
                            .join('_').replace('.', '\\.'));
                })
            })
        });
        jQuery(_dropTarget.el.dom).find(toCheck.join(', ')).prop('checked', true);
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

ExportDropTarget.init(HighDimensionDialogService);
