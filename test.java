import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CSVToJsonWithRestTemplate {
    private static final String CSV_FILE_PATH = "path/to/your/file.csv";
    private static final String REST_ENDPOINT = "http://example.com/api/endpoint";

    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            RestTemplate restTemplate = new RestTemplate();
            StringBuilder batch = new StringBuilder();
            String line;
            int index = 0;

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                
                // Replace these values dynamically from CSV or another source
                String userId = fields[0];  // Assuming the userId is in the first column
                String groupId = fields.length > 1 ? fields[1] : "PH01";  // Example: Taking groupId from second column if exists, otherwise default value
                String appId = fields.length > 2 ? fields[2] : "IDC";  // Example: Taking appId from third column if exists, otherwise default value
                
                // Construct JSON string for each record
                String jsonString = String.format("{\"appId\":\"%s\",\"groupId\":\"%s\",\"userId\":\"%s\"}", appId, groupId, userId);

                // Append to batch string
                if (batch.length() > 0) {
                    batch.append(",");
                }
                batch.append(jsonString);

                if ((index + 1) % 10 == 0) {
                    sendBatchToRestEndpoint(restTemplate, batch.toString());
                    batch.setLength(0); // Clear the batch
                }

                index++;
            }

            // Process remaining records
            if (batch.length() > 0) {
                sendBatchToRestEndpoint(restTemplate, batch.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendBatchToRestEndpoint(RestTemplate restTemplate, String batch) {
        String jsonArrayString = "[" + batch + "]";

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
