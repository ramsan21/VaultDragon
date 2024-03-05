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

@SpringBootTest
class YourServiceTest {

    @Autowired
    private YourService yourService; // Inject your main service where addCustomerKeys method is

    @MockBean
    private KeyService keyService; // Mock the correct KeyService

    @Test
    void testAddCustomerKeys() throws Exception {
        CustomerKeyRequest request = new CustomerKeyRequest();
        request.setPublickeyFilePath("/path/to/public/key");

        // Assume BankKey.BankKeyBuilder and CustomerKey.CustomerKeyBuilder have private constructors
        // Use reflection if necessary to create instances of these builders
        Constructor<BankKey.BankKeyBuilder> bankKeyBuilderConstructor = BankKey.BankKeyBuilder.class.getDeclaredConstructor();
        bankKeyBuilderConstructor.setAccessible(true);
        BankKey.BankKeyBuilder bankKeyBuilder = bankKeyBuilderConstructor.newInstance();

        Constructor<CustomerKey.CustomerKeyBuilder> customerKeyBuilderConstructor = CustomerKey.CustomerKeyBuilder.class.getDeclaredConstructor();
        customerKeyBuilderConstructor.setAccessible(true);
        CustomerKey.CustomerKeyBuilder customerKeyBuilder = customerKeyBuilderConstructor.newInstance();

        // Mock the behavior of keyService to return the builders
        when(keyService.bankKeyBuilder()).thenReturn(bankKeyBuilder);
        when(keyService.customerKeyBuilder()).thenReturn(customerKeyBuilder);

        // Call the method under test
        MessageResponse response = yourService.addCustomerKeys(request);

        // Asserts and verifies
        assertEquals(StatusCode.SUCCESS.getCode(), response.getStatusCode());
        assertEquals("Customer Key Inserted Successfully.", response.getSuccessMessage());

        // Verify that keyService methods were called
        verify(keyService, times(1)).saveCurrentCustomerKey();
        // Verify other interactions as necessary

        // Reset the constructor accessibility if desired
        bankKeyBuilderConstructor.setAccessible(false);
        customerKeyBuilderConstructor.setAccessible(false);
    }
}
