package dev
/**
* This is the config file used by the RModules plugin
* @author SMunikuntla
*
*/
RModules 
{
	// plugin script directory
       pluginScriptDirectory = "C:\\workspace\\Rmodules\\web-app\\Rscripts\\"
	   defaultStatusList = ["Started",
			"Validating Cohort Information",
			"Triggering Data-Export Job",
			"Gathering Data",
			"Running Conversions",
			"Running Analysis",
			"Rendering Output"]
	   
	   /**
	    * Define a different statusList for each module if it varies from the defaultStatusList
	    * Make sure the moduleName is the jobType
	    */
	   survivalAnalysis.StatusList = ["Started",
		   "Validating Cohort Information",
		   "Triggering Data-Export Job",
		   "Gathering Data",
		   "Running Conversions",
		   "Running Analysis",
		   "Rendering Output"]
	   
	   //Configuration for plugins.
        // insert username
	   tempFolderDirectory = "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\Temp\\jobs\\"
	   //I use this to do local development. It causes the analysis controller
	   transferImageFile = true
	   //This is the system path where we move the image file to so we can serve it.
	   temporaryImageFolder = "C:\\workspace\\transmartApp\\web-app\\images\\tempImages"
	   //This is the path that we use to render the image.
	   imageURL = "/transmart/images/tempImages/"
	   
	// whether or not to enable ftp
	   ftpFlag = "FALSE"
	   
	   //Make sure there is groovy class with the name+'Definition' 
	   //(For ex:BoxPlotDefinition) in the com.recomdata.transmart.rmodules
	   //Add new modules here, as module-registration will happen only if it is defined here

	   modules = ["SurvivalAnalysis", "BoxPlot", "ScatterPlot", "TableWithFisher","Heatmap"]
}
