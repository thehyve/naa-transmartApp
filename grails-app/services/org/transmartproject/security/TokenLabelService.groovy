package org.transmartproject.security

import org.transmart.oauth2.TokenLabel

class TokenLabelService {

    static datasource = 'oauth2'
    
    def springSecurityService

    def grailsApplication
    
    def find(clientIdValue) {
        return find(clientIdValue, springSecurityService.currentUser.username)
    }
    
    def find(clientIdValue, usernameValue) {
        return TokenLabel.find { clientId == clientIdValue && username == usernameValue }
    }
    
    def save(tokenlabel) {
        return tokenlabel.save()
    }
    
}
