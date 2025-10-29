// src/test/java/.../ConfigUtilsTest.java
// Adjust the package to your project structure.
package com.yourorg.yourpkg;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

// Import your real classes
// import com.scb.starsec.uaasv2.softtoken.db.model.Instance;
// import com.scb.starsec.uaasv2.softtoken.util.ConfigUtils;
// import com.scb.starsec.uaasv2.softtoken.policy.ApplicationPolicy;

public class ConfigUtilsTest {

    // ---------- Test A: when config is fetched via a *static* helper ----------
    // Assumptions:
    //   - ConfigUtils.calculateCoolingPeriod(ApplicationPolicy, Instance)
    //   - ConfigUtils.getSysConfig(ApplicationPolicy, String) -> Optional<String> or Optional<Config> with .getValue()
    @Test
    @DisplayName("negative remaining: activatedOn + cooling < now => remaining < 0 (static getSysConfig)")
    void negativeRemaining_staticConfig() {
        // Arrange
        ApplicationPolicy applicationPolicy = mock(ApplicationPolicy.class);

        Instance instance = new Instance();
        // Make sure default-able fields are set if your entity requires them
        instance.setSerialNumber("S1");
        // Put activation FAR in the past so the remaining time will be negative regardless of 'now'
        instance.setActivatedOn(0L); // epoch second 0

        // Mock the static getSysConfig to return "60" seconds as cooling period
        try (MockedStatic<ConfigUtils> mocked = Mockito.mockStatic(ConfigUtils.class, Mockito.CALLS_REAL_METHODS)) {
            // If getSysConfig returns Optional<String>
            mocked.when(() -> ConfigUtils.getSysConfig(eq(applicationPolicy), anyString()))
                  .thenReturn(Optional.of("60"));

            // Act
            Pair<Long, Long> result = ConfigUtils.calculateCoolingPeriod(applicationPolicy, instance);

            // Assert
            assertNotNull(result, "Result pair must not be null");
            assertEquals(60L, result.getLeft(), "Cooling period configuration should be 60 seconds");
            assertTrue(result.getRight() < 0, "Remaining cooling time should be negative");
        }
    }

    // ---------- Test B: when config is fetched from the ApplicationPolicy instance ----------
    // Use this if your calculate method reads something like:
    //   applicationPolicy.get("SOFTTOKEN_COOLING_PERIOD_SECONDS") -> "60"
    @Test
    @DisplayName("negative remaining: activatedOn + cooling < now => remaining < 0 (policy-provided config)")
    void negativeRemaining_policyProvidedConfig() {
        // Arrange
        ApplicationPolicy applicationPolicy = mock(ApplicationPolicy.class);

        // Adapt the key/method to whatever your ApplicationPolicy exposes:
        // e.g., when(applicationPolicy.getSysConfig("SOFTTOKEN_COOLING_PERIOD_SECONDS")).thenReturn(Optional.of("60"));
        when(applicationPolicy.get("SOFTTOKEN_COOLING_PERIOD_SECONDS")).thenReturn(Optional.of("60"));

        Instance instance = new Instance();
        instance.setSerialNumber("S2");
        instance.setActivatedOn(0L); // epoch second 0

        // Act
        Pair<Long, Long> result = ConfigUtils.calculateCoolingPeriod(applicationPolicy, instance);

        // Assert
        assertNotNull(result, "Result pair must not be null");
        assertEquals(60L, result.getLeft(), "Cooling period configuration should be 60 seconds");
        assertTrue(result.getRight() < 0, "Remaining cooling time should be negative");
    }
}