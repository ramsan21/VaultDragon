import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.bettercloud.vault.api.AuthResponse;
import com.bettercloud.vault.api.VaultException;
import com.bettercloud.vault.rest.RestResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;

public class VaultAuthTest {

    @Mock
    private VaultConfig mockConfig;

    private WireMockServer wireMockServer;
    private VaultAuth vaultAuth;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // Start WireMock server on port 8200
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8200));
        wireMockServer.start();

        // Mock VaultConfig setup
        when(mockConfig.getAddress()).thenReturn("http://localhost:8200");
        when(mockConfig.getNameSpace()).thenReturn("namespace");
        when(mockConfig.getOpenTimeout()).thenReturn(5);
        when(mockConfig.getReadTimeout()).thenReturn(5);
        when(mockConfig.getSslConfig().isVerify()).thenReturn(true);

        vaultAuth = new VaultAuth(mockConfig);
    }

    @After
    public void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testLoginByAppRole_SuccessfulResponse() throws Exception {
        // Arrange
        String path = "approle";
        String roleId = "test-role-id";
        String secretId = "test-secret-id";

        // Stub WireMock to return a success response
        wireMockServer.stubFor(post(urlEqualTo("/v1/auth/approle/login"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"auth\":{\"client_token\":\"test-token\"}}")));

        // Act
        AuthResponse authResponse = vaultAuth.loginByAppRole(path, roleId, secretId);

        // Assert
        assertNotNull(authResponse);
        assertEquals(200, authResponse.getRestResponse().getStatus());
        assertTrue(new String(authResponse.getRestResponse().getBody(), StandardCharsets.UTF_8).contains("test-token"));
    }

    @Test
    public void testLoginByAppRole_FailedResponse() {
        // Arrange
        String path = "approle";
        String roleId = "test-role-id";
        String secretId = "test-secret-id";

        // Stub WireMock to return a failure response
        wireMockServer.stubFor(post(urlEqualTo("/v1/auth/approle/login"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("{\"errors\":[\"Invalid credentials\"]}")));

        // Act & Assert
        try {
            vaultAuth.loginByAppRole(path, roleId, secretId);
            fail("Expected VaultException to be thrown");
        } catch (VaultException e) {
            assertTrue(e.getMessage().contains("Vault responded with HTTP status code: 400"));
            assertTrue(e.getMessage().contains("Invalid credentials"));
        }
    }

    @Test
    public void testLoginByAppRole_MaxRetries() {
        // Arrange
        String path = "approle";
        String roleId = "test-role-id";
        String secretId = "test-secret-id";

        // Stub WireMock to simulate a server error
        wireMockServer.stubFor(post(urlEqualTo("/v1/auth/approle/login"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("{\"errors\":[\"Internal server error\"]}")));

        // Mock retry configurations
        when(mockConfig.getMaxRetries()).thenReturn(3);
        when(mockConfig.getRetryIntervalMilliseconds()).thenReturn(100);

        // Act & Assert
        try {
            vaultAuth.loginByAppRole(path, roleId, secretId);
            fail("Expected VaultException to be thrown");
        } catch (VaultException e) {
            assertTrue(e.getMessage().contains("Vault responded with HTTP status code: 500"));
            assertTrue(e.getMessage().contains("Internal server error"));
        }
    }
}
