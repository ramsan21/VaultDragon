import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
public class MySpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApplication.class, args);
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
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
        // Asynchronously send POST requests to both URLs using MyService
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

    private final WebClient webClient;

    public MyService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public CompletableFuture<String> sendAsyncPostRequest(String apiUrl, String requestBody) {
        return webClient.post()
                .uri(apiUrl)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(String.class)
                .toFuture();
    }
}
