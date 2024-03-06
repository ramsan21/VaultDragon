import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class VerifySignatureListTest {

    @Autowired
    private YourServiceClass service; // Replace YourServiceClass with the actual class name

    @Test
    public void testVerifySignatureList() throws Exception {
        DecryptVerifyRequest request = mock(DecryptVerifyRequest.class);
        PGPSignatureList pgpSignatureList = mock(PGPSignatureList.class);
        PGPSignature pgpSignature = mock(PGPSignature.class);
        PGPOnePassSignature ops = mock(PGPOnePassSignature.class);
        PGPPublicKey key = mock(PGPPublicKey.class);
        KeyChainHandler keyChainHandler = mock(KeyChainHandler.class);

        // Setup mock behavior
        when(pgpSignatureList.get(0)).thenReturn(pgpSignature);
        when(pgpSignature.getKeyID()).thenReturn(123L);
        when(ops.getKeyID()).thenReturn(123L); // Adjust as necessary to match your logic
        when(service.keyChainHandler).thenReturn(keyChainHandler); // Assume service has a field keyChainHandler
        when(keyChainHandler.getCustKeyByKeyId(anyLong())).thenReturn(key);

        // Use Mockito to mock construction of AlgoUtil objects
        try (MockedConstruction<AlgoUtil> mocked = Mockito.mockConstruction(AlgoUtil.class, (mock, context) -> {
            when(mock.getDigestName(anyInt())).thenReturn("SHA-256");
            when(mock.getPublicKeyCipherName(anyInt())).thenReturn("RSA");
        })) {
            // Use reflection to invoke the private method
            PGPSignature result = (PGPSignature) ReflectionTestUtils.invokeMethod(service, "verifySignatureList", request, pgpSignatureList, ops);

            // Assertions to verify the behavior and interactions
            assertNotNull(result);
            verify(pgpSignatureList).get(0);
            verify(keyChainHandler).getCustKeyByKeyId(123L);
            // Add more verifications and assertions as needed
        }
    }
}
