import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class KeyChainHandlerTest {

    @Test
    public void testLogUserIdCheck_UserIdIsBlank() throws Exception {
        // Arrange
        KeyChainHandler keyChainHandler = mock(KeyChainHandler.class);
        String groupId = "group1";
        String inputUser = "testUser";
        long keyId = 123456L;

        // Mock behavior
        when(keyChainHandler.getCustUserByKeyId(keyId)).thenReturn("");

        // Act
        keyChainHandler.logUserIdCheck(groupId, inputUser, keyId);

        // Assert
        // Verify that the method logged the correct message for subkey
        verify(keyChainHandler).log.info("The message is signed by subkey");
    }

    @Test
    public void testLogUserIdCheck_UserIdMatchesInputUser() throws Exception {
        // Arrange
        KeyChainHandler keyChainHandler = mock(KeyChainHandler.class);
        String groupId = "group1";
        String inputUser = "testUser";
        long keyId = 123456L;
        String userId = "testUser";

        // Mock behavior
        when(keyChainHandler.getCustUserByKeyId(keyId)).thenReturn(userId);

        // Act
        keyChainHandler.logUserIdCheck(groupId, inputUser, keyId);

        // Assert
        // Verify that the method logged the successful validation message
        verify(keyChainHandler).log.info("The Key ID Validation is Successful for GroupID = group1 & KeyID = 123456 = testUser");
    }

    @Test
    public void testLogUserIdCheck_UserIdDoesNotMatchInputUser() throws Exception {
        // Arrange
        KeyChainHandler keyChainHandler = mock(KeyChainHandler.class);
        String groupId = "group1";
        String inputUser = "testUser";
        long keyId = 123456L;
        String userId = "anotherUser";

        // Mock behavior
        when(keyChainHandler.getCustUserByKeyId(keyId)).thenReturn(userId);

        // Act
        keyChainHandler.logUserIdCheck(groupId, inputUser, keyId);

        // Assert
        // Verify that the method logged the failed validation message
        verify(keyChainHandler).log.info("The Key ID Validation is Failed for GroupID = group1 & KeyID = testUser used KeyID is = anotherUser");
    }

    @Test
    public void testLogUserIdCheck_SubkeySigned() throws Exception {
        // Arrange
        KeyChainHandler keyChainHandler = mock(KeyChainHandler.class);
        String groupId = "group1";
        String inputUser = "testUser";
        long keyId = 123456L;

        // Mock behavior
        when(keyChainHandler.getCustUserByKeyId(keyId)).thenReturn(null); // simulating subkey signing

        // Act
        keyChainHandler.logUserIdCheck(groupId, inputUser, keyId);

        // Assert
        // Verify that the method logged the message for subkey signing
        verify(keyChainHandler).log.info("The message is signed by subkey");
    }
}
