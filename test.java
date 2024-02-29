import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class CalculatorTest {

    @Test
    public void testGetCustEncKey_Success() throws ValidationException, IOException {
        // Arrange
        KeyService keyService = Mockito.mock(KeyService.class);
        Calculator calculator = new Calculator(keyService);

        List<CustomerKey> customerKeys = new ArrayList<>();
        CustomerKey customerKey = new CustomerKey();
        customerKey.setUser("testUser");
        customerKey.setPublicKeyData(new byte[]{/* your public key data here */});
        customerKeys.add(customerKey);

        when(keyService.customerKeyByUser("testUser")).thenReturn(customerKeys);

        // Act
        PGPPublicKey result = calculator.getCustEncKey("testUser");

        // Assert
        // You may want to assert the actual PGPPublicKey instance or other conditions based on your requirements
        assertEquals(/* expected PGPPublicKey */, result);

        // Verify that the keyService.customerKeyByUser method was called with the correct argument
        verify(keyService, times(1)).customerKeyByUser("testUser");
    }

    @Test
    public void testGetCustEncKey_NoKeyFound() throws ValidationException, IOException {
        // Arrange
        KeyService keyService = Mockito.mock(KeyService.class);
        Calculator calculator = new Calculator(keyService);

        when(keyService.customerKeyByUser(anyString())).thenReturn(new ArrayList<>());

        // Act
        PGPPublicKey result = calculator.getCustEncKey("nonExistentUser");

        // Assert
        assertEquals(null, result);

        // Verify that the keyService.customerKeyByUser method was called with the correct argument
        verify(keyService, times(1)).customerKeyByUser("nonExistentUser");
    }

    // Similar tests can be written for edge cases, exceptions, etc.

    // You may also want to test the private method extractEncryptionKey using reflection or other techniques.
}
