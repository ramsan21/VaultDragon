public String getUserID(long keyID, boolean bankKey) throws Exception {
        String publicKeyPath = bankKey ? getConfig().getBankPublicKeyPath() : getConfig().getClientPublicKeyPath();

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

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YourClassNameTest {

    @Mock
    private FileInputStream fileInputStreamMock;

    @Mock
    private YourConfigClass configMock;

    @Mock
    private PGPPublicKeyRingCollection pgpPubMock;

    @Mock
    private PGPPublicKey pgpPublicKeyMock;

    @Mock
    private Iterator<String> userIdsMock;

    @InjectMocks
    private YourClassName yourClassName;

    @Test
    void testGetUserID() throws Exception {
        // Arrange
        long keyID = 123;
        boolean bankKey = true;

        // Mock dependencies
        when(configMock.getBankPublicKeyPath()).thenReturn("bankPublicKeyPath");
        when(configMock.getClientPublicKeyPath()).thenReturn("clientPublicKeyPath");

        when(fileInputStreamMock.read()).thenReturn(-1);  // Simulate an empty stream
        when(pgpPublicKeyMock.getUserIDs()).thenReturn(userIdsMock);
        when(userIdsMock.hasNext()).thenReturn(true);
        when(userIdsMock.next()).thenReturn("testUserID");

        // Mock static method
        PowerMockito.mockStatic(PGPUtil.class);
        when(PGPUtil.getDecoderStream(any(InputStream.class))).thenReturn(fileInputStreamMock);

        // Mock behavior for getPgpPub() method
        doReturn(pgpPubMock).when(yourClassName, "getPgpPub", any(InputStream.class));

        // Act
        String result = yourClassName.getUserID(keyID, bankKey);

        // Assert
        assertEquals("testUserID", result);

        // Verify interactions
        verify(fileInputStreamMock).close();
        verifyStatic(PGPUtil.class);
        PGPUtil.getDecoderStream(any(InputStream.class));
        verify(pgpPubMock).getPublicKey(keyID);
        verify(pgpPublicKeyMock).getUserIDs();
        verify(userIdsMock).hasNext();
        verify(userIdsMock).next();
    }
}

