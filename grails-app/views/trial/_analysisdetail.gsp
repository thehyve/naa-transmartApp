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

<table class="detail" style="width: 515px;">
	<tbody>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Title:</td>
			<!-- <td valign="top" class="value">${fieldValue(bean:analysis, field:'shortDescription')}</td>-->
			<td valign="top" class="value">${raw(analysis?.shortDescription)}</td>
			
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Data Loaded:</td>
			<td valign="top" class="value">${fieldValue(bean:analysis, field:'createDate')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Updated analysis from:</td>
			<td valign="top" class="value">${analysis?.updateOf?.name}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Analysis Description:</td>
			<td valign="top" class="value">${raw(analysis?.longDescription)}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Diseases:</td>
			<td valign="top" class="value"><g:each in="${analysis.diseases}" var="disease"><li><g:meshLineage disease="${disease}"/></li></g:each></td>
		</tr>
		<g:if test='${"comparison".equals(analysis.analysisMethodCode)}'>
			<tr class="prop">
				<td valign="top" class="name" style="text-align: right">p-Value	Cut Off:</td>
				<td valign="top" class="value">${fieldValue(bean:analysis, field:'pvalueCutoff')}</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name" style="text-align: right">Fold Change Cut Off:</td>
				<td valign="top" class="value">${fieldValue(bean:analysis, field:'foldChangeCutoff')}</td>
			</tr>
		</g:if>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">QA Criteria:</td>
			<td valign="top" class="value">${fieldValue(bean:analysis, field:'qaCriteria')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Analysis Platform:</td>
			<td valign="top" class="value">${fieldValue(bean:analysis, field:'analysisPlatform.platformName')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Method:</td>
			<td valign="top" class="value">${fieldValue(bean:analysis, field:'analysisMethodCode')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Data type:</td>
			<td valign="top" class="value">${fieldValue(bean:analysis, field:'assayDataType')}</td>
		</tr>
		<!--  GWAS new analysis fields for Q4 2015 Pfizer release -->
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Update Of:</td>
			<td valign="top" class="value">${fieldValue(bean:analysis, field:'updateOf')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Effect Type:</td>
			<td valign="top" class="value">${fieldValue(bean:analysisExt, field:'effectType')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Effect Units:</td>
			<td valign="top" class="value">${fieldValue(bean:analysisExt, field:'effectUnits')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Effect Error 1 Type:</td>
			<td valign="top" class="value">${fieldValue(bean:analysisExt, field:'effectError1Type')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Effect Error 2 Type:</td>
			<td valign="top" class="value">${fieldValue(bean:analysisExt, field:'effectError2Type')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Effect Error Description:</td>
			<td valign="top" class="value">${fieldValue(bean:analysisExt, field:'effectErrorDesc')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Trait:</td>
			<td valign="top" class="value">${fieldValue(bean:analysisExt, field:'traits')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Standardized trait:</td>
			<td valign="top" class="value"></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Scientific contact:</td>
			<td valign="top" class="value">${fieldValue(bean:analysisExt, field:'dataOwner')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Percent male:</td>
			<td valign="top" class="value">${fieldValue(bean:analysisExt, field:'percentMale')}</td>
		</tr>
		
		
	</tbody>
</table>
