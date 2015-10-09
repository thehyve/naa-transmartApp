package org.transmart.oauth2
import groovy.json.JsonOutput

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.token.TokenEnhancer
import org.springframework.security.oauth2.provider.token.TokenStore
import org.transmartproject.security.TokenLabelService


class CustomTokenEnhancer implements TokenEnhancer {
    
    Log log = LogFactory.getLog(getClass())
    
    @Autowired
    TokenLabelService tokenLabelService
    
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken token,
            OAuth2Authentication auth) {
        def tokenlabel = tokenLabelService.find(auth.OAuth2Request.clientId, auth.name)
        log.debug 'CustomTokenEnhancer: setting label on token: ' + tokenlabel.label
        if (!token.additionalInformation) {
            token.additionalInformation = new HashMap()
        }
        token.additionalInformation['label'] = tokenlabel.label
        return token;
    }

}
