import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public RestTemplate restTemplate() throws Exception {
    // Create an SSLContext that trusts all certificates
    SSLContext sslContext = SSLContextBuilder.create()
        .loadTrustMaterial(null, (certificate, authType) -> true)
        .build();

    // Create a CloseableHttpClient that uses the custom SSLContext
    CloseableHttpClient httpClient = HttpClients.custom()
        .setSSLContext(sslContext)
        .setSSLHostnameVerifier(new NoopHostnameVerifier())
        .build();

    // Use the CloseableHttpClient with the RestTemplate
    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    requestFactory.setHttpClient(httpClient);

    return new RestTemplate(requestFactory);
}
