<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="grails.converters.JSON" %>
<!DOCTYPE HTML>
<html>
<head>
    <!-- Force Internet Explorer 8 to override compatibility mode -->
    <meta http-equiv="X-UA-Compatible" content="IE=Edge">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

    <title>Dataset Explorer</title>

    <link href="${resource(dir: 'images', file: 'searchtool.ico')}" rel="shortcut icon" />
    <link href="${resource(dir: 'images', file: 'searchtool.ico')}" rel="icon" />

    <%-- We do not have a central template, so this only works in the database explorer for now --%>
    <g:if test="${['true', true]*.equals(grailsApplication.config.com.recomdata.debug.jsCallbacks).any()}">
        <g:javascript src="long-stack-traces.js"/>
    </g:if>

    <!-- Include jQuery, Ext and app-specific scripts: -->
    <g:javascript library="jquery" />
    <r:require module="analyseTab" />
    <tmpl:/RWG/urls/>
    <r:layoutResources/>
    <link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'folderManagement.css', plugin: 'folder-management')}">
    <script type="text/javascript" src="${resource(dir:'js', file:'folderManagementDE.js', plugin: 'folder-management')}"></script>

    <script type="text/javascript">

        var pageInfo = {
            basePath: "${request.getContextPath()}"
        }

        GLOBAL = {
            Version: '1.0',
            Domain: '${i2b2Domain}',
            ProjectID: '${i2b2ProjectID}',
            Username: '${i2b2Username}',
            Password: '${i2b2Password}',
            AutoLogin: true,
            Debug: false,
            NumOfSubsets: 2,
            NumOfQueryCriteriaGroups: 20,
            NumOfQueryCriteriaGroupsAtStart: 3,
            MaxSearchResults: 100,
            ONTUrl: '',
            usePMHost: '${grailsApplication.config.com.recomdata.datasetExplorer.usePMHost}',
            Config: 'jj',
            CurrentQueryName: '',
            CurrentComparisonName: ' ',
            CurrentSubsetIDs: [],
            CurrentSubsetQueries: ["", "", ""],
            CurrentPathway: '',
            CurrentPathwayName: '',
            CurrentGenes: '',
            CurrentChroms: '',
            CurrentDataType: '',
            GPURL: '${grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL}',
            EnableGP: '${grailsApplication.config.com.recomdata.datasetExplorer.enableGenePattern}',
            HeatmapType: 'Compare',
            IsAdmin: ${admin},
            Tokens: "${tokens}",
            InitialSecurity: ${initialaccess},
            RestoreComparison: ${restorecomparison},
            RestoreQID1: "${qid1}",
            RestoreQID2: "${qid2}",
            resulttype: 'applet',
            searchType: "${grailsApplication.config.com.recomdata.search.genepathway}",
            DefaultCohortInfo: '',
            CurrentTimepoints: new Array(),
            CurrentSamples: new Array(),
            CurrentPlatforms: new Array(),
            CurrentGpls: new Array(),
            CurrentTissues: new Array(),
            CurrentRbmpanels: new Array(),
            DefaultPathToExpand: "${pathToExpand}",
            UniqueLeaves: "",
            preloadStudy: "${params.DataSetName}",
            Binning: false,
            ManualBinning: false,
            NumberOfBins: 4,
            HelpURL: '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}',
            ContactUs: '${grailsApplication.config.com.recomdata.contactUs}',
            AppTitle: '${grailsApplication.config.com.recomdata.appTitle}',
            BuildVersion: 'Build Version: <g:meta name="environment.BUILD_NUMBER"/> - <g:meta name="environment.BUILD_ID"/>',
            AnalysisRun: false,
            Analysis: 'Advanced',
            HighDimDataType: '',
            SNPType: '',
            basePath: pageInfo.basePath,
            hideAcrossTrialsPanel: '${grailsApplication.config.com.recomdata.datasetExplorer.hideAcrossTrialsPanel}',
            metacoreAnalyticsEnabled: '${grailsApplication.config.com.thomsonreuters.transmart.metacoreAnalyticsEnable}',
            metacoreUrl: '${grailsApplication.config.com.thomsonreuters.transmart.metacoreURL}',
            AnalysisHasBeenRun: false,
            ResultSetRegionParams: {},
            currentReportCodes: [],
            currentReportStudy: [],
            currentSubsetsStudy: '',
            isGridViewLoaded: false,
            galaxyEnabled: '${grailsApplication.config.com.galaxy.blend4j.galaxyEnabled}',
            galaxyUrl: "${grailsApplication.config.com.galaxy.blend4j.galaxyURL}",
            analysisTabExtensions: ${grailsApplication.mainContext.getBean('transmartExtensionsRegistry').analysisTabExtensions as JSON}
        };
        // initialize browser version variables; see http://www.quirksmode.org/js/detect.html
        BrowserDetect.init();
        if (BrowserDetect.browser == "Explorer") {
            if (BrowserDetect.version < 7) {
                GLOBAL.resulttype = 'image';
            }
        }
    </script>
</head>

        var sessionSearch = "${rwgSearchFilter}";
        var sessionOperators = "${rwgSearchOperators}";
        var sessionSearchCategory = "${rwgSearchCategory}";
        var searchPage = "datasetExplorer";
<body>

<script type="text/javascript">
    var $j = jQuery.noConflict();
    Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";

    window.rwgSearchConfig = {
        onConceptsListChanges: function() { window.datasetExplorer_conceptsListChanges.apply(this, arguments); },
    };
        var dseOpenedNodes = "${dseOpenedNodes}";
        var dseClosedNodes = "${dseClosedNodes}";

    //set ajax to 600*1000 milliseconds
        Ext.Ajax.timeout = 1800000;

    // this overrides the above
        Ext.Updater.defaults.timeout = 1800000;

    var helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
    </script>

<style>
    #dialog-form {
        font-family: Arial, "sans serif";
        font-size: 1em;
    }
    #dialog-form label, #dialog-form input { display:block; }
    #dialog-form input.text, #dialog-form select { margin-bottom:12px; width:95%; padding: .4em; }
    fieldset { padding:0; border:0; margin-top:25px; }
    .ui-dialog .ui-state-error { padding: .3em; }
    .validateTips { border: 1px solid transparent; padding: 0.3em; }
</style>

<div id="header-div"><g:render template="/layouts/commonheader" model="['app': 'datasetExplorer']"/></div>
<div id="main"></div>
<h3 id="test">Loading ...</h3>
<tmpl:/RWG/boxSearch hide="true"/>
<tmpl:/RWG/filterBrowser/>
<div id="sidebartoggle">&nbsp;</div>
<div id="noAnalyzeResults" style="display: none;">No subject-level results found.<br/><!--<g:link controller="RWG" action="index">Switch to Browse view</g:link>--></div>
<div id="filter-div" style="display: none;"></div>
<g:form name="exportdsform" controller="export" action="exportDataset"/>
<g:form name="exportgridform" controller="chart" action="exportGrid"/>
<g:if test="${'true' == grailsApplication.config.com.recomdata.datasetExplorer.enableGenePattern}">
    <g:set var="gplogout" value="${grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL}/gp/logout"/>
</g:if>
<g:else>
    <g:set var="gplogout" value=""/>
</g:else>
<IFRAME src="${gplogout}" width="1" height="1" scrolling="no" frameborder="0" id="gplogin"></IFRAME>
<IFRAME src="${gplogout}" width="1" height="1" scrolling="no" frameborder="0" id="altgplogin"></IFRAME>


%{--High Dimension Dialog--}%
<div id="dialog-form" title="Filter data">
    <div id="loadingPleaseWait">Loading, please wait...</div>
    <form id="filterForm">
        <fieldset>
            <label for="filterType">Filter Type</label>
            <select id="filterType" onchange="HighDimensionDialogService.showKeywordDescription(); HighDimensionDialogService.createAutocompleteInput();" >
                <option value="snps">SNP identifiers</option>
                <option value="genes">Genes</option>
                <option value="chromosome_segment">Genomic region</option>
            </select>
            <label for="filterKeyword">Keyword</label>
            <input type="text" name="filterKeyword" id="filterKeyword" value="" class="text ui-widget-content ui-corner-all">
            <p id="filterKeywordFeedback" class="text warning">No valid filter specification.</p>
            <p id="filterKeywordDescription[snps]" class="text filter_keyword_description">
                Please provide one or multiple SNP identifiers (separated by commas), e.g.,
                <code>rs12890222, rs1234567</code>.
            </p>
            <p id="filterKeywordDescription[genes]" class="text filter_keyword_description">
                Please provide one or multiple gene names (separated by commas), e.g.,
                <code>TP53, AURKA</code>.
            </p>
            <p id="filterKeywordDescription[chromosome_segment]" class="text filter_keyword_description">
                Please provide one or multiple genomic region (separated by commas) in the format:
                &lsquo;<var class="var">C</var><code>:</code><var class="var">start</var><code>-</code><var class="var">end</var>&rsquo;,
                where <var class="var">C</var> is in the range 1&ndash;22 or one of <code>X</code>, <code>Y</code>, <code>XY</code>, <code>M</code>, <code>MT</code>.
                E.g., <code>X:1-1000000, 3:200000-400000</code>.
            </p>

            <!-- Allow form submission with keyboard without duplicating the dialog button -->
            <input type="button" tabindex="-1" style="position:absolute; top:-1000px">
        </fieldset>
    </form>
</div>

<div id="saveReportDialog" style="display:none;font: 11px arial,tahoma,helvetica,sans-serif;font-weight:normal;">
    <br/>
    Report Name : <input id='txtReportName' type='text' title="Report Name"/> <br/>
    Make Report Public : <input id='chkReportPublic' type='checkbox' value='Y' title="Make Report Public"/><br/><br/>

        <input type="button" onclick="saveReport(true,jQuery('#txtReportName').val(),jQuery('#txtReportDescription').val(),jQuery('#chkReportPublic').is(':checked'),GLOBAL.currentReportCodes.join('|'),GLOBAL.currentReportStudy)" value="Create Report" />
</div>

<div id="saveSubsetsDialog" style="display:none;font: 11px arial,tahoma,helvetica,sans-serif;font-weight:normal;">
    <form id="saveSubsetForm">
        <br/>
            <em>*</em> Description : <input id='txtSubsetDescription' type='text' name='txtSubsetDescription' title="Subset Description"/>
        <br/>
        <em>*</em> Make Subset Public : <input id='chkSubsetPublic' type='checkbox' value='Y' title="Subset Public"/>
        <br/>
        <br/>
        <input class="submit" type="submit" value="Save Subsets"/>
    </form>
</div>
<span id="visualizerSpan0"></span> <!-- place applet tag here -->
<span id="visualizerSpan1"></span> <!-- place applet tag here -->
<!-- ************************************** -->
<!-- This implements the Help functionality -->
<script type="text/javascript" src="${resource(dir: 'js', file: 'help/D2H_ctxt.js')}"></script>
<script language="javascript">
    helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
</script>
<!-- ************************************** -->
<r:layoutResources/><%-- XXX: Use template --%>
</body>
</html>
