import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

class ImportPublicKeyFileCommandTest {

    @Mock
    private wtValidationKeyRepository jwtValidationKeyRepository;

    @InjectMocks
    private ImportPublicKeyFileCommand importPublicKeyFileCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testImportPublicKey_Success() throws IOException {
        Map<String, String> args = new HashMap<>();
        args.put("issuer", "testIssuer");
        args.put("fileName", "testFile");
        args.put("override", "true");

        // Mock behavior for jwtValidationKeyRepository using doReturn
        doReturn(Optional.empty()).when(jwtValidationKeyRepository).findByIssuer("testIssuer");

        // Mock BufferedReader behavior
        BufferedReader mockBufferedReader = createMockBufferedReader("-----BEGIN PUBLIC KEY-----\npublic_key_content\n-----END PUBLIC KEY-----");
        doReturn(mockBufferedReader).when(importPublicKeyFileCommand).getBufferedReader("testFile");

        Map<String, String> result = importPublicKeyFileCommand.with(args);

        assertEquals("success", result.get("output"));
    }

    private BufferedReader createMockBufferedReader(String content) throws IOException {
        BufferedReader mockBufferedReader = mock(BufferedReader.class);
        when(mockBufferedReader.readLine()).thenReturn(content, null);
        return mockBufferedReader;
    }

    // Add more test cases for different scenarios
}
