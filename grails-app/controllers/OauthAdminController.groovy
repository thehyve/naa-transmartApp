import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.transmart.oauth2.Client


@Secured(['ROLE_ADMIN'])
class OauthAdminController {

    @Autowired
    private TokenStore tokenStore
    
    def springSecurityService

    def grailsApplication
    
    def index = {
        redirect action: list, params: params
    }
    
    def list = {
        log.info('Listing client applications...')
        def clients = Client.findAll()
        clients.each {
            log.info 'Client: ' + it.clientId
        }
        
        def configClients = grailsApplication.config.grails.plugin.springsecurity.oauthProvider.clients
        Set configClientIds = configClients.collect { it.clientId }
        log.info configClientIds

        render view: 'list', model: [clients: clients, configClientIds: configClientIds]
    }
    
    def create = {
        String pathStr =  request.getScheme() + '://' + request.getServerName() + ((request.getLocalPort() != 80) ? ':' + request.getLocalPort() : '') + request.getContextPath() + '/oauth/verify'
    
        def client = new Client()
        client.redirectUris = [ pathStr ]
        client.clientId = 'test'
        client.authorizedGrantTypes = [ 'implicit', 'password']
        render view: 'edit', model: [client: client]
    }

    private findClient(id) {
        def client = Client.get(params.id)
        if (!client) {
            flash.message = "Client application not found with id $params.id"
            redirect action: list
            return
        }
        client
    }

    private copyProperties(client, params) {
        client.properties = params
        
        log.info 'Client: ' + client
    }
    
    def edit = {
        def client = findClient(params.id)
        log.info 'Client: ' + client
        render view: 'edit', model: [client: client]
    }
    
    def view = {
        def client = findClient(params.id)
        render view: 'view', model: [client: client]
    }

    def save = {
        log.info 'Save client. Data: ' + params
        def client
        if (params.id) {
            client = findClient(params.id)
        } else {
            client = new Client()
        }
        copyProperties(client, params)

        def redirectUris = []
        client.redirectUris.each {
            def uri = it.trim()
            if (uri) {
                redirectUris += uri
            }
        }
        client.redirectUris = redirectUris
                
        if (client.save()) {
            log.info 'client saved: ' + client.id
            redirect (action: 'view', id: client.id)
        } else {
            log.info 'saving client failed'
            render view: 'edit', model: [client: client]
        }
    }
    
    def delete = {
        def client = findClient(params.id)
        
        log.info 'Removing client with client ID ${params.id}'
        
        // Remove associated tokens from tokenStore
        def tokens = tokenStore.findTokensByClientId(params.id)
        tokens.each { token -> 
            log.info 'Removing refresh token and access token...'
            if (token.refreshToken) {
                tokenStore.removeRefreshToken(token.refreshToken)
            }
            tokenStore.removeAccessToken(token)
        }
        
        client.delete()
    }
}
