import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImportPublicKeyFileCommandTest {

    @Mock
    private wtValidationKeyRepository jwtValidationKeyRepository;

    @InjectMocks
    private ImportPublicKeyFileCommand importPublicKeyFileCommand;

    @Mock
    private BufferedReader bufferedReader;

    @BeforeEach
    public void setUp() throws FileNotFoundException {
        // Set up a mock BufferedReader for FileReader
        when(bufferedReader.readLine()).thenReturn("-----BEGIN PUBLIC KEY-----", "public_key_content", "-----END PUBLIC KEY-----", null);
        when(new BufferedReader(any(FileReader.class))).thenReturn(bufferedReader);
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
        verify(jwtValidationKeyRepository, times(1)).save(any());
    }

    @Test
    public void testImportPublicKey_IssuerExists_Override() {
        // Mock repository to return an existing JwtValidationKey
        JwtValidationKey existingKey = new JwtValidationKey();
        existingKey.setIssuer("testIssuer");
        existingKey.setPublicKey("existingPublicKey");
        when(jwtValidationKeyRepository.findByIssuer(anyString())).thenReturn(Optional.of(existingKey));

        Map<String, String> args = new HashMap<>();
        args.put("issuer", "testIssuer");
        args.put("fileName", "testFile");
        args.put("override", "true");

        Map<String, String> result = importPublicKeyFileCommand.with(args);

        assertEquals("Success updating to Database.", result.get("output"));
        verify(jwtValidationKeyRepository, times(1)).save(any());
    }

    // Add more test cases for different scenarios (e.g., existing issuer without override, invalid parameters, etc.)
}
