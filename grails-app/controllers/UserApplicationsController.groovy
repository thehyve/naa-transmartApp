import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.security.oauth2.provider.token.TokenStore
import org.transmart.oauth2.AccessToken

@Secured(['ROLE_CLIENT'])
class UserApplicationsController {

    @Autowired
    private TokenStore tokenStore
    
    def springSecurityService
    
    def list = {
        def principal = springSecurityService.principal
        
        log.info 'Fetching access tokens for ' + principal.username
        def tokens = AccessToken.findAll { username == principal.username }
        def result = []
        tokens.each { 
            def t = tokenStore.readAccessToken(it.value)
            it.additionalInformation['refreshTokenExpiration'] = t.refreshToken.expiration.time.toString()
        }
        render view: 'list', model: [tokens: tokens]
    }
    
    def revoke = {
        def principal = springSecurityService.principal
        def token = AccessToken.find { id == params.id }
        if (token.username == principal.username) {
            log.info 'Removing access token ' + token.id
            if (token.refreshToken) {
                tokenStore.removeRefreshToken token.refreshToken
            } 
            tokenStore.removeAccessToken token.value
        }
        flash.message = 'The access token has been revoked.'
        redirect (action: 'list')
    }
    
    def revokeAll = {
        def principal = springSecurityService.principal
        def tokens = AccessToken.findAll { username == principal.username }
        tokens.each { token ->
            if (token.refreshToken) {
                tokenStore.removeRefreshToken token.refreshToken
            }
            tokenStore.removeAccessToken token.value
        }
        flash.message = 'All access tokens have been revoked.'
        redirect (action: 'list')
    }

}
