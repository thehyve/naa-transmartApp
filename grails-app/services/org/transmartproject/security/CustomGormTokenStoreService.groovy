package org.transmartproject.security

import grails.plugin.springsecurity.oauthprovider.GormTokenStoreService
import grails.plugin.springsecurity.oauthprovider.exceptions.OAuth2ValidationException

import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.transmart.oauth2.AccessToken

class CustomGormTokenStoreService extends GormTokenStoreService {

    @Override
    void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        storeNewAccessToken(token, authentication);
    }

    void updateAccessToken(String oldTokenValue, OAuth2AccessToken token, OAuth2Authentication authentication) {
        def (accessTokenLookup, GormAccessToken) = getAccessTokenLookupAndClass()

        def authenticationKeyPropertyName = accessTokenLookup.authenticationKeyPropertyName
        def authenticationPropertyName = accessTokenLookup.authenticationPropertyName
        def usernamePropertyName = accessTokenLookup.usernamePropertyName
        def clientIdPropertyName = accessTokenLookup.clientIdPropertyName
        def valuePropertyName = accessTokenLookup.valuePropertyName
        def tokenTypePropertyName = accessTokenLookup.tokenTypePropertyName
        def expirationPropertyName = accessTokenLookup.expirationPropertyName
        def refreshTokenPropertyName = accessTokenLookup.refreshTokenPropertyName
        def scopePropertyName = accessTokenLookup.scopePropertyName
        //def additionalInformationPropertyName = accessTokenLookup.additionalInformationPropertyName

        def gormAccessToken = GormAccessToken.findWhere ((valuePropertyName): oldTokenValue)
        if (!gormAccessToken) {
            throw new InvalidRequestException("Access token not found")
        }

        //gormAccessToken."$authenticationKeyPropertyName" = authenticationKey
        gormAccessToken."$authenticationPropertyName" = oauth2AuthenticationSerializer.serialize(authentication)
        gormAccessToken."$usernamePropertyName" = authentication.isClientOnly() ? null : authentication.name
        gormAccessToken."$clientIdPropertyName" = authentication.getOAuth2Request().clientId
        gormAccessToken."$valuePropertyName" = token.value
        gormAccessToken."$tokenTypePropertyName" = token.tokenType
        gormAccessToken."$expirationPropertyName" = token.expiration
        gormAccessToken."$refreshTokenPropertyName" = token.refreshToken?.value
        gormAccessToken."$scopePropertyName" = token.scope
        //gormAccessToken."$additionalInformationPropertyName" = token.additionalInformation

        if(!gormAccessToken.save()) {
            throw new OAuth2ValidationException("Failed to save access token", gormAccessToken.errors)
        }
    }
    
    public void storeNewAccessToken(OAuth2AccessToken token,
            OAuth2Authentication authentication) {
            
        log.debug 'Store new access token.'
        def (accessTokenLookup, GormAccessToken) = getAccessTokenLookupAndClass()

        def authenticationPropertyName = accessTokenLookup.authenticationPropertyName
        def usernamePropertyName = accessTokenLookup.usernamePropertyName
        def clientIdPropertyName = accessTokenLookup.clientIdPropertyName
        def valuePropertyName = accessTokenLookup.valuePropertyName
        def tokenTypePropertyName = accessTokenLookup.tokenTypePropertyName
        def expirationPropertyName = accessTokenLookup.expirationPropertyName
        def refreshTokenPropertyName = accessTokenLookup.refreshTokenPropertyName
        def scopePropertyName = accessTokenLookup.scopePropertyName
        def additionalInformationPropertyName = accessTokenLookup.additionalInformationPropertyName

        def gormAccessToken = GormAccessToken.newInstance()

        gormAccessToken."$authenticationPropertyName" = oauth2AuthenticationSerializer.serialize(authentication)
        gormAccessToken."$usernamePropertyName" = authentication.isClientOnly() ? null : authentication.name
        gormAccessToken."$clientIdPropertyName" = authentication.OAuth2Request.clientId
        gormAccessToken."$valuePropertyName" = token.value
        gormAccessToken."$tokenTypePropertyName" = token.tokenType
        gormAccessToken."$expirationPropertyName" = token.expiration
        gormAccessToken."$refreshTokenPropertyName" = token.refreshToken?.value
        gormAccessToken."$scopePropertyName" = token.scope
        gormAccessToken."$additionalInformationPropertyName" = token.additionalInformation

        if(!gormAccessToken.save()) {
            throw new OAuth2ValidationException("Failed to save access token", gormAccessToken.errors)
        }
    }

    @Override
    OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        
        log.info 'CustomGormTokenStoreService.getAccessToken' 
        
        def clientIdValue = authentication.OAuth2Request.clientId
        def usernameValue = authentication.isClientOnly() ? null : authentication.name
        def gormAccessToken = AccessToken.findWhere ( clientId: clientIdValue, username: usernameValue )

        if (!gormAccessToken) {
            log.debug("Failed to find access token for authentication [$authentication]")
            return null
        }

        def accessToken = createOAuth2AccessToken(gormAccessToken)
        def tokenValue = accessToken.value

        if(!checkAuthenticationForAccessToken(tokenValue, authentication)) {
            log.warn("Authentication [$authentication] is not associated with retrieved access token")
            removeAccessToken(tokenValue)
            // fixme
            storeAccessToken(accessToken, authentication)
        }
        return accessToken
    }

    private boolean checkAuthenticationForAccessToken(String token, OAuth2Authentication authentication) {
        def auth = readAuthentication(token)
        return auth.name == authentication.name && auth.OAuth2Request.clientId == authentication.OAuth2Request.clientId;
    }
    
    
}
