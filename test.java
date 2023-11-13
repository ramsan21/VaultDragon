import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataMigrationService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void generateAndExecuteInsert(String tableName, String whereClause, List<Object> values) {
        // Generate the insert statement
        String insertStatement = generateInsertStatement(tableName, values);

        // Append the WHERE clause if provided
        if (whereClause != null && !whereClause.isEmpty()) {
            insertStatement += " WHERE " + whereClause;
        }

        // Execute the insert statement
        jdbcTemplate.update(insertStatement, values.toArray());
    }

    private String generateInsertStatement(String tableName, List<Object> values) {
        // Assuming columns are named column1, column2, ...
        String columns = "column" + String.join(", column", String.valueOf(values.size()).split("(?!^)"));

        // Generate the insert statement
        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + generatePlaceholder(values.size()) + ")";
    }

    private String generatePlaceholder(int count) {
        // Generate placeholders for the prepared statement
        return String.join(", ", String.valueOf(count).split("(?!^)")).replaceAll("\\d", "?");
    }
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
public class DataMigrationController {

    @Autowired
    private DataMigrationService dataMigrationService;

    @PostMapping("/api/migrate")
    public ResponseEntity<String> migrateTableData(@RequestParam String tableName, @RequestParam String whereClause) {
        // Assuming values list for the insert
        List<Object> values = Arrays.asList("value1", "value2", "value3");

        // Execute the dynamic insert
        dataMigrationService.generateAndExecuteInsert(tableName, whereClause, values);

        return ResponseEntity.ok("Data migration completed");
    }
}

spring.datasource.prod.url=jdbc:oracle:thin:@//production-host:1521/production-service
spring.datasource.prod.username=production-username
spring.datasource.prod.password=production-password
spring.datasource.prod.driver-class-name=oracle.jdbc.OracleDriver


