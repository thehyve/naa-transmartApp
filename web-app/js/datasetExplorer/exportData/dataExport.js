
/**
 *  Data Export Object
 * @constructor
 * @param errorHandler function taking response status and message
 */
var DataExport = function() {

    this.records = null;

    this.exportMetaDataStore = null;

    /**
     * Get export metadata store
     * @returns {Ext.data.JsonStore}
     * @private
     */
    var _getExportMetadataStore = function () {
        var ret = new Ext.data.JsonStore({
            url : pageInfo.basePath+'/dataExport/getMetaData',
            root : 'exportMetaData',
            fields : ['subsetId1', 'subsetName1', 'subset1', 'subsetId2', 'subsetName2', 'subset2', 'dataTypeId',
                'dataTypeName', 'metadataExists', 'supportedDataConstraints']
        });
        ret.proxy.addListener('loadexception', function(dummy, dummy2, response) {
            if (response.status != 200) {
                var responseText,
                    parsedResponseText;
                responseText = response.responseText;
                try {
                    parsedResponseText = JSON.parse(responseText);
                    if (parsedResponseText.message) {
                        responseText = parsedResponseText.message;
                    }
                } catch (syntaxError) {}
                exportListFetchErrorHandler(response.status, responseText);
            }
        });
        return ret;
    };

    var exportListFetchErrorHandler = function(status, text) {
        Ext.Msg.alert('Status', "Error fetching export metadata.<br/>Status " +
            status + "<br/>Message: " + text);
    };

    // let's create export metadata json store
    this.exportMetaDataStore = _getExportMetadataStore();

};


/**
 * Display data to be exported
 * @param records
 * @param options
 * @param success
 */
DataExport.prototype.displayResult = function (records, options, success) {

    var _this = this;

    _this.records = records;

    /**
     * Get tool bar component
     * @returns {Ext.Toolbar}
     * @private
     */
    var _getToolBar = function () {
        return new Ext.Toolbar(
            {
                id : 'dataExportToolbar',
                title : 'dataExportToolbar',
                items : ['->', // aligns the items to the right
                    new Ext.Toolbar.Button(
                        {
                            id:'help',
                            tooltip:'Click for Data Export help',
                            iconCls: "contextHelpBtn",
                            handler: function(event, toolEl, panel){
                                D2H_ShowHelp("1312",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
                            }
                        }
                    )]
            }
        );
    };

    var _getDataTypesGridPanel = function (newStore, columns) {

        var _toolBar = _getToolBar();

        var _grid = new CustomGridPanel ({
            id: "dataTypesGridPanel",
            store: newStore,
            columns: columns,
            title: "Instructions: <br>" +
            "1. Select the check boxes to indicate the data types and file formats that are " +
            "desired for export. <br>" +
            "2. Optionally you can filter the data by dragging and dropping some " +
            "criteria onto each data type row.<br> " +
            "3. Click on the \"Export Data\" button at the bottom of the screen to " +
            "initiate an asynchronous data download job.  <br>" +
            "4. To download your data navigate to the \"Export Jobs\" tab.",
            viewConfig:	{
                forceFit : true,
                emptyText : "No rows to display"
            },
            sm : new Ext.grid.RowSelectionModel({singleSelect : true}),
            layout : "fit",
            width : 600,
            tbar : _toolBar,
            buttons: [{
                id : "dataTypesToExportRunButton",
                text : "Export Data",
                handler : function () {
                    _this.createDataExportJob(_grid);
                }
            }],
            buttonAlign:"center"
        });

        return _grid;
    };

    // Display data when success and records contains data
    if (success && (_this.records.length > 0)) {

        var _selectedCohortData = [];

        _selectedCohortData['dataTypeId'] = '';
        _selectedCohortData['dataTypeName'] = 'Selected Cohort';
        _selectedCohortData['subset1'] = getQuerySummary(1);
        _selectedCohortData['subset2'] = getQuerySummary(2);

        var _columns = _this.prepareColumnModel(_this.exportMetaDataStore, _selectedCohortData);
        var _newStore = _this.prepareNewStore(_this.exportMetaDataStore, _columns, _selectedCohortData);

        var _dataTypesGridPanel = _getDataTypesGridPanel(_newStore, _columns);
        _dataTypesGridPanel.records = _this.records;
        _dataTypesGridPanel.on("afterrender", _dataTypesGridPanel.dropZonesChecker);

        // add gridPanel to the main panel
        analysisDataExportPanel.add(_dataTypesGridPanel);
        analysisDataExportPanel.doLayout();

    } else {
        console.error("cannot load export metadata .. ");
    }
    // unmask data export panel
    analysisDataExportPanel.body.unmask();
};

/**
 *
 * @param store
 * @param selectedCohortData
 * @returns {Array}
 */
DataExport.prototype.prepareColumnModel = function (store, selectedCohortData) {
    var columns = [];
    var columnModelPrepared = false;

    var this_column = [];
    this_column['name'] = 'dataTypeName';
    this_column['header'] = '';
    this_column['sortable'] = false;
    columns.push(this_column);

    var subsetsAdded = false;
    store.each(function (row) {
        if (!subsetsAdded) {
            var this_column = [];
            this_column['name'] = row.data.subsetId1;
            this_column['header'] = row.data.subsetName1;
            this_column['sortable'] = false;
            columns.push(this_column);
            if (selectedCohortData['subset2'] != null && selectedCohortData['subset2'].length > 1) {
                this_column = [];
                this_column['name'] = row.data.subsetId2;
                this_column['header'] = row.data.subsetName2;
                this_column['sortable'] = false;
                columns.push(this_column);
            }
            subsetsAdded = true;
        }
    });

    return columns;
}

/**
 *
 * @param files
 * @param subset
 * @param dataTypeId
 * @param metadataExists
 * @returns {string}
 */
DataExport.prototype.prepareOutString = function (files, subset, dataTypeId, metadataExists) {
    var outStr = '';
    var dataCountExists = false;
    var _this = this;

    var groupedfiles = _this.groupAndTranspose(files);

    groupedfiles.forEach(function (platform) {
        if (platform.gplId == _this._absentPlatformsMarker) {
            dataCountExists = true;
            outStr += _this.createSelectBoxHtml(platform.exports, subset, dataTypeId, platform.fileDataCount);
        } else if (platform.gplId == _this._emptyPlatformsMarker) {
            platform.exports.forEach(function(exp) {
                outStr += exp.dataFormat + ' is not available. ';
                outStr += '<br/><br/>';
            })
        } else {
            if (platform.fileDataCount == 0) return;
            dataCountExists = true;
            outStr += _this.createSelectBoxHtml(platform.exports, subset, dataTypeId, platform.fileDataCount, platform)
        }
    });

    if (dataCountExists && metadataExists)
        outStr += 'Metadata will be downloaded in a separate file.';
    return outStr;
}

/**
 * The files array is a list of export formats. Each export format can have data from one or multiple platforms.
 * This method groups the export formats that have the same 'group' attribute together per platform.
 * The result is transposed in the sense that it is a list of platforms with one export format per platform, or more
 * if the export formats are in the same group.
 *
 * @param files The list of export file types as received from the server
 * @returns A list of transposed and grouped platform descriptions, each with one or more files
 */
DataExport.prototype.groupAndTranspose = function(files) {
    var groupedfiles = [];
    var skip = {};
    for (var i=0; i<files.length; i++) {
        if (i in skip) continue;

        var groupname = files[i].displayAttributes.group || null;
        var group_platforms = {};

        // We use the same code for exports with and without a 'group' attribute. Loop accordingly.
        var limit = groupname == null ? i+1 : files.length;
        for (var j=i; j<limit; j++) {
            if (j in skip) continue;
            if ((files[j].displayAttributes.group || null) != groupname) continue;
            var platforms = files[j].platforms;

            // clinical data
            if (!platforms) {
                platforms = [{gplId: this._absentPlatformsMarker, gplTitle: null, fileDataCount: files[j].fileDataCount}];
            }
            // also record if an export format is not applicable to any platforms
            if (platforms.length == 0) {
                platforms = [{gplId: this._emptyPlatformsMarker, gplTitle: null, fileDataCount: 0}]
            }

            platforms.forEach(function(platform) {
                var id = platform.gplId;
                if (!(id in group_platforms)) {
                    // create a new object since we will be modifying it
                    group_platforms[id] = {
                        gplId: id,
                        gplTitle: platform.gplTitle,
                        fileDataCount: platform.fileDataCount,
                        exports: [files[j]]
                    };
                    return;
                }
                if (group_platforms[id].gplTitle != platform.gplTitle) {
                    console.warn(jQuery.validator.format('Different platform definitions for {0}: titles "{1}" and' +
                        ' "{2}"', id, group_platforms[id].gplTitle, platform.gplTitle));
                }
                group_platforms[id].exports.push(files[j]);
                // We assume grouped export file types 'belong' together and so support the same data types and thus
                // have the same count. If not, we'll display the max.
                group_platforms[id].fileDataCount = Math.max(group_platforms[id].fileDataCount, platform.fileDataCount);
            });
            skip[j] = true;
        }
        groupedfiles.push.apply(groupedfiles, Object.values(group_platforms));
    }

    return groupedfiles;
}
// Random string to avoid collissions with real platform identifiers. We could use ES6 symbols for this.
DataExport.prototype._emptyPlatformsMarker = '_platformsEmpty_W1qe1445Pl8';
DataExport.prototype._absentPlatformsMarker = '_platformsAbsent_W1qe1445Pl8';

/**
 *
 * @param files
 * @param subset
 * @param dataTypeId
 * @param fileDataCount
 * @param platform
 * @returns {string|*}
 */
DataExport.prototype.createSelectBoxHtml = function (files, subset, dataTypeId, fileDataCount, platform) {
    var outStr = '';
    console.log('about to createSelectBoxHtml ..');

    var dataFormat = files.map(function(file) {return file.dataFormat}).join(' and ');

    if (platform) {
        outStr += dataFormat + ' is available for <br/>' + platform.gplTitle + ": " + fileDataCount + ' samples<br/>';
        outStr += 'Export ';
        outStr += files.map(function(file) {
            return  file.fileType + '&nbsp;&nbsp;' +
                    '<input type="checkbox" name="SubsetDataTypeFileType"' +
                    ' value="{subset: ' + subset + ', dataTypeId: ' + dataTypeId + ', fileType: ' + file.fileType + ', gplId: ' + platform.gplId + '}"' +
                    ' id="' + subset + '_' + dataTypeId + '_' + file.fileType + '_' + platform.gplId + '"' +
                    ' />';
        }).join(' &nbsp;');
        outStr += '<br/><br/>';
    } else {
        outStr += dataFormat + ' is available for ' + fileDataCount + ' patients<br/>';
        outStr += 'Export ';
        outStr += files.map(function(file) {
            return  file.fileType + '&nbsp;&nbsp;' +
                    '<input type="checkbox" name="SubsetDataTypeFileType"' +
                    ' value="{subset: ' + subset + ', dataTypeId: ' + dataTypeId + ', fileType: ' + file.fileType + '}"' +
                    ' id="' + subset + '_' + dataTypeId + '_' + file.fileType + '"' +
                    ' />';
        }).join(' &nbsp;');
        outStr += '<br/><br/>';
    }

    return outStr
};

/**
 *
 * @param store
 * @param columns
 * @param selectedCohortData
 * @returns {Ext.data.JsonStore}
 */
DataExport.prototype.prepareNewStore = function (store, columns, selectedCohortData) {
    var _this = this;

    //Remove existing check-boxes
    var subsetDataTypeFiles = document.getElementsByName('SubsetDataTypeFileType');

    while (subsetDataTypeFiles.length >= 1) {
        subsetDataTypeFiles[0].parentNode.removeChild(subsetDataTypeFiles[0])
    }

    var data = [];
    data.push(selectedCohortData);

    /**
     * get export data tips
     * @returns {string}
     * @private
     */
    var _get_export_data_tip = function (files) {
        var _str_data_type = 'low dimensional';

        files.each(function (file) {
            if (file.platforms) {

                file.platforms.each(function (platform) {
                    if (platform.fileDataCount > 0) {
                        _str_data_type = 'high dimensional';
                    }
                });
            }
        });

        return " <br><span class='data-export-filter-tip'>(Drag and drop " + _str_data_type
            + " nodes here to filter the exported data.)</span>";
    };

    store.each(function (row) {
        var this_data = [];
        this_data['dataTypeId'] = row.data.dataTypeId;
        this_data['dataTypeName'] = row.data.dataTypeName + _get_export_data_tip (row.data.subset1);

        var outStr = _this.prepareOutString(row.data.subset1, row.data.subsetId1, row.data.dataTypeId, row.data.metadataExists);
        this_data[row.data.subsetId1] = outStr;

        if (selectedCohortData['subset2'] != null && selectedCohortData['subset2'].length > 1) {
            // cohort string for subset 2
            this_data['dataTypeName'] = row.data.dataTypeName + _get_export_data_tip (row.data.subset2);
            // outstring for subset 2
            outStr = _this.prepareOutString(row.data.subset2, row.data.subsetId2, row.data.dataTypeId, row.data.metadataExists);
            this_data[row.data.subsetId2] = outStr;
        }

        data.push(this_data);
    });

    var myStore = new Ext.data.JsonStore({
        id: 'metadataStore',
        autoDestroy: true,
        root: 'subsets',
        fields: columns,
        data: {subsets: []}
    });

    myStore.loadData({subsets: data}, false);

    return myStore;
};

/**
 * Create data export job
 * @param gridPanel
 */
DataExport.prototype.createDataExportJob = function (gridPanel) {
    var _this = this;
    Ext.Ajax.request({
        url: pageInfo.basePath + "/dataExport/createnewjob",
        method: 'POST',
        success: function (result, request) {
            //Handle data export process
            _this.runDataExportJob(result, gridPanel);
        },
        failure: function (result, request) {
            Ext.Msg.alert('Status', 'Unable to create data export job.');
        },
        timeout: '1800000',
        params: {
            querySummary1: getQuerySummary(1),
            querySummary2: getQuerySummary(2),
            analysis: "DataExport"
        }
    });
};

/**
 * Get export parameters to be sent to the backend
 * @param gridPanel
 * @param selectedFiles
 * @returns {{}}
 */
DataExport.prototype.getExportParams = function (gridPanel, selectedFiles) {

    var params = {}; // init params

    /**
     * Check what subset of a file string
     * @param file
     * @returns {string}
     * @private getQuerySummaryItem
     */
    var _checkSubset = function (file) {
        var _subsets = ["subset1", "subset2"];
        var _subsetRegexs = [new RegExp(_subsets[0]), new RegExp(_subsets[1])];
        var _returnVal = "";

        for (var i = 0, maxLength = _subsetRegexs.length; i < maxLength; i++) {
            if (_subsetRegexs[i].test(file)) {
                _returnVal = _subsets[i];
            }
        }
        return _returnVal;
    }; //

    /**
     * Check if a particular data type is selected
     * @param file
     * @param type
     * @returns {boolean}
     * @private
     */
    var _checkDataType = function (file, type) {
        var _typeRegex = new RegExp(type);
        return _typeRegex.test(file);
    };

    /**
     * Get concept paths
     * @param el
     * @private
     */
    var _get_concept_path = function (tr) {
        var  _concept_path_arr = [],
            _vals,
            _el = Ext.get(tr); // convert tr to element

        for (var i = 1; i < _el.dom.childNodes.length; i++) {

            var _concept_path = (_el.dom.childNodes[i]).getAttribute("conceptid");
            var _filterType = (_el.dom.childNodes[i]).getAttribute("conceptfiltertype");
            var _filterVal = (_el.dom.childNodes[i]).getAttribute("conceptfiltervalues");

            _vals = JSON.parse(_filterVal);

            if (_filterType === 'chromosome_segment') {

                jQuery.each(_vals, function (k, v) {
                    _concept_path_arr.push({
                        id:_concept_path,
                        type:_filterType,
                        chromosome: v.chromosome,
                        start: v.start,
                        end: v.end
                    });
                });

            } else {
                // create selector object
                _concept_path_arr.push({
                    id:_concept_path,
                    type:_filterType,
                    names: _vals
                });
            }

        }
        return _concept_path_arr;

    };


    if (gridPanel.records.length > 0) {

        for (var i = 0; i < gridPanel.records.length; i++) {

            // get data type
            var _data_type = gridPanel.records[i].data.dataTypeId;

            // get concept paths
            var _concept_path_arr = _get_concept_path(gridPanel.getView().getRow(i+1));
            //var _concept_path_arr = _getSelectors(gridPanel.getView().getRow(i+1), gridPanel.records[i].data);

            // loop through selected files
            for (var j = 0; j < selectedFiles.length; j++) {

                var _sub = _checkSubset(selectedFiles[j]);
                var _type = _checkDataType(selectedFiles[j], _data_type);

                if (_sub) {

                    // create subset node
                    if (!params[_sub]) params[_sub] = {};

                    // create selector node
                    if (_type) {
                        params[_sub][_data_type.toLowerCase()] = {
                            'selector' : _concept_path_arr
                        };
                    }

                }
            }

        }
    }

    return params;
};

/**
 * Run data export job
 * @param result
 * @param gridPanel
 */
DataExport.prototype.runDataExportJob = function (result, gridPanel) {
    var jobNameInfo = Ext.util.JSON.decode(result.responseText);
    var jobName = jobNameInfo.jobName;

    var messages = {
        cancelMsg: "Your Job has been cancelled.",
        backgroundMsg: "Your job has been put into background process. Please check the job status in " +
        "the 'Export Jobs' tab."
    };

    showJobStatusWindow(result, messages);

    var subsetDataTypeFiles = document.getElementsByName('SubsetDataTypeFileType');
    var selectedSubsetDataTypeFiles = [];

    for (var i = 0; i < subsetDataTypeFiles.length; i++) {
        if (subsetDataTypeFiles[i].checked) selectedSubsetDataTypeFiles.push(subsetDataTypeFiles[i].value);
    }

    if (window.console) {
        console.log("selectedSubsetDataTypeFiles", selectedSubsetDataTypeFiles);
    }

    var _exportParams = this.getExportParams(gridPanel, selectedSubsetDataTypeFiles);

    Ext.Ajax.request(
        {
            url: pageInfo.basePath + "/dataExport/runDataExport",
            method: 'POST',
            timeout: '1800000',
            params: Ext.urlEncode(
                {
                    result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
                    result_instance_id2: GLOBAL.CurrentSubsetIDs[2],
                    analysis: 'DataExport',
                    jobName: jobName,
                    selectedSubsetDataTypeFiles: selectedSubsetDataTypeFiles,
                    selection : JSON.stringify(_exportParams)
                }) // or a URL encoded string
        });
    checkJobStatus(jobName);
};


/**
 * Where everything starts. Create Data Export instance, load the data store to display all data in a cohort
 * that can be exported.
 */
function getDatadata() {

    // create new instance of data export
    var dataExport = new DataExport();

    // load export metadata
    dataExport.exportMetaDataStore.load({
        params: {result_instance_id1: GLOBAL.CurrentSubsetIDs[1], result_instance_id2: GLOBAL.CurrentSubsetIDs[2]},
        scope: dataExport,
        callback: dataExport.displayResult
    });
}

