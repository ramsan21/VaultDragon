import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

// ...

RestTemplate restTemplate() throws Exception {
    SSLContext sslContext = new SSLContextBuilder()
            .loadTrustMaterial(null, (certificate, authType) -> true).build();

    SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
    CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

    return new RestTemplate(factory);
}
