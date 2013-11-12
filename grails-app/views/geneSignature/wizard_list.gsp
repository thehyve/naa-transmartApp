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

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="genesigmain" />
	<g:if test="${wizard.wizardType==1}">
		<title>Gene List Edit</title>
	</g:if>
	<g:else>
		<title>Gene List Create</title>
	</g:else>

    <script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery-1.8.3.min.js')}"></script>
    <script>$j = jQuery.noConflict();</script>

	<script type="text/javascript">

        jQuery(document).ready(function() {
            var pasteContent;

            jQuery('.biomarkerEntry').on('change', function(event) {
                var name = jQuery(this).attr('name');
                var index = parseInt(name.substring(10));
                checkGene(index);
            });

            jQuery('.biomarkerEntry').on('paste', function(event) {
                pasteContent = null;
                var startingInput = jQuery(this);
                startingInput.val('');
                if (window.clipboardData) {
                    pasteContent = window.clipboardData.getData('Text');
                }
                //Paste is ABOUT TO happen, so set timeout
                setTimeout(function() {
                    if (pasteContent == null) {
                        pasteContent = startingInput.val();
                    }
                    var pasteList = pasteContent.split(",");
                    if (pasteList.length < 2) {
                        pasteList = pasteContent.split("\n");
                    }
                    var name = startingInput.attr('name');
                    var startingIndex = parseInt(name.substring(10));
                    for (var i = 0; i < pasteList.length; i++) {
                        var currentIndex = startingIndex + i;
                        $j('#biomarker_' + currentIndex).val(pasteList[i].trim());
                        checkGene(currentIndex);
                    }
                    jQuery('#pasteSource').val('');
                }, 1);
            });
        });

		function validate() {

            var errorMsg = "";
			// list name required
			if(document.geneSignatureFrm.name.value=="") {
				errorMsg = "You must specify a list name";
            }

            if(document.geneSignatureFrm.uploadFile.value=="" && document.geneSignatureFrm.biomarker_1.value=="")
                 errorMsg = errorMsg + "\n- Please select a file, or manually enter a gene list";

            if(document.geneSignatureFrm.uploadFile.value!="" && document.geneSignatureFrm.biomarker_1.value!="")
                errorMsg = errorMsg + "\n- You have both specified a file and manually entered a list";
            // if no errors, continue submission
            if(errorMsg=="") return true;

            alert("Please correct the following errors:\n" + errorMsg);
            return false;
		}

        // hide inidcated row
        function removeNewItem(rowNum) {
            var rowId = "new_item_"+rowNum;
            var geneId = "biomarker_"+rowNum;
            var probesetId = "probeset_"+rowNum;
            var metricId = "foldChgMetric_"+rowNum;

            // remove and reset
            jQuery('#geneItem').val('');
            jQuery('#probesetItem').val('');
            jQuery('#metricItem').val('');
            jQuery('#rowItem').hide();
            checkGene(rowNum);
        }

        function checkGene(index) {
            var geneName = jQuery('#biomarker_' + index).val();
            jQuery('#geneCheckIcon' + index).removeClass('loading').removeClass('success').removeClass('failure');
            if (geneName == null || geneName == "") {
                return;
            }
            else {
                jQuery('#geneCheckIcon' + index).addClass('loading')
                jQuery.ajax({
                    "url": '${createLink(controller: 'geneSignature', action: 'checkGene')}',
                    data: {geneName : geneName},
                    "success": function (jqXHR) {
                        if (jqXHR.geneFound == true) {
                            jQuery('#geneCheckIcon' + index).removeClass('loading').addClass('success');
                        }
                        else {
                            jQuery('#geneCheckIcon' + index).removeClass('loading').addClass('failure');
                        }
                    },
                    "error": function (jqXHR, error, e) {
                        jQuery('#geneCheckIcon' + index).removeClass('loading')
                        alert(error);
                    },
                    "dataType": "json"
                });
            }

        }
	</script>
</head>

<body>

<div class="body">
	<!-- initialize -->
    <g:set var="gs" value="${wizard.geneSigInst.properties}" />

<!--  show message -->
    <g:if test="${flash.message}">
        <div class="warning">${flash.message}</div>
        <g:hasErrors bean="${wizard.geneSigInst}"><div class="errors"><g:renderErrors bean="${wizard.geneSigInst}" as="list" /></div></g:hasErrors>
        <br>
    </g:if>

    <h1>Gene List Create</h1>
    <g:form name="geneSignatureFrm" enctype="multipart/form-data" method="post">

     <table class="detail" style="width: 100%">
        <tbody id="ListNameDetail">
        <tr class="prop">
            <td class="name">Signature/List Name<g:requiredIndicator/></td>
            <td class="value"><g:textField name="name" value="${gs.name}" size="100%" maxlength="100" /></td>
        </tr>
        </tbody>
     </table>
     <br>
     <table class="detail">
         <tbody id="FileInfoDetail">
        <tr class="prop">
            <td class="name">Upload File<br>(tab delimited text files only)</td>
            <td class="value"><input type="file" name="uploadFile" <g:if test="${wizard.wizardType==0}">value="${gs.uploadFile}"</g:if><g:else>value=""</g:else> size="100" /></td>
        </tr>

        <tr>
            <td class="name">Enter List Manually</td>
            <td>
        <table class="detail">
        <tbody id="_new_items_detail" style="display: block;">
        <tr id="new_header">
            <%--<th style="text-align: center;">#</th>--%>
            <th style="text-align: center;">Gene Symbol</th>
            <th style="text-align: center;">&nbsp;</th>
            <th style="text-align: center;">Remove</th>
        </tr>

        <g:set var="n" value="${0}"/>
        <g:while test="${n < 10}">

            <tr id="new_item_${n}">
                <%--<td style="color: gray;">${n}</td>--%>

            <!-- check if coming from an error -->
                <td><g:textArea name="biomarker_${n}" class="biomarkerEntry"/></td>
                <td><div class="geneCheckIcon" id="geneCheckIcon${n}">&nbsp;</div></td>
                <td style="text-align: center;"><img alt="remove item" onclick="javascript:removeNewItem(${n});" src="${resource(dir:'images',file:'remove.png')}" /></td>

            </tr>
            <%n++%>
        </g:while>
        </tbody>
    </table>
            </td>
        </tr>
</tbody>
      </table>

        <div class="buttons">
        <g:actionSubmit class="save" action="${(wizard.wizardType==1) ? 'update' : 'saveList'}" value="Save" onclick="return validate();" />
		<g:actionSubmit class="cancel" action="refreshSummary" onclick="return confirm('Are you sure you want to exit?')" value="Cancel" />
	</div>			

	<br>
	</g:form>
</div>
</body>
</html>
