/*************************************************************************   
* Copyright 2008-2012 Janssen Research & Development, LLC.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************/

var analysisConcept = null;

function renderCohortSummary(){
    var summary = ""
    for(var subset = 1; subset<=GLOBAL.NumOfSubsets; subset++){
        var subset_query = getQuerySummary(subset)
        if(subset_query != ""){
            summary += "Subset " + subset + ": "
            summary += "<br>"
            summary += subset_query
            summary += "<br><br>"
        }

    }
    var summary_html = ""
    if(summary == ""){
        summary_html = "<br><span class=\"cohortwarning\">Warning! You have not selected a study and the analyses will not work. Please go back to the 'Comparison' tab and make a cohort selection.</span><br>";
    }else{
        summary_html = "<br>" + summary;
    }

    var cohort_element = document.getElementById("cohortSummary");
    if (cohort_element != null) {
        cohort_element.innerHTML=summary_html;
    }
}

function checkPreviousAnalysis() {
	//If the user clicks submit but they've run a analysis recently check with them to make sure they want to clear the results.
	if(GLOBAL.AnalysisRun)
	{
		return confirm('When you navigate to a new analysis the current analysis results will be cleared! If you would like your results to be saved click the "Save to PDF" button. Are you sure you wish to navigate away?');
	}
	
	return true;
}

//This function fires when an item is dropped onto one of the independent/dependent variable DIVs in the data association tool.
function dropOntoVariableSelection(source, e, data) {
	data.node.attributes.oktousevalues = "N"
	var concept = createPanelItemNew(this.el, convertNodeToConcept(data.node));
	return true;
}

//This function fires when an item is dropped onto one of the
//independent/dependent variable DIVs in the data association tool.
//Used to ensure only a numeric value is dropped. For all values use dropOntoCategorySelection function
function dropNumericOntoCategorySelection(source, e, data){
	var targetdiv=this.el;
	if(data.node.leaf==false && !data.node.isLoaded()){
		data.node.reload(function(){dropNumericOntoCategorySelection2(source, e, data, targetdiv);});
		}
	else{
		dropNumericOntoCategorySelection2(source, e, data, targetdiv);
		}
	return true;
}

function dropNumericOntoCategorySelection2(source, e, data, targetdiv)
{
	//Node must be folder so use children leafs
	if(data.node.leaf==false) 
	{
		//Keep track of whether all the nodes are numeric or not.
		var allNodesNumeric = true
		
		//Keep track of whether the folder has any leaves.
		var foundLeafNode = false
		
		//Loop through child nodes to add them to input.
		for ( var i = 0; i<data.node.childNodes.length; i++)
		{
			//Grab the child node.
			var child=data.node.childNodes[i];
			
			//This tells us whether it is a numeric or character node.
			var val=child.attributes.oktousevalues;

			//If we are a numeric leaf node, add it to the tree.
			if(val==='Y' && child.leaf==true)
			{
				//Reset the alpha/numeric flag so we don't get the popup for entering a value.
				child.attributes.oktousevalues = "N"; 

				//Set the flag indicating we had a leaf node.
				foundLeafNode = true;
				
				//Add the item to the input.
				var concept = createPanelItemNew(targetdiv, convertNodeToConcept(child));
				
				//Set back to original value
				child.attributes.oktousevalues=val; 
			}
			else if(val==='N' && child.leaf==true)
			{
				//Set the flag indicating we had a leaf node.
				foundLeafNode = true;				
				
				//If we find a non-numeric node, set our flag.
				allNodesNumeric = false
			}
			
		}

		//If no leaf nodes found, alert the user.
		if(!foundLeafNode)
		{
			Ext.Msg.alert('No Nodes in Folder','When dragging in a folder you must select a folder that has leaf nodes directly under it.');
		}		
		
		//If we found a non numeric node, alert the user.
		if(!allNodesNumeric && foundLeafNode)
		{
			Ext.Msg.alert('Numeric Input Required','Please select numeric concepts only for this input. Numeric concepts are labeled with a "123" in the tree.');
		}
	}
	else 
	{
		//If we dragged a numeric leaf, add it to the input. Otherwise alert the user.
		if(data.node.attributes.oktousevalues==='Y')
		{
			//This tells us whether it is a numeric or character node.
			var val=data.node.attributes.oktousevalues;
			
			//Reset the alpha/numeric flag so we don't get the popup for entering a value.
			data.node.attributes.oktousevalues="N";
			
			//Add the item to the input.
			var concept = createPanelItemNew(targetdiv, convertNodeToConcept(data.node));
			
			//Set back to original value
			data.node.attributes.oktousevalues=val;
		}
		else
		{
			Ext.Msg.alert('Numeric Input Required','Please select numeric concepts only for this input. Numeric concepts are labeled with a "123" in the tree.');
		}		
	}
	return true;
} 


//This function fires when an item is dropped onto one of the
//independent/dependent variable DIVs in the data association tool.
function dropOntoCategorySelection(source, e, data)
{
	var targetdiv=this.el;
	
	if(data.node.leaf==false && !data.node.isLoaded())
	{
		data.node.reload(function(){
			analysisConcept = dropOntoCategorySelection2(source, e, data, targetdiv);
		});
	}
	else
	{
		analysisConcept = dropOntoCategorySelection2(source, e, data, targetdiv);
	}
	return true;
}

function dropOntoCategorySelection2(source, e, data, targetdiv)
{
	//Node must be folder so use children leafs
	if(data.node.leaf==false) 
	{

		//Keep track of whether the folder has any leaves.
		var foundLeafNode = false
		
		for ( var i = 0; i<data.node.childNodes.length; i++)
		{
			//Grab the child node.
			var child=data.node.childNodes[i];
			
			//This tells us whether it is a numeric or character node.
			var val=child.attributes.oktousevalues;
			
			//Reset the alpha/numeric flag so we don't get the popup for entering a value.
			child.attributes.oktousevalues = "N"; 
			
			//If this is a leaf node, add it.
			if(child.leaf==true)
			{
				//Add the item to the input.
				var concept = createPanelItemNew(targetdiv, convertNodeToConcept(child));
				
				//Set the flag indicating we had a leaf node.
				foundLeafNode = true;
			}
			
			//Set back to original value
			child.attributes.oktousevalues=val;
		}
		//Adding this condition for certain nodes like Dosage and Response, where children of Dosage & Response are intentionally hidden 
		if (data.node.childrenRendered && data.node.firstChild == null) {
			foundLeafNode = true;
			var concept = createPanelItemNew(targetdiv, convertNodeToConcept(data.node));
		}
		
		//If no leaf nodes found, alert the user.
		if(!foundLeafNode)
		{
			Ext.Msg.alert('No Nodes in Folder','When dragging in a folder you must select a folder that has leaf nodes directly under it.');
		}				
	}
	else 
	{
		//This tells us whether it is a numeric or character node.
		var val=data.node.attributes.oktousevalues;
		
		//Reset the alpha/numeric flag so we don't get the popup for entering a value.
		data.node.attributes.oktousevalues="N";
		
		//Add the item to the input.
		var concept = createPanelItemNew(targetdiv, convertNodeToConcept(data.node));
		
		//Set back to original value
		data.node.attributes.oktousevalues=val;
	}
	return concept;
} 

//This function will create an array of all the node types from a box that i2b2 nodes were dragged into.
function createNodeTypeArrayFromDiv(divElement,attributeToPull)
{
	var nodeTypeList = [];	
	
	//If the category variable element has children, we need to parse them and add their values to an array.
	if(divElement.dom.childNodes[0])
	{
		//Loop through the category variables and add them to a comma seperated list.
		for(nodeIndex = 0; nodeIndex < divElement.dom.childNodes.length; nodeIndex++)
		{
			var currentNode = divElement.dom.childNodes[nodeIndex]
			var currentNodeType = currentNode.attributes.getNamedItem(attributeToPull).value
			
			//If we find an item, add it to the array.
			if(currentNodeType) nodeTypeList.push(currentNodeType.toString());
		}
	}
	
	//Make the elements in the array unique.
	return nodeTypeList.unique();
}

//This might be inefficient. 
//Return new array with duplicate values removed
Array.prototype.unique =
function() {
var a = [];
var l = this.length;
for(var i=0; i<l; i++) {
  for(var j=i+1; j<l; j++) {
    // If this[i] is found later in the array
    if (this[i] === this[j])
      j = ++i;
  }
  a.push(this[i]);
}
return a;
};

function setupSubsetIds(formParams){
    runAllQueries(function(){
        submitJob(formParams);
    });
}

function readConceptVariables(divIds){
	var variableConceptCode = ""
	var variableEle = Ext.get(divIds);
	
	//If the variable element has children, we need to parse them and concatenate their values.
	if(variableEle && variableEle.dom.childNodes[0])
	{
		//Loop through the variables and add them to a comma seperated list.
		for(nodeIndex = 0; nodeIndex < variableEle.dom.childNodes.length; nodeIndex++)
		{
			//If we already have a value, add the seperator.
			if(variableConceptCode != '') variableConceptCode += '|' 
			
			//Add the concept path to the string.
				variableConceptCode += getQuerySummaryItem(variableEle.dom.childNodes[nodeIndex]).trim()
		}
	}
	return variableConceptCode;
}

function waitWindowForAnalysis() {
	//Mask the panel while the analysis runs.
	Ext.getCmp('dataAssociationPanel').body.mask("Running analysis...", 'x-mask-loading');
}

function checkPluginJobStatus(jobName) {
	var secCount = 0;
	var pollInterval = 1000;   // 1 second
	
	var updateJobStatus = function(){
		secCount++;
		Ext.Ajax.request(
			{
				url : pageInfo.basePath+"/asyncJob/checkJobStatus",
				method : 'POST',
				success : function(result, request)
				{
					var jobStatusInfo = Ext.util.JSON.decode(result.responseText);					 
					var status = jobStatusInfo.jobStatus;
					var errorType = jobStatusInfo.errorType;
					var viewerURL = jobStatusInfo.jobViewerURL;
					var altViewerURL = jobStatusInfo.jobAltViewerURL;
					var exception = jobStatusInfo.jobException;
					var resultType = jobStatusInfo.resultType;
					var jobResults = jobStatusInfo.jobResults;
					
					if(status =='Completed') {
						//Ext.getCmp('dataAssociationPanel').body.unmask();
						Ext.TaskMgr.stop(checkTask);
						
						var fullViewerURL = pageInfo.basePath + viewerURL;
						
						//Set the results DIV to use the URL from the job.
						Ext.get('analysisOutput').load({url : fullViewerURL,callback: loadModuleOutput});
						
						//Set the flag that says we run an analysis so we can warn the user if they navigate away.
						GLOBAL.AnalysisRun = true;
						
					} else if(status == 'Cancelled' || status == 'Error') {
						Ext.TaskMgr.stop(checkTask);						
					}
					updateWorkflowStatus(jobStatusInfo);
				},
				failure : function(result, request)
				{
					Ext.TaskMgr.stop(checkTask);
					showWorkflowStatusErrorDialog('Failed', 'Could not complete the job, please contact an administrator');
				},
				timeout : '300000',
				params: {jobName: jobName}
			}
		);
  	}

	var checkTask =	{
			run: updateJobStatus,
	  	    interval: pollInterval	
	}	
	Ext.TaskMgr.start(checkTask);
}

function loadModuleOutput() {
	var selectedAnalysis = document.getElementById("analysis").value;
	selectedAnalysis = selectedAnalysis.charAt(0).toUpperCase()+selectedAnalysis.substring(1);
	
	var funcName = "load"+selectedAnalysis+"Output";
	
	if (typeof funcName == 'string' && eval('typeof ' + funcName) == 'function') 
	{
		eval(funcName+'()');
	}
}

function setupCategoricalItemsList(strDivSource,strDivTarget) {
	// copy from the category div at top of page first and add drag handlers
	var categoricalSourceDiv = Ext.get(strDivSource);
	var categoricalTargetDiv = Ext.get(strDivTarget);

	// clear it out first
	while (categoricalTargetDiv.dom.hasChildNodes())
		categoricalTargetDiv.dom
				.removeChild(categoricalTargetDiv.dom.firstChild);
	for ( var i = 0, n = categoricalSourceDiv.dom.childNodes.length; i < n; ++i) {
		// clone and append
		var newnode = categoricalSourceDiv.dom.childNodes[i].cloneNode(true);
		categoricalTargetDiv.dom.appendChild(newnode);
		// add drag handler
		Ext.dd.Registry.register(newnode, {
			el : newnode
		});
	}
	var dragZone = new Ext.dd.DragZone(categoricalTargetDiv.dom.parentNode, {
		ddGroup : 'makeBin',
		isTarget: true,
		ignoreSelf: false
	});

	var dropZone = new Ext.dd.DropTarget(categoricalTargetDiv, {
		ddGroup : 'makeBin',
		isTarget: true,
		ignoreSelf: false,
		onNodeEnter: function(target, dd, e, dragData) {
		    delete this.dropOK;
		    this.dropOK=true;
		    return true;
		    
		},
		onNodeOver: function(target, dd, e, dragData) {
			var ret= this.dropOK ? this.dropAllowed : this.dropNotAllowed;
		    console.log(ret);
		    return ret;
		}
	});
	dropZone.notifyDrop = dropOntoBin;
}

function clearDataAssociation() {
	//Remove the output screen.
	document.getElementById("analysisOutput").innerHTML = "";
	//Remove the variable selection screen.
	document.getElementById("variableSelection").innerHTML = "";
	
	//Whenever we switch views, make the binning toggle false. All the analysis pages default to this state.
	GLOBAL.Binning = false
	GLOBAL.ManualBinning = false
	GLOBAL.NumberOfBins = 4
	GLOBAL.AnalysisRun = false
	
	//Set the message below the cohort summary that lets the user know they need to select a cohort.
	renderCohortSummary();
	
}

function registerDragNDrop() {
    var independentDiv = Ext.get("divIndependentVariable");
    drop_target = new Ext.dd.DropTarget(independentDiv,{ddGroup : 'makeQuery'});
    drop_target.notifyDrop =  dropOntoCategorySelection;

}

function clearAnalysisData(divName) {
    //Clear the drag and drop div.
    var qc = Ext.get(divName);
    for(var i = qc.dom.childNodes.length-1; i>=0; i--) {
        var child= qc.dom.childNodes[i];
        qc.dom.removeChild(child);
    }
    // in highDimensionData.js
    clearHighDimDataSelections(divName);
    clearSummaryDisplay(divName);
}
