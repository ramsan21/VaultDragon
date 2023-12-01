import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

public class APIActivationHelperTest {

    @Mock
    private CadmClient cadmClient;

    @Mock
    private APIRegistrationRepository repository;

    @Mock
    private KeyHelper keyHelper;

    @Mock
    private SymmCipherHandler handler;

    @InjectMocks
    private APIActivationHelper apiActivationHelper;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(keyHelper.getStorageSecretKey()).thenReturn("mockedSecretKey");
    }

    @Test
    public void testActivate() {
        Map<String, String> data = new HashMap<>();
        data.put("groupId", "testGroupId");
        data.put("tp", "testTp");
        data.put("publicKey", "testPublicKey");
        data.put("webhook", "testWebhook");

        when(cadmClient.patchGroup("testGroupId", any())).thenReturn(true);

        apiActivationHelper.activate(data);

        // Add assertions here based on your implementation and expected behavior
    }

    @Test
    public void testDelete() {
        String groupId = "testGroupId";
        String tp = "testTp";

        when(repository.findById("testTp")).thenReturn(Optional.of(new APIRegistration()));

        String result = apiActivationHelper.delete(groupId, tp);

        // Add assertions here based on your implementation and expected behavior
    }

    @Test
    public void testEncryptAndDecryptData() throws GeneralSecurityException {
        Map<String, String> data = new HashMap<>();
        data.put("key1", "value1");
        data.put("key2", "value2");

        when(handler.encrypt(any())).thenReturn("mockedEncryptedData".getBytes());
        when(handler.decrypt(any())).thenReturn("mockedDecryptedData".getBytes());

        String encryptedData = apiActivationHelper.encryptData(data);
        Map<String, String> decryptedData = apiActivationHelper.decryptData(encryptedData);

        // Add assertions here based on your implementation and expected behavior
    }
}
