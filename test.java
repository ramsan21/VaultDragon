// Vault.java
import java.util.Objects;

public class Vault {
    private String id;
    private String name;
    private String owner;
    private String location;
    private boolean isEncrypted;
    private String encryptionAlgorithm;
    private int capacity;
    private int usedSpace;

    // Default constructor
    public Vault() {
    }

    // Parameterized constructor
    public Vault(String id, String name, String owner, String location, boolean isEncrypted, String encryptionAlgorithm, int capacity, int usedSpace) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.location = location;
        this.isEncrypted = isEncrypted;
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.capacity = capacity;
        this.usedSpace = usedSpace;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(int usedSpace) {
        this.usedSpace = usedSpace;
    }

    // Additional methods
    public boolean isFull() {
        return usedSpace >= capacity;
    }

    public void addData(int dataSize) {
        if (usedSpace + dataSize > capacity) {
            throw new IllegalStateException("Vault is full!");
        }
        usedSpace += dataSize;
    }

    public void clearVault() {
        usedSpace = 0;
    }

    // Equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vault vault = (Vault) o;
        return isEncrypted == vault.isEncrypted &&
                capacity == vault.capacity &&
                usedSpace == vault.usedSpace &&
                Objects.equals(id, vault.id) &&
                Objects.equals(name, vault.name) &&
                Objects.equals(owner, vault.owner) &&
                Objects.equals(location, vault.location) &&
                Objects.equals(encryptionAlgorithm, vault.encryptionAlgorithm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, owner, location, isEncrypted, encryptionAlgorithm, capacity, usedSpace);
    }

    @Override
    public String toString() {
        return "Vault{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", location='" + location + '\'' +
                ", isEncrypted=" + isEncrypted +
                ", encryptionAlgorithm='" + encryptionAlgorithm + '\'' +
                ", capacity=" + capacity +
                ", usedSpace=" + usedSpace +
                '}';
    }
}


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VaultTest {

    @Test
    public void testDefaultConstructor() {
        Vault vault = new Vault();
        assertNotNull(vault);
    }

    @Test
    public void testParameterizedConstructor() {
        Vault vault = new Vault("1", "Vault A", "Owner1", "Location1", true, "AES", 100, 50);
        assertEquals("1", vault.getId());
        assertEquals("Vault A", vault.getName());
        assertEquals("Owner1", vault.getOwner());
        assertEquals("Location1", vault.getLocation());
        assertTrue(vault.isEncrypted());
        assertEquals("AES", vault.getEncryptionAlgorithm());
        assertEquals(100, vault.getCapacity());
        assertEquals(50, vault.getUsedSpace());
    }

    @Test
    public void testGettersAndSetters() {
        Vault vault = new Vault();
        vault.setId("2");
        vault.setName("Vault B");
        vault.setOwner("Owner2");
        vault.setLocation("Location2");
        vault.setEncrypted(false);
        vault.setEncryptionAlgorithm("RSA");
        vault.setCapacity(200);
        vault.setUsedSpace(100);

        assertEquals("2", vault.getId());
        assertEquals("Vault B", vault.getName());
        assertEquals("Owner2", vault.getOwner());
        assertEquals("Location2", vault.getLocation());
        assertFalse(vault.isEncrypted());
        assertEquals("RSA", vault.getEncryptionAlgorithm());
        assertEquals(200, vault.getCapacity());
        assertEquals(100, vault.getUsedSpace());
    }

    @Test
    public void testIsFull() {
        Vault vault = new Vault("3", "Vault C", "Owner3", "Location3", true, "AES", 100, 100);
        assertTrue(vault.isFull());
    }

    @Test
    public void testAddData() {
        Vault vault = new Vault("4", "Vault D", "Owner4", "Location4", false, "AES", 100, 90);
        vault.addData(5);
        assertEquals(95, vault.getUsedSpace());
    }

    @Test
    public void testAddDataThrowsException() {
        Vault vault = new Vault("5", "Vault E", "Owner5", "Location5", false, "AES", 100, 95);
        assertThrows(IllegalStateException.class, () -> vault.addData(10));
    }

    @Test
    public void testClearVault() {
        Vault vault = new Vault("6", "Vault F", "Owner6", "Location6", false, "AES", 100, 50);
        vault.clearVault();
        assertEquals(0, vault.getUsedSpace());
    }

    @Test
    public void testEqualsAndHashCode() {
        Vault vault1 = new Vault("7", "Vault G", "Owner7", "Location7", true, "AES", 150, 75);
        Vault vault2 = new Vault("7", "Vault G", "Owner7", "Location7", true, "AES", 150, 75);

        assertEquals(vault1, vault2);
        assertEquals(vault1.hashCode(), vault2.hashCode());
    }

    @Test
    public void testToString() {
        Vault vault = new Vault("8", "Vault H", "Owner8", "Location8", true, "AES", 200, 100);
        String expected = "Vault{id='8', name='Vault H', owner='Owner8', location='Location8', isEncrypted=true, encryptionAlgorithm='AES', capacity=200, usedSpace=100}";
        assertEquals(expected, vault.toString());
    }
}
