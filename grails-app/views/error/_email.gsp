<!DOCTYPE html>
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
	  <title>Server Error Alert</title>
	  <style type="text/css">
            body {
                background-color: #F0F8FF;
                font-family: 'Helvetica',arial,clean,sans-serif;
            }
            .messageWindow {
                border-radius: 16px;
                padding: 16px;
                margin: 64px auto;
                width: 800px;
                background-color: white;
                border: 1px solid #CCC;
            }
	  		.message {
	  			border: 1px solid #DDD;
	  			padding: 5px;
	  			background-color:#F8F8F8;
                font-size: 10pt;
	  		}
	  		.stack {
	  			border: 1px solid #DDD;
	  			padding: 5px;
	  			overflow:auto;
                font-size: 10pt;
                font-family: 'Consolas', 'Courier New', monospace;
                white-space: pre-wrap;
                background-color: #F8F8F8;
	  		}
	  		.snippet {
	  			padding: 5px;
	  			background-color:white;
	  			border:1px solid #DDD;
	  			margin:3px;
	  			font-family: 'Consolas', 'Courier New', monospace;
	  		}
          h1 {
              font-size: 12pt;
              margin: 0px;
              color: #A00;
          }
          p {
              font-size: 10pt;
          }
          h2 {
              text-align: center;
              font-size: 11pt;
          }
        .link {
            color: blue;
            font-size: 10pt;
            cursor: pointer;
        }
	  </style>
  </head>

  <body>
    <div class="messageWindow">
        <div style="text-align: center"/>
            <h1>tranSMART Server Error</h1>
            <p>tranSMART encountered a problem while serving a request at <g:formatDate date="${new Date()}" format="HH:mm:ss yyyy-MM-dd"/>. Further information may be available in the web server log files.</p>
        </div>
        <g:if test="${exception}">
            <hr />
            <tmpl:technicalInfo exception="${exception}"/>
        </g:if>
    </div>
  </body>
</html>
