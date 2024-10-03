import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@ActiveProfiles("test")
public class HazelcastConfigTest {

    @InjectMocks
    private HazelcastConfig hazelcastConfig;

    @Mock
    private String hazelcastDNSName;

    @Mock
    private String projectVersion;

    @Mock
    private String commitId;

    @Test
    public void testConfigCreationWhenPropertyIsSetToServerMode() {
        // Simulate property "tmx-hazelcast.mode" being set to "HAZELCAST_SERVER_MODE"
        System.setProperty("tmx-hazelcast.mode", "HAZELCAST_SERVER_MODE");

        // Set mocked properties
        when(hazelcastDNSName).thenReturn("mock-dns-name");
        when(projectVersion).thenReturn("1.0.0");
        when(commitId).thenReturn("abc123");

        Config config = hazelcastConfig.config();

        // Validate that the config object is not null
        assertNotNull(config, "Config bean should not be null when tmx-hazelcast.mode is set to HAZELCAST_SERVER_MODE");

        // Verify that JetConfig is enabled
        assertTrue(config.getJetConfig().isEnabled(), "JetConfig should be enabled");

        // Verify Kubernetes configuration
        assertTrue(config.getNetworkConfig().getJoin().getKubernetesConfig().isEnabled(), "KubernetesConfig should be enabled");
        assertEquals("mock-dns-name", config.getNetworkConfig().getJoin().getKubernetesConfig().getProperty("service-dns"));

        // Verify cluster name
        assertEquals("TMX-v1.0.0.abc123", config.getClusterName(), "Cluster name should be properly set");

        // Verify the ORIG_LOGIN_EVENTS map configuration
        MapConfig origLoginEventsMap = config.getMapConfig(HazelcastConfig.ORIG_LOGIN_EVENTS);
        assertNotNull(origLoginEventsMap, "ORIG_LOGIN_EVENTS MapConfig should be present");
        assertEquals(360, origLoginEventsMap.getTimeToLiveSeconds(), "Time to live should be 360 seconds");
        assertEquals(EvictionPolicy.NONE, origLoginEventsMap.getEvictionConfig().getEvictionPolicy(), "Eviction policy should be NONE");
        assertEquals(MaxSizePolicy.FREE_HEAP_PERCENTAGE, origLoginEventsMap.getEvictionConfig().getMaxSizePolicy(), "Max size policy should be FREE_HEAP_PERCENTAGE");
        assertEquals(10, origLoginEventsMap.getEvictionConfig().getSize(), "Map size should be 10");

        // Verify the SYS_LOGIN_EVENTS map configuration
        MapConfig sysLoginEventsMap = config.getMapConfig(HazelcastConfig.SYS_LOGIN_EVENTS);
        assertNotNull(sysLoginEventsMap, "SYS_LOGIN_EVENTS MapConfig should be present");
        assertEquals(360, sysLoginEventsMap.getTimeToLiveSeconds(), "Time to live should be 360 seconds");
        assertEquals(EvictionPolicy.NONE, sysLoginEventsMap.getEvictionConfig().getEvictionPolicy(), "Eviction policy should be NONE");
        assertEquals(MaxSizePolicy.FREE_HEAP_PERCENTAGE, sysLoginEventsMap.getEvictionConfig().getMaxSizePolicy(), "Max size policy should be FREE_HEAP_PERCENTAGE");
        assertEquals(10, sysLoginEventsMap.getEvictionConfig().getSize(), "Map size should be 10");
    }

    @Test
    public void testConfigNotCreatedWhenPropertyIsNotServerMode() {
        // Simulate property "tmx-hazelcast.mode" being set to something other than "HAZELCAST_SERVER_MODE"
        System.setProperty("tmx-hazelcast.mode", "some-other-mode");

        Config config = hazelcastConfig.config();

        // Expect config to be null since the bean should not be created
        assertNull(config, "Config bean should not be created when tmx-hazelcast.mode is not HAZELCAST_SERVER_MODE");
    }
}
