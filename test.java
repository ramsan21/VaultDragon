import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@PrepareForTest({SymmCipherHandler.class})
class SymmCipherHandlerTest {

    @Mock
    private Cipher mockCipher;

    @Mock
    private SecretKey mockSecretKey;

    @InjectMocks
    private SymmCipherHandler symmCipherHandler;

    @BeforeEach
    void setUp() {
        PowerMockito.mockStatic(Cipher.class);
        when(Cipher.getInstance(anyString(), anyString())).thenReturn(mockCipher);
        when(Cipher.getInstance(anyString())).thenReturn(mockCipher);
    }

    @Test
    void testDecrypt() throws Exception {
        byte[] cipherText = "TestCipherText".getBytes();
        byte[] expectedPlainText = "TestPlainText".getBytes();

        when(mockCipher.getAlgorithm()).thenReturn("AES/CBC/PKCS5Padding");
        when(mockCipher.doFinal(cipherText)).thenReturn(expectedPlainText);

        symmCipherHandler.setAlgorithm("AES");
        symmCipherHandler.setSecretKey(mockSecretKey);

        byte[] result = symmCipherHandler.decrypt(cipherText);

        verify(mockCipher).init(eq(Cipher.DECRYPT_MODE), eq(mockSecretKey), any(IvParameterSpec.class));
        assertArrayEquals(expectedPlainText, result);
    }

    @Test
    void testEncrypt() throws Exception {
        byte[] plainText = "TestPlainText".getBytes();
        byte[] expectedCipherText = "TestCipherText".getBytes();

        when(mockCipher.getAlgorithm()).thenReturn("AES/CBC/PKCS5Padding");
        when(mockCipher.doFinal(plainText)).thenReturn(expectedCipherText);

        symmCipherHandler.setAlgorithm("AES");
        symmCipherHandler.setSecretKey(mockSecretKey);

        byte[] result = symmCipherHandler.encrypt(plainText);

        verify(mockCipher).init(eq(Cipher.ENCRYPT_MODE), eq(mockSecretKey), any(IvParameterSpec.class));
        assertArrayEquals(expectedCipherText, result);
    }

    @Test
    void testSetIv() {
        byte[] iv = "TestIV".getBytes();

        symmCipherHandler.setIv(iv);

        byte[] actualIv = symmCipherHandler.getIv();
        assertArrayEquals(iv, actualIv);
    }

    @Test
    void testGetDefaultIV() {
        int totalIVByte = 16;
        byte[] expectedIV = new byte[totalIVByte];
        for (int i = 0; i < totalIVByte; i++) {
            expectedIV[i] = (byte) 0xf3;
        }

        byte[] result = PowerMockito.method(SymmCipherHandler.class, "getDefaultIV", int.class)
                .withArguments(totalIVByte)
                .invoke();

        assertArrayEquals(expectedIV, result);
    }
}
