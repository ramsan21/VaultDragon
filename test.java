SELECT DBMS_METADATA.GET_DDL('TABLE', 'YOUR_TABLE_NAME') AS TABLE_DDL,
       DBMS_METADATA.GET_DDL('INDEX', index_name) AS INDEX_DDL,
       DBMS_METADATA.GET_DDL('CONSTRAINT', constraint_name) AS CONSTRAINT_DDL
FROM all_tables t
LEFT JOIN all_indexes i ON t.table_name = i.table_name
LEFT JOIN all_constraints c ON t.table_name = c.table_name
WHERE t.table_name = 'YOUR_TABLE_NAME';


SELECT DBMS_METADATA.GET_DDL('TABLE', 'YOUR_TABLE_NAME') AS DDL_SCRIPT
FROM DUAL;
