import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CSVToJsonWithRestTemplate {
    private static final String CSV_FILE_PATH = "path/to/your/file.csv";
    private static final String REST_ENDPOINT = "http://example.com/api/endpoint";

    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            RestTemplate restTemplate = new RestTemplate();
            List<String> batch = new ArrayList<>();
            String line;
            int index = 0;

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                // Construct JSON string for each record
                String jsonString = String.format("{\"appId\":\"IDC\",\"groupId\":\"PH01\",\"userId\":\"%s\"}", fields[0]);

                batch.add(jsonString);

                if (batch.size() == 10) {
                    sendBatchToRestEndpoint(restTemplate, batch);
                    batch.clear();
                }

                index++;
            }

            // Process remaining records
            if (!batch.isEmpty()) {
                sendBatchToRestEndpoint(restTemplate, batch);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendBatchToRestEndpoint(RestTemplate restTemplate, List<String> batch) {
        String jsonArrayString = batch.stream().collect(Collectors.joining(",", "[", "]"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonArrayString, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(REST_ENDPOINT, entity, String.class);
            System.out.println("Response: " + response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
