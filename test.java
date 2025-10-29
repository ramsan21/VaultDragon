import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class CalculateCoolingPeriodTest {

  @Test
  void returnsDefaultWhenConfigBlank() {
    ApplicationPolicy policy = mock(ApplicationPolicy.class);
    Instance instance = mock(Instance.class);
    SysConfig sysCfg = mock(SysConfig.class);

    when(policy.getAppId()).thenReturn("app");
    when(policy.getCountryCode()).thenReturn("SG");
    when(sysCfg.getValue()).thenReturn(""); // or null

    try (MockedStatic<ConfigUtils> cfg = mockStatic(ConfigUtils.class)) {
      cfg.when(() -> ConfigUtils.getSysConfig(eq("app"), anyString(), eq("SG")))
         .thenReturn(sysCfg);

      Pair<Long, Long> out = MyClass.calculateCoolingPeriod(policy, instance);
      assertEquals(0L, out.getLeft());
      assertEquals(0L, out.getRight());
    }
  }

  @Test
  void happyPath_validNumber_returnsParsedAndRemaining() {
    ApplicationPolicy policy = mock(ApplicationPolicy.class);
    Instance instance = mock(Instance.class);
    SysConfig sysCfg = mock(SysConfig.class);

    when(policy.getAppId()).thenReturn("app");
    when(policy.getCountryCode()).thenReturn("SG");
    when(sysCfg.getValue()).thenReturn("3600");

    long fixedNow = 1_000_000L;
    long activated = 990_000L; // remaining = 10_000

    when(instance.getActivatedOnEpochSecond()).thenReturn(activated);

    try (MockedStatic<ConfigUtils> cfg = mockStatic(ConfigUtils.class);
         MockedStatic<Instant> clock = mockStatic(Instant.class)) {

      cfg.when(() -> ConfigUtils.getSysConfig(eq("app"), anyString(), eq("SG")))
         .thenReturn(sysCfg);

      Instant nowInstant = mock(Instant.class);
      when(nowInstant.getEpochSecond()).thenReturn(fixedNow);
      clock.when(Instant::now).thenReturn(nowInstant);

      Pair<Long, Long> out = MyClass.calculateCoolingPeriod(policy, instance);
      assertEquals(3600L, out.getLeft());
      assertEquals(fixedNow - activated, out.getRight()); // 10_000
    }
  }

  @Test
  void invalidNumber_triggersNumberFormatCatch_returnsDefault() {
    ApplicationPolicy policy = mock(ApplicationPolicy.class);
    Instance instance = mock(Instance.class);
    SysConfig sysCfg = mock(SysConfig.class);

    when(policy.getAppId()).thenReturn("app");
    when(policy.getCountryCode()).thenReturn("SG");
    when(sysCfg.getValue()).thenReturn("not-a-long");

    // These won’t be used because parseLong will throw first.
    when(instance.getActivatedOnEpochSecond()).thenReturn(0L);

    try (MockedStatic<ConfigUtils> cfg = mockStatic(ConfigUtils.class);
         MockedStatic<Instant> clock = mockStatic(Instant.class)) {

      cfg.when(() -> ConfigUtils.getSysConfig(eq("app"), anyString(), eq("SG")))
         .thenReturn(sysCfg);

      Pair<Long, Long> out = MyClass.calculateCoolingPeriod(policy, instance);
      assertEquals(0L, out.getLeft());
      assertEquals(0L, out.getRight());
    }
  }

  @Test
  void unexpectedException_triggersGenericCatch_returnsDefault() {
    ApplicationPolicy policy = mock(ApplicationPolicy.class);
    Instance instance = mock(Instance.class);
    SysConfig sysCfg = mock(SysConfig.class);

    when(policy.getAppId()).thenReturn("app");
    when(policy.getCountryCode()).thenReturn("SG");
    when(sysCfg.getValue()).thenReturn("600"); // valid parse

    // Make an exception occur inside the try block AFTER parsing succeeds
    when(instance.getActivatedOnEpochSecond()).thenThrow(new RuntimeException("boom"));

    try (MockedStatic<ConfigUtils> cfg = mockStatic(ConfigUtils.class);
         MockedStatic<Instant> clock = mockStatic(Instant.class)) {

      cfg.when(() -> ConfigUtils.getSysConfig(eq("app"), anyString(), eq("SG")))
         .thenReturn(sysCfg);

      Instant nowInstant = mock(Instant.class);
      when(nowInstant.getEpochSecond()).thenReturn(1_000L);
      clock.when(Instant::now).thenReturn(nowInstant);

      Pair<Long, Long> out = MyClass.calculateCoolingPeriod(policy, instance);
      assertEquals(0L, out.getLeft());
      assertEquals(0L, out.getRight());
    }
  }

  @Test
  void happyPath_futureActivation_negativeRemainingIsReturned() {
    ApplicationPolicy policy = mock(ApplicationPolicy.class);
    Instance instance = mock(Instance.class);
    SysConfig sysCfg = mock(SysConfig.class);

    when(policy.getAppId()).thenReturn("app");
    when(policy.getCountryCode()).thenReturn("SG");
    when(sysCfg.getValue()).thenReturn("120");

    long fixedNow = 2000L;
    long activated = 3000L; // remaining = -1000

    when(instance.getActivatedOnEpochSecond()).thenReturn(activated);

    try (MockedStatic<ConfigUtils> cfg = mockStatic(ConfigUtils.class);
         MockedStatic<Instant> clock = mockStatic(Instant.class)) {

      cfg.when(() -> ConfigUtils.getSysConfig(eq("app"), anyString(), eq("SG")))
         .thenReturn(sysCfg);

      Instant nowInstant = mock(Instant.class);
      when(nowInstant.getEpochSecond()).thenReturn(fixedNow);
      clock.when(Instant::now).thenReturn(nowInstant);

      Pair<Long, Long> out = MyClass.calculateCoolingPeriod(policy, instance);
      assertEquals(120L, out.getLeft());
      assertEquals(-1000L, out.getRight());
    }
  }
}
@Test
void returnsDefaultWhenConfigBlank() {
    ApplicationPolicy policy = mock(ApplicationPolicy.class);
    Instance instance = mock(Instance.class);
    SysConfig sysCfg = mock(SysConfig.class); // mock is NOT null!

    when(policy.getAppId()).thenReturn("app");
    when(policy.getCountryCode()).thenReturn("SG");
    when(sysCfg.getValue()).thenReturn(""); // return blank, triggers early return

    try (MockedStatic<ConfigUtils> cfg = mockStatic(ConfigUtils.class)) {
        cfg.when(() -> ConfigUtils.getSysConfig(eq("app"), anyString(), eq("SG")))
           .thenReturn(sysCfg); // <- return mock SysConfig, NOT null

        Pair<Long, Long> out = ConfigUtils.calculateCoolingPeriod(policy, instance);
        assertNotNull(out); // sanity check
        assertEquals(0L, out.getLeft());
        assertEquals(0L, out.getRight());
    }
}
