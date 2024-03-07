@Test
    public void testUpdateBankPrivateKey() throws IOException {
        InputRequest ir = mock(InputRequest.class);
        BankKeyService bankKeyService = mock(BankKeyService.class);
        Util util = mock(Util.class); // Assuming Util is a utility class for file path operations

        when(ir.getInput()).thenReturn("testPath");
        when(ir.getPrefix()).thenReturn("testPrefix");

        Path mockPath = mock(Path.class);
        when(util.getFilePath("testPath")).thenReturn(mockPath);

        // Mock DirectoryStream and its iterator
        DirectoryStream<Path> directoryStream = mock(DirectoryStream.class);
        Iterator<Path> iterator = Collections.singleton(Paths.get("testPrefix_user")).iterator();
        
        when(directoryStream.iterator()).thenReturn(iterator);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.newDirectoryStream(any(Path.class))).thenReturn(directoryStream);
            mockedFiles.when(() -> Files.readString(any(Path.class))).thenReturn("privateKeyContents");

            // Mocking static methods of Files
            YourServiceClass service = new YourServiceClass();
            ReflectionTestUtils.setField(service, "bankKeyService", bankKeyService);
            ReflectionTestUtils.setField(service, "util", util);

            MessageResponse response = service.updateBankPrivateKey(ir);

            assertEquals(StatusCode.SUCCESS.getCode(), response.getStatusCode());
            assertEquals("Private key update Successfully.", response.getSuccessMessage());
            verify(bankKeyService).updatePrivateKey("user", "privateKeyContents");
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
