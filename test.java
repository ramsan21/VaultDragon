SELECT 
  TimeStamp,
  TO_CHAR(
    TO_DATE('1970-01-01','YYYY-MM-DD') + (TimeStamp / 1000) / 86400,
    'YYYY-MM-DD HH24:MI:SS'
  ) AS readable_time
FROM your_table;
