<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
 
-->

<g:set var="overlayDiv" value="metaData_div" />
<script type="text/javascript">
   $j(document).ready(function() 
    {     
        var analysisCount = 1;
		var assayCount = 3; 
		
		function resizeAccordion() {
	
			var windowHeight = jQuery(window).height();
			jQuery('#sidebar').height(jQuery(window).height()-30);
			jQuery('#main').height(jQuery(window).height()-30);
			var ypos = jQuery('#program-explorer').offset()['top'];
			
			var targetHeight = windowHeight - ypos - 60;
			jQuery('#results-div').height(targetHeight);
			jQuery('#welcome').height(windowHeight - 90);
			
			if (jQuery('#sidebar:visible').size() > 0) {
				jQuery('#main').width(jQuery('body').width() - jQuery('#sidebar').width() - 12);
			}
			else {
				jQuery('#main').width("100%");
			}
	
	
			jQuery('#box-search').width(jQuery('#program-explorer').width());
		}

		function updateExportCount() {
			var checkboxes = jQuery('#exporttable input:checked');
			
			if (checkboxes.size() == 0) {
				jQuery('#exportbutton').text('No files to export').addClass('disabled');
			}
			else {
				jQuery('#exportbutton').removeClass('disabled').text('Export selected files (' + checkboxes.size() + ')');
			}
		}

		function dataTableWrapper (containerId, tableId, title, sort, pageSize)
		{
		
		    var data;
		    var gridPanelHeaderTips;
		    
		    function setupWrapper()
		    {
		        var gridContainer =  $j('#' + containerId);
		        gridContainer.html('<table id=\'' + tableId + '\'></table></div>');
		         }
		
		    function overrideSort() {
		
		        $j.fn.dataTableExt.oSort['numeric-pre']  = function(a) {
		            
		            var floatA = parseFloat(a);
		            var returnValue;
		            
		            if (isNaN(floatA))
		                returnValue = Number.MAX_VALUE * -1;    //Emptys will go to top for -1, bottom for +1   
		                else
		                    returnValue = floatA;
		            
		                return returnValue;
		            };
		
		    };
		
		    this.loadData = function(dataIn) {
		
		
		        setupWrapper();
		        
		        data = dataIn;
		        setupGridData(data, sort, pageSize);
		        
		        gridPanelHeaderTips = data.headerToolTips.slice(0);
		
		        //Add the callback for when the grid is redrawn
		        data.fnDrawCallback = function( oSettings ) {
		            
		            //Hide the pagination if both directions are disabled.
		            if (jQuery('#' + tableId + '_paginate .paginate_disabled_previous').size() > 0 && jQuery('#' + tableId + '_paginate .paginate_disabled_next').size() > 0) {
		            	jQuery('#' + tableId + '_paginate').hide();
		            }
		        };
		
		        data.fnInitComplete = function() {this.fnAdjustColumnSizing();};
		
		        //$j('#' + tableId).dataTable(data);
		
		        //$j(window).bind('resize', function () {
		        //    if($j('#' + tableId).dataTable().oSettings){
		        //        $j('#' + tableId).dataTable().fnAdjustColumnSizing();
		        //    }
		        //  } );
		        
		         $j("#" + containerId + " div.gridTitle").html(data.iTitle);                  
		
		    };
		    
		
		    function setupGridData(data, sort, pageSize)
		    {
		        data.bAutoWidth = true;
		        data.bScrollAutoCss = true;
		//        data.sScrollY = 400;
		        data.sScrollX = "100%";
		        data.bDestroy = true;
		        data.bProcessing = true;
		        data.bLengthChange = false;
		        data.bScrollCollapse = false;
		        data.iDisplayLength = 10;
		        if (pageSize != null && pageSize > 0) {
		        	data.iDisplayLength = pageSize;
		        }
		        if (sort != null) {
		 			data.aaSorting = sort;
		 		}
		        data.sDom = '<"top"<"gridTitle">Rrt><"bottom"p>' //WHO DESIGNED THIS
		    }
		
		}
		
        var dt1 = new dataTableWrapper('gridViewWrapper1', 'gridViewTable1', 'Analysis (' + analysisCount + ')', [[2, "asc"]], 25);
        dt1.loadData(${jSONForGrid});

        var dt2 = new dataTableWrapper('gridViewWrapper2', 'gridViewTable2', 'Assays (' + assayCount + ')', [[2, "asc"]], 25);
        dt2.loadData(${jSONForGrid1});
        
     });
</script>

<g:if test="${!layout}">
	<i>No columns have been set up for the study view</i>
</g:if>

<div style="margin:10px;padding:10px;">
<h3 class="rdc-h3">${experimentInstance?.title}</h3>
<div style="line-height:14px;font-family:arial,​tahoma,​helvetica,​sans-serif; font-size: 12px;">
 <g:if test="${experimentInstance?.description != null && experimentInstance?.description.length() > 325000}">
                       ${(experimentInstance?.description).substring(0,324000)}&nbsp;&nbsp;
                       <a href=# >...See more</a>
                       </g:if>
                       <g:elseif test="${experimentInstance?.description != null}">
                       ${experimentInstance?.description}
                       </g:elseif></div>
<div style="height:20px;"></div>

<div style="width:800px; border:2px solid #DDD; border-radius:8px;-moz-border-radius: 8px;">
<table class="details-table">
            <thead style="border-radius:8px;-moz-border-radius: 8px;">
                <tr style="border-radius:8px;-moz-border-radius: 8px;">   
                    <th style="border-radius:8px;-moz-border-radius: 8px;">&nbsp;</th>
                    <th align="right"><g:remoteLink controller="fmFolder" action="editMetaData" update="${overlayDiv}" 
                            params="[eleId:overlayDiv, experimentId:experimentInstance.id]" 
                            before="initLoadingDialog('${overlayDiv}')" onComplete="centerDialog('${overlayDiv}')">
                      <img align="right" src="${resource(dir:'images', file:'pencil.png')}"/></g:remoteLink>
                    </th>
                </tr>
            </thead>
            
	<tbody>
		<g:each in="${layout}" status="i" var="layoutRow">
           <g:if test="${layoutRow.display}">
			<tr class='details-row'> <!-- class="${(i % 2) == 0 ? 'odd' : 'even'}"> -->
				<td valign="top" align="right" class="columnname" width="20%">${layoutRow.displayName}</td>
			
				<td valign="top" align="left" class="columnvalue" width="80%">
					<g:if test="${layoutRow.dataType == 'date'}">
						<g:fieldDate bean="${experimentInstance}" field="${layoutRow.column}" format="yyyy-MM-dd"/>
					</g:if>
					
					<%-- Special cases --%>
					<g:elseif test="${layoutRow.dataType == 'special'}">
						<g:if test="${layoutRow.column == 'accession'}">
							${fieldValue(bean:experimentInstance,field:'accession')}
							<g:if test="${experimentInstance?.files.size() > 0}">
								<g:set var="fcount" value="${0}" />
								<g:each in="${experimentInstance.files}" var="file">
									<g:if test="${file.content.type=='Experiment Web Link'}">
										<g:set var="fcount" value="${fcount++}" />
										<g:if test="${fcount > 1}">, </g:if>
										<g:createFileLink content="${file.content}" displayLabel="${file.content.repository.repositoryType}"/>
									</g:if>
									<g:elseif test="${file.content.type=='Dataset Explorer Node Link'&&search==1}">
									<g:link controller="datasetExplorer" action="index" params="[path:file.content.location]">Dataset Explorer<img src="${resource(dir:'images', file:'internal-link.gif')}"/></g:link>
									</g:elseif>
								</g:each>
							</g:if>
							<g:if test="${searchId!=null}">
								| <g:link controller="search" action="newSearch" id="${searchId}">Search analyzed Data <img src="${resource(dir:'images', file:'internal-link.gif')}"/></g:link>
							</g:if>
						</g:if>
						<g:elseif test="${layoutRow.column == 'platforms'}">
							<g:each var="pf" in="${expPlatforms}">
								${pf?.name.encodeAsHTML()}<br>
							</g:each>
						</g:elseif>
						<g:elseif test="${layoutRow.column == 'organism'}">
							<g:each var="og" in="${expOrganisms}">
								${og?.encodeAsHTML()}<br>
							</g:each>
						</g:elseif>
					</g:elseif>
					
					<g:else> <%-- In all other cases, display as string --%>
					   <g:if test="${fieldValue(bean:experimentInstance,field:layoutRow.column).length() > 325}">
					   ${(fieldValue(bean:experimentInstance,field:layoutRow.column)).substring(0,324)}&nbsp;&nbsp;
					   <a href=# >...See more</a>
					   </g:if>
					   <g:else>
    					${fieldValue(bean:experimentInstance,field:layoutRow.column)}
    					</g:else>
					</g:else>
				</td>
			</tr>
			</g:if>
		</g:each>
	</tbody>
	            <thead>
                <tr>                
                    <th>&nbsp;</th>
                    <th align="right"><div align="right">Subject1&nbsp;Level&nbsp;Data&nbsp;is&nbsp;available&nbsp;
      <a href="#subjectFake" onclick="showTab('analyze')"><img src="${resource(dir:'images', file:'application_go.png')}"/></a>
      </div>
      </th>
                </tr>
            </thead>
	
</table>
</div>
<span></span>
                    
       <div style="height:20px;"></div>
       <div style="width:1100px">
            <div id='gridViewWrapper1'>
            </div>        
        </div>

       <div style="height:30px;"></div>
       <div style="width:1100px">
            <div id='gridViewWrapper2'>
            </div>        
        </div>

<!--  overlay div  -->

<g:overlayDiv divId="${overlayDiv}" />
</div>
 
 <!-- background-color:#9CA4E4;  -->   
    
 