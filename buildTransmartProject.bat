set JAVA_HOME="C:\Dev Tools\Java"

set GRAILS_COMMAND="C:\Dev Tools\grails-1.3.7\bin\grails.bat"
set GRAILS_PLUGIN="C:\Dev Tools\grails-1.3.7\plugins"
set PROJECT_HOME="C:\code\Projects\GWAS\transmart"

set IVY_PLUGIN_HOME="C:\Users\engina01\.ivy2\cache\org.grails.plugins"

set PLUGIN_PROJECT="c:\code\Projects\GWAS\transmart-java"
set PLUGIN="c:\code\Projects\GWAS\transmart-java\grails-transmart-java-1.0-SNAPSHOT.zip"

echo %PLUGIN_PROJECT%
cd %PLUGIN_PROJECT%
call %GRAILS_COMMAND% package-plugin
copy %PLUGIN_PROJECT% %GRAILS_PLUGIN%

set PLUGIN_PROJECT="c:\code\Projects\GWAS\biomart-domain"
set PLUGIN="c:\code\Projects\GWAS\biomart-domain\grails-biomart-domain-1.0-SNAPSHOT.zip"

echo %PLUGIN_PROJECT%
del /S /Q %IVY_PLUGIN_HOME%\biomart-domain
cd %PLUGIN_PROJECT%

call %GRAILS_COMMAND% package-plugin
copy %PLUGIN_PROJECT% %GRAILS_PLUGIN%

set PLUGIN_PROJECT="c:\code\Projects\GWAS\search-domain"
set PLUGIN="c:\code\Projects\GWAS\search-domain\grails-search-domain-1.0-SNAPSHOT.zip"

echo %PLUGIN_PROJECT%
del /S /Q %IVY_PLUGIN_HOME%\search-domain
cd %PLUGIN_PROJECT%
call %GRAILS_COMMAND% package-plugin
copy %PLUGIN_PROJECT% %GRAILS_PLUGIN%

set PLUGIN_PROJECT="c:\code\Projects\GWAS\folder-management"
set PLUGIN="c:\code\Projects\GWAS\folder-management\grails-folder-management-1.0-SNAPSHOT.zip"

echo %PLUGIN_PROJECT%
del /S /Q %IVY_PLUGIN_HOME%\folder-management
cd %PLUGIN_PROJECT%
call %GRAILS_COMMAND% package-plugin
copy %PLUGIN_PROJECT% %GRAILS_PLUGIN%

set PLUGIN_PROJECT="c:\code\Projects\GWAS\transmart-gwas"
set PLUGIN="c:\code\Projects\GWAS\transmart-gwas\grails-transmart-gwas-1.0-SNAPSHOT.zip"

echo %PLUGIN_PROJECT%
del /S /Q %IVY_PLUGIN_HOME%\transmart-gwas
cd %PLUGIN_PROJECT%
call %GRAILS_COMMAND% package-plugin
copy %PLUGIN_PROJECT% %GRAILS_PLUGIN%

set PLUGIN_PROJECT="C:\code\Projects\GWAS\Rmodules"
set PLUGIN="c:\code\Projects\GWAS\Rmodules\grails-rdc-rmodules-0.1.zip"

echo %PLUGIN_PROJECT%
del /S /Q %IVY_PLUGIN_HOME%\Rmodules
cd %PLUGIN_PROJECT%
call %GRAILS_COMMAND% package-plugin
copy %PLUGIN_PROJECT% %GRAILS_PLUGIN%

set PLUGIN_PROJECT="C:\code\Projects\GWAS\workspace"
set PLUGIN="c:\code\Projects\GWAS\workspace\grails-transmart-workspace-1.0-SNAPSHOT.zip"

echo %PLUGIN_PROJECT%
del /S /Q %IVY_PLUGIN_HOME%\workspace
cd %PLUGIN_PROJECT%
call %GRAILS_COMMAND% package-plugin
copy %PLUGIN_PROJECT% %GRAILS_PLUGIN%


set PLUGIN_PROJECT="C:\code\Projects\GWAS\tmart-gex"
set PLUGIN="c:\code\Projects\GWAS\tmart-gex\grails-gex-0.1.zip"

echo %PLUGIN_PROJECT%
del /S /Q %IVY_PLUGIN_HOME%\tmart-gex
cd %PLUGIN_PROJECT%
call %GRAILS_COMMAND% package-plugin
copy %PLUGIN_PROJECT% %GRAILS_PLUGIN%


cd C:\code\Projects\GWAS\transmart
call %GRAILS_COMMAND% clean --non-interactive
call %GRAILS_COMMAND% war --non-interactive














