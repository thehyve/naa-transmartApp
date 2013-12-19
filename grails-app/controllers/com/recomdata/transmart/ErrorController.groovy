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


package com.recomdata.transmart

import org.codehaus.groovy.grails.commons.ConfigurationHolder

class ErrorController {

    def index = {
            try {
            def exception = request.exception
            def emailAddress = ConfigurationHolder.config.grails.mail.error.alert
            def emailHtml = g.render(template: 'email', model: [exception: exception])

            if (emailAddress) {
                //Inner try for mail send - if this fails we still want to be able to display the error.
                try {
                    sendMail {
                        to emailAddress
                        html emailHtml
                        subject '[tranSMART] Application error report'
                    }
                }
                catch (Exception e) {
                    e.printStackTrace()
                    emailAddress = "";
                }
            }

            render(view: 'error', model: [exception: exception, hasEmail: emailAddress ? true : false])
        }
        catch (Exception e) { //MUST catch exception and stop circular error call!
            e.printStackTrace()
            render(status: 200, text: "An exception occurred but the application was unable to display it. Please check the Transmart logs for more information.")
        }
    }

    def notFound = {
        try {
            def exception = request.exception
            render(view: 'notFound', model: [exception: exception])
        }
        catch (Exception e) { //MUST catch exception and stop circular error call!
            render(status: 200, text: "An exception occurred but the application was unable to display it. Please check the Transmart logs for more information.")
        }
    }
}
