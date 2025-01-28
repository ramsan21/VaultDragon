import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.util.Base64;

public class SecureEndpointConnector {
    public static void main(String[] args) {
        String truststorePath = "path/to/truststore.jks"; // Path to your truststore.jks file
        String truststorePassword = "your-truststore-password"; // Truststore password
        String endpointUrl = "https://example.com/api"; // The endpoint URL
        String username = "your-username"; // Basic authentication username
        String password = "your-password"; // Basic authentication password

        try {
            // Load the truststore
            KeyStore truststore = KeyStore.getInstance("JKS");
            try (FileInputStream truststoreStream = new FileInputStream(truststorePath)) {
                truststore.load(truststoreStream, truststorePassword.toCharArray());
            }

            // Initialize TrustManagerFactory with the truststore
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(truststore);

            // Initialize SSLContext with the TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            // Set the SSLContext to the default
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            // Create a connection to the endpoint
            URL url = new URL(endpointUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST"); // Change to GET/PUT/DELETE if needed
            connection.setDoOutput(true); // Enable output for POST/PUT requests

            // Set Basic Authentication header
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);

            // Set other headers (if needed)
            connection.setRequestProperty("Content-Type", "application/json");

            // Write data to the request body (if required)
            String requestBody = "{\"key\": \"value\"}"; // Example JSON payload
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(requestBody.getBytes());
                outputStream.flush();
            }

            // Send the request and get the response
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Process the response (if needed)
            if (responseCode == HttpsURLConnection.HTTP_OK) {
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
