import java.util.List;

public class DynamicInsertRequest {
    private List<TableWhereClause> tables;

    // Getters and setters

    public List<TableWhereClause> getTables() {
        return tables;
    }

    public void setTables(List<TableWhereClause> tables) {
        this.tables = tables;
    }
}

public class TableWhereClause {
    private String tableName;
    private String whereClause;

    // Getters and setters

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dynamic-insert")
public class DynamicInsertController {

    @Autowired
    private DynamicInsertService dynamicInsertService;

    @PostMapping
    public ResponseEntity<String> generateInsertStatements(@RequestBody DynamicInsertRequest request) {
        try {
            String result = dynamicInsertService.generateInsertStatements(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating insert statements: " + e.getMessage());
        }
    }
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DynamicInsertService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String generateInsertStatements(DynamicInsertRequest request) {
        StringBuilder insertStatements = new StringBuilder();

        for (TableWhereClause tableClause : request.getTables()) {
            String tableName = tableClause.getTableName();
            String whereClause = tableClause.getWhereClause();

            String selectQuery = "SELECT * FROM " + tableName + " WHERE " + whereClause;
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectQuery);

            if (!rows.isEmpty()) {
                for (Map<String, Object> row : rows) {
                    String insertStatement = generateInsertStatement(tableName, row);
                    insertStatements.append(insertStatement).append("\n");
                }
            }
        }

        return insertStatements.toString();
    }

    private String generateInsertStatement(String tableName, Map<String, Object> row) {
        List<String> columnNames = jdbcTemplate.queryForList(
                "SELECT column_name FROM all_tab_columns WHERE table_name = ?", String.class, tableName);

        StringBuilder insertStatement = new StringBuilder("INSERT INTO ");
        insertStatement.append(tableName).append(" (");

        for (String columnName : columnNames) {
            insertStatement.append(columnName).append(", ");
        }

        insertStatement.delete(insertStatement.length() - 2, insertStatement.length()); // Remove the last comma and space
        insertStatement.append(") VALUES (");

        for (String columnName : columnNames) {
            insertStatement.append(getFormattedValue(columnName, row.get(columnName))).append(", ");
        }

        insertStatement.delete(insertStatement.length() - 2, insertStatement.length()); // Remove the last comma and space
        insertStatement.append(")");

        return insertStatement.toString();
    }

    private String getFormattedValue(String columnName, Object value) {
        if (value == null) {
            return "NULL";
        } else {
            return "'" + value.toString() + "'";
        }
    }
}
