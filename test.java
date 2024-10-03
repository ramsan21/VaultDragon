import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HazelcastDNSAddressConditionTest {

    private HazelcastDNSAddressCondition condition;
    private ConditionContext mockContext;
    private AnnotatedTypeMetadata mockMetadata;

    @BeforeEach
    public void setUp() {
        // Initialize the condition
        condition = new HazelcastDNSAddressCondition();
        
        // Mock ConditionContext and AnnotatedTypeMetadata
        mockContext = mock(ConditionContext.class);
        mockMetadata = mock(AnnotatedTypeMetadata.class);
    }

    @Test
    public void testConditionWhenDNSAddressIsPresent() {
        // Mock ConfigHandler to return a valid DNS address
        ConfigHandler mockHandler = Mockito.mock(ConfigHandler.class);
        when(mockHandler.getUAASConf().getProperty("hazelcast.dns.address")).thenReturn("dns-address");

        // Ensure the condition evaluates to true
        boolean result = condition.matches(mockContext, mockMetadata);
        assertTrue(result, "Condition should return true when DNS address is present");
    }

    @Test
    public void testConditionWhenDNSAddressIsMissing() {
        // Mock ConfigHandler to return null (DNS address missing)
        ConfigHandler mockHandler = Mockito.mock(ConfigHandler.class);
        when(mockHandler.getUAASConf().getProperty("hazelcast.dns.address")).thenReturn(null);

        // Ensure the condition evaluates to false
        boolean result = condition.matches(mockContext, mockMetadata);
        assertFalse(result, "Condition should return false when DNS address is missing");
    }

    @Test
    public void testConditionWhenDNSAddressIsEmpty() {
        // Mock ConfigHandler to return an empty string (DNS address is empty)
        ConfigHandler mockHandler = Mockito.mock(ConfigHandler.class);
        when(mockHandler.getUAASConf().getProperty("hazelcast.dns.address")).thenReturn("");

        // Ensure the condition evaluates to false
        boolean result = condition.matches(mockContext, mockMetadata);
        assertFalse(result, "Condition should return false when DNS address is empty");
    }
}
