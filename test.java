import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = HazelcastConfig.class)
@ActiveProfiles("test")
public class HazelcastConfigTest {

    @Autowired
    private HazelcastConfig hazelcastConfig;

    @MockBean
    private Config config;

    @MockBean
    private ClientConfig clientConfig;

    @Test
    public void testConfigBeanCreation_WhenModeIsMember() {
        // Simulate that the "hazelcast.mode" is set to "member"
        System.setProperty("hazelcast.mode", "member");

        Config resultConfig = hazelcastConfig.config();
        assertNotNull(resultConfig, "Config bean should not be null when hazelcast.mode is member.");
    }

    @Test
    public void testClientConfigBeanCreation_WhenModeIsClient() {
        // Simulate that the "hazelcast.mode" is set to "client"
        System.setProperty("hazelcast.mode", "client");

        ClientConfig resultClientConfig = hazelcastConfig.clientConfig();
        assertNotNull(resultClientConfig, "ClientConfig bean should not be null when hazelcast.mode is client.");
    }

    @Test
    public void testHazelcastInstanceCreation_ClientMode() {
        // Simulate client mode
        System.setProperty("hazelcast.mode", "client");

        HazelcastInstance mockInstance = Mockito.mock(HazelcastInstance.class);

        when(hazelcastConfig.hazelcastInstance(clientConfig, config, "client")).thenReturn(mockInstance);

        HazelcastInstance instance = hazelcastConfig.hazelcastInstance(config, clientConfig, "client");
        assertNotNull(instance, "HazelcastInstance should not be null for client mode.");
    }

    @Test
    public void testHazelcastInstanceCreation_MemberMode() {
        // Simulate member mode
        System.setProperty("hazelcast.mode", "member");

        HazelcastInstance mockInstance = Mockito.mock(HazelcastInstance.class);

        when(hazelcastConfig.hazelcastInstance(clientConfig, config, "member")).thenReturn(mockInstance);

        HazelcastInstance instance = hazelcastConfig.hazelcastInstance(config, clientConfig, "member");
        assertNotNull(instance, "HazelcastInstance should not be null for member mode.");
    }
}
