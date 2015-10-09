package org.transmart.oauth2

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.oauthprovider.endpoint.WrappedAuthorizationEndpoint
import groovy.json.JsonOutput

import java.security.Principal

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.support.SessionStatus
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.View
import org.transmartproject.security.TokenLabelService

class CustomAuthorizationEndpoint extends WrappedAuthorizationEndpoint {

    @Autowired
    TokenLabelService tokenLabelService
    
    @Override
    public View approveOrDeny(@RequestParam Map<String,String> approvalParameters, Map<String,?> model, SessionStatus sessionStatus, Principal principal) {
        def tokenlabel = tokenLabelService.find(approvalParameters['client_id'], principal.name)
        log.debug 'Stored label: ' + tokenlabel?.label + ', new label: ' + approvalParameters['label']
        if (!tokenlabel) {
            tokenlabel = new TokenLabel()
            tokenlabel.username = principal.name
            tokenlabel.clientId = approvalParameters['client_id']
            tokenlabel.label = approvalParameters['label']
            tokenlabel = tokenLabelService.save(tokenlabel)
        } else {
            tokenlabel.label = approvalParameters['label']
            tokenlabel = tokenLabelService.save(tokenlabel)
        }
        log.debug 'Stored label: ' + tokenlabel.label + ', new label: ' + approvalParameters['label']
        return super.approveOrDeny(approvalParameters, model, sessionStatus, principal);
    }

}
