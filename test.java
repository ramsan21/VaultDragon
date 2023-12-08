import com.scb.starsec.utility.exceptions.CryptoException;
import com.scb.starsec.utility.helpers.KeyHelper;
import com.scb.starsec.utility.helpers.SymmCipherHandler;
import com.scb.starsec.utility.helpers.Utf8;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.s1f4j.Logger;

import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class DbCryptoHelperTest {

    @Mock
    private Logger logger;

    @Mock
    private KeyHelper keyHelper;

    @Mock
    private SymmCipherHandler symmCipherHandler;

    @InjectMocks
    private DbCryptoHelper dbCryptoHelper;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(keyHelper.getStorageSecretkey()).thenReturn(Mockito.mock(SecretKey.class));
        dbCryptoHelper.init();
    }

    @Test
    void testProtect() {
        String plainText = "TestPlainText";
        when(symmCipherHandler.encrypt(Mockito.any())).thenReturn(new byte[]{1, 2, 3});

        String encryptedValue = dbCryptoHelper.protect(plainText);

        assertEquals("010203", encryptedValue); // Expected hex representation of encrypted bytes
    }

    @Test
    void testUnprotect() {
        String cipherTextHex = "010203";
        when(symmCipherHandler.decrypt(Mockito.any())).thenReturn(new byte[]{4, 5, 6});

        String decryptedValue = dbCryptoHelper.unprotect(cipherTextHex);

        assertEquals("040506", decryptedValue); // Expected plain text
    }

    @Test
    void testProtectWithException() throws CryptoException, GeneralSecurityException {
        String plainText = "TestPlainText";
        when(symmCipherHandler.encrypt(Mockito.any())).thenThrow(new CryptoException("Encryption error"));

        String encryptedValue = dbCryptoHelper.protect(plainText);

        assertEquals(plainText, encryptedValue); // Should return original plain text on exception
    }

    @Test
    void testUnprotectWithException() throws CryptoException, GeneralSecurityException {
        String cipherTextHex = "010203";
        when(symmCipherHandler.decrypt(Mockito.any())).thenThrow(new CryptoException("Decryption error"));

        String decryptedValue = dbCryptoHelper.unprotect(cipherTextHex);

        assertEquals(cipherTextHex, decryptedValue); // Should return original cipher text on exception
    }

    // Add more test cases as needed

}
