import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RestTemplateExample {

    private static final int THREAD_POOL_SIZE = 10;
    private static final String URL = "http://example.com/api"; // Replace with your URL

    public static void main(String[] args) {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Read CSV file and convert to list of data records
        List<MyData> dataList = readCsvFile("path/to/your/csvfile.csv");

        // Initialize ExecutorService
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Process data in batches
        for (int i = 0; i < dataList.size(); i += THREAD_POOL_SIZE) {
            List<MyData> batch = dataList.subList(i, Math.min(i + THREAD_POOL_SIZE, dataList.size()));

            // Submit tasks to executor service
            List<Future<Void>> futures = batch.stream()
                    .map(data -> (Callable<Void>) () -> {
                        // Make POST request
                        restTemplate.postForObject(URL, data, String.class);
                        return null;
                    })
                    .map(executorService::submit)
                    .toList();

            // Wait for all tasks in the batch to complete
            for (Future<Void> future : futures) {
                try {
                    future.get(); // Waits for the task to complete
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Shutdown executor service
        executorService.shutdown();
    }

    private static List<MyData> readCsvFile(String filePath) {
        // Implement your CSV reading logic here
        // Return a list of MyData objects
        return List.of(); // Replace with actual implementation
    }
}import com.opencsv.bean.CsvToBeanBuilder;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class CsvReader {

    public static List<MyData> readCsvFile(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            return new CsvToBeanBuilder<MyData>(reader)
                    .withType(MyData.class)
                    .build()
                    .parse();
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}

