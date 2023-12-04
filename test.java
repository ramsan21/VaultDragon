import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.classic.methods.expect.ExpectContinueStrategy;
import org.apache.hc.client5.http.classic.methods.expect.NoopExpectContinueStrategy;
import org.apache.hc.client5.http.classic.methods.http.*;
import org.apache.hc.client5.http.classic.methods.http2.*;
import org.apache.hc.client5.http.classic.methods.http2.push.PushConsumerRegistry;
import org.apache.hc.client5.http.classic.methods.http2.push.PushConsumerTargetRegistry;
import org.apache.hc.client5.http.classic.methods.http2.push.PushPromiseConsumerRegistry;
import org.apache.hc.client5.http.classic.methods.http2.push.PushPromiseConsumerTargetRegistry;
import org.apache.hc.client5.http.classic.methods.http2.push.PushStream;
import org.apache.hc.client5.http.classic.methods.http2.push.PushStreamRegistry;
import org.apache.hc.client5.http.classic.methods.nio.entity.EntityAsyncRequestConsumerSupportBase;
import org.apache.hc.client5.http.classic.methods.nio.entity.EntityAsyncResponseConsumerSupportBase;
import org.apache.hc.client5.http.classic.methods.nio.entity.EntityEnclosingRequestBase;
import org.apache.hc.client5.http.classic.methods.uri.*;
import org.apache.hc.client5.http.classic.methods.http.entity.*;
import org.apache.hc.client5.http.classic.methods.http.entity.mime.*;
import org.apache.hc.client5.http.classic.methods.http.entity.mime.content.*;
import org.apache.hc.client5.http.classic.methods.http.entity.mime.content.multipart.*;
import org.apache.hc.client5.http.classic.methods.http.entity.mime.content.multipart.FileBody;
import org.apache.hc.client5.http.classic.methods.http.entity.mime.content.multipart.InputStreamBody;
import org.apache.hc.client5.http.classic.methods.http.entity.mime.content.multipart.StringBody;
import org.apache.hc.client5.http.classic.methods.http.entity.mime.content.src.ByteSourceEntity;
import org.apache.hc.client5.http.classic.methods.http.entity.mime.content.src.FilePathByteSourceEntity;
import org.apache.hc.client5.http.classic.methods.http.entity.mime.content.src.InputStreamByteSourceEntity;
import org.apache.hc.client5.http.classic.methods.http.entity.mime.content.src.StringByteSourceEntity;
import org.apache.hc.client5.http.classic.protocol.*;
import org.apache.hc.client5.http.classic.ssl.*;
import org.apache.hc.client5.http.config.*;
import org.apache.hc.client5.http.cookie.*;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.entity.mime.*;
import org.apache.hc.client5.http.entity.mime.content.*;
import org.apache.hc.client5.http.entity.mime.content.src.*;
import org.apache.hc.client5.http.impl.*;
import org.apache.hc.client5.http.impl.async.*;
import org.apache.hc.client5.http.impl.classic.*;
import org.apache.hc.client5.http.impl.cookie.*;
import org.apache.hc.client5.http.impl.io.*;
import org.apache.hc.client5.http.io.*;
import org.apache.hc.client5.http.io.entity.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CadmClientTest {

    private CadmClient cadmClient;

    @BeforeEach
    void setUp() {
        cadmClient = new CadmClient();
    }

    @Test
    void testBuildHttpsClientWithoutSSL() throws Exception {
        // Mock TrustStrategy
        TrustStrategy mockTrustStrategy = mock(TrustStrategy.class);
        when(mockTrustStrategy.isTrusted(any(), any())).thenReturn(true);

        // Mock SSLContextBuilder
        SSLContextBuilder mockBuilder = mock(SSLContextBuilder.class);
        when(mockBuilder.setProtocol(any())).thenReturn(mockBuilder);
        when(mockBuilder.loadTrustMaterial(any(), any())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mock(SSLContext.class));

        // Mock SSLConnectionSocketFactory
        SSLConnectionSocketFactory mockSocketFactory = mock(SSLConnectionSocketFactory.class);
        when(mockSocketFactory.getSchemeRegistry()).thenReturn(mock(SchemeRegistry.class));

        // Mock RegistryBuilder
        RegistryBuilder<ConnectionSocketFactory> mockRegistryBuilder = mock(RegistryBuilder.class);
        when(mockRegistryBuilder.register(anyString(), any())).thenReturn(mockRegistryBuilder);
        when(mockRegistryBuilder.register(anyString(), any())).thenReturn(mockRegistryBuilder);

        // Mock PoolingHttClientConnectionManager
        PoolingHttClientConnectionManager mockConnectionManager = mock(PoolingHttClientConnectionManager.class);
        when(mockConnectionManager.setMaxTotal(anyInt())).thenReturn(mockConnectionManager);
        when(mockConnectionManager.setDefaultMaxPerRoute(anyInt())).thenReturn(mockConnectionManager);

        // Mock HttpClientBuilder
        HttpClientBuilder mockClientBuilder = mock(HttpClientBuilder.class);
        when(mockClientBuilder.setConnectionManager(any())).thenReturn(mockClientBuilder);
        when(mockClientBuilder.build()).thenReturn(mock(CloseableHttpClient.class));

        // Mock HttpClients
        HttpClients mockHttpClients = mock(HttpClients.class);
        when(mockHttpClients.custom()).thenReturn(mockClientBuilder);

        // Set up the mocks in your CadmClient instance
        cadmClient.setTrustStrategy(mockTrustStrategy);
        cadmClient.setSslContextBuilder(mockBuilder);
        cadmClient.setSslConnectionSocketFactory(mockSocketFactory);
        cadmClient.setRegistryBuilder(mockRegistryBuilder);
        cadmClient.setConnectionManager(mockConnectionManager);
        cadmClient.setClientBuilder(mockClientBuilder);
        cadmClient.setHttpClients(mockHttpClients);

        // Invoke the method
        CloseableHttpClient result = cadmClient.buildHttpsClientWithoutSSL();

        // Verify that the method under test was called with the expected arguments
        verify(mockBuilder, times(1)).setProtocol("TLSv1.2");
        verify(mockRegistryBuilder, times(2)).register(anyString(), any());
        verify(mockConnectionManager, times(1)).setMaxTotal(40);
        verify(mockConnectionManager, times(1)).setDefaultMaxPerRoute(5);
        verify(mockClientBuilder, times(1)).setConnectionManager(mockConnectionManager);

        // Perform assertions
        // Add more assertions based on your specific implementation and expected behavior
        // For example, assert that the result is not null
        assertNotNull(result);
    }

    @Test
    void testInit() {
        // Mock HttpComponentsClientHttpRequestFactory
        HttpComponentsClientHttpRequestFactory mockRequestFactory =
                mock(HttpComponentsClientHttpRequestFactory.class);

        // Mock CadmClient dependencies
        cadmClient.setRequestFactory(mockRequestFactory);

        // Invoke the method
        cadmClient.init();

        // Verify that the method under test was called with the expected arguments
        verify(mockRequestFactory, times(1)).setHttpClient(any(CloseableHttpClient.class));
        verify(mockRequestFactory, times(1)).setConnectTimeout(5_000);
        verify(mockRequestFactory, times(1)).setConnectionRequestTimeout(5_000);

        // Add more assertions based on your specific implementation and expected behavior
        // For example, assert that the restTemplate field is not null
        assertNotNull(cadmClient.getRestTemplate());
    }
}

@Test
    void testInit() {
        // Mock HttpComponentsClientHttpRequestFactory
        HttpComponentsClientHttpRequestFactory mockRequestFactory =
                mock(HttpComponentsClientHttpRequestFactory.class);

        // Mock CloseableHttpResponse
        CloseableHttpResponse mockHttpResponse = mock(CloseableHttpResponse.class);

        // Mock CloseableHttpClient
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(mockHttpResponse);

        // Set up the mocks in your CadmClient instance
        cadmClient.setRequestFactory(mockRequestFactory);

        // Invoke the method
        cadmClient.init();

        // Verify that the method under test was called with the expected arguments
        verify(mockRequestFactory, times(1)).setHttpClient(any(CloseableHttpClient.class));
        verify(mockRequestFactory, times(1)).setConnectTimeout(5_000);
        verify(mockRequestFactory, times(1)).setConnectionRequestTimeout(5_000);

        // Add more assertions based on your specific implementation and expected behavior
        // For example, assert that the restTemplate field is not null
        assertNotNull(cadmClient.getRestTemplate());
    }
