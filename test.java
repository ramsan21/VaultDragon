import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@EnableAsync
public class MySpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApplication.class, args);
    }
}

@RestController
class MyController {

    private final String apiUrl1 = "http://example.com/api/resource1";
    private final String apiUrl2 = "http://example.com/api/resource2";

    private final AsyncRestTemplate asyncRestTemplate;

    public MyController(AsyncRestTemplate asyncRestTemplate) {
        this.asyncRestTemplate = asyncRestTemplate;
    }

    @PostMapping("/send-post-request")
    public void sendPostRequest(@RequestBody String requestBody) {
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity with headers and payload
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // Asynchronously send POST requests to both URLs using AsyncRestTemplate
        CompletableFuture<ResponseEntity<String>> future1 = sendAsyncPostRequest(apiUrl1, requestEntity);
        CompletableFuture<ResponseEntity<String>> future2 = sendAsyncPostRequest(apiUrl2, requestEntity);

        // Wait for both CompletableFuture to complete
        CompletableFuture.allOf(future1, future2).join();

        // Process the responses (you can add more logic here)
        System.out.println("Response from " + apiUrl1 + ": " + future1.join().getBody());
        System.out.println("Response from " + apiUrl2 + ": " + future2.join().getBody());
    }

    @Async
    private CompletableFuture<ResponseEntity<String>> sendAsyncPostRequest(String apiUrl, HttpEntity<String> requestEntity) {
        return CompletableFuture.completedFuture(asyncRestTemplate.postForEntity(apiUrl, requestEntity, String.class));
    }
}

