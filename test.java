import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class VerifyOnePassSignatureListTest {

    @Autowired
    private YourServiceClass service; // Replace YourServiceClass with the actual class name

    @Test
    public void testVerifyOnePassSignatureList() throws Exception {
        // Mock the dependencies
        DecryptVerifyRequest request = mock(DecryptVerifyRequest.class); // Assuming request is your custom class
        PGPOnePassSignatureList opsList = mock(PGPOnePassSignatureList.class);
        PGPOnePassSignature ops = mock(PGPOnePassSignature.class);
        PGPPublicKey key = mock(PGPPublicKey.class);
        KeyChainHandler keyChainHandler = mock(KeyChainHandler.class);

        // Configure mocks
        when(opsList.get(0)).thenReturn(ops);
        when(ops.getKeyID()).thenReturn(123L);
        when(service.keyChainHandler).thenReturn(keyChainHandler);
        when(keyChainHandler.getCustKeyByKeyId(123L)).thenReturn(key);

        try (MockedConstruction<AlgoUtil> mocked = Mockito.mockConstruction(AlgoUtil.class, (mock, context) -> {
            when(mock.getDigestName(anyInt())).thenReturn("SHA-256");
            when(mock.getPublicKeyCipherName(anyInt())).thenReturn("RSA");
        })) {
            // Use ReflectionTestUtils to access the private method
            PGPOnePassSignature result = (PGPOnePassSignature) ReflectionTestUtils.invokeMethod(service, "verifyOnePassSignatureList", request, opsList);

            // Asserts and verifies
            assertNotNull(result);
            verify(opsList).get(0);
            verify(ops).getKeyID();
            verify(keyChainHandler).getCustKeyByKeyId(123L);
            // Add more assertions and verifications as needed
        }
    }
}
