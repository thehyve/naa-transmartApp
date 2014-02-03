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

	// Administrator email
	administratorEmail = "ami.khandeshi@pfizer.com"

    apiKey="eece99dcbafde61880a429acd07038cd1c91da76"
}

grails {
	// SMTP Server
	mail {
		host = "ndhsmtp.amer.pfizer.com"
	}
}