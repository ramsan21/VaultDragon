import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportPublicKeyFileCommandTest {

    @Mock
    private wtValidationKeyRepository jwtValidationKeyRepository;

    @InjectMocks
    private ImportPublicKeyFileCommand importPublicKeyFileCommand;

    @BeforeEach
    void setUp() throws IOException {
        // Mock BufferedReader behavior
        BufferedReader bufferedReader = new BufferedReader(new StringReader("-----BEGIN PUBLIC KEY-----\npublic_key_content\n-----END PUBLIC KEY-----"));
        when(jwtValidationKeyRepository.findByIssuer(anyString())).thenReturn(Optional.empty());
        when(importPublicKeyFileCommand.createBufferedReader(anyString())).thenReturn(bufferedReader);
    }

    @Test
    void testImportPublicKey_Success() {
        Map<String, String> args = new HashMap<>();
        args.put("issuer", "testIssuer");
        args.put("fileName", "testFile");
        args.put("override", "true");

        Map<String, String> result = importPublicKeyFileCommand.with(args);

        assertEquals("success", result.get("output"));
    }

    // Add more test cases for different scenarios
}
