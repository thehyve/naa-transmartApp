/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.    You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.    You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/

/**
 * This file contains functions for handling the UI elements related to the concept code reporting system.
 */

/**
 * When the user clicks to save the report, prompt for a name/description/public flag and submit to the controller.
 */
function saveParamAnalysis() 
{
	jQuery( "#saveAnalysisDialog" ).dialog({title: 'Save Analysis', modal:true});
	jQuery( "#saveAnalysisDialog" ).dialog("open");
}

/**
 * When the user clicks to save an analysis, send off the ajax call to save the record.
 * @param newReport
 * @param reportName
 * @param reportDescription
 * @param reportPublic
 * @param parConceptList
 */
function saveAnalysis(newAnalysis,analysisName,analysisDescription,analysisPublic,formParams,studyName)
{
	//Validate the user input.
	if(analysisName == "")
	{
		Ext.Msg.alert('Missing Input','Please enter an analysis name.')
		return;
	}
	
	jQuery.ajax({
		  url: pageInfo.basePath + '/saveAnalysis/saveAnalysis',
		  method: 'POST',
		  success:function(data){
			  jQuery( "#saveAnalysisDialog" ).dialog("close");
			  resultsTabPanel.setActiveTab('workspacePanel');
			  },
		  failure:function(data){alert("Analysis failed to save.");},
		  data: {	name:analysisName,
			  		description:analysisDescription,
			  		publicflag:analysisPublic,
			  		paramList:Ext.urlEncode(formParams),
			  		study:studyName
			  		}
		});
}



function pullAnalysisParameters(analysisParams)
{
	resultsTabPanel.body.mask("Generating Analysis From Saved parameters", 'x-mask-loading');
	
	//Get the JSON list of codes for this report.
	jQuery.ajax({
		  url: pageInfo.basePath + 'saveAnalysist/retrieveAnalysisParameters',
		  success:function(returnedData){drawReports(returnedData, reportParams[1]);},
		  failure:function(returnedData){resultsTabPanel.body.unmask();alert("There was an error retrieving your analysis.")},
		  data: {reportid:reportParams[0]}
		});
}


function buildAnalysisFromCode(nodeCode, lastCode, reportsStudy)
{
    Ext.Ajax.request(
            {
                url : pageInfo.basePath+"/chart/analysis",
                method : 'POST',
                timeout: '600000',
                params :  Ext.urlEncode(
                        {
                            charttype : "analysis",
                            concept_key : nodeCode,
                            result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
                            result_instance_id2 : GLOBAL.CurrentSubsetIDs[2]
                        }
                ), // or a URL encoded string
                success : function(result, request)
                {
                	GLOBAL.currentReportCodes.push(nodeCode);
                	GLOBAL.currentReportStudy.push(reportsStudy);
                	buildAnalysisComplete(result);
                	if(lastCode) resultsTabPanel.body.unmask();
                },
	            failure : function(result, request)
	            {
	                buildAnalysisComplete(result);
	                resultsTabPanel.body.unmask()
	            }
            }
    );

}

function populateAnalysis(){
    var returnedData = GLOBAL.returnedAnalysisData[1];
    var binningEnabled = false;
    if(returnedData){
        for (var i = 0; i < returnedData.length; i++) {
            var obj = returnedData[i]
            var res = obj.split("=");
            if(res.length == 2){
                var concept_key = res[0];
                var concept_value = res[1];

                switch (concept_key) {
                    case "dependentVariable":
                        populatePanel("divDependentVariable",concept_value);
                        break;
                    case "independentVariable":
                        populatePanel("divIndependentVariable",concept_value);
                        break;
                    case "variablesConceptPaths":
                        populatePanel("divVariables",concept_value);
                        break;
                    case "correlationBy":
                        populateElement("correlationBy",concept_value);
                        break;
                    case "correlationType":
                        populateElement("correlationType",concept_value);
                        break;
                    case "binning":
                        if(concept_value === "TRUE"){
                            binningEnabled = true;
                        }
                        break;
                    case "groupByVariable":
                        populatePanel("divGroupByVariable");
                        break;
                    default:
                        //alert(concept_key +"="+concept_value);
                        break;
                }//switch
            }//if
        }//for
        if(binningEnabled){
            populateBinning(returnedData);
        }
    }//if
}
function populatePanel(elementId, concept_value,nodeType){
	  panel = document.getElementById(elementId);
	  if(panel  && concept_value){
		  if(concept_value.indexOf('|') !== -1)
		  {
    		    //multiple concepts, so parse them into array
    			  var splits=concept_value.split("|");
    			  for(j=0;j<splits.length;j++){
    				 concept_value =splits[j];
    				 var concept=new Concept('', concept_value, '', concept_value, '',concept_value, '', '', '', new Value(), nodeType);
    				 createPanelItemNew(panel, concept);
    				 }
    		  } else{
    			  	 var concept=new Concept('', concept_value, '', concept_value, '', concept_value, '', '', '', new Value(), nodeType);	
    				 createPanelItemNew(panel, concept);
    	}
	  }
}
function populateElement(elementId,concept_value){
	  variable = document.getElementById(elementId);
	  if(variable && concept_value){
		  variable.value= concept_value;	
		  variable.src = variable.src;
	  }
}
function populateBinning(returnedData){
	if(returnedData){
	    var manualBinning = '' ;
	    var binVariable = '';
	    var variableType = '';
	    var numberOfBins = '';
	    var binRanges = '';
		for (var i = 0; i < returnedData.length; i++) {
			var obj = returnedData[i]
			var res = obj.split("=");
			if(res.length == 2){
			var concept_key = res[0];
			var concept_value = res[1];

			   switch (concept_key)
			   {
			      case "manualBinning":
			    		  manualBinning = concept_value;
			    	  break;
			      case "binRanges":
			    		  binRanges = concept_value;
			    	  break;
			      case "binVariable":
			    		  binVariable = concept_value;
			    	  break;
			      case "variableType":
			    		  variableType = concept_value;
			    	  break;
			      case "numberOfBins":
			    	  numberOfBins = concept_value;
			    	  break;	  
			      default: 
			          //alert(concept_key +"="+concept_value);
			          break;
			   }//switch
			}//if
		}//for
		//First enable binning
		var binningToggle = document.getElementById('BinningToggle');
		if(binningToggle){
			binningToggle.onclick();
		}
		//Set Values
		 if(manualBinning === 'TRUE'){
			 document.getElementById('chkManualBin').checked =  true;
	   	  }else{
	   		 document.getElementById('chkManualBin').checked = false;
	   	  }
		 if(binVariable.length>0){
			 populateElement("selBinVariableSelection",binVariable);
		 }
		 if(variableType.length>0){
			 populateElement("variableType",variableType);
		 }
		 manageBins(numberOfBins);
		 if(numberOfBins.length>0){
			 populateElement("txtNumberOfBins",numberOfBins);
		 }
		 populateManualBins(binRanges,variableType);
	}//if
}
function populateManualBins(binRanges,variableType){
	if(binRanges  && variableType){
		if(binRanges.indexOf('|') !== -1)
		  {
  		    //multiple concepts, so parse them into array
  			  var splits=binRanges.split("|");
  			  for(i=0;i<splits.length;i++){
  				var binNo = i+1;
  				var binsplits =splits[i].split("<>");
	  				for(j=1;j<binsplits.length;j++){
	  					var bin_value = binsplits[j];
						populatePanel("div" + variableType + "Bin" + binNo,bin_value);
	  						}
	  						
	  					}
  		  } else{
  			  	var binsplits =binRanges.split("<>");
					for(j=0;j<binsplits.length;j++){
						var bins = binsplits[j];
						alert(bins +"="+bins);
					}

  		  }
		if(variableType ==='Categorical'){
			//clear the categorical items 
			document.getElementById("divCategoricalItems").innerHTML = "";
		}
	}
}