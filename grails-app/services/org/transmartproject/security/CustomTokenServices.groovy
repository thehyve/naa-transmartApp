package org.transmartproject.security

import groovy.json.JsonOutput

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2RefreshToken
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.TokenRequest
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.transmart.oauth2.AccessToken

class CustomTokenServices extends DefaultTokenServices {

    @Autowired
    protected CustomGormTokenStoreService tokenStore;

    @Override
    public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {

        log.info 'Creating an access token.'

        OAuth2AccessToken existingAccessToken = tokenStore.getAccessToken(authentication);
        OAuth2RefreshToken refreshToken = null;
        if (existingAccessToken != null) {
            if (existingAccessToken.isExpired()) {
                log.info 'Removing expired token.'
                if (existingAccessToken.getRefreshToken() != null) {
                    refreshToken = existingAccessToken.getRefreshToken();
                    // The token store could remove the refresh token when the access token is removed, but we want to
                    // be sure...
                    tokenStore.removeRefreshToken(refreshToken);
                }
                tokenStore.removeAccessToken(existingAccessToken);
            }
        }
        def clientId = authentication.OAuth2Request.clientId
        refreshToken = createRefreshToken(authentication);
        OAuth2AccessToken accessToken = createAccessToken(authentication, refreshToken);
        tokenStore.storeNewAccessToken(accessToken, authentication);
        if (refreshToken != null) {
            tokenStore.storeRefreshToken(refreshToken, authentication);
        }
        return accessToken;
    }

    @Override
    public OAuth2AccessToken refreshAccessToken(String refreshTokenValue,
            TokenRequest request) throws AuthenticationException {

        OAuth2RefreshToken refreshToken = tokenStore.readRefreshToken(refreshTokenValue);
        if (refreshToken == null) {
            throw new InvalidGrantException("Invalid refresh token: " + refreshTokenValue);
        }
        
        def oldAccessToken = AccessToken.findByRefreshToken refreshToken
        if (!oldAccessToken) {
            throw new InvalidRequestException("Access token not found")
        }
        
        OAuth2Authentication authentication = tokenStore.readAuthenticationForRefreshToken(refreshToken);
        def clientId = authentication.OAuth2Request.clientId
        if (clientId == null || !clientId.equals(request.clientId)) {
            throw new InvalidGrantException("Wrong client for this refresh token: " + refreshTokenValue);
        }

        if (isExpired(refreshToken)) {
            tokenStore.removeRefreshToken(refreshToken);
            throw new InvalidTokenException("Invalid refresh token (expired): " + refreshToken);
        }

        authentication = createRefreshedAuthentication(authentication, request.getScope());

        tokenStore.removeRefreshToken(refreshToken);
        refreshToken = createRefreshToken(authentication);

        OAuth2AccessToken accessToken = createAccessToken(authentication, refreshToken);
        tokenStore.updateAccessToken(oldAccessToken.value, accessToken, authentication);
        tokenStore.storeRefreshToken(refreshToken, authentication);
        return accessToken;
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        return tokenStore.getAccessToken(authentication);
    }
}
