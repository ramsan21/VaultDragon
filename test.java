SELECT owner, table_name, ROUND((num_rows * avg_row_len + 38) / 1024 / 1024, 2) AS size_mb
FROM all_tables
ORDER BY size_mb DESC;
