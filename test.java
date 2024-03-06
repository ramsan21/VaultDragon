@Test
    void testLoadStreamFromClasspath() throws Exception {
        // Prepare the method to be invoked using reflection
        Method loadStreamMethod = PGPPropertiesReader.class.getDeclaredMethod("loadStream", String.class);
        loadStreamMethod.setAccessible(true);

        String classpathResource = "classpath:resource";
        when(util.getFilePath(anyString())).thenReturn(null); // Simulate the behavior
        
        // Invoke the private method
        InputStream result = (InputStream) loadStreamMethod.invoke(pgpPropertiesReader, classpathResource);
        
        assertNotNull(result, "InputStream should not be null for classpath resources");
    }

    @Test
    void testLoadStreamFromFile() throws Exception {
        // Prepare the method to be invoked using reflection
        Method loadStreamMethod = PGPPropertiesReader.class.getDeclaredMethod("loadStream", String.class);
        loadStreamMethod.setAccessible(true);

        String filePath = "file:path/to/resource";
        // Assume util.getFilePath properly mocked if necessary

        // Invoke the private method
        InputStream result = (InputStream) loadStreamMethod.invoke(pgpPropertiesReader, filePath);
        
        assertNotNull(result, "InputStream should not be null for file paths");
    }
