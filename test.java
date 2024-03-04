import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YourClassTest {

    @Test
    void testAddCustomerKeys_Success() throws Exception {
        try (MockedConstruction<File> mockedFile = mockConstruction(File.class, (info, context) -> {
            if (info.getMethod().getName().equals("getAbsolutePath")) {
                // Mocking the behavior of getAbsolutePath
                context.returnValue("mockedAbsolutePath");
            } else {
                // Mocking the behavior of other methods if needed
                context.returnValue(mock(File.class));
            }
        })) {
            try (MockedConstruction<FileInputStream> mockedInputStream = mockConstruction(FileInputStream.class)) {
                try (MockedConstruction<PGPUtil> mockedPGPUtil = mockConstruction(PGPUtil.class)) {
                    try (MockedConstruction<PGPPublicKeyRingCollection> mockedPGPPublicKeyRingCollection =
                                 mockConstruction(PGPPublicKeyRingCollection.class)) {

                        CustomerKeyService customerKeyService = mock(CustomerKeyService.class);
                        BankKeyService bankKeyService = mock(BankKeyService.class);

                        YourClass yourClass = new YourClass(customerKeyService, bankKeyService);

                        MessageResponse response = yourClass.addCustomerKeys(mock(CustomerKeyRequest.class));

                        verify(customerKeyService, times(1)).customerKeyBuilder();
                        verify(customerKeyService, times(1)).saveCurrentCustomerKey();

                        assertEquals(StatusCode.SUCCESS.getCode(), response.getStatusCode());
                        assertEquals("Customer Key Inserted Successfully.", response.getSuccessMessage());
                    }
                }
            }
        }
    }

    // Additional tests for other scenarios...

}

@Test
    public void testAddCustomerKeys() throws Exception {
        // Mock external dependencies
        // Assuming you have appropriate mocks for these
        MockCustomerKeyService customerKeyService = Mockito.mock(MockCustomerKeyService.class);
        MockBankKeyService bankKeyService = Mockito.mock(MockBankKeyService.class);

        // Define expected behavior for File constructor
        Answer<File> fileAnswer = invocation -> {
            String path = (String) invocation.getArguments()[0];
            return new File("test_file.txt");  // Simulate a test file
        };

        try (MockedConstruction<File> mockedFile = Mockito.mockConstruction(File.class, fileAnswer)) {
            PGPPublicKeyRingCollection mockPgpPub = Mockito.mock(PGPPublicKeyRingCollection.class);
            PGPPublicKeyRing mockPgpPublicKey = Mockito.mock(PGPPublicKeyRing.class);
            Mockito.when(mockPgpPub.iterator()).thenReturn(Collections.singletonList(mockPgpPublicKey).iterator());

            // Set up mocked InputStream behavior (optional for more fine-grained control)
            // ...

            // Call the method under test
            YourClass yourClass = new YourClass(customerKeyService, bankKeyService);
            MessageResponse response = yourClass.addCustomerKeys(new CustomerKeyRequest("test_path"));

            // Assertions
            Mockito.verify(mockedFile).constructor(Mockito.eq("test_path"));
            Mockito.verify(customerKeyService).saveCurrentCustomerKey();
            // ... other verifications as needed

            // Assert the response
            Assert.assertEquals(StatusCode.SUCCESS.getCode(), response.getStatusCode());
            Assert.assertEquals("Customer Key Inserted Successfully.", response.getSuccessMessage());
        }
    }
