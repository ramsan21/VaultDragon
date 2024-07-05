import java.util.Properties;
import java.util.Set;

public class PropertiesCopy {
    public static void main(String[] args) {
        // Example properties
        Properties originalProperties = new Properties();
        originalProperties.setProperty("vault.url", "https://vault.example.com");
        originalProperties.setProperty("vault.token", "sometoken");
        originalProperties.setProperty("db.username", "user");
        originalProperties.setProperty("db.password", "password");

        // New properties object to hold the copied properties
        Properties vaultProperties = new Properties();

        // Prefix to look for
        String prefix = "vault.";

        // Copy properties with the specified prefix
        copyPropertiesWithPrefix(originalProperties, vaultProperties, prefix);

        // Print the copied properties
        vaultProperties.forEach((key, value) -> System.out.println(key + ": " + value));
    }

    private static void copyPropertiesWithPrefix(Properties source, Properties target, String prefix) {
        Set<String> propertyNames = source.stringPropertyNames();
        for (String propertyName : propertyNames) {
            if (propertyName.startsWith(prefix)) {
                target.setProperty(propertyName, source.getProperty(propertyName));
            }
        }
    }
}
