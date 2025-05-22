@Test
void testTokens_CodeGrant_Success() throws Exception {
    // Create real AuthorizationCodeGrant
    AuthorizationCodeGrant grant = new AuthorizationCodeGrant(
        new AuthorizationCode("dummyCode"),
        new URI("https://dummy.redirect.uri")
    );

    // Create real TokenRequest with the real grant
    TokenRequest tokenRequest = new TokenRequest(
        new URI("https://dummy.token.endpoint"),
        new ClientID("client-id"),
        grant
    );

    // Use mockConstruction for TokenRequestValidator
    try (MockedConstruction<TokenRequestValidator> mockValidator = 
            mockConstruction(TokenRequestValidator.class,
                (mock, context) -> {
                    when(mock.validate(any(TokenRequest.class))).thenReturn(
                        ValidationResult.builder()
                            .success(true)
                            .tokenRequest(tokenRequest)
                            .relyingParty("dummyRP")
                            .user(new User("dummyUser"))
                            .tokens(new Tokens("access-token", "refresh-token"))
                            .build()
                    );
                })) {

        // Continue with your mocks for request, response, writer, etc.
        when(request.getParameterMap()).thenReturn(new HashMap<>());

        // call your controller method
        oidcController.token(request, response);

        // verify expected response
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }
}