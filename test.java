import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class YourClassTest {

    @Mock
    private CustomerKeyService customerKeyService;

    @Mock
    private BankKeyService bankKeyService;

    @InjectMocks
    private YourClass yourClass;

    @Test
    public void testAddCustomerKeys_Success() throws Exception {
        // Mocking the behavior of getPublicKeyFilePath in the request
        String publicKeyFilePath = "testPublicKeyFilePath";
        CustomerKeyRequest request = new CustomerKeyRequest();
        request.setPublicKeyFilePath(publicKeyFilePath);

        // Mocking the behavior of FileInputStream
        try (InputStream mockInputStream = mock(InputStream.class)) {
            when(new FileInputStream(publicKeyFilePath)).thenReturn(mockInputStream);

            // Mocking the behavior of PGPPublicKeyRingCollection
            PGPPublicKeyRingCollection mockPublicKeyRingCollection = mock(PGPPublicKeyRingCollection.class);
            when(new PGPPublicKeyRingCollection(any(), any())).thenReturn(mockPublicKeyRingCollection);

            // Mocking the behavior of PGPPublicKeyRing stream
            PGPPublicKeyRing mockPublicKeyRing = mock(PGPPublicKeyRing.class);
            when(mockPublicKeyRingCollection.getKeyRings()).thenReturn(Stream.of(mockPublicKeyRing));

            // Mocking the behavior of getUser
            when(yourClass.getUser(any())).thenReturn("testUser");

            // Execute the method
            yourClass.addCustomerKeys(request);

            // Verify that the expected methods are called
            verify(customerKeyService).customerKeyBuilder();
            verify(customerKeyService).saveCurrentCustomerKey();
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void testAddCustomerKeys_FileNotFound() throws Exception {
        // Mocking the behavior of getPublicKeyFilePath in the request
        String publicKeyFilePath = "nonExistentFilePath";
        CustomerKeyRequest request = new CustomerKeyRequest();
        request.setPublicKeyFilePath(publicKeyFilePath);

        // Execute the method, expect FileNotFoundException
        yourClass.addCustomerKeys(request);
    }

    // Additional tests for other scenarios...

}
