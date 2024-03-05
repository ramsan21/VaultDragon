import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class YourServiceTest {

    @Test
    void testAddCustomerKeys() throws Exception {
        // Assuming CustomerKeyRequest and MessageResponse are your classes
        CustomerKeyRequest request = new CustomerKeyRequest();
        request.setPublickeyFilePath("/path/to/public/key");

        // Mock dependencies
        try (MockedConstruction<FileInputStream> mockedFileInputStream = Mockito.mockConstruction(FileInputStream.class, (mock, context) -> {
            // Mock constructor behavior if needed
        });
             MockedConstruction<PGPPublicKeyRingCollection> mockedPGPPublicKeyRingCollection = Mockito.mockConstruction(PGPPublicKeyRingCollection.class);
             MockedConstruction<File> mockedFile = Mockito.mockConstruction(File.class)) {

            // Mock the static utility methods if they're involved (for demonstration, let's assume they're static methods in a Utility class)
            // For example, Mockito.mockStatic(Utility.class), then inside: utilityMock.when(() -> Utility.method()).thenReturn(value);

            // Mock your service dependencies if any, using @Mock or Mockito.mock()
            CustomerKeyService customerKeyService = Mockito.mock(CustomerKeyService.class);
            BankKeyService bankKeyService = Mockito.mock(BankKeyService.class);

            // Assuming enrich method does something with these services that can be verified
            // Example of setting expectations
            when(customerKeyService.customerKeyBuilder()).thenReturn(new CustomerKey.CustomerKeyBuilder());
            when(bankKeyService.bankKeyBuilder()).thenReturn(new BankKey.BankKeyBuilder());

            YourService service = new YourService(); // Replace with actual service class that contains addCustomerKeys
            // Inject mocks into your service if needed
            // service.setCustomerKeyService(customerKeyService);
            // service.setBankKeyService(bankKeyService);

            // Execute the method to be tested
            MessageResponse response = service.addCustomerKeys(request);

            // Verify the outcome
            assertEquals(StatusCode.SUCCESS.getCode(), response.getStatusCode());
            assertEquals("Customer Key Inserted Successfully.", response.getSuccessMessage());

            // Verify interactions with the mocked objects
            verify(customerKeyService, times(1)).saveCurrentCustomerKey();
            // Add more verify() calls as needed to ensure the correct methods were called with expected parameters
        }
    }
}
