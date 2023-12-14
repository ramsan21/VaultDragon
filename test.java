import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.api.mockito.PowerMockito;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@PrepareForTest({ImportPublicKeyFileCommand.class, BufferedReader.class})
public class ImportPublicKeyFileCommandTest {

    @Mock
    private wtValidationKeyRepository jwtValidationKeyRepository;

    @InjectMocks
    private ImportPublicKeyFileCommand importPublicKeyFileCommand;

    @Mock
    private BufferedReader bufferedReader;

    @BeforeEach
    public void setUp() throws IOException {
        // Mock static method for BufferedReader instantiation
        PowerMockito.mockStatic(BufferedReader.class);
        when(BufferedReader.class.getResourceAsStream(any(String.class))).thenReturn(null);
        when(BufferedReader.class.getResourceAsStream(any(String.class))).thenReturn(null);

        // Mock constructor of FileReader
        PowerMockito.whenNew(FileReader.class).withAnyArguments().thenReturn(new FileReader("dummyFile"));

        // Set up a mock BufferedReader for FileReader
        when(bufferedReader.readLine()).thenReturn("-----BEGIN PUBLIC KEY-----", "public_key_content", "-----END PUBLIC KEY-----", null);
    }

    @Test
    public void testImportPublicKey_Success() {
        // Mock repository to return an empty Optional, indicating the issuer doesn't exist
        when(jwtValidationKeyRepository.findByIssuer(anyString())).thenReturn(Optional.empty());

        Map<String, String> args = new HashMap<>();
        args.put("issuer", "testIssuer");
        args.put("fileName", "testFile");
        args.put("override", "true");

        Map<String, String> result = importPublicKeyFileCommand.with(args);

        assertEquals("success", result.get("output"));
        // Add any additional verifications as needed
    }

    // Add more test cases for different scenarios (e.g., existing issuer without override, invalid parameters, etc.)
}
