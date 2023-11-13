import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DynamicInsertService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String generateInsertStatement(String selectStatement, String tableName) {
        String query = selectStatement.replace("select *", "select * from " + tableName);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);

        if (rows.isEmpty()) {
            return "No rows returned from the select statement.";
        }

        Map<String, Object> firstRow = rows.get(0);
        StringBuilder insertStatement = new StringBuilder("INSERT INTO ");
        insertStatement.append(tableName).append(" (");

        firstRow.forEach((columnName, value) -> insertStatement.append(columnName).append(", "));

        insertStatement.delete(insertStatement.length() - 2, insertStatement.length()); // Remove the last comma and space
        insertStatement.append(") VALUES (");

        firstRow.forEach((columnName, value) -> {
            insertStatement.append(getFormattedValue(value)).append(", ");
        });

        insertStatement.delete(insertStatement.length() - 2, insertStatement.length()); // Remove the last comma and space
        insertStatement.append(")");

        return insertStatement.toString();
    }

    private String getFormattedValue(Object value) {
        if (value == null) {
            return "NULL";
        } else if (value instanceof Number) {
            return value.toString();
        } else {
            // Handle other data types (string, date, etc.)
            return "'" + value.toString() + "'";
        }
    }
}


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DynamicInsertController {

    @Autowired
    private DynamicInsertService dynamicInsertService;

    @PostMapping("/api/generate-insert")
    public String generateInsertStatement(@RequestParam String selectStatement, @RequestParam String tableName) {
        return dynamicInsertService.generateInsertStatement(selectStatement, tableName);
    }
}
