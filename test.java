import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api-caller")
public class ApiController {

    private final RestTemplate restTemplate;

    @Autowired
    public ApiController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/call-api-multiple-times")
    public List<String> callApiMultipleTimes() throws ExecutionException, InterruptedException {
        int numberOfCalls = 10;

        // Create an ExecutorService with a fixed thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfCalls);

        // List to store CompletableFuture instances
        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfCalls; i++) {
            // Asynchronously call the API using CompletableFuture
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
                    restTemplate.getForObject("https://api.example.com", String.class), executorService);
            
            // Add the CompletableFuture to the list
            futures.add(future);
        }

        // Combine all CompletableFuture instances into a single CompletableFuture
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        // Wait for all CompletableFuture instances to complete
        allOf.get();

        // Retrieve results from CompletableFuture instances
        List<String> results = new ArrayList<>();
        for (CompletableFuture<String> future : futures) {
            // Retrieve the result from each CompletableFuture
            String result = future.get();
            results.add(result);
        }

        // Shutdown the ExecutorService
        executorService.shutdown();

        return results;
    }
}
