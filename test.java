import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import javax.net.ssl.SSLContext;

// ...

try {
    SSLContext sslContext = new SSLContextBuilder()
                                .loadTrustMaterial(null, (certificate, authType) -> true)
                                .build();

    SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
    
    CloseableHttpClient httpClient = HttpClients.custom()
                                                .setSSLSocketFactory(socketFactory)
                                                .build();
    
    // Use httpClient as needed for requests
} catch (Exception e) {
    e.printStackTrace();
}
