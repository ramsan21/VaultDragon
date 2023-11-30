 @Test
    public void testAuthenticate() throws NamingException {
        // Create a mock object for the CryptoHelper class
        CryptoHelper cryptoHelper = Mockito.mock(CryptoHelper.class);
        when(cryptoHelper.decrypt("encryptedPass")).thenReturn("decryptedPass");

        // Create a mock LDAP context
        LdapContext ldapContext = Mockito.mock(LdapContext.class);
        when(ldapContext.getAttributes("loginUser")).thenReturn(new Hashtable<>());

        // Create a mock search result
        SearchResult searchResult = Mockito.mock(SearchResult.class);
        when(searchResult.getNameInNamespace()).thenReturn("loginUser");

        // Create an instance of the LDAPManager class
        LDAPManager ldapManager = new LDAPManager("ctxFactory", "url", "authentication", "protocol", "encryptedPass", "baseDN", "attr", cryptoHelper);

        // Call the authenticate method
        Attributes attributes = ldapManager.authenticate("bankId", "secret");

        // Verify that the methods of the mock objects were called as expected
        verify(ldapContext).addToEnvironment(Context.SECURITY_PRINCIPAL, "loginUser");
        verify(ldapContext).addToEnvironment(Context.SECURITY_CREDENTIALS, "decryptedPass");
        verify(ldapContext).reconnect(null);
        verify(ldapContext).getAttributes("loginUser");

        // Verify that the correct attributes were returned
        Assertions.assertNotNull(attributes);
        Assertions.assertEquals(0, attributes.size());
    }

    @Test
    public void testExists() throws NamingException {
        // Create a mock LDAP context
        LdapContext ldapContext = Mockito.mock(LdapContext.class);
        when(ldapContext.search("baseDN", "(attr=bankId)", SearchControls.SUBTREE_SCOPE)).thenReturn(new NamingEnumeration<>());

        // Create an instance of the LDAPManager class
        LDAPManager ldapManager = new LDAPManager("ctxFactory", "url", "authentication", "protocol", "encryptedPass", "baseDN", "attr", null);

        // Call the exists method
        Pair<LdapContext, Optional<SearchResult>> existsResult = ldapManager.exists("bankId");

        // Verify that the mock objects were called as expected
        verify(ldapContext).search("baseDN", "(attr=bankId)", SearchControls.SUBTREE_SCOPE);

        // Verify that the correct pair was returned
        Assertions.assertNotNull(existsResult);
        Assertions.assertEquals(ldapContext, existsResult.getKey());
        Assertions.assertFalse(existsResult.getValue().isPresent());
    }
}
