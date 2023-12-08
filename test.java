import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SymmCipherHandlerTest {

    @Mock
    private Cipher mockCipher;

    @Mock
    private SecretKey mockSecretKey;

    @InjectMocks
    private SymmCipherHandler symmCipherHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDecrypt() throws Exception {
        byte[] cipherText = "TestCipherText".getBytes();
        byte[] expectedPlainText = "TestPlainText".getBytes();

        when(mockCipher.getAlgorithm()).thenReturn("AES/CBC/PKCS5Padding");
        when(mockCipher.doFinal(cipherText)).thenReturn(expectedPlainText);

        Whitebox.setInternalState(symmCipherHandler, "algorithm", "AES");
        Whitebox.setInternalState(symmCipherHandler, "secretkey", mockSecretKey);

        byte[] result = symmCipherHandler.decrypt(cipherText);

        verify(mockCipher).init(eq(Cipher.DECRYPT_MODE), eq(mockSecretKey), any(IvParameterSpec.class));
        assertArrayEquals(expectedPlainText, result);
    }

    @Test
    public void testEncrypt() throws Exception {
        byte[] plainText = "TestPlainText".getBytes();
        byte[] expectedCipherText = "TestCipherText".getBytes();

        when(mockCipher.getAlgorithm()).thenReturn("AES/CBC/PKCS5Padding");
        when(mockCipher.doFinal(plainText)).thenReturn(expectedCipherText);

        Whitebox.setInternalState(symmCipherHandler, "algorithm", "AES");
        Whitebox.setInternalState(symmCipherHandler, "secretkey", mockSecretKey);

        byte[] result = symmCipherHandler.encrypt(plainText);

        verify(mockCipher).init(eq(Cipher.ENCRYPT_MODE), eq(mockSecretKey), any(IvParameterSpec.class));
        assertArrayEquals(expectedCipherText, result);
    }

    @Test
    public void testSetIv() {
        byte[] iv = "TestIV".getBytes();

        symmCipherHandler.setIv(iv);

        byte[] actualIv = Whitebox.getInternalState(symmCipherHandler, "iv");
        assertArrayEquals(iv, actualIv);
    }

    @Test
    public void testGetDefaultIV() {
        int totalIVByte = 16;
        byte[] expectedIV = new byte[totalIVByte];
        for (int i = 0; i < totalIVByte; i++) {
            expectedIV[i] = (byte) 0xf3;
        }

        byte[] result = Whitebox.invokeMethod(symmCipherHandler, "getDefaultIV", totalIVByte);

        assertArrayEquals(expectedIV, result);
    }
}
