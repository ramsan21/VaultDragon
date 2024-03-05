import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.lang.reflect.Constructor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest // If your service is a Spring component
class YourServiceTest {

    @Autowired
    private YourService service; // Your service should be a @Service annotated class

    @MockBean
    private CustomerKeyService customerKeyService;

    @MockBean
    private BankKeyService bankKeyService;

    @Test
    void testAddCustomerKeys() throws Exception {
        CustomerKeyRequest request = new CustomerKeyRequest();
        request.setPublickeyFilePath("/path/to/public/key");

        // Use reflection to instantiate BankKey.BankKeyBuilder
        Constructor<BankKey.BankKeyBuilder> constructor = BankKey.BankKeyBuilder.class.getDeclaredConstructor();
        constructor.setAccessible(true); // Make the private constructor accessible
        BankKey.BankKeyBuilder bankKeyBuilder = constructor.newInstance();

        Constructor<CustomerKey.CustomerKeyBuilder> customerKeyConstructor = CustomerKey.CustomerKeyBuilder.class.getDeclaredConstructor();
        customerKeyConstructor.setAccessible(true);
        CustomerKey.CustomerKeyBuilder customerKeyBuilder = customerKeyConstructor.newInstance();

        // Setting expectations
        when(bankKeyService.bankKeyBuilder()).thenReturn(bankKeyBuilder);
        when(customerKeyService.customerKeyBuilder()).thenReturn(customerKeyBuilder);

        // Execute the method to be tested
        MessageResponse response = service.addCustomerKeys(request);

        // Verify the outcome
        assertEquals(StatusCode.SUCCESS.getCode(), response.getStatusCode());
        assertEquals("Customer Key Inserted Successfully.", response.getSuccessMessage());

        // Verify interactions
        verify(customerKeyService, times(1)).saveCurrentCustomerKey();
        // Add more verify() calls as needed

        // Cleanup: Reset accessibility if desired
        constructor.setAccessible(false);
        customerKeyConstructor.setAccessible(false);
    }
}
