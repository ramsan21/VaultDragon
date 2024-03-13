@Test
    void testSeckey() throws Exception {
        // Mock data
        String identity = "testIdentity";
        byte[] keyBytes = "testKey".getBytes(); // example key bytes

        // Mock behavior of pgpPropertiesReader.getPrikeyFromkeyStore(identity)
        RSAPrivateKey privateKeyMock = mock(RSAPrivateKey.class);
        when(pgpPropertiesReader.getPrikeyFromkeyStore(identity)).thenReturn(privateKeyMock);
        when(privateKeyMock.getEncoded()).thenReturn(keyBytes);

        // Call the method
        String result = yourClass.seckey(identity);

        // Verify the result
        // Ensure that the returned string is Base64 encoded
        assertEquals("dGVzdEtleQ==", result); // Adjust the expected Base64 encoded string based on your test data
    }
