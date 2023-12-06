import com.scb.starsec.utility.exceptions.CryptoException;
import com.scb.starsec.utility.helpers.Utf8;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(dbCryptoHelper, "alias", "testAlias");
        ReflectionTestUtils.setField(dbCryptoHelper, "algorithm", "testAlgorithm");
        ReflectionTestUtils.setField(dbCryptoHelper, "provider", "testProvider");
        ReflectionTestUtils.setField(dbCryptoHelper, "keystorePath", "testKeystorePath");
        ReflectionTestUtils.setField(dbCryptoHelper, "keystorePassword", "testKeystorePassword");
        ReflectionTestUtils.setField(dbCryptoHelper, "keystoreType", "testKeystoreType");
        ReflectionTestUtils.setField(dbCryptoHelper, "keyHelper", keyHelper);
        ReflectionTestUtils.setField(dbCryptoHelper, "handler", symmCipherHandler);
    }

    @Test
    void testProtect() throws CryptoException, GeneralSecurityException {
        String plainText = "testPlainText";
        when(symmCipherHandler.encrypt(any())).thenReturn(new byte[]{});
        when(logger.isDebugEnabled()).thenReturn(true);

        String result = dbCryptoHelper.protect(plainText);

        verify(logger, times(2)).debug(anyString());
        assertEquals(plainText, result);
    }

    @Test
    void testUnprotect() throws CryptoException, GeneralSecurityException {
        String cipherTextHex = "testCipherTextHex";
        when(symmCipherHandler.decrypt(any())).thenReturn(new byte[]{});
        when(logger.isDebugEnabled()).thenReturn(true);

        String result = dbCryptoHelper.unprotect(cipherTextHex);

        verify(logger, times(2)).debug(anyString());
        assertEquals(cipherTextHex, result);
    }
}
