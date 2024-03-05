<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

    @DataJpaTest
public class BankKeyRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BankKeyRepository repository;

    @Test
    public void testSaveAndFindById() {
        // Create a new BankKey entity
        BankKey newBankKey = new BankKey();
        newBankKey.setUser("testUser");
        newBankKey.setGroupId("testGroup");
        newBankKey.setKeyId("testKey");
        newBankKey.setPublicKeyData(new byte[]{1, 2, 3});
        newBankKey.setPrivatekey("privateKey");
        newBankKey.setExpiryDate(new Date());
        newBankKey.setCreatedon(new Timestamp(System.currentTimeMillis()));

        // Persist the entity
        newBankKey = entityManager.persistFlushFind(newBankKey);

        // Retrieve the entity using the repository
        Optional<BankKey> foundBankKey = repository.findById(newBankKey.getId());

        // Assert the found entity is not null and equals the original entity
        assertTrue(foundBankKey.isPresent());
        assertEquals(newBankKey.getUser(), foundBankKey.get().getUser());
        // Continue assertions for other fields...
    }
}
