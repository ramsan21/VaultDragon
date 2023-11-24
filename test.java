import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ApiService {

    public void makeApiCallsConcurrently() {
        try {
            // Create a HttpClient with SSL verification disabled
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER))
                    .build();

            // Make the first API call
            String firstApiUrl = "https://your-first-api-url"; // Replace with your actual API URL
            HttpPost firstApiRequest = new HttpPost(firstApiUrl);
            HttpResponse firstApiResponse = httpClient.execute(firstApiRequest);

            // TODO: Handle the response of the first API call as needed
            System.out.println("First API call response code: " + firstApiResponse.getStatusLine().getStatusCode());

            // Close the HttpClient after the first API call
            httpClient.close();

            // Now, make 10 concurrent API calls
            ExecutorService executorService = Executors.newFixedThreadPool(10);

            for (int i = 0; i < 10; i++) {
                executorService.submit(() -> {
                    try {
                        CloseableHttpClient concurrentHttpClient = HttpClients.custom()
                                .setSSLSocketFactory(new SSLConnectionSocketFactory(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER))
                                .build();

                        String concurrentApiUrl = "https://your-concurrent-api-url"; // Replace with your actual API URL
                        HttpPost concurrentApiRequest = new HttpPost(concurrentApiUrl);

                        // Execute the concurrent request
                        HttpResponse concurrentApiResponse = concurrentHttpClient.execute(concurrentApiRequest);

                        // TODO: Handle the response of the concurrent API call as needed
                        System.out.println("Concurrent API call response code: " + concurrentApiResponse.getStatusLine().getStatusCode());

                        // Close the HttpClient for the concurrent request
                        concurrentHttpClient.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            // Shutdown the executor service and wait for all tasks to finish
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
