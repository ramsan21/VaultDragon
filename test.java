@Test
public void testAuthenticate() throws NamingException {
    // Create the `attrs` Hashtable
    Hashtable<String, String> attrs = new Hashtable<>();
    attrs.put(Context.INITIAL_CONTEXT_FACTORY, ctxFactory);
    attrs.put(Context.PROVIDER_URL, url);
    attrs.put(Context.SECURITY_PROTOCOL, protocol);
    attrs.put(Context.SECURITY_PRINCIPAL, principleDN);
    attrs.put(Context.SECURITY_CREDENTIALS, encryptedPass);

    // Create a mock object for the CryptoHelper class
    CryptoHelper cryptoHelper = Mockito.mock(CryptoHelper.class);
    when(cryptoHelper.decrypt(encryptedPass)).thenReturn("decryptedPass");

    // Create a mock LDAP context
    LdapContext ldapContext = Mockito.mock(LdapContext.class);
    when(ldapContext.getAttributes("loginUser")).thenReturn(new Hashtable<>());

    // Create a mock search result
    SearchResult searchResult = Mockito.mock(SearchResult.class);
    when(searchResult.getNameInNamespace()).thenReturn("loginUser");

    // Create an instance of the LDAPManager class
    LDAPManager ldapManager = new LDAPManager(attrs, cryptoHelper);

    // Call the authenticate method
    Attributes attributes = ldapManager.authenticate("bankId", "secret");

    // Verify that the methods of the mock objects were called as expected
    verify(cryptoHelper).decrypt(encryptedPass);
    verify(ldapContext).addToEnvironment(Context.SECURITY_PRINCIPAL, "loginUser");
    verify(ldapContext).addToEnvironment(Context.SECURITY_CREDENTIALS, "decryptedPass");
    verify(ldapContext).reconnect(null);
    verify(ldapContext).getAttributes("loginUser");

    // Verify that the correct attributes were returned
    Assertions.assertNotNull(attributes);
    Assertions.assertEquals(0, attributes.size());
}
