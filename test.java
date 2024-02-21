@Mock
    private PGPCongi configMock;

    @InjectMocks
    private KeyService keyService;

    @Test
    void testGetUserID() throws Exception {
        // Arrange
        long keyID = 123;
        boolean bankKey = true;

        // Mock dependencies
        when(configMock.getBankPublicKeyPath()).thenReturn("bankPublicKeyPath");
        when(configMock.getClientPublicKeyPath()).thenReturn("clientPublicKeyPath");

        InputStream inMock = mock(InputStream.class);
        PGPPublicKeyRingCollection pgpPubMock = mock(PGPPublicKeyRingCollection.class);
        PGPPublicKey pgpPublicKeyMock = mock(PGPPublicKey.class);
        Iterator<String> userIdsMock = mock(Iterator.class);

        // Mock behavior for getPgpPub() method
        doReturn(pgpPubMock).when(keyService, "getPgpPub", any(InputStream.class));

        // Mock interactions
        when(inMock.read()).thenReturn(-1);  // Simulate an empty stream
        when(pgpPubMock.getPublicKey(keyID)).thenReturn(pgpPublicKeyMock);
        when(pgpPublicKeyMock.getUserIDs()).thenReturn(userIdsMock);
        when(userIdsMock.hasNext()).thenReturn(true);
        when(userIdsMock.next()).thenReturn("testUserID");

        // Act
        String result = keyService.getUserID(keyID, bankKey);

        // Assert
        assertEquals("testUserID", result);

        // Verify interactions
        verify(inMock).close();
        verify(pgpPubMock).getPublicKey(keyID);
        verify(pgpPublicKeyMock).getUserIDs();
        verify(userIdsMock).hasNext();
        verify(userIdsMock).next();
    }
