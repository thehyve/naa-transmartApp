//# sourceURL=rwg.js

// store probe Ids for each analysis that has been loaded
var analysisProbeIds = new Array();

var openAnalyses = new Array(); //store the IDs of the analyses that are open


//create an ajaxmanager named rwgAJAXManager
//this will handle all ajax calls on this page and prevent too many 
//requests from hitting the server at once
var rwgAJAXManager = jQuery.manageAjax.create('rwgAJAXManager', {
	queue: true, 			//(true|false|'clear') the queue-type specifies the queue-behaviour.
	maxRequests: 5, 		//(number (1)) limits the number of simultaneous request in the queue. queue-option must be true or 'clear'.
	cacheResponse: false 	//(true|false): caches the response data of successful response
});

var cohortBGColors = new Array(
		/*
		"#F5A9E1",  // light pink
		"#00FFFF",  // light blue
		"#FE9A2E",  // light orange
		"#BDBDBD",  // light grey
		"#2EFE2E",  // light green
		"#FF00FF",   // pink
		"#F3F781"  // light yellow
	*/
		
		/* Pastel */
		"#FFFFD9", //light yellow
		"#80B1D3", //light blue
		"#B3DE69", //moss green
		"#D9D9D9", //grey
		"#BC80BD", //lavender
		"#91d4c5"  //teal

		
		/*Light yellow to green, sequential 
		"#FFFFE5",
		"#F7FCB9",
		"#D9F0A3",
		"#ADDD8E",
		"#78C679",
		"#41AB5D",
		"#238443",
		"#006837"
		*/
		
);


////////////////////////////////////////////////////////////////////
// Not in the July 2012 Release
////////////////////////////////////////////////////////////////////
/*function updateAnalysisCount(checkedState)	{	
	var currentCount = jQuery("#analysisCount").val();
	if (checkedState)	{
		currentCount++;
	} else	{
		currentCount--;
	}
	jQuery("#analysisCount").val(currentCount);
	var newLabel = currentCount + " Analyses Selected";
	if (currentCount == 0)	{
		newLabel = "No Analysis Selected";
	} else if (currentCount == 1)	{
		newLabel = "1 Analysis Selected";
	}
	jQuery("#analysisCountLabel").html(newLabel);
	return false;
}
*/
////////////////////////////////////////////////////////////////////
function showDetailDialog(folderId)	{

	jQuery('#welcome-viewer').empty();

	jQuery('#metadata-viewer').empty().addClass('ajaxloading');
	jQuery('#metadata-viewer').load(folderDetailsURL + '?id=' + folderId, {}, function() {
		jQuery('#metadata-viewer').removeClass('ajaxloading');
	});
	return false;
}

//when a new object is created, show its details, highlight it and check for its parent to add expand/collapse image
function updateForNewFolder(folderId)	{
	jQuery('#metadata-viewer').load(folderDetailsURL + '?id=' + folderId, {}, function() {
		var parentId=jQuery('#parentId').val();
		
		//update parent folder
		var imgExpand = "#imgExpand_"  + parentId;
		var src = jQuery(imgExpand).attr('src').replace('folderplus.png', 'ajax-loader-flat.gif').replace('folderminus.png', 'ajax-loader-flat.gif').replace('folderleaf.png', 'ajax-loader-flat.gif');
		jQuery(imgExpand).attr('src',src);
		
		jQuery.ajax({
			url:folderContentsURL,
			data: {id: parentId, auto: false},
			success: function(response) {
				jQuery('#' + parentId + '_detail').html(response).addClass('gtb1').addClass('analysesopen').attr('data', true);
				
				//check if the object has children
				if(jQuery('#' + parentId + '_detail .search-results-table .folderheader').size() > 0){
					jQuery(imgExpand).attr('src', jQuery(imgExpand).attr('src').replace('ajax-loader-flat.gif', 'folderminus.png'));
				}else{
					jQuery(imgExpand).attr('src', jQuery(imgExpand).attr('src').replace('ajax-loader-flat.gif', 'folderleaf.png'));
				}
				jQuery('.result-folder-name').removeClass('selected');
				jQuery('#result-folder-name-' + folderId).addClass('selected');
			},
			error: function(xhr) {
				console.log('Error!  Status = ' + xhr.status + xhr.statusText);
			}
		});
		
		var imgExpand = "#imgExpand_"  + parentId;
		var src = jQuery(imgExpand).attr('src').replace('folderplus.png', 'folderminus.png').replace('ajax-loader-flat.gif', 'folderminus.png').replace('folderleaf.png', 'folderminus.png');
		jQuery(imgExpand).attr('src',src);
		var action="toggleDetailDiv('"+parentId+"', folderContentsURL + '?id="+parentId+"&auto=false');";
		jQuery('#toggleDetail_'+parentId).attr('onclick', action);
		openFolderAndShowChild(parentId, folderId);
		jQuery('.result-folder-name').removeClass('selected');
		jQuery('#result-folder-name-' + folderId).addClass('selected');
	});
	
	return false;
}

function openFolderAndShowChild(parent, child) {
	toggleDetailDiv(parent, folderContentsURL + '?id=' + parent + '&auto=false', true, child);
	showDetailDialog(child);
}

// Open and close the analysis for a given trial
function toggleDetailDiv(trialNumber, dataURL, forceOpen, highlightFolder, manual)	{	
	if(manual == undefined){ manual=false;}
	var imgExpand = "#imgExpand_"  + trialNumber;
	var trialDetail = "#" + trialNumber + "_detail";
	
	// If data attribute is undefined then this is the first time opening the div, load the analysis... 
	if (typeof jQuery(trialDetail).attr('data') == 'undefined')	{
		//add node
		var src = jQuery(imgExpand).attr('src').replace('folderplus.png', 'ajax-loader-flat.gif');	
		jQuery(imgExpand).attr('src',src);
		jQuery.ajax({
			url:dataURL,			
			success: function(response) {
				jQuery(imgExpand).attr('src', jQuery(imgExpand).attr('src').replace('ajax-loader-flat.gif', 'folderminus.png'));
				jQuery(trialDetail).addClass("gtb1");
				jQuery(trialDetail).html(response);
				jQuery(trialDetail).addClass("analysesopen");
				jQuery(trialDetail).attr('data', true);// Add an attribute that we will use as a flag so we don't need to load the data multiple times
				
				//If we have a folder to highlight, transfer the highlight to it
				if (highlightFolder) {
			    	jQuery('.result-folder-name').removeClass('selected');
					jQuery('#result-folder-name-' + highlightFolder).addClass('selected');
				}
			},
			error: function(xhr) {
				console.log('Error!  Status = ' + xhr.status + xhr.statusText);
			}
		});
	} else	{
		var src = jQuery(imgExpand).attr('src').replace('folderminus.png', 'folderplus.png');
		if (jQuery(trialDetail).attr('data') == "true" && !forceOpen)	{
			//remove node
			jQuery(trialDetail).attr('data',false);
			jQuery(trialDetail).removeClass("analysesopen");
		} else	{
			src = jQuery(imgExpand).attr('src').replace('folderplus.png', 'folderminus.png');
			jQuery(trialDetail).attr('data',true);
			jQuery(trialDetail).addClass("analysesopen");
			
			if (highlightFolder) {
				jQuery('.result-folder-name').removeClass('selected');
				jQuery('#result-folder-name-' + highlightFolder).addClass('selected');
			}
		}	
		jQuery(imgExpand).attr('src',src);
		if (!forceOpen) {
			jQuery(trialDetail).toggle();
		}
		else {
			jQuery(trialDetail).show();
		}
	}
	return false;
}

// Method to add the toggle button to show/hide the search filters
function addToggleButton()	{
	jQuery("#toggle-btn").button({
		text: false
		}).click(function() {
			toggleFilters();
			jQuery("#main").css('left') == "0px" ? switchImage('toggle-icon-left', 'toggle-icon-right') : switchImage('toggle-icon-right', 'toggle-icon-left');
		}
	).addClass('toggle-icon-left');
	return false;
}

// Add and remove the right/left image for the toggle button
function switchImage(imgToRemove, imgToAdd)	{
	jQuery("#toggle-btn").removeClass(imgToRemove);
	jQuery("#toggle-btn").addClass(imgToAdd);
}

// Method to show/hide the search/filters 
function toggleFilters()	{
	if (jQuery("#main").css('left') == "0px"){		
		jQuery("#search-categories").attr('style', 'visibility:visible; display:inline');
		jQuery("#search-ac").attr('style', 'visibility:visible; display:inline');
		jQuery("#search-div").attr('style', 'visibility:visible; display:inline');
		jQuery("#active-search-div").attr('style', 'visibility:visible; display:inline');
		jQuery("#title-search-div").attr('style', 'visibility:visible; display:inline');
		jQuery("#title-filter").attr('style', 'visibility:visible; display:inline');
		jQuery("#side-scroll").attr('style', 'visibility:visible; display:inline');
		jQuery("#main").css('left', 300);
		jQuery("#toggle-btn").css('left', 278);
		jQuery("#toggle-btn").css('height;', 20);
		jQuery("#main").css('padding-left', 0);	
		jQuery("#menu_bar").css('margin-left', -1);
		jQuery("#toggle-btn").css('height', 20);	
	} else	{
		jQuery("#search-categories").attr('style', 'visibility:hidden; display:none');
		jQuery("#search-ac").attr('style', 'visibility:hidden; display:none');
		jQuery("#search-div").attr('style', 'visibility:hidden; display:none');
		jQuery("#active-search-div").attr('style', 'visibility:hidden; display:none');
		jQuery("#title-search-div").attr('style', 'visibility:hidden; display:none');
		jQuery("#title-filter").attr('style', 'visibility:hidden; display:none');
		jQuery("#side-scroll").attr('style', 'visibility:hidden; display:none');
		jQuery("#main").css('left', 0);	
		jQuery("#toggle-btn").css('left', 0);	
		jQuery("#toggle-btn").css('height', '100%');	
		jQuery("#main").css('padding-left', 20);	
		jQuery("#menu_bar").css('margin-left', -21);	
	}	   
}

function setVisTabs(analysisID){
	var tabID = "#visTabs_" + analysisID;
	jQuery(tabID).tabs();	
	jQuery(tabID).bind( "tabsshow", function(event, ui) {
	    if (ui.panel.id == "boxplot_" + analysisID) {
	    	showBoxOrLinePlotVisualization(ui.panel, analysisID, true);
	    } else if (ui.panel.id == "lineplot_" + analysisID)	{
	    	showBoxOrLinePlotVisualization(ui.panel, analysisID, false);
	    }
	});
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Box or Line Plot Visualization Methods
// Show, Load Data and Draw
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function showBoxOrLinePlotVisualization(panel, analysisID, isBoxplot)	{
	var analysisIndex = getAnalysisIndex(analysisID);
	var probeIds = analysisProbeIds[analysisIndex].probeIds;
	var selectList = analysisProbeIds[analysisIndex].selectList;

	var typeString = '';
	if (isBoxplot) {
		typeString = 'Box';
	}  
	else  {		
		typeString = 'Line';
	}
	
	// retrieve the active probe for the current analysis 
	var activeProbe = getActiveProbe(analysisID);
	
	// if the currently displayed plot is not the active probe then redraw
	var redraw = false;
	
	var currentProbe = jQuery('body').data("active" + typeString + "plot:" + analysisID);
	
	if (currentProbe != activeProbe)  {
		redraw = true;
	}

	// if we're not showing a plot for the active probe, then reload
	if(redraw) { 
		jQuery("#analysis_holder_" + analysisID).mask("Loading...");
	
		if (isBoxplot)  {			
			loadBoxPlotData(analysisID, activeProbe);

		}
		else  {
			loadLinePlotData(analysisID, activeProbe);			
		}
	}
	
}


//Method to add the probes for the Line plot
function setLineplotProbes(analysisID, probeID)	{
	setProbesDropdown(analysisID, probeID, "#probeSelectionLineplot_" + analysisID);	
}


function getActiveProbe(analysisId){
	
	// retreive the active probe for the current analysis, first retrieve from global data
	var probeId = jQuery('body').data("activeAnalysisProbe:" + analysisId);
	
	// if not defined yet, set to the first one for the current page showing for the analysis
	if (probeId == undefined)  {
		var analysisIndex = getAnalysisIndex(analysisId);
		probeId = analysisProbeIds[analysisIndex].probeIds[0];		
	}
	 
	return probeId;
	
}

function setActiveProbe(analysisId, probeId){
	//store the currently active probe for the analysis; i.e the last one drawn for the box or line plot
	jQuery('body').data("activeAnalysisProbe:" + analysisId, probeId); 
	
}

function getGeneforDisplay(analysisID, probeID){

	var analysisIndex = getAnalysisIndex(analysisID);
	var probeIds = analysisProbeIds[analysisIndex].probeIds ;

	var maxProbeIndex = analysisProbeIds[analysisIndex].maxProbeIndex;
	 var probeDisplay = "";
    for (var i=0; i<maxProbeIndex; i++)  {
    	if (probeIds[i] == probeID) {
    		probeDisplay = analysisProbeIds[analysisIndex].selectList[i];
    		return probeDisplay;
    	}
    }
	 return false;
}


//Load the line plot data
function loadLinePlotData(analysisID, probeID)	{
	
	if (probeID === undefined)	{
		// We are called from the user switching probes, throw up the mask and get the probeID
		jQuery("#analysis_holder_" + analysisID).mask(); //hide the loading screen
		probeID = jQuery("#probeSelectionLineplot_" + analysisID).find('option:selected').attr('id');
		
	}
	
	// retrieve the corresponding display value for the probe Id 
    var analysisIndex = getAnalysisIndex(analysisID);
    var probeIds = analysisProbeIds[analysisIndex].probeIds ;
    var maxProbeIndex = analysisProbeIds[analysisIndex].maxProbeIndex; 

	
	rwgAJAXManager.add({
		url:getLinePlotDataURL,									
		data: {id: analysisID, probeID: probeID},
		timeout:60000,
		success: function(response) {
			
			//store the response
			jQuery('body').data("LineplotData:" + analysisID, response); //store the response
			
			setActiveProbe(analysisID, probeID);
			jQuery('#analysis_holder_' +analysisID).unmask(); //hide the loading msg, unblock the div 
			drawLinePlot('lineplotAnalysis_'+analysisID, response, analysisID);
			jQuery('#lineplotAnalysis_'+analysisID).show();
			jQuery('#lineplot_'+analysisID).show();

	//		jQuery('#lineplotLegend_'+analysisID).prepend("<p class='legend_probe'>Line plot for "+probeDisplay +"</p>"); //add the probe ID to the legend
			
			jQuery('#lineplotLegend_'+analysisID).show();

			jQuery('body').data("LineplotData:" + analysisID, response); //store the response
			
			jQuery('body').data("activeLineplot:" + analysisID, probeID); //store the analysis ID and probe ID of this lineplot;
																		 //used to determine if the lineplot has already been drawn
			setLineplotProbes(analysisID, probeID);
			jQuery("#analysis_holder_" + analysisID).unmask(); 
			
			
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
}

//Draw the line plot
function drawLinePlot(divId, linePlotJSON, analysisID, forExport)	{
	
	
	var cohortArray = new Array();   // array of cohort ids
	var cohortDesc = new Array();    // array of cohort descriptions
	var cohortDisplayStyles = new Array();    // array of cohort display styles
	
	var gene_id = parseInt(linePlotJSON['gene_id']);   // gene_id will be null if this is a protein since first char is alpha for proteins

	// loop through and get the cohort ids and description into arrays in the order they should be displayed
	for (var key in linePlotJSON)  {
		// the "order" of the json objects starts with 1, so subtract 1 so it doesn't leave gap at start of array
		var arrayIndex = linePlotJSON[key]['order'] - 1;
		cohortArray[arrayIndex] = key;
		cohortDesc[arrayIndex] = linePlotJSON[key]['desc'];
		cohortDisplayStyles[arrayIndex] = linePlotJSON[key]['order'] % cohortBGColors.length;		
		
	}
	
	var statMapping = cohortArray.map(function(i)	{
		var data = linePlotJSON[i]['data'];
		
		// retrieve mean and standard error- round to 4 decimal places
		var mean = data['mean'];
		var stdError = data['stdError'];
		var min = mean - stdError;
		var max = mean + stdError;
		var desc = linePlotJSON[i]['desc'].replace(/_/g, ', ');
		var sampleCount = linePlotJSON[i]['sampleCount'];

		var meanFormatted = parseFloat(mean);
		meanFormatted = meanFormatted.toFixed(4);
		
		var stdErrorFormatted = parseFloat(stdError);
		stdErrorFormatted = stdErrorFormatted.toFixed(4);
		
		var cohortDisplayStyle = linePlotJSON[i]['order'] % cohortBGColors.length; 
		
		return {
			id:i,
			desc:desc,
			sampleCount:sampleCount,
			mean:mean,
			stdError:stdError,			
			meanFormatted:meanFormatted,
			stdErrorFormatted:stdErrorFormatted,			
			min:min,
			max:max,
			cohortDisplayStyle:cohortDisplayStyle
		};		
	});

	
	
	
	//if the user is setting the range manually:
	if(jQuery('#lineplotRangeRadio_Manual_'+analysisID).is(':checked')){
		
		var yMin = parseFloat(jQuery('#lineplotRangeMin_'+analysisID).val());
		var yMax = parseFloat(jQuery('#lineplotRangeMax_'+analysisID).val());

		
	}else{
		
		var yMin = statMapping[0].min;
		var yMax = statMapping[0].max;
		for (var idx=1; idx < statMapping.length; idx++)	{	
			yMin = statMapping[idx].min < yMin ? statMapping[idx].min : yMin;
			yMax = statMapping[idx].max > yMax ? statMapping[idx].max : yMax;
		}
		
		// Put in a rough switch so things can scale on the y axis somewhat dynamically
		if (yMax-yMin < 2)	{
			// round down to next 0.1
			yMin = Math.floor((yMin-0.1) * 10) / 10;

			// round up to next 0.1
			// and add another 0.01 to ensure that the highest tenths line gets included
			yMax = Math.ceil((yMax+0.1) * 10) / 10  + 0.01;
		} else	{
			yMin = Math.floor(yMin);
			yMax = Math.ceil(yMax);
		}		
			
		
		//set the manual value textboxes with the current yMin and yMax
		jQuery('#lineplotRangeMin_'+analysisID).val(roundNumber(yMin,2));
		jQuery('#lineplotRangeMax_'+analysisID).val(roundNumber(yMax,2));
		
	}

		
	var w = cohortArray.length * 150;//generate the width dynamically using the cohort count
	h = 300,
	margin = 55,
	widthErrorBarBottomAndTopLines = 6,
	radiusDot = 3,
	h_legend = 0; //used to draw the legend for export

	
	if(forExport){
		h_legend=35+ 30 * (cohortArray.length); //h_legend is the extra space required for the legend 
	}
	
	
	var x = pv.Scale.ordinal(statMapping, function(e){return e.id}).splitBanded(0, w, 1/2);
	var y = pv.Scale.linear(yMin, yMax).range(0, h)			
	
	var numCohorts = cohortArray.length;
	
	// need to add a blank entry at the beginning of the arrays for use by drawCohortLegend
	cohortArray = [''].concat(cohortArray);
	cohortDesc = [''].concat(cohortDesc);
	cohortDisplayStyles = [''].concat(cohortDisplayStyles);
	
	if(forExport){
		cohortDesc=highlightCohortDescriptions(cohortDesc, true);
	}
	
	
	var vis = new pv.Panel().canvas(document.getElementById(divId)) 	
	.width(w)
	.height(h+h_legend)
	.margin(margin);
	
	/* Add the y-axis rules */
	vis.add(pv.Rule)
	.data(y.ticks())
	.strokeStyle("#ccc")
	.bottom(y)
	.anchor("left").add(pv.Label)
	.font("14px sans-serif")
	.text(y.tickFormat);

	vis.add(pv.Label)
	.data(statMapping)
	.left(function(d){return x(d.id)})
	.bottom(-20)
	.textAlign("center")
	.font("14px sans-serif")
	.events("all")
	.title(function(d){return d.desc})	
	.text(function(d){return d.id + "(n=" + d.sampleCount + ")"});
	
	/* Add the log2 label */
	vis.add(pv.Label)
	.left(-40)
	.bottom(h/2)
	.textAlign("center")
	.textAngle(-Math.PI / 2)
	.font("14px sans-serif")
    .text("log2 intensity");
	
	if (gene_id)  {
		/* Add the title with link to gene info*/
		vis.add(pv.Label)
		.font("bold 16px sans-serif")
		.textStyle("#065B96")
	    .left(w/2)
	    .bottom(300)
	    .textAlign("center")
	  
		    /*Add link in title to gene info */
	    .cursor("pointer")
	    .event("mouseover", function(){ self.status = "Gene Information"})
	    .event("mouseout", function(){ self.status = ""})
	    .event("click", function(d) {self.location = "javascript:showGeneInfo('"+gene_id +"');"})
		.events("all")
	    .title("View gene information")
	    .text(getGeneforDisplay(analysisID, getActiveProbe(analysisID)));
	}
	else  {
		/* Add the title without link to gene info*/
		vis.add(pv.Label)
		.font("bold 16px sans-serif")
		.textStyle("#065B96")
	    .left(w/2)
	    .bottom(300)
	    .textAlign("center")
	    .text(getGeneforDisplay(analysisID, getActiveProbe(analysisID)));		
	}
	// create line
    var line = 	vis.add(pv.Line)
    .data(statMapping)
	.strokeStyle("#000000")
    .bottom(function(d){return y(d.mean)})
	.left(function(d){return x(d.id)});
    
    // add dots at each point in line
    line.add(pv.Dot)
      .radius(radiusDot)
	  .strokeStyle("#000000")
      .fillStyle("#000000")
      .title(function(d){return d.meanFormatted + " +/- " + d.stdErrorFormatted});

    // Add error bars
    // vertical line
    line.add(pv.Rule)
      .left(function(d){return x(d.id)})
      .bottom(function(d) {return y(d.mean - Math.abs(d.stdError))})
      .top(function(d) {return y(yMax) - y(d.mean + Math.abs(d.stdError)) + h_legend}); 
    

    // bottom horizontal line
    line.add(pv.Rule)
      .left(function(d){return x(d.id) - widthErrorBarBottomAndTopLines/2} )
      .bottom(function(d) { return y(d.mean - d.stdError)})
      .width(widthErrorBarBottomAndTopLines);
    // top horizontal line
    line.add(pv.Rule)
      .left(function(d){return x(d.id) - widthErrorBarBottomAndTopLines/2} )
      .bottom(function(d) { return y(d.mean + d.stdError)})
      .width(widthErrorBarBottomAndTopLines);
    
	/*add legend if export */
	if(forExport){
			
	    /*		Legend	     */
	    var legend = vis.add(pv.Bar)
	    	.data(statMapping)
	    	.height(25)
	    	.top(function(){return (this.index * 30)-20 })
	    	.antialias(false)
	    	.left(-30)
	    	.strokeStyle("#000")
	    	.lineWidth(1)
	    	.width(30)
	    	.fillStyle(function (d) {return cohortBGColors[d.cohortDisplayStyle]});

	    legend.anchor("center").add(pv.Label)
    	.textStyle("#000")
    	.font("12px  sans-serif")
    	.text(function(d){return d.id} );
	    
	    vis.add(pv.Label)
	    	.data(statMapping)
	    .top(function(){return this.index * 30})
	    .antialias(false)
	    .left(5)
    	.textStyle("#000")
    	.font("12px  sans-serif")
    //	.text(function(d){return d.desc});   	
    	.text(function(){return cohortDesc[this.index+1].replace(/_/g, ', ')});
	}

	

	vis.root.render();
	

	/////////////////////////////////////////////////////////////////////////////////////////////////////////		
	jQuery("#lineplotLegend_" + analysisID).html(drawCohortLegend(numCohorts, cohortArray, cohortDesc, cohortDisplayStyles));
	
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Box Plot Visualization Methods
// Show, Load Data and Draw
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Method set the probes in a select list for current page
function setProbesDropdown(analysisID, selectedProbeID, divId)	{
	var analysisIndex = getAnalysisIndex(analysisID);
	var probeIds = analysisProbeIds[analysisIndex].probeIds;
	var selectList = analysisProbeIds[analysisIndex].selectList;

	jQuery(divId).empty();
	for (var i=0; i<probeIds.length; i++) {
		
		if (probeIds[i] == selectedProbeID)	{
			jQuery(divId).append(jQuery("<option id></option>").attr("selected", "selected").attr("id", probeIds[i]).attr("value", selectList[i]).text(selectList[i]));
		} else	{
			jQuery(divId).append(jQuery("<option></option>").attr("id", probeIds[i]).attr("value", selectList[i]).text(selectList[i]));
		}
	}	
}


//Method to add the probes for the box plot
function setBoxplotProbes(analysisID, selectedProbeID)	{
	setProbesDropdown(analysisID, selectedProbeID, "#probeSelection_" + analysisID);
}

// Load the box plot data
function loadBoxPlotData(analysisID, probeID)	{	
	jQuery('#boxplotEmpty_' +analysisID).hide(); //hide the message that tells the user to select a probe first
	
	if (probeID === undefined)	{
		// We are called from the user switching probes, throw up the mask and get the probeID
		jQuery("#analysis_holder_" + analysisID).mask(); //hide the loading screen
		probeID = jQuery("#probeSelection_" + analysisID).find('option:selected').attr('id');
		
	}
	
	// retrieve the corresponding display value for the probe Id 
	
	/*
    var analysisIndex = getAnalysisIndex(analysisID);
    var probeDisplay = ""
    var probeIds = analysisProbeIds[analysisIndex].probeIds ;
    var maxProbeIndex = analysisProbeIds[analysisIndex].maxProbeIndex; 
    for (var i=0; i<maxProbeIndex; i++)  {
    	if (probeIds[i] == probeID) {
    		probeDisplay = analysisProbeIds[analysisIndex].selectList[i];
    		break;
    	}
    }
        
        */
	rwgAJAXManager.add({
		url:getBoxPlotDataURL,
		data: {id: analysisID, probeID: probeID},
		timeout:60000,
		success: function(response) {
			setActiveProbe(analysisID, probeID);
			drawBoxPlot('boxplotAnalysis_'+analysisID, response, analysisID);
			jQuery('#boxplotLegend_'+analysisID).show();
			jQuery('#boxplotAnalysis_'+analysisID).show();	
			
			jQuery('body').data("BoxplotData:" + analysisID, response); //store the response
			
			jQuery('body').data("activeBoxplot:" + analysisID, probeID); //store the analysis ID and probe ID of this boxplot;
																		 //used to determine if the boxplot has already been drawn
		
			setBoxplotProbes(analysisID, probeID);
			jQuery("#analysis_holder_" + analysisID).unmask(); 
			
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
}

// Helper function to provide the rank for the percentile calculation in the box plot
function getRank(P, N)	{
	return Math.round(P/100 * N + 0.5);			// Use P/100 * N + 0.5 as denoted here: http://en.wikipedia.org/wiki/Percentile
}

// Draw the box plot
function drawBoxPlot(divId, boxPlotJSON, analysisID, forExport)	{
	// boxPlotJSON should be a map of cohortID:[desc:cohort description, order:display order for the cohort, data:sorted log2 intensities]
	
	
	var cohortArray = new Array();   // array of cohort ids
	var cohortDesc = new Array();    // array of cohort descriptions
	var cohortDisplayStyles = new Array();    // array of cohort display styles (i.e. number from 0..4)

	var gene_id = parseInt(boxPlotJSON['gene_id']);   // gene_id will be null if this is a protein since first char is alpha for proteins
	
	// loop through and get the cohort ids and description into arrays in the order they should be displayed
	for (var key in boxPlotJSON)  {
		// the "order" of the json objects starts with 1, so subtract 1 so it doesn't leave gap at start of array
		var arrayIndex = boxPlotJSON[key]['order'] - 1;
		cohortArray[arrayIndex] = key;
		cohortDesc[arrayIndex] = boxPlotJSON[key]['desc'];
		cohortDisplayStyles[arrayIndex] = boxPlotJSON[key]['order'] % cohortBGColors.length;		
	}
	
	// Map the all four quartiles to the key (e.g. C1)
	var statMapping = cohortArray.map(function(i)	{
		var data = boxPlotJSON[i]['data'];
		var cohortDisplayStyle = boxPlotJSON[i]['order'] % cohortBGColors.length;		
		var desc = boxPlotJSON[i]['desc'].replace(/_/g, ', ');
		var sampleCount = boxPlotJSON[i]['sampleCount'];
		
		return {
			id:i,
			cohortDisplayStyle:cohortDisplayStyle,
			desc:desc,
			sampleCount:sampleCount,
			min:data[getRank(5, data.length)-1],
			max:data[getRank(95, data.length)-1],			
			median:data[getRank(50, data.length)-1],
			lq:data[getRank(25, data.length)-1],
			uq:data[getRank(75, data.length)-1]
		};		
	});
	
	
	//if the user is setting the range manually:
	if(jQuery('#boxplotRangeRadio_Manual_'+analysisID).is(':checked')){
		
		var yMin = parseFloat(jQuery('#boxplotRangeMin_'+analysisID).val());
		var yMax = parseFloat(jQuery('#boxplotRangeMax_'+analysisID).val());

		
	}else{
		//auto set range otherwise
		var yMin = statMapping[0].min;
		var yMax = statMapping[0].max;
		for (var idx=1; idx < statMapping.length; idx++)	{	
			yMin = statMapping[idx].min < yMin ? statMapping[idx].min : yMin;
			yMax = statMapping[idx].max > yMax ? statMapping[idx].max : yMax;
		}
		
		// Put in a rough switch so things can scale on the y axis somewhat dynamically
		if (yMax-yMin < 2)	{
			// round down to next 0.1
			yMin = Math.floor((yMin-0.2) * 10) / 10 ;
			
			// round up to next 0.1
			// and add another 0.01 to ensure that the highest tenths line gets included
			yMax = Math.ceil((yMax+0.2) * 10) / 10 + 0.01;
		} else	{
			yMin = Math.floor(yMin);
			yMax = Math.ceil(yMax);
		}
		
		//set the manual value textboxes with the current yMin and yMax
		jQuery('#boxplotRangeMin_'+analysisID).val(roundNumber(yMin,2));
		jQuery('#boxplotRangeMax_'+analysisID).val(roundNumber(yMax,2));
		
	}
	
	var title = getGeneforDisplay(analysisID, getActiveProbe(analysisID));
	
	var w = cohortArray.length * 140;//generate the width dynamically using the cohort count	
	var  h = 300,  
		x = pv.Scale.ordinal(statMapping, function(e){return e.id}).splitBanded(0, w, 1/2),
		y = pv.Scale.linear(yMin, yMax).range(0, h-15),
		s = x.range().band / 2;
	
	
	var numCohorts = cohortArray.length;
	
	// need to add a blank entry at the beginning of the arrays for use by drawCohortLegend
	cohortArray = [''].concat(cohortArray);
	cohortDesc = [''].concat(cohortDesc);
	cohortDisplayStyles = [''].concat(cohortDisplayStyles);
	
	if(forExport){
		h=320 + 30 * (cohortArray.length);
		cohortDesc=highlightCohortDescriptions(cohortDesc, true);
	}

		var vis = new pv.Panel().canvas(document.getElementById(divId)) 	
		.width(w)
		.height(h)
		.margin(55);

		if (gene_id)  {
			/* Add the title with link to gene info*/
			vis.add(pv.Label)
			.font("bold 16px sans-serif")
		    .left(w/2)
		    .bottom(300)
		    .textStyle("#065B96")
		    .textAlign("center")
	    	/*Add link in title to gene info */
		    .cursor("pointer")
		    .event("mouseover", function(){ self.status = "Gene Information"})
		    .event("mouseout", function(){ self.status = ""})
		    .event("click", function(d) {self.location = "javascript:showGeneInfo('"+gene_id +"');"})
			.events("all")   
			.title("View gene information")
			.text(title);
		}
		else {
			/* Add the title without link to gene info*/
			vis.add(pv.Label)
			.font("bold 16px sans-serif")
		    .left(w/2)
		    .bottom(300)
		    .textStyle("#065B96")
		    .textAlign("center")
			.text(title);
			
		}
	
		/* Add the y-axis rules */
		vis.add(pv.Rule)
		.data(y.ticks())
		.strokeStyle("#ccc")
		.bottom(y)
		.anchor("left").add(pv.Label)
		.font("14px sans-serif")
		.text(y.tickFormat);	
		
		/* Add the log2 label */
		vis.add(pv.Label)
		.left(-40)
		.bottom(300/2) //300 is the height of the boxplot
		.textAlign("center")
		.textAngle(-Math.PI / 2)
		.font("14px sans-serif")
	    .text("log2 intensity");

		/* Add a panel for each data point */
		var points = vis.add(pv.Panel)
		.def("showValues", false)
		.data(statMapping)
		.left(function(d){return x(d.id)})
		.width(s * 2)
		.events("all");

		/* Add the experiment id label */
		vis.add(pv.Label)
		.data(statMapping)
		.left(function(d){return x(d.id) + s})
		.bottom(-20)
		.textAlign("center")
		.font("14px sans-serif")
		.events("all")
		.title(function(d){return d.desc})
		.text(function(d){return d.id + "(n=" + d.sampleCount + ")"});
		
		/*add legend if export */
		if(forExport){
				
		    /*		Legend	     */
		    var legend = vis.add(pv.Bar)
		    	.data(statMapping)
		    	.height(25)
		    	.top(function(){return (this.index * 30)-20 })
		    	.antialias(false)
		    	.left(-30)
		    	.strokeStyle("#000")
		    	.lineWidth(1)
		    	.width(30)
		    	.fillStyle(function (d) {return cohortBGColors[d.cohortDisplayStyle]});

		    legend.anchor("center").add(pv.Label)
	    	.textStyle("#000")
	    	.font("12px  sans-serif")
	    	.text(function(d){return d.id} );
		    
		    vis.add(pv.Label)
		    	.data(statMapping)
		    .top(function(){return this.index * 30})
		    .antialias(false)
		    .left(5)
	    	.textStyle("#000")
	    	.font("12px  sans-serif")
	    //	.text(function(d){return d.desc});   	
	    	.text(function(){return cohortDesc[this.index+1].replace(/_/g, ', ')});
		}
		
		

		/* Add the range line */
		points.add(pv.Rule)
		.left(s)
		.bottom(function(d){return y(d.min)})
		.height(function(d){return y(d.max) - y(d.min)});

		/* Add the min and max indicators */
		var minLine = points.add(pv.Rule)
			.data(function(d){return [d.min]})
			.bottom(y)
			.left(s / 2)
			.width(s)
			.anchor("bottom").add(pv.Label)
			.visible(function(){return this.parent.showValues()}) 
			.text(function(d){return d.toFixed(2)});
		
		var maxLine = points.add(pv.Rule)
			.data(function(d){return [d.max]})
			.bottom(y)
			.left(s / 2)
			.width(s)
			.anchor("top").add(pv.Label)
			.visible(function(){return this.parent.showValues()}) 
			.text(function(d){return d.toFixed(2)});

		/* Add the upper/lower quartile ranges */
		var quartileBar = points.add(pv.Bar)
			.fillStyle(function (d) {return cohortBGColors[d.cohortDisplayStyle]})
			.bottom(function(d){return y(d.lq)})
			.height(function(d){return y(d.uq) - y(d.lq)})
			.strokeStyle("black")
			.lineWidth(1)
			.event("mouseover", function() {return this.parent.showValues(true)}) 
			.event("mouseout", function() {return this.parent.showValues(false)})
			.antialias(false);
		
		var lqLabel = quartileBar.add(pv.Label)
			.visible(function(){return this.parent.showValues()})
			.text(function(d){return d.lq.toFixed(2)})
			.textAlign("right")
			.textBaseline("top");
		
		var uqLabel = quartileBar.anchor("top").add(pv.Label)		
			.visible(function(){return this.parent.showValues()})
			.left(-15)
			.text(function(d){return d.uq.toFixed(2)})
			.textMargin(-10);
		
		/* Add the median line */
		points.add(pv.Rule)
		.bottom(function(d){ return y(d.median)})
		.anchor("right").add(pv.Label)
		.visible(function(){return this.parent.showValues()})
		.text(function(d){return d.median.toFixed(2)});

		vis.render();

		/////////////////////////////////////////////////////////////////////////////////////////////////////////		
		jQuery("#boxplotLegend_" + analysisID).html(drawCohortLegend(numCohorts, cohortArray, cohortDesc, cohortDisplayStyles));
		
}

// Show the heatmap visualization 
function showVisualization(analysisID, changedPaging)	{	
	
	var analysisHeaderDiv = "#TrialDetail_" + analysisID + "_anchor"
	var divID = "#analysis_results_" + analysisID;
	var divID2 = "analysis_results_" + analysisID;
	var loadingDiv = "#analysis_holder_"+ analysisID;
	var imgExpand = "#imgExpand_"  + analysisID;
	var div = document.getElementById(divID);	
	var hmFlagDiv = divID+"_state";
	var hmFlag = jQuery(hmFlagDiv).val();
	
	// Check the value of the hidden field that is capturing the following "click" states
	// 0: No heatmap loaded, hidden
	// 1: Heatmap loaded, visible
	// 2: Heatmap loaded, hidden
	
	// if the paging has changed, need to reload page
	if (hmFlag != "1")	{				
		var src = jQuery(imgExpand).attr('src').replace('down_arrow_small2.png', 'up_arrow_small2.png');
		jQuery(imgExpand).attr('src',src);
		jQuery(analysisHeaderDiv).addClass("active-analysis");
		jQuery(loadingDiv).toggle();
		openAnalyses.push(analysisID); //store this as an open analysis
		
		if (hmFlag == "0")	{

			setVisTabs(analysisID);
			jQuery(loadingDiv).mask("Loading...");
			loadAnalysisResultsGrid(analysisID, {'max': 10, 'offset':0, 'cutoff': 0, 'search': "", 'sortField': "", "order": "asc"});
		}
		
		jQuery(hmFlagDiv).val("1");
	} else	{
		var src = jQuery(imgExpand).attr('src').replace('up_arrow_small2.png', 'down_arrow_small2.png');
		jQuery(imgExpand).attr('src',src);
		jQuery(loadingDiv).toggle('blind', {}, 'fast');
		jQuery(analysisHeaderDiv).removeClass("active-analysis");	
		jQuery(hmFlagDiv).val("2");
		
		//remove the analysis from the array, while leaving all others
		//openAnalyses = openAnalyses.splice( jQuery.inArray(analysisID, openAnalyses), 1 );
		removeByValue(openAnalyses,analysisID);
		
	} 	
	return false;
}

//This function will kick off the webservice that generates the QQ plot.
function loadQQPlot(analysisID)
{
	jQuery('#qqplot_results_' +analysisID).empty().addClass('ajaxloading');
	jQuery.ajax( {
	    "url": getQQPlotURL,
	    bDestroy: true,
	    bServerSide: true,
	    data: {analysisId: analysisID},
	    "success": function ( json ) {
	    	jQuery('#analysis_holder_' +analysisID).unmask();
	    	jQuery('#qqplot_results_' + analysisID).prepend("<img src='" + json.imageURL + "' />").removeClass('ajaxloading');
	    	jQuery('#qqplot_export_' + analysisID).attr('href', json.imageURL);
	    	},
	    "error": function ( json ) {
	    	jQuery('#qqplot_results_' + analysisID).prepend(json).removeClass('ajaxloading');
	    	jQuery('#analysis_holder_' +analysisID).unmask();
	    },
	    "dataType": "json"
	} );		
}

// This function will load the analysis data into a GRAILS template.
function loadAnalysisResultsGrid(analysisID, paramMap)
{
	paramMap.analysisId = analysisID
	jQuery('#analysis_results_table_' + analysisID + '_wrapper').empty().addClass('ajaxloading');
	jQuery.ajax( {
	    "url": getAnalysisDataURL,
	    bDestroy: true,
	    bServerSide: true,
	    data: paramMap,
	    "success": function (jqXHR) {
	    	jQuery('#analysis_holder_' +analysisID).unmask();
	    	jQuery('#analysis_results_table_' + analysisID + '_wrapper').html(jqXHR).removeClass('ajaxloading');
	    },
	    "error": function (jqXHR, error, e) {
	    	jQuery('#analysis_results_table_' + analysisID + '_wrapper').html(error).removeClass('ajaxloading');
	    	jQuery('#analysis_holder_' +analysisID).unmask();
	    },
	    "dataType": "html"
	} );		
}

// Helper function to draw the legend for the cohorts in the visualization panel
function drawCohortLegend(numCohorts, cohorts, cohortDescriptions, cohortDisplayStyles)	{
	
	cohortDescriptions = highlightCohortDescriptions(cohortDescriptions);
	
	var pCohortAll = "<table class='cohort_table'>"
	var classIndex = null;
	var pCohort = "";
	for(var i=1; i<=numCohorts; i++) {
		pCohort = "<tr><td style='width:40px'><p class='cohort' style='background-color:" + cohortBGColors[cohortDisplayStyles[i]]  + "'>" +cohorts[i] +"</p></td><td><p class='cohortDesc'>"+cohortDescriptions[i].replace(/_/g, ', ')+'</p></td>';
		pCohortAll = pCohortAll +  pCohort;
	}
	return pCohortAll + "</table>	";
}

//Show diff between each cohort
//returnOnlyDiff: if true, return only the different terms
function highlightCohortDescriptions(cohortDesc, returnOnlyDiff){
	
	var arySplit = new Array();
	var aryDif = new Array();
	var aryDescNew = new Array();
	
	//1. Split each cohort description into an array of terms
	for (var i=1; i<cohortDesc.length; i++){
		arySplit[i]= cohortDesc[i].split('_');
	}
	
	//2. Loop through the array and compare each term to the term in the same position of the next description
	//	 mark which ones are same and different in aryDif
	for (var i=1; i<arySplit.length-1; i++){
		
			for(var x=0; x < arySplit[i].length; x++){
				
					if(trim(arySplit[i][x]).toUpperCase() == trim(arySplit[i+1][x]).toUpperCase()){
						
							if(aryDif[x] != false){
								aryDif[x] = true;
							}
							else{
								aryDif[x] = false;
								}
						}
					else{
						aryDif[x] = false;
					}
				}
		}
	
	//3. Rebuild array, inserting syntax to denote which terms are different
	for (var i=1; i<arySplit.length; i++){
		
		aryDescNew[i]=''; //initilize the first value
		
		for(var x=0; x < arySplit[i].length; x++){

			
				if(aryDif[x] == true){ //the terms are the same
					if(!returnOnlyDiff){
						aryDescNew[i] = aryDescNew[i] + arySplit[i][x];	
					}
				}
				else{	//the terms are different
					if(!returnOnlyDiff){ 
						aryDescNew[i] = aryDescNew[i] +"<span class='highlight'>" +arySplit[i][x] +"</span>";
					}
					else if(returnOnlyDiff){
						aryDescNew[i] = aryDescNew[i] +arySplit[i][x] + ', ';
					}
				}
				
				//check if this is the last term; if not, add an underscore between terms
				if(x+1 < arySplit[i].length && !returnOnlyDiff){
					aryDescNew[i] = aryDescNew[i]+'_';
				}
			}
		
			if(returnOnlyDiff){//remove trailing space and comma
				aryDescNew[i] = aryDescNew[i].slice(0,-2);		
			}
	}
	
	return aryDescNew;

}
//remove whitespace
function trim(stringToTrim) {
	return stringToTrim.replace(/^\s+|\s+$/g,"");
}

/* Find the width of a text element */
String.prototype.visualLength = function(fontFamily) 
{ 
    var ruler = document.getElementById("ruler"); 
    ruler.style.font = fontFamily; 
    ruler.innerHTML = this; 
    return ruler.offsetWidth; 
}



//Round number to given decimal place
function roundNumber(num, dec) {
	var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec);
	return result;
}

function openSaveSearchDialog()  {

	var keywords = getSearchKeywordList();

	if (keywords.length>0)  {
		jQuery('#save-modal-content').modal();
	}
	else  {
		alert("No search criteria to save!")
	}
		

	return false;

}

// find the analysis in the array with the given id
function getAnalysisIndex(id)  {
	for (var i = 0; i < analysisProbeIds.length; i++)  {
		if (analysisProbeIds[i].analysisId == id)  {
			return i;
		}
	}
	
    return -1;  // analysis not found		
}

//remove an element from an array by value, keeping all others in place
function removeByValue(arr, val) {
	for(var i=0; i<arr.length; i++) {
		if(arr[i] == val) {
			arr.splice(i, 1);
			break;
		}
	}
}

function updateSelectedAnalyses() {
	var selectedboxes = jQuery(".analysischeckbox:checked");
	if (selectedboxes.length > 0) {
		jQuery('#selectedAnalyses').html("<b>(" + selectedboxes.length + "</b> analyses selected)");
	}
	else {
		jQuery('#selectedAnalyses').html("&nbsp;");
	}
}

function updateAnalysisData(analysisId, full) {
	jQuery('#partialanalysiswarning').hide();
	jQuery('#analysisgenefilteredwarning').hide();
	
	//If passed a null analysisId, check to see if an analysis is displayed, then update it.
	if (analysisId == null) {
		var currentId = jQuery('#gridViewWrapperAnalysis').attr('name');
		if (currentId == null || currentId == 0) {
			return false;
		}
		analysisId = currentId;
	}
	
	jQuery('#gridViewWrapperAnalysis').empty().addClass('ajaxloading');
	$j.ajax({
		url:analysisDataURL,
		data: {id: analysisId, full: full},
		success: function(response) {
			jQuery('#gridViewWrapperAnalysis').removeClass('ajaxloading');
	 	 	var dtAnalysis  = new dataTableWrapper('gridViewWrapperAnalysis', 'gridViewTableAnalysis', 'Title', [[2, "asc"]], 25);
	   		dtAnalysis.loadData(response);
	   		if (!full && response.rowCount > 1000) {
		   		jQuery('#loadfullanalysis').text('Load all ' + response.rowCount + ' rows')
	   			jQuery('#partialanalysiswarning').show();
	   		}
	   		if (response.filteredByGenes) {
		   		jQuery('#analysisgenefilteredwarning').show();
		   	}
		},
		error: function(xhr) {
			alert(xhr.message);
		}
	});
}

function toggleSidebar() {
	var sidebarIsVisible = (jQuery('#sidebar:visible').size() > 0);
	if (sidebarIsVisible) {
		jQuery('#sidebar').fadeOut(resizeAccordion);
		var bgimg = jQuery('#sidebartoggle').css('background-image').replace('-left', '-right');
		jQuery('#sidebartoggle').css('background-image', bgimg);
	}
	else {
		jQuery('#sidebar').fadeIn();
		resizeAccordion(); //Not a callback here - resize as soon as it starts appearing.
		var bgimg = jQuery('#sidebartoggle').css('background-image').replace('-right', '-left');
		jQuery('#sidebartoggle').css('background-image', bgimg);
	}
}

//Globally prevent AJAX from being cached (mostly by IE)
jQuery.ajaxSetup({
	cache: false
});

function rwg_onFoldersListChanges(newFolderList) {
    var isSearch = newFolderList !== undefined;
    var data = {
        search: isSearch,
    };

    if (isSearch) {
        data.folderIds = newFolderList;
    }

    jQuery.ajax({
        url: renderRootURL,
        data: jQuery.param(data, true /* no brackets */),
        success: function(response) {
            jQuery('#results-div').removeClass('ajaxloading').html(response);
            updateAnalysisData(null, false);
            if (isSearch) {
                displayResultsNumber();
            }
        },
        error: function(xhr) {
            console.log('Error rendering root folders (possibly after search)', xhr);
        }
    });
}
