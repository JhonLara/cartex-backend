package com.cartex.domain.port;

public interface ExternalApiPort {

    /**
     * Authenticates with the external API for a specific company
     * and returns a Bearer token. Tokens are cached per username.
     */
    String authenticateAndGetToken(String username, String password);

    /**
     * Executes an API call using the provided Bearer token.
     */
    <T> T executeWithToken(String endpoint, Object requestBody, Class<T> responseType, String token);
}
