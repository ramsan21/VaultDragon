import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    private final RestTemplate restTemplate;

    @Autowired
    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String makePostApiCall(String apiUrl, String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForObject(apiUrl, requestEntity, String.class);
    }
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/example")
public class ExampleController {

    private final ApiService apiService;

    @Autowired
    public ExampleController(ApiService apiService) {
        this.apiService = apiService;
    }

    @PostMapping("/make-multiple-post-calls")
    public void makeMultiplePostApiCalls(@RequestBody String requestBody) {
        String apiUrl = "https://api.example.com/endpoint";

        int numberOfCalls = 5;

        for (int i = 0; i < numberOfCalls; i++) {
            String result = apiService.makePostApiCall(apiUrl, requestBody);
            System.out.println("Result of POST API call " + (i + 1) + ": " + result);
        }
    }
}



