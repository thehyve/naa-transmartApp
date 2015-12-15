/**
 * Extend Ext.GridPanel to have 'afterrender' event
 * @type {*}
 */
var CustomGridPanel = Ext.extend(Ext.grid.GridPanel, {
    constructor : function (config) {
        CustomGridPanel.superclass.constructor.apply(this, arguments);
        // add 'afterrender' or any other event here
        this.addEvents({
            'afterrender': true
        });
    },
    afterRender: function () {
        Ext.grid.GridPanel.superclass.afterRender.call(this);
        this.view.layout();
        if (this.deferRowRender) {
            this.view.afterRender.defer(10, this.view);
        } else {
            this.view.afterRender();
        }
        this.viewReady = true;
        this.fireEvent('afterrender');
    }
});

/**
 * Function to check if row element already existing in the Grid Panel
 * When it is, convert them to drop zones.
 */
CustomGridPanel.prototype.dropZonesChecker = function () {

    var _this = this;

    // init row element checker task
    var checkTask = {

        run: function () {

            // init rows array
            var rows = [];

            // check if view already have rows represent the number of records
            for (var i = 1; i <= _this.records.length; i++) {

                var recordData = _this.records[i - 1].data;
                var _rowEl = _this.getView().getRow(i);
                rows.push(_rowEl);

                var _dtgI = new Ext.dd.DropTarget(_rowEl, {ddGroup: 'makeQuery'});
                _dtgI.recordData = recordData;

                var _notifyDropF = ExportDropTarget.notifyDropF;
                _dtgI.notifyDrop = _notifyDropF;
                _dtgI.notifyEnter = ExportDropTarget.getEnterHandler(_dtgI, recordData);
                _dtgI.notifyOver =  ExportDropTarget.getOverHandler(_dtgI, recordData);
            }

            // stop runner when it's already found the elements
            if (rows.length > 0) {
                runner.stopAll();
            }

        },

        interval: 500 // repeat every 0.5 seconds
    };

    // Need to have a task runner since there's no other way to retrieve
    // row elements after they're rendered.

    var runner = new Ext.util.TaskRunner();  // define a runner
    runner.start(checkTask); // start the task
};

