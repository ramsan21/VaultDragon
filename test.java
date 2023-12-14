import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

        // Mock behavior for jwtValidationKeyRepository
        when(jwtValidationKeyRepository.findByIssuer("testIssuer")).thenReturn(Optional.empty());

        // Mock BufferedReader behavior
        BufferedReader mockBufferedReader = new BufferedReader(/* your mock setup */);
        doReturn(mockBufferedReader).when(importPublicKeyFileCommand).getBufferedReader("testFile");

        Map<String, String> result = importPublicKeyFileCommand.with(args);

        assertEquals("success", result.get("output"));
    }

    // Add more test cases for different scenarios
}
