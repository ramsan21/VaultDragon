import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class BankKeyServiceTest {

    @Mock
    private BankKeyRepository repository;

    @InjectMocks
    private BankKeyService bankKeyService;

    @Test
    public void testSaveCurrentBankKey() {
        BankKey.BankKeyBuilder bankKeyBuilderMock = Mockito.mock(BankKey.BankKeyBuilder.class);
        when(bankKeyService.bankKeyBuilder()).thenReturn(bankKeyBuilderMock);

        bankKeyService.saveCurrentBankkey();

        verify(repository, times(1)).save(any(BankKey.class));
        verify(bankKeyService, times(1)).clearCurrentBankKey();
    }

    @Test
    public void testClearCurrentBankKey() {
        bankKeyService.clearCurrentBankKey();

        // Add assertions if needed
    }

    @Test
    public void testBankKeyBuilderWithNonNullThreadLocal() {
        BankKey.BankKeyBuilder expectedBuilder = BankKey.builder().createdon(Timestamp.from(Instant.now()));
        bankKeyService.bankKeyThreadLocal.set(expectedBuilder);

        BankKey.BankKeyBuilder actualBuilder = bankKeyService.bankKeyBuilder();

        assertEquals(expectedBuilder, actualBuilder);
    }

    @Test
    public void testBankKeyBuilderWithNullThreadLocal() {
        bankKeyService.bankKeyThreadLocal.set(null);

        BankKey.BankKeyBuilder actualBuilder = bankKeyService.bankKeyBuilder();

        // Add assertions if needed
    }

    @Test
    public void testGetBankkey() {
        String user = "testUser";
        List<BankKey> expectedBankKeys = Arrays.asList(new BankKey(), new BankKey());

        when(repository.findyUser(user)).thenReturn(expectedBankKeys);

        List<BankKey> actualBankKeys = bankKeyService.getBankkey(user);

        assertEquals(expectedBankKeys, actualBankKeys);
    }

    @Test
    public void testGetBankKeyByKeyId() {
        String keyId = "testKeyId";
        List<BankKey> expectedBankKeys = Arrays.asList(new BankKey(), new BankKey());

        when(repository.findBykeyId(keyId)).thenReturn(expectedBankKeys);

        List<BankKey> actualBankKeys = bankKeyService.getBankKeyByKeyId(keyId);

        assertEquals(expectedBankKeys, actualBankKeys);
    }
}
