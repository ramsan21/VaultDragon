import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.AsyncResultCallback;
import org.apache.hc.client5.http.async.methods.BasicRequestProducer;
import org.apache.hc.client5.http.async.methods.BasicResponseConsumer;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.nio.support.BasicRequestConsumer;
import org.apache.hc.client5.http.impl.classic.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.async.methods.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ApiService {

    public void makeApiCallsConcurrently() {
        try {
            // Create a CloseableHttpAsyncClient with SSL verification disabled
            CloseableHttpAsyncClient asyncHttpClient = HttpAsyncClients.custom()
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            // Start the async client
            asyncHttpClient.start();

            // Make the first API call
            String firstApiUrl = "https://your-first-api-url"; // Replace with your actual API URL
            Future<HttpResponse> firstApiFuture = asyncHttpClient.execute(SimpleRequestBuilder.get(firstApiUrl).build(), new BasicResponseConsumer<>(), null);

            // Wait for the first API call to finish
            HttpResponse firstApiResponse = firstApiFuture.get();

            // TODO: Handle the response of the first API call as needed
            System.out.println("First API call response code: " + firstApiResponse.getCode());

            // Now, make 10 concurrent API calls
            for (int i = 0; i < 10; i++) {
                String concurrentApiUrl = "https://your-concurrent-api-url"; // Replace with your actual API URL
                HttpUriRequestBase request = SimpleRequestBuilder.get(concurrentApiUrl).build();

                asyncHttpClient.execute(request, new BasicResponseConsumer<>(), null);
            }

            // Wait for all tasks to finish
            asyncHttpClient.awaitTermination(30, TimeUnit.SECONDS);

        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
        }
    }
}
