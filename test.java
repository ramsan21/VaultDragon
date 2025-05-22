@Test
void testTokens_CodeGrant_Success() throws Exception {
    // Do not mock AuthorizationGrant
    AuthorizationCode code = new AuthorizationCode("dummyAuthCode");
    URI redirectUri = new URI("https://dummy.redirect.uri");
    AuthorizationCodeGrant codeGrant = new AuthorizationCodeGrant(code, redirectUri);

    TokenRequest tokenRequest = new TokenRequest(
        new URI("https://dummy.token.endpoint"),
        new ClientID("client-id"),
        codeGrant
    );

    // If your controller pulls this from the request, mock accordingly
    when(request.getParameterMap()).thenReturn(new HashMap<>());

    // Inject your constructed TokenRequest (with the real grant)
    when(requestValidator.validate(any())).thenReturn(
        ValidationResult.builder()
            .success(true)
            .tokenRequest(tokenRequest)
            .relyingParty("dummyRP")
            .user(new User("dummyUser"))
            .tokens(new Tokens("access-token", "refresh-token"))
            .build()
    );

    // Remaining mocking for response/writer/assertions...
}