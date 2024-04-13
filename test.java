import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class FileProcessor {
    private final RestTemplate restTemplate;

    public FileProcessor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String processFile(String inputFile, String outputFile, String bankIdentity, String clientIdentity,
                              boolean armor, String encAlgo, String hashAlgo) {
        // Create the request body as a map
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("inputFile", inputFile);
        requestBody.put("outputFile", outputFile);
        requestBody.put("bankIdentity", bankIdentity);
        requestBody.put("clientIdentity", clientIdentity);
        requestBody.put("armor", armor);
        requestBody.put("encAlgo", encAlgo);
        requestBody.put("hashAlgo", hashAlgo);

        // Create the HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the HTTP entity with the request body and headers
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // Send the POST request to the API endpoint
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity("API_ENDPOINT_URL", requestEntity, Map.class);

            // Check the response status code
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = responseEntity.getBody();
                int statusCode = (int) responseBody.get("statusCode");
                String statusMessage = (String) responseBody.get("statusMessage");

                if (statusCode == 0 && statusMessage.equals("success")) {
                    return (String) responseBody.get("outputFile");
                } else {
                    throw new RuntimeException("File processing failed. Status code: " + statusCode + ", Message: " + statusMessage);
                }
            } else {
                throw new RuntimeException("API request failed. HTTP status code: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while processing the file.", e);
        }
    }
}
