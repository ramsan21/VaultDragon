To check if Oracle is slow, you need to analyze various factors like CPU, memory, disk I/O, wait events, and query performance. Here are some key methods to diagnose Oracle performance issues:

⸻

1. Check Database Load (Active Sessions)

Run the following query to check the number of active sessions and their status:

SELECT COUNT(*), STATUS 
FROM V$SESSION 
GROUP BY STATUS;

	•	If many sessions are in ACTIVE state, it might indicate high load.

⸻

2. Identify Long-Running Queries

To find slow queries running in the database:

SELECT SID, SQL_ID, ELAPSED_TIME/1000000 AS ELAPSED_SEC, CPU_TIME/1000000 AS CPU_SEC, SQL_TEXT 
FROM V$SQL 
WHERE ELAPSED_TIME > 10000000 
ORDER BY ELAPSED_TIME DESC;

	•	If ELAPSED_TIME is high, those queries are taking too long to execute.

⸻

3. Check Session Waits (Wait Events)

Slow performance is often due to sessions waiting on some resource. Find the top wait events:

SELECT EVENT, TOTAL_WAITS, TIME_WAITED 
FROM V$SYSTEM_EVENT 
ORDER BY TIME_WAITED DESC;

Common wait events:
	•	“db file sequential read” – Slow disk I/O or index access.
	•	“log file sync” – Slow commit due to redo log contention.
	•	“CPU Wait” – High CPU usage.

⸻

4. Check CPU & Memory Usage

To see current CPU and memory usage at the database level:

SELECT * FROM V$OSSTAT WHERE STAT_NAME IN ('NUM_CPUS', 'LOAD', 'PHYSICAL_MEMORY_BYTES');

	•	High LOAD value compared to NUM_CPUS indicates CPU contention.
	•	Memory pressure may slow down query performance.

⸻

5. Identify Blocking Sessions

Blocking sessions can slow down the database. Check for locks:

SELECT BLOCKING_SESSION, SID, SERIAL#, WAIT_CLASS, EVENT 
FROM V$SESSION 
WHERE BLOCKING_SESSION IS NOT NULL;

If a session is blocking many others, it might need to be terminated.

⸻

6. Analyze Disk I/O Performance

Check tablespace and disk I/O statistics:

SELECT FILE_ID, TABLESPACE_NAME, PHYSICAL_READS, PHYSICAL_WRITES 
FROM V$DATAFILE 
JOIN V$FILESTAT USING (FILE_ID) 
ORDER BY PHYSICAL_READS DESC;

	•	High PHYSICAL_READS might indicate slow disk performance or missing indexes.

⸻

7. Review AWR or ADDM Reports

If you have AWR (Automatic Workload Repository) enabled, generate a performance report:

SELECT * FROM DBA_HIST_SNAPSHOT ORDER BY SNAP_ID DESC;

Then, run:

@$ORACLE_HOME/rdbms/admin/awrrpt.sql

	•	This generates an AWR report showing bottlenecks.

Alternatively, run ADDM (Automatic Database Diagnostic Monitor):

SELECT DBMS_ADVISOR.GET_TASK_REPORT('ADDM_TASK') FROM DUAL;



⸻

8. Check Redo Log & Archive Log Performance

If commits are slow, check redo logs:

SELECT SEQUENCE#, FIRST_TIME, NEXT_TIME, BLOCKS 
FROM V$LOG_HISTORY 
ORDER BY SEQUENCE# DESC;

	•	Frequent log switches might indicate log contention.

⸻

9. Check Index Usage

Missing or unused indexes can slow down queries:

SELECT TABLE_NAME, INDEX_NAME, NUM_ROWS, LAST_ANALYZED 
FROM DBA_INDEXES 
WHERE LAST_ANALYZED IS NULL;

	•	If LAST_ANALYZED is old, update statistics:

EXEC DBMS_STATS.GATHER_TABLE_STATS('SCHEMA_NAME', 'TABLE_NAME');



⸻

10. Monitor Session Activity (Real-Time)

Use V$SESSION_LONGOPS to see long-running operations:

SELECT SID, OPNAME, TARGET, SOFAR, TOTALWORK, ELAPSED_SECONDS 
FROM V$SESSION_LONGOPS 
WHERE TOTALWORK > 0 
ORDER BY ELAPSED_SECONDS DESC;



⸻

Summary: How to Diagnose Slow Oracle Performance

Issue	Query to Check
High Load (Sessions)	SELECT COUNT(*), STATUS FROM V$SESSION GROUP BY STATUS;
Slow Queries	SELECT SQL_ID, ELAPSED_TIME FROM V$SQL ORDER BY ELAPSED_TIME DESC;
Wait Events	SELECT EVENT, TIME_WAITED FROM V$SYSTEM_EVENT ORDER BY TIME_WAITED DESC;
CPU & Memory Usage	SELECT * FROM V$OSSTAT WHERE STAT_NAME IN ('NUM_CPUS', 'LOAD');
Blocking Sessions	SELECT BLOCKING_SESSION, SID FROM V$SESSION WHERE BLOCKING_SESSION IS NOT NULL;
Disk I/O Issues	SELECT FILE_ID, TABLESPACE_NAME, PHYSICAL_READS FROM V$DATAFILE JOIN V$FILESTAT;
Index Problems	SELECT TABLE_NAME, INDEX_NAME FROM DBA_INDEXES WHERE LAST_ANALYZED IS NULL;
AWR Report	Run @$ORACLE_HOME/rdbms/admin/awrrpt.sql

Would you like help analyzing specific query performance?