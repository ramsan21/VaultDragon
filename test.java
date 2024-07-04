SELECT DBMS_METADATA.GET_DDL('TABLE', t.table_name) AS TABLE_DDL,
       DBMS_METADATA.GET_DDL('INDEX', i.index_name) AS INDEX_DDL,
       DBMS_METADATA.GET_DDL('CONSTRAINT', c.constraint_name) AS CONSTRAINT_DDL
FROM all_tables t
LEFT JOIN all_indexes i ON t.table_name = i.table_name
LEFT JOIN all_constraints c ON t.table_name = c.table_name
WHERE t.table_name = 'YOUR_TABLE_NAME';


SELECT DBMS_METADATA.GET_DDL('TABLE', 'YOUR_TABLE_NAME') AS DDL_SCRIPT
FROM DUAL;


SELECT index_name, index_type, uniqueness
FROM user_indexes
WHERE table_name = 'YOUR_TABLE_NAME';

SELECT index_name, column_name, column_position
FROM user_ind_columns
WHERE table_name = 'YOUR_TABLE_NAME'
ORDER BY index_name, column_position;

SELECT owner, synonym_name, table_owner, table_name, db_link
FROM all_synonyms
WHERE table_name = 'YOUR_TABLE_NAME';
