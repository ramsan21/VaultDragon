AuthorizationCodeGrant codeGrant = new AuthorizationCodeGrant(new AuthorizationCode("dummyCode"), new URI("https://dummy.redirect.uri"));
when(request.getParameterMap()).thenReturn(new HashMap<>());
...
when(tokenRequest.getAuthorizationGrant()).thenReturn(codeGrant);


AuthorizationCodeGrant codeGrant = new AuthorizationCodeGrant(new AuthorizationCode("auth-code"), new URI("https://dummy.uri"));
when(tokenRequest.getAuthorizationGrant()).thenReturn(codeGrant);

AuthorizationCodeGrant codeGrant = (AuthorizationCodeGrant) grant;