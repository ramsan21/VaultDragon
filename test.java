import com.scb.starsec.utility.exceptions.CryptoException;
import com.scb.starsec.utility.helpers.Utf8;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.s1f4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.GeneralSecurityException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DbCryptoHelperTest {

    @Mock
    private KeyHelper keyHelper;

    @Mock
    private SymmCipherHandler symmCipherHandler;

    @InjectMocks
    private DbCryptoHelper dbCryptoHelper;

    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(dbCryptoHelper, "alias", "testAlias");
        ReflectionTestUtils.setField(dbCryptoHelper, "algorithm", "testAlgorithm");
        ReflectionTestUtils.setField(dbCryptoHelper, "provider", "testProvider");
        ReflectionTestUtils.setField(dbCryptoHelper, "keystorePath", "testKeystorePath");
        ReflectionTestUtils.setField(dbCryptoHelper, "keystorePassword", "testKeystorePassword");
        ReflectionTestUtils.setField(dbCryptoHelper, "keystoreType", "testKeystoreType");

        when(keyHelper.getStorageSecretkey()).thenReturn(new byte[]{1, 2, 3});
    }

    @Test
    public void testProtect() {
        String plainText = "testPlainText";
        when(symmCipherHandler.encrypt(any())).thenReturn(new byte[]{4, 5, 6});

        String result = dbCryptoHelper.protect(plainText);

        assertEquals("040506", result); // Hex encoding of the byte array {4, 5, 6}
    }

    @Test
    public void testProtectWithNullPlainText() {
        String result = dbCryptoHelper.protect(null);
        assertEquals(null, result);
    }

    @Test
    public void testUnprotect() {
        String cipherTextHex = "040506";
        when(symmCipherHandler.decrypt(any())).thenReturn(new byte[]{1, 2, 3});

        String result = dbCryptoHelper.unprotect(cipherTextHex);

        assertEquals("010203", result); // Utf8 string representation of the byte array {1, 2, 3}
    }

    @Test
    public void testUnprotectWithNullCipherTextHex() {
        String result = dbCryptoHelper.unprotect(null);
        assertEquals(null, result);
    }
}
