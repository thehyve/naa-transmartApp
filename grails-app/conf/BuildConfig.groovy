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
  

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

//grails.plugin.location.rmodules = "C:\\SVN\\repo1\\pharma\\transmart\\trunk\\plugins\\Rmodules"

//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
		mavenLocal()
		grailsPlugins()
        grailsHome()
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        
        //mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
    }
    plugins {
        compile(':transmart-java:1.0-SNAPSHOT')
        compile(':biomart-domain:1.0-SNAPSHOT')
        compile(':search-domain:1.0-SNAPSHOT')
        compile(':folder-management:1.0-SNAPSHOT')
        compile(':transmart-gwas:1.0-SNAPSHOT')
        compile(':spring-security-core:1.1.2')
        compile(':rdc-rmodules:0.1')
        compile(':quartz:0.4.2')
    }

}

//grails.plugin.location.transmartJava='C:\\Users\\davinewton\\Documents\\workspace-sts-2.6.1.RELEASE-pfizer\\transmartPfizer\\transmart-java'
//grails.plugin.location.biomartDomain='C:\\Users\\davinewton\\Documents\\workspace-sts-2.6.1.RELEASE-pfizer\\transmartPfizer\\transmart-domain'
//grails.plugin.location.searchDomain='C:\\Users\\davinewton\\Documents\\workspace-sts-2.6.1.RELEASE-pfizer\\transmartPfizer\\searchapp-domain'
//grails.plugin.location.folderManagement='C:\\Users\\davinewton\\Documents\\workspace-sts-2.6.1.RELEASE-pfizer\\folder-management'
//grails.plugin.location.transmartGwas='W:\\transmart\\pfizer\\transmartApp\\transmart-gwas'