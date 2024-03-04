import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YourClassTest {

    @Test
    void testAddCustomerKeys_Success() throws Exception {
        try (MockedConstruction<File> mockedFile = mockConstruction(File.class)) {
            // Mocking the behavior of File
            when(mockedFile.construct(any(), any())).thenReturn(mock(File.class));

            // Mocking the behavior of FileInputStream
            try (MockedConstruction<FileInputStream> mockedInputStream = mockConstruction(FileInputStream.class)) {
                // Mocking the behavior of PGPUtil
                try (MockedConstruction<PGPUtil> mockedPGPUtil = mockConstruction(PGPUtil.class)) {
                    // Mocking the behavior of PGPPublicKeyRingCollection
                    try (MockedConstruction<PGPPublicKeyRingCollection> mockedPGPPublicKeyRingCollection =
                                 mockConstruction(PGPPublicKeyRingCollection.class)) {

                        // Mocking the behavior of customerKeyService and bankKeyService
                        CustomerKeyService customerKeyService = mock(CustomerKeyService.class);
                        BankKeyService bankKeyService = mock(BankKeyService.class);

                        // Create an instance of YourClass with the mocked dependencies
                        YourClass yourClass = new YourClass(customerKeyService, bankKeyService);

                        // Execute the method
                        MessageResponse response = yourClass.addCustomerKeys(mock(CustomerKeyRequest.class));

                        // Verify that the expected methods are called
                        verify(customerKeyService, times(1)).customerKeyBuilder();
                        verify(customerKeyService, times(1)).saveCurrentCustomerKey();

                        // Assert the response
                        assertEquals(StatusCode.SUCCESS.getCode(), response.getStatusCode());
                        assertEquals("Customer Key Inserted Successfully.", response.getSuccessMessage());
                    }
                }
            }
        }
    }

    // Additional tests for other scenarios...

}
