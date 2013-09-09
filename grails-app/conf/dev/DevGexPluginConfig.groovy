package dev

Gex {

    // working area for exported files
	tempFolderDirectory = "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\Temp\\jobs\\"

    // parameters for invoking Genedata Analyst
    analystServerHostname = ""
    analystServerPort = -1
    analystLoaderID = "transmart"

    // Analyst Loader URL - Do not manually configure!
    analystLoaderBaseURL = "http://${analystServerHostname}:${analystServerPort}/analyst/loader"

}
