DECLARE
   l_file      UTL_FILE.file_type;
   l_buffer    RAW(32767);
   l_amount    BINARY_INTEGER := 32767;
   l_pos       INTEGER := 1;
   l_blob      BLOB;
   l_blob_len  INTEGER;
BEGIN
   -- Query the BLOB data
   SELECT blob_column INTO l_blob FROM your_table WHERE condition;

   -- Get the length of the BLOB
   l_blob_len := DBMS_LOB.getlength(l_blob);

   -- Open a file to write (ensure the directory is valid in UTL_FILE)
   l_file := UTL_FILE.fopen('YOUR_DIRECTORY', 'output_file.dat', 'wb', 32767);

   -- Write the BLOB to file in chunks
   WHILE l_pos <= l_blob_len LOOP
      DBMS_LOB.read(l_blob, l_amount, l_pos, l_buffer);
      UTL_FILE.put_raw(l_file, l_buffer, TRUE);
      l_pos := l_pos + l_amount;
   END LOOP;

   -- Close the file
   UTL_FILE.fclose(l_file);
END;