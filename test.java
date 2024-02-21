public class YourClassName {
    private static final Logger log = LoggerFactory.getLogger(YourClassName.class);

    private final YourConfigClass config;

    public YourClassName(YourConfigClass config) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
    }

    public String getUserID(long keyID, boolean bankKey) throws Exception {
        String publicKeyPath = bankKey ? config.getBankPublicKeyPath() : config.getClientPublicKeyPath();

        try (InputStream in = new FileInputStream(Objects.requireNonNull(publicKeyPath, "Public key path cannot be null"))) {
            PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(
                    PGPUtil.getDecoderStream(in), new JcaKeyFingerprintCalculator());

            PGPPublicKey key = Objects.requireNonNull(pgpPub.getPublicKey(keyID),
                    "Unable to get the Key from Public Keyring. KeyID = " + Long.toHexString(keyID));

            Iterator<String> userIds = key.getUserIDs();

            if (userIds.hasNext()) {
                return userIds.next();
            } else {
                log.error("Unable to find the associated user id for the keyID. KeyID = {}", Long.toHexString(keyID));
                return null;
            }
        }
    }

@Test
    void testGetUserID() throws Exception {
        // Arrange
        YourConfigClass configMock = mock(YourConfigClass.class);
        when(configMock.getBankPublicKeyPath()).thenReturn("bankPublicKeyPath");
        when(configMock.getClientPublicKeyPath()).thenReturn("clientPublicKeyPath");

        YourClassName yourClassName = new YourClassName(configMock);

        long keyID = 123;
        boolean bankKey = true;

        InputStream inMock = mock(InputStream.class);
        PGPPublicKeyRingCollection pgpPubMock = mock(PGPPublicKeyRingCollection.class);
        PGPPublicKey pgpPublicKeyMock = mock(PGPPublicKey.class);
        Iterator<String> userIdsMock = mock(Iterator.class);

        // Mock behavior for getPgpPub() method
        when(yourClassName.getPgpPub(any(InputStream.class)))
                .thenReturn(pgpPubMock);

        // Mock interactions
        when(inMock.read()).thenReturn(-1);  // Simulate an empty stream
        when(pgpPubMock.getPublicKey(keyID)).thenReturn(pgpPublicKeyMock);
        when(pgpPublicKeyMock.getUserIDs()).thenReturn(userIdsMock);
        when(userIdsMock.hasNext()).thenReturn(true);
        when(userIdsMock.next()).thenReturn("testUserID");

        // Act
        String result = yourClassName.getUserID(keyID, bankKey);

        // Assert
        assertEquals("testUserID", result);

        // Verify interactions
        verify(inMock).close();
        verify(pgpPubMock).getPublicKey(keyID);
        verify(pgpPublicKeyMock).getUserIDs();
        verify(userIdsMock).hasNext();
        verify(userIdsMock).next();
    }
