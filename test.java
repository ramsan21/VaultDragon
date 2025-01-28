import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class BasicAuthXmlRequest {
    public static void main(String[] args) {
        String endpointUrl = "https://example.com/api"; // The endpoint URL
        String username = "your-username"; // Basic authentication username
        String password = "your-password"; // Basic authentication password
        String xmlFilePath = "path/to/your/xmlfile.xml"; // Path to your XML file

        try {
            // Read the XML content from the file
            String requestBody = Files.readString(Path.of(xmlFilePath));

            // Create a connection to the endpoint
            URL url = new URL(endpointUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST"); // Set the HTTP method
            connection.setDoOutput(true); // Enable output for POST requests

            // Set Basic Authentication header
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);

            // Set Content-Type to XML
            connection.setRequestProperty("Content-Type", "application/xml");

            // Write the XML payload to the request body
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(requestBody.getBytes());
                outputStream.flush();
            }

            // Send the request and get the response
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Process the response (if needed)
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Request was successful!");
                // Handle the response here (e.g., read from connection.getInputStream())
            } else {
                System.out.println("Request failed with response code: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
