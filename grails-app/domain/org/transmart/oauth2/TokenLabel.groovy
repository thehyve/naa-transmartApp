package org.transmart.oauth2

class TokenLabel {

    String username
    String clientId
    String label

    static constraints = {
        username nullable: false
        clientId nullable: false, blank: false
        label nullable: false, blank: false
    }

    static mapping = {
        datasource 'oauth2'
        version false
    }
}
