import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.crypto.Cipher;
import java.security.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AsymCipherHandlerTest {

    @Mock
    private Cipher mockCipher;
    @Mock
    private PublicKey mockPublicKey;
    @Mock
    private PrivateKey mockPrivateKey;

    private AsymCipherHandler handler;

    @Before
    public void setUp() {
        handler = new AsymCipherHandler("RSA");
        when(mockCipher.getBlockSize()).thenReturn(-1);
    }

    @Test
    public void testConstructorWithAlgorithm() {
        assertEquals("RSA", handler.algorithm);
        assertNull(handler.provider);
        assertNull(handler.publickey);
        assertNull(handler.privatekey);
    }

    @Test
    public void testConstructorWithAlgorithmAndProvider() {
        handler = new AsymCipherHandler("RSA", "BC");
        assertEquals("RSA", handler.algorithm);
        assertEquals("BC", handler.provider);
        assertNull(handler.publickey);
        assertNull(handler.privatekey);
    }

    @Test(expected = RuntimeException.class)
    public void testDecryptWithNullPrivateKey() throws Exception {
        handler.decrypt(new byte[1]);
    }

    @Test
    public void testDecryptWithRSAECBAlgorithm() throws Exception {
        when(mockCipher.getAlgorithm()).thenReturn("RSA/ECB/PKCS1Padding");
        when(((RSAPublicKey) mockPublicKey).getModulus()).thenReturn(2048);
        handler.setPrivateKey(mockPrivateKey);
        handler.setPublicKey(mockPublicKey);
        handler.decrypt(new byte[1]);

        verify(mockCipher).init(Cipher.DECRYPT_MODE, mockPrivateKey);
        verify(mockCipher).getBlockSize();
        verify(mockCipher).doFinal(any(byte[].class));
    }

    @Test
    public void testDecryptWithRSAAlgorithm() throws Exception {
        when(mockCipher.getAlgorithm()).thenReturn("RSA");
        when(((RSAPublicKey) mockPublicKey).getModulus()).thenReturn(2048);
        handler.setPrivateKey(mockPrivateKey);
        handler.setPublicKey(mockPublicKey);
        handler.decrypt(new byte[1]);

        verify(mockCipher).init(Cipher.DECRYPT_MODE, mockPrivateKey);
        verify(mockCipher).getBlockSize();
        verify(mockCipher).doFinal(any(byte[].class));
    }

    @Test
    public void testDecryptWithUnknownAlgorithm() throws Exception {
        when(mockCipher.getAlgorithm()).thenReturn("UNKNOWN");
        handler.setPrivateKey(mockPrivateKey);
        handler.setPublicKey(mockPublicKey);

        try {
            handler.decrypt(new byte[1]);
            fail("Expected exception");
        } catch (RuntimeException e) {
            assertEquals("Unknow blocksize Error!!!", e.getMessage());
        }
    }

    @Test(expected = RuntimeException.class)
    public void testEncryptWithNullPublicKey() throws Exception {
        handler.encrypt(new byte[1]);
    }

    @Test
    public void testEncryptWithRSAAlgorithm() throws Exception {
        when(mockCipher.getAlgorithm()).thenReturn("RSA");
        when(((RSAPublicKey) mockPublicKey).getModulus()).thenReturn(1024);
        handler.setPublicKey(mockPublicKey);
        handler.encrypt(new byte[1]);

        verify(mockCipher).init(Cipher.ENCRYPT_MODE, mockPublicKey);
        verify(mockCipher).getBlockSize();
        verify(mockCipher).doFinal(any(byte[].class));
    }

    @Test
    public void testSetPrivateKey() {
        handler.setPrivateKey(mockPrivateKey);
        assertEquals(mockPrivateKey, handler.getPrivatekey());
    }

    @Test
    public void testSetPublicKey() {
        handler.setPublicKey(mockPublicKey);
        assertEquals(mockPublicKey, handler.getPublickey());
    }

    @Test
