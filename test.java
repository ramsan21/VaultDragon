SELECT 
    owner,
    segment_name AS table_name,
    segment_type,
    bytes / 1024 / 1024 AS size_mb
FROM 
    dba_segments
WHERE 
    segment_type = 'TABLE'
    AND owner = 'HR'
ORDER BY 
    size_mb DESC;
