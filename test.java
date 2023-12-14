import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ImportPublicKeyFileCommandTest {

    @Mock
    private wtValidationKeyRepository jwtValidationKeyRepository;
    @Mock
    private Logger logger;

    @TempDir
    private File tempDir;
    private ImportPublicKeyFileCommand command;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        command = new ImportPublicKeyFileCommand();
        command.setJwtValidationKeyRepository(jwtValidationKeyRepository);
        command.setLogger(logger);
        command.setInFilePath(tempDir.getAbsolutePath());
    }

    @Test
    public void testWith_MissingParameters() {
        Map<String, String> args = new HashMap<>();
        Map<String, String> outputMap = command.with(args);

        assertEquals(StatusCode.STAR_invalid_command_parameter.getCodeAsString(), outputMap.get("statusCode"));
        assertEquals("error", outputMap.get("output"));
        verify(logger, times(2)).info("Invalid parameters provided");
    }

    @Test
    public void testWith_NonExistentFile() {
        Map<String, String> args = new HashMap<>();
        args.put("issuer", "test");
        args.put("fileName", "nonexistent.pub");
        Map<String, String> outputMap = command.with(args);

        assertEquals(StatusCode.STAR_file_not_found.getCodeAsString(), outputMap.get("statusCode"));
        assertEquals("error", outputMap.get("output"));
        verify(logger).error(any(String.class), any(FileNotFoundException.class));
    }

    @Test
    public void testWith_IOException() throws IOException {
        File publicKeyFile = tempDir.newFile("error.pub");
        publicKeyFile.deleteOnExit();
        publicKeyFile.setWritable(false);
        Map<String, String> args = new HashMap<>();
        args.put("issuer", "test");
        args.put("fileName", publicKeyFile.getName());
        Map<String, String> outputMap = command.with(args);

        assertEquals(StatusCode.STAR_file_error.getCodeAsString(), outputMap.get("statusCode"));
        assertEquals("error", outputMap.get("output"));
        verify(logger).error(any(String.class), any(IOException.class));
    }

    @Test
    public void testWith_InvalidPublicKey() throws IOException {
        File publicKeyFile = createTempFile("invalid_key.pub", "invalid public key content");
        Map<String, String> args = new HashMap<>();
        args.put("issuer", "test");
        args.put("fileName", publicKeyFile.getName());
        Map<String, String> outputMap = command.with(args);

        assertEquals(StatusCode.STAR_invalid_command_parameter.getCodeAsString(), outputMap.get("statusCode"));
        assertEquals("error", outputMap.get("output"));
        verify(logger).info("Invalid parameters provided");
    }

    @Test
    public void testWith_ExistingKey_NoOverride() throws IOException {
        File publicKeyFile = createTempFile("existing_key.pub", "valid public key content");
        doReturn(Optional.of(new JwtValidationKey())).when(jwtValidationKeyRepository).findByIssuer("test");
        Map<String, String> args = new HashMap<>();
        args.put("issuer", "test");
        args.put("fileName", publicKeyFile.getName());
        Map<String, String> outputMap = command.with(args);

        assertEquals(StatusCode
                     }
            ]
