import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
@EnableAsync
public class MySpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

@RestController
class MyController {

    private final String apiUrl1 = "http://example.com/api/resource1";
    private final String apiUrl2 = "http://example.com/api/resource2";

    private final MyService myService;

    public MyController(MyService myService) {
        this.myService = myService;
    }

    @PostMapping("/send-post-request")
    public void sendPostRequest(@RequestBody String requestBody) {
        // Asynchronously send POST requests to both URLs using MyService and ExecutorService
        CompletableFuture<String> response1 = myService.sendAsyncPostRequest(apiUrl1, requestBody);
        CompletableFuture<String> response2 = myService.sendAsyncPostRequest(apiUrl2, requestBody);

        // Wait for both CompletableFuture to complete
        CompletableFuture.allOf(response1, response2).join();

        // Process the responses (you can add more logic here)
        System.out.println("Response from " + apiUrl1 + ": " + response1.join());
        System.out.println("Response from " + apiUrl2 + ": " + response2.join());
    }
}

@Service
class MyService {

    private final RestTemplate restTemplate;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2); // Adjust the pool size as needed

    public MyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CompletableFuture<String> sendAsyncPostRequest(String apiUrl, String requestBody) {
        return CompletableFuture.supplyAsync(() -> {
            // Perform the POST request using RestTemplate
            String response = restTemplate.postForObject(apiUrl, requestBody, String.class);
            System.out.println("Received response from " + apiUrl);
            return response;
        }, executorService);
    }
}
