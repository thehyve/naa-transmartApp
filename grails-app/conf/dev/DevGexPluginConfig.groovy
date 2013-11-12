package dev

Gex {

    // parameters for invoking Genedata Analyst
    analystServerHostname = ""
    analystServerPort = -1
    analystLoaderID = "transmart"

	// Temp directory on server for processing uploaded data
    analsytUploadTempDir = "/22/"

    // Analyst Loader URL - Do not manually configure!
    analystLoaderBaseURL = "http://${analystServerHostname}:${analystServerPort}/analyst/loader"

}
