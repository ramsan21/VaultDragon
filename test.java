AuthorizationCodeGrant codeGrant = new AuthorizationCodeGrant(new AuthorizationCode("dummyCode"), new URI("https://dummy.redirect.uri"));
when(request.getParameterMap()).thenReturn(new HashMap<>());
...
when(tokenRequest.getAuthorizationGrant()).thenReturn(codeGrant);