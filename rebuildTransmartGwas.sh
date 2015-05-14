#!/bin/sh

#  rebuildTransmartGwas.sh
#  
#
#  Created by Berube, Hugo on 9/10/14.
#

set JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home"

GRAILS="/Users/berubh/Work/grails/grails-2.3.7"
GRAILS_COMMAND="$GRAILS/bin/grails"
GRAILS_PLUGIN="$GRAILS/plugins"
TRANSMART_PROJECT_NAME="transmartApp"

#PROJECT_HOME="/Users/berubh/Work/GitHub/Foundation/1.2"
PROJECT_HOME="/Users/berubh/Work/Transmart/env/Foundation/1.2.2"
GRAILS_PROJECTS="/Users/berubh/.grails/2.3.7/projects"


#rm -r "$GRAILS_PROJECTS/Rmodules"

#PLUGIN_PROJECT="$PROJECT_HOME/Rmodules"
#PLUGIN="$PROJECT_HOME/Rmodules/grails-rdc-rmodules-1.2.2.zip"
#echo $PLUGIN_PROJECT
#cd $PLUGIN_PROJECT
#$GRAILS_COMMAND package-plugin
#cp -r $PLUGIN "$GRAILS_PLUGIN/rdc-rmodules-1.2.2.zip"


#rm -r "$GRAILS_PROJECTS/folder-management"

#PLUGIN_PROJECT="$PROJECT_HOME/folder-management-plugin"
#PLUGIN="$PROJECT_HOME/folder-management/grails-folder-management-1.2.2.zip"
#echo $PLUGIN_PROJECT
#cd $PLUGIN_PROJECT
#$GRAILS_COMMAND package-plugin
#cp -r $PLUGIN "$GRAILS_PLUGIN/folder-management-1.2.2.zip"

rm -r "$GRAILS_PROJECTS/search-domain"

PLUGIN_PROJECT="$PROJECT_HOME/transmart-extensions/search-domain"
PLUGIN="$PLUGIN_PROJECT/grails-search-domain-1.2.2.zip"
echo $PLUGIN_PROJECT
rm -r $IVY_PLUGIN_HOME/search-domain
cd $PLUGIN_PROJECT
$GRAILS_COMMAND package-plugin
cp -r $PLUGIN "$GRAILS_PLUGIN/search-domain-1.2.2.zip"

rm -r "$GRAILS_PROJECTS/transmart-core"

PLUGIN_PROJECT="$PROJECT_HOME/transmart-core-db"
PLUGIN="$PROJECT_HOME/transmart-core-db/grails-transmart-core-1.2.2.zip"
echo $PLUGIN_PROJECT
rm -r $IVY_PLUGIN_HOME/transmart-core
cd $PLUGIN_PROJECT
$GRAILS_COMMAND package-plugin
cp -r $PLUGIN "$GRAILS_PLUGIN/transmart-core-1.2.2.zip"


rm -r "$GRAILS_PROJECTS/transmart-gwas"

PLUGIN_PROJECT="$PROJECT_HOME/transmart-gwas-plugin"
PLUGIN="$PROJECT_HOME/transmart-gwas-plugin/grails-transmart-gwas-1.2.2.zip"
echo $PLUGIN_PROJECT
rm -r $IVY_PLUGIN_HOME/transmart-gwas
cd $PLUGIN_PROJECT
$GRAILS_COMMAND package-plugin
cp -r $PLUGIN "$GRAILS_PLUGIN/transmart-gwas-1.2.2.zip"

cd "$PROJECT_HOME/$TRANSMART_PROJECT_NAME"
rm -r "$GRAILS_PROJECTS/projects/transmart"
$GRAILS_COMMAND clean --non-interactive
$GRAILS_COMMAND war --non-interactive
