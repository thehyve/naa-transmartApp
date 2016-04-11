<!DOCTYPE html>
<html>
<head>
    <title><g:if env="development">Grails Runtime Exception</g:if><g:else>Error</g:else></title>
    <meta name="layout" content="main">
    <g:if env="development"><link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css"></g:if>
</head>
<body>

<table style="width:auto; border:0px; text-align:center; margin:auto;" align="center">
        <tr>
            <td style="text-align:center;vertical-align:middle;margin-left:-40px;">
  			<g:link controller="RWG" action="index"><img src="${resource(dir:'images',file:grailsApplication.config.com.recomdata.largeLogo)}" alt="Transmart" /></g:link>
            </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <tr><td>
	        <b><u><p style="color:red;font-size: 125%;">User '${userName}' not found!</p></u></b> <br><p><br><p>
	        Please request an account in order to use TranSMART (click <a href="${grailsApplication.config.com.recomdata.requestAccountUrl}" target='_BLANK'><u>here</u></a>).<br><p> <br><p>You will be able to request an account in the <b>'User Resources - How to get access:'</b> section on the right panel.

	    </td></tr>
    

</body>
</html>
