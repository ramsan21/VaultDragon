package com.scb.starsec.uaasv2.softtoken.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.scb.starsec.uaasv2.softtoken.db.model.Instance;
import com.scb.starsec.uaasv2.softtoken.policy.ApplicationPolicy;

public class ConfigUtilsTest {

    @Test
    @DisplayName("negative remaining: activatedOn + cooling < now => remaining < 0")
    void negativeRemaining_withoutStaticMock() {
        // Arrange
        ApplicationPolicy applicationPolicy = mock(ApplicationPolicy.class);

        // Create an instance with old activation time
        Instance instance = new Instance();
        instance.setSerialNumber("S1");
        instance.setActivatedOn(0L); // very old epoch -> ensures negative remaining

        // If calculateCoolingPeriod internally uses applicationPolicy.getValue(), mock it:
        when(applicationPolicy.getValue()).thenReturn("60"); // cooling period 60 seconds

        // Act
        Pair<Long, Long> result = ConfigUtils.calculateCoolingPeriod(applicationPolicy, instance);

        // Assert
        assertNotNull(result, "Result pair must not be null");
        assertEquals(60L, result.getLeft(), "Cooling period configuration should be 60 seconds");
        assertTrue(result.getRight() < 0, "Remaining cooling time should be negative");
    }
}