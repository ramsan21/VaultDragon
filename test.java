import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.bettercloud.vault.api.AuthResponse;
import com.bettercloud.vault.api.VaultException;
import com.bettercloud.vault.rest.RestResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;

public class VaultAuthTest {

    @Mock
    private VaultConfig mockConfig;

    @Mock
    private Rest mockRest;

    @Mock
    private RestResponse mockRestResponse;

    private VaultAuth vaultAuth;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockConfig.getAddress()).thenReturn("http://localhost:8200");
        when(mockConfig.getNameSpace()).thenReturn("namespace");
        when(mockConfig.getOpenTimeout()).thenReturn(5);
        when(mockConfig.getReadTimeout()).thenReturn(5);
        when(mockConfig.getSslConfig().isVerify()).thenReturn(true);

        vaultAuth = spy(new VaultAuth(mockConfig));
    }

    @Test
    public void testLoginByAppRole_SuccessfulResponse() throws Exception {
        // Arrange
        String path = "approle";
        String roleId = "test-role-id";
        String secretId = "test-secret-id";
        String url = "http://localhost:8200/v1/auth/approle/login";

        when(mockRestResponse.getStatus()).thenReturn(200);
        when(mockRestResponse.getBody()).thenReturn("{"token":"test-token"}".getBytes(StandardCharsets.UTF_8));
        when(mockRest.post()).thenReturn(mockRestResponse);

        doReturn(mockRest).when(vaultAuth).buildRest(anyString(), any(), anyString(), anyInt(), anyInt(), anyBoolean());

        // Act
        AuthResponse authResponse = vaultAuth.loginByAppRole(path, roleId, secretId);

        // Assert
        assertNotNull(authResponse);
        assertEquals(200, authResponse.getRestResponse().getStatus());
        verify(mockRest, times(1)).post();
    }

    @Test
    public void testLoginByAppRole_FailedResponse() {
        // Arrange
        String path = "approle";
        String roleId = "test-role-id";
        String secretId = "test-secret-id";
        String url = "http://localhost:8200/v1/auth/approle/login";

        when(mockRestResponse.getStatus()).thenReturn(400);
        when(mockRestResponse.getBody()).thenReturn("Error response".getBytes(StandardCharsets.UTF_8));
        when(mockRest.post()).thenReturn(mockRestResponse);

        doReturn(mockRest).when(vaultAuth).buildRest(anyString(), any(), anyString(), anyInt(), anyInt(), anyBoolean());

        // Act & Assert
        try {
            vaultAuth.loginByAppRole(path, roleId, secretId);
            fail("Expected VaultException to be thrown");
        } catch (VaultException e) {
            assertTrue(e.getMessage().contains("Vault responded with HTTP status code: 400"));
        }

        verify(mockRest, times(1)).post();
    }

    @Test
    public void testLoginByAppRole_MaxRetries() {
        // Arrange
        String path = "approle";
        String roleId = "test-role-id";
        String secretId = "test-secret-id";
        String url = "http://localhost:8200/v1/auth/approle/login";

        when(mockRestResponse.getStatus()).thenReturn(500);
        when(mockRest.post()).thenReturn(mockRestResponse);

        doReturn(mockRest).when(vaultAuth).buildRest(anyString(), any(), anyString(), anyInt(), anyInt(), anyBoolean());
        when(mockConfig.getMaxRetries()).thenReturn(3);
        when(mockConfig.getRetryIntervalMilliseconds()).thenReturn(100);

        // Act & Assert
        try {
            vaultAuth.loginByAppRole(path, roleId, secretId);
            fail("Expected VaultException to be thrown");
        } catch (VaultException e) {
            assertTrue(e.getMessage().contains("Vault responded with HTTP status code: 500"));
        }

        verify(mockRest, times(4)).post(); // Initial attempt + 3 retries
    }
}

protected Rest buildRest(String url, Pair<String, String> headers, String body, int connectTimeout, int readTimeout, boolean verifySsl) {
    Rest rest = new Rest()
            .url(url)
            .header(headers.getLeft(), headers.getRight())
            .body(body.getBytes(StandardCharsets.UTF_8))
            .connectTimeoutSeconds(connectTimeout)
            .readTimeoutSeconds(readTimeout)
            .sslVerification(verifySsl);
    return rest;
}

