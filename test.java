import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.sql.Timestamp;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImportPublicKeyFileCommandTest {

    @Mock
    private JwtValidationKeyRepository jwtValidationKeyRepository;

    @Value("${starss.utility.inFilePath:/prd/starss/utility/starss-utility/in/}")
    private String inFilePath;

    @InjectMocks
    private ImportPublicKeyFileCommand importPublicKeyFileCommand;

    private Map<String, String> args;

    @BeforeEach
    void setUp() {
        args = new HashMap<>();
    }

    @Test
    void testWithInvalidParameters() {
        Map<String, String> result = importPublicKeyFileCommand.with(args);

        assertEquals(args, result);
    }

    @Test
    void testWithValidParametersAndFileReadError() throws IOException {
        args.put("issuer", "validIssuer");
        args.put("fileName", "nonexistentFile");

        Map<String, String> result = importPublicKeyFileCommand.with(args);

        // Verify that the correct log statements are made
        // You may want to use a logging library and mock it for testing
        // Verify log statements or expected output accordingly

        assertEquals(args, result);
    }

    @Test
    void testWithValidParametersAndOverrideTrue() throws IOException {
        args.put("issuer", "validIssuer");
        args.put("fileName", "validFileName");
        args.put("override", "true");

        // Mock the repository behavior
        when(jwtValidationKeyRepository.findByIssuer("validIssuer")).thenReturn(Optional.empty());

        Map<String, String> result = importPublicKeyFileCommand.with(args);

        // Verify that the correct log statements are made
        // Verify log statements or expected output accordingly

        verify(jwtValidationKeyRepository, times(1)).save(any());

        assertEquals(StatusCode.STAR_success.getCodeAsString(), result.get("statusCode"));
        assertEquals("success", result.get("output"));
    }

    @Test
    void testWithValidParametersAndOverrideFalse() throws IOException {
        args.put("issuer", "validIssuer");
        args.put("fileName", "validFileName");
        args.put("override", "false");

        // Mock the repository behavior
        when(jwtValidationKeyRepository.findByIssuer("validIssuer")).thenReturn(Optional.of(new JwtValidationKey()));

        Map<String, String> result = importPublicKeyFileCommand.with(args);

        // Verify that the correct log statements are made
        // Verify log statements or expected output accordingly

        assertEquals("exists", result.get("output"));
        assertEquals(StatusCode.STAR_entry_exist.getCodeAsString(), result.get("statusCode"));
    }

    // Add more tests to cover other scenarios and branches

    @Test
    void testGetParams() {
        assertArrayEquals(new String[]{"issuer", "fileName", "override"}, importPublicKeyFileCommand.getParams());
    }

    @Test
    void testGetHelpText() {
        assertEquals("Usage: IMPORT_PUBLIC_KEY_FILE [issuer] [fileName] [Optional:override(true false)]\n", importPublicKeyFileCommand.getHelpText());
    }
}
