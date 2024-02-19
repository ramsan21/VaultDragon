import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyStore;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class YourClassTest {

    @Test
    void testGetPrikeyFromkeyStore() throws Exception {
        // Mocking necessary objects
        Properties props = new Properties();
        props.setProperty("KEYSTORE_PROVIDER", "your_provider");
        props.setProperty("KEYSTORE_TYPE", "your_keystore_type");
        props.setProperty("KEYSTORE_PATH", "your_keystore_path");
        String identity = "test_identity";
        String passwd = "test_password";

        Logger log = Mockito.mock(Logger.class);
        Map<String, PrivateKey> prikeyTable = new HashMap<>();
        Map<String, PublicKey> pubkeyTable = new HashMap<>();

        YourClass yourClass = new YourClass(props, log, prikeyTable, pubkeyTable);

        // Mocking the FileInputStream and KeyStore
        FileInputStream fileInputStreamMock = Mockito.mock(FileInputStream.class);
        KeyStore keyStoreMock = Mockito.mock(KeyStore.class);
        Mockito.when(keyStoreMock.getKey(Mockito.anyString(), Mockito.any(char[].class)))
                .thenReturn(Mockito.mock(PrivateKey.class));
        Mockito.when(keyStoreMock.getCertificate(Mockito.anyString()).getPublicKey())
                .thenReturn(Mockito.mock(PublicKey.class));

        Mockito.whenNew(FileInputStream.class).withAnyArguments().thenReturn(fileInputStreamMock);
        Mockito.when(KeyStore.getInstance(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(keyStoreMock);

        // Testing the method
        PrivateKey privateKey = yourClass.getPrikeyFromkeyStore(identity);

        // Asserting the result
        assertNotNull(privateKey);
        // Add more assertions as needed
    }
}
