import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class BankPrivateKeyUpdateServiceTest {

    @Test
    public void testUpdateBankPrivateKey() {
        // Mock the dependencies
        InputRequest ir = mock(InputRequest.class);
        BankKeyService bankKeyService = mock(BankKeyService.class);
        Util util = mock(Util.class); // Assuming Util is a dependency used to getFilePath
        when(ir.getInput()).thenReturn("testPath");
        when(ir.getPrefix()).thenReturn("testPrefix");

        // Mock the util.getFilePath to return a mock Path that simulates DirectoryStream
        Path mockPath = mock(Path.class);
        when(util.getFilePath("testPath")).thenReturn(mockPath);

        // Set up DirectoryStream to simulate file reading
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
             MockedStatic<DirectoryStream> mockedStream = mockStatic(DirectoryStream.class)) {

            DirectoryStream<Path> directoryStream = mock(DirectoryStream.class);
            Path entryPath = Paths.get("testPrefix_user");
            Stream<Path> pathStream = Stream.of(entryPath);
            mockedStream.when(() -> Files.newDirectoryStream(mockPath)).thenReturn(directoryStream);
            when(directoryStream.spliterator()).thenReturn(pathStream.spliterator());

            // Mock Files.readString to return a private key
            mockedFiles.when(() -> Files.readString(any(Path.class))).thenReturn("privateKeyContents");

            // Create an instance of your service class
            YourServiceClass service = new YourServiceClass();
            // Set mock dependencies using ReflectionTestUtils
            ReflectionTestUtils.setField(service, "bankKeyService", bankKeyService);
            ReflectionTestUtils.setField(service, "util", util);

            // Call the public method
            MessageResponse response = service.updateBankPrivateKey(ir);

            // Verify
            assertEquals(StatusCode.SUCCESS.getCode(), response.getStatusCode());
            assertEquals("Private key update Successfully.", response.getSuccessMessage());
            verify(bankKeyService).updatePrivateKey("user", "privateKeyContents");
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }
}


import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class YourClassTest {

    @Test
    void testPgpExpiryDate_NeverExpires() {
        PGPPublicKey publicKey = Mockito.mock(PGPPublicKey.class);
        Mockito.when(publicKey.getValidSeconds()).thenReturn(0L);

        Date result = YourClass.pgpExpiryDate(publicKey);
        assertNull(result);
    }

    @Test
    void testPgpExpiryDate_WithExpiry() {
        PGPPublicKey publicKey = Mockito.mock(PGPPublicKey.class);
        // Simulate a key created now with a validity of 1 day (86400 seconds)
        Mockito.when(publicKey.getCreationTime()).thenReturn(new Date());
        Mockito.when(publicKey.getValidSeconds()).thenReturn(86400L);

        Date result = YourClass.pgpExpiryDate(publicKey);
        // Expect the expiry date to be approximately one day from now. Adjusting for potential milliseconds difference in execution
        long expectedExpiryMillis = System.currentTimeMillis() + 86400L * 1000;
        assertEquals(expectedExpiryMillis, result.getTime(), 1000);
    }
}
import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YourClassTest {

    @Test
    void testGetUserIDsAsString() {
        PGPPublicKey publicKey = Mockito.mock(PGPPublicKey.class);
        Iterator<String> userIDsIterator = Arrays.asList("user1@example.com", "user2@example.com").iterator();
        Mockito.when(publicKey.getUserIDs()).thenReturn(userIDsIterator);

        String result = YourClass.getUserIDsAsString(publicKey);
        assertEquals("user1@example.com, user2@example.com", result);
    }
}
