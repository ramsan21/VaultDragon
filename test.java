SELECT
    TO_CHAR(t_created, 'YYYY-MM-DD') AS record_date,
    COUNT(*) AS total_records,
    SUM(CASE WHEN res_status = 'FAILED' THEN 1 ELSE 0 END) AS failed_records,
    ROUND(
        (SUM(CASE WHEN res_status = 'FAILED' THEN 1 ELSE 0 END) / COUNT(*)) * 100, 2
    ) AS failure_percentage
FROM
    tmx_audit
GROUP BY
    TO_CHAR(t_created, 'YYYY-MM-DD')
ORDER BY
    record_date;