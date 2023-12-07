import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.nio.charset.CharacterCodingException;
import org.junit.Test;

public class YourClassTest {

    @Test
    public void testGetBytes() {
        String inputString = "Hello, World!";
        byte[] expectedBytes;

        try {
            // Calculate the expected byte array using a reliable method
            expectedBytes = inputString.getBytes("UTF-8");
        } catch (Exception e) {
            // If the standard method fails, the test case should fail as well
            fail("Unexpected exception while getting expected bytes: " + e.getMessage());
            return;
        }

        try {
            // Call the method you want to test
            byte[] resultBytes = YourClass.getBytes(inputString);

            // Assert that the result matches the expected value
            assertArrayEquals(expectedBytes, resultBytes);
        } catch (IllegalArgumentException e) {
            // If the method throws IllegalArgumentException, fail the test
            fail("IllegalArgumentException not expected: " + e.getMessage());
        }
    }

    @Test
    public void testGetBytesWithException() {
        String inputString = "Invalid \uD83D\uDE00 Emoji"; // This emoji may cause CharacterCodingException

        try {
            // Call the method you want to test, expecting an exception
            YourClass.getBytes(inputString);

            // If no exception is thrown, fail the test
            fail("Expected IllegalArgumentException, but no exception was thrown");
        } catch (IllegalArgumentException e) {
            // Assert that the exception message contains the expected substring
            // This is just an example; you may adjust it based on your actual implementation
            assert(e.getMessage().contains("Encoding failed"));
            assert(e.getCause() instanceof CharacterCodingException);
        }
    }
}
