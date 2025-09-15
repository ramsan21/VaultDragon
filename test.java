BEGIN
   FOR t IN (SELECT table_name FROM user_tables) LOOP
      EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM ' || t.table_name INTO :cnt;
      DBMS_OUTPUT.PUT_LINE(t.table_name || ' => ' || :cnt);
   END LOOP;
END;
/