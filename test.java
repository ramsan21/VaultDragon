import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.Context;
import javax.naming.DirContext;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.SearchResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LDAPManagerTest {

    @Mock
    private CryptoHelper mockCryptoHelper;

    @Mock
    private SearchResult mockSearchResult;

    @InjectMocks
    private LDAPManager ldapManager;

    @Test
    public void testInit() {
        // Set up mocks
        when(mockCryptoHelper.decrypt("encryptedPass")).thenReturn("decryptedPassword");

        // Execute the method
        ldapManager.init();

        // Verify attribute population
        ArgumentCaptor<Hashtable<String, String>> attrsCaptor = ArgumentCaptor.forClass(Hashtable.class);
        verify(new Hashtable<>(attrsCaptor.capture())).put(Context.INITIAL_CONTEXT_FACTORY, "ctxFactory");
        verify(new Hashtable<>(attrsCaptor.capture())).put(Context.PROVIDER_URL, "url");
        verify(new Hashtable<>(attrsCaptor.capture())).put(Context.SECURITY_PROTOCOL, "protocol");
        verify(new Hashtable<>(attrsCaptor.capture())).put(Context.SECURITY_AUTHENTICATION, "authentication");
        verify(new Hashtable<>(attrsCaptor.capture())).put(Context.SECURITY_PRINCIPAL, "principleDN");
        verify(new Hashtable<>(attrsCaptor.capture())).put(Context.SECURITY_CREDENTIALS, "decryptedPassword");
    }

    @Test
    public void testAuthenticateWithUserFound() throws NamingException {
        String bankId = "bankId";
        String secret = "secret";

        // Set up mocks
        when(ldapManager.exists(bankId)).thenReturn(new Pair<>(mockLdapContext, Optional.of(mockSearchResult)));
        when(ldapManager.authenticate(mockLdapContext, mockSearchResult, secret)).thenReturn(mockAttributes);

        // Execute the method
        Attributes attributes = ldapManager.authenticate(bankId, secret);

        // Verify authentication and attribute retrieval
        assertEquals(mockAttributes, attributes);
        verify(mockLdapContext, times(2)).addToEnvironment(Context.SECURITY_PRINCIPAL, "loginUser");
        verify(mockLdapContext, times(2)).addToEnvironment(Context.SECURITY_CREDENTIALS, secret);
        verify(mockLdapContext).reconnect(null);
    }

    @Test
    public void testAuthenticateWithUserNotFound() throws NamingException {
        String bankId = "bankId";
        String secret = "secret";

        // Set up mocks
        when(ldapManager.exists(bankId)).thenReturn(new Pair<>(mockLdapContext, Optional.empty()));

        // Execute the method
        assertThrows(RuntimeException.class, () -> ldapManager.authenticate(bankId, secret));
    }

    @Test
    public void testExists() throws NamingException {
        String bankId = "bankId";

        // Set up mocks
        when(new InitialLdapContext(ldapManager.getAttributes(), null)).thenReturn(mockLdapContext);
        when(mockLdapContext.search(ldapManager.getBaseDN(), ldapManager.getAccAttribute(), ldapManager.getConstraints())).thenReturn(mockNamingEnumeration);

        // Execute the method
        Pair<LdapContext, Optional<SearchResult>> existsResult = ldapManager.exists(bankId);

        // Verify context creation and search
        assertEquals(mockLdapContext, existsResult.getKey());
        assertEquals(Optional.of(mockSearchResult), existsResult.getValue());
    }

    @Test
    public void testHandleNamingException() throws NamingException {
        LdapContext ldapContext = mock(LdapContext.class);
        SearchResult searchResult = mock(SearchResult.class);
        String secret = "secret";

        // Set up mocks
        when(ldapManager.authenticate(ldapContext, searchResult, secret)).thenThrow(new NamingException("Authentication failed"));

        // Execute the method
        assertThrows
