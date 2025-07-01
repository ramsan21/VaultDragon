import com.scb.s2b.api.vault.configuration.VaultConfigInitializer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Properties;

public class ConsoleApp {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        // Manually register any needed PropertySources
        Properties vaultProps = new Properties();
        vaultProps.setProperty("vault.url", "https://your-vault-url");
        context.getEnvironment().getPropertySources().addFirst(
            new org.springframework.core.env.PropertiesPropertySource("manualProps", vaultProps)
        );

        // Register VaultConfigInitializer manually
        context.register(VaultConfigInitializer.class);

        // Manually add PropertySourcesPlaceholderConfigurer if needed
        context.register(PropertySourcesPlaceholderConfigurer.class);

        context.refresh();

        // Get your beans
        Object vaultBean = context.getBean("vaultSpringConfig"); // or actual type
        System.out.println("Vault config bean: " + vaultBean);

        context.close();
    }
}