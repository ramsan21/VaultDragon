import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Service
public class ApiService {

    private final RestTemplate restTemplate;

    @Autowired
    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CompletableFuture<String> makePostApiCallAsync(String apiUrl, String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        return CompletableFuture.supplyAsync(() ->
                restTemplate.postForObject(apiUrl, requestEntity, String.class)
        );
    }
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/example")
public class ExampleController {

    private final ApiService apiService;

    @Autowired
    public ExampleController(ApiService apiService) {
        this.apiService = apiService;
    }

    @PostMapping("/make-concurrent-post-calls")
    public void makeConcurrentPostApiCalls(@RequestBody String requestBody) {
        String apiUrl = "https://api.example.com/endpoint";

        int numberOfCalls = 5;

        // Create a list of CompletableFuture for concurrent API calls
        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (int i = 0; i < numberOfCalls; i++) {
            futures.add(apiService.makePostApiCallAsync(apiUrl, requestBody));
        }

        // Wait for all CompletableFuture to complete
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        // Join and collect results
        allOf.join();

        // Process results as needed
        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // Print results
        results.forEach(result -> System.out.println("Result: " + result));
    }
}

