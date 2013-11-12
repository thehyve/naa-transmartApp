//import com.recomdata.transmart.domain.searchapp.Analysis;
//import com.recomdata.transmart.domain.searchapp.AnalysisItem;

import com.recomdata.transmart.domain.searchapp.Report;
import com.recomdata.transmart.domain.searchapp.ReportItem;

import grails.converters.JSON

/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  



/**
 * Class for controlling Analyses.
 * @author MMcDuffie
 *
 */
class SaveAnalysisController {
	
	def springSecurityService
	
	def saveAnalysis = 
	{
		
		if(params.name && params.paramList)
		{
			//Initialize new analysis object.
			def report = new Report()
			
			//Set the analysis properties from form.
			report.name = params.name
			report.description = params.description
			report.study = params.study

			
			if(params.publicflag == "true")
			{
				report.publicFlag = "Y"
			}
			else
			{
				report.publicFlag = "N"
			}
			
			report.creatingUser = springSecurityService.getPrincipal().username
			
			//Create the analysis and get its id.
			report.save(flush:true, failOnError:true)
			
			//Loop through the parameters and add them to the report.
//			def parameters = params.paramList.split("&");
//			parameters.each()
//			{
//					if(it.contains("analysis=")  ||it.contains("jobType=") ){
//						def values = it.split("=");
//						report.moduleName = values[1];
//					}
//
//			}
			

			//Loop through the parameters and add them to the report.
			params.paramList.split("&").each()
			{
				report.addToReportItems(new ReportItem(reportId:report.id,code:it.decodeURL()))
				if(it.contains("analysis=")  ||it.contains("jobType=") ){
					def values = it.split("=");
					report.moduleName = values[1];
				}

			}	

			//Create the analysis and get its id.
			report.save(flush:true, failOnError:true)
			
		}
		else
		{
			throw new Exception("Invalid parameters supplied!")
		}
		
		render "sucess";
	}
	
	
	/**
	 * This will get all the parameters from the saved analysis.
	 */
	def retrieveAnalysisParameters =
	{
		//Get the analysis object.
		def retrievedAnalysis = Report.get(params.analysisId)
		
		//Pull the AnalysisItem objects for this analysis.
		def retrievedAnalysisItems = retrievedAnalysis.reportItems
		
		//Push the actual parameter for the AnalysisItems into an array to be passed back as JSON.
		def parameterArray = []
		
		retrievedAnalysisItems.each()
		{
			parameterArray.push(it.parameter)
		}
		
		render parameterArray as JSON
	}
	
}
