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
  



class SubsetService {
	
	def i2b2HelperService

    boolean transactional = true

    def serviceMethod() {

    }
	
	def getDisplayQuery(queryMasterId){
		StringBuilder queryStringBuilder = new StringBuilder()
		
		def requestXml = i2b2HelperService.getQueryDefinitionXMLFromQID(Long.toString(queryMasterId))
		def xml = new XmlSlurper().parseText(requestXml)
		
		def panels = xml.panel
		def panelIdx =0 
		panels.each{ panel ->
			if(panelIdx > 0){
				queryStringBuilder.append("<br>AND<br>")
			}
			
			def excludePanel = panel.invert
			if (excludePanel==1){
				queryStringBuilder.append("<b>NOT(</b>")
			}else{
				queryStringBuilder.append("<b>(</b>")
			}
			
			def items = panel.item
			def itemIdx = 0
			items.each{item->
				if(itemIdx>0){
					queryStringBuilder.append("<br><b>OR</b><br>")
				}
				queryStringBuilder.append(item.tooltip)
				itemIdx++
			}
			
			queryStringBuilder.append("<b>)</b>")
			
			panelIdx++
		}
		
		return queryStringBuilder.toString()
	}
}
