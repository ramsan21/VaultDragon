BEGIN
  FOR r IN (
    SELECT table_name FROM all_tables WHERE owner = 'HR'
  ) LOOP
    EXECUTE IMMEDIATE 'GRANT INSERT, UPDATE, DELETE ON HR.' || r.table_name || ' TO APP_USER';
  END LOOP;
END;
/