import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

class ConfigUtilsTest {

  // --- helpers to build common mocks ---
  private ApplicationPolicy policy(String appId, String country) {
    ApplicationPolicy p = mock(ApplicationPolicy.class);
    when(p.getAppId()).thenReturn(appId);
    when(p.getCountryCode()).thenReturn(country);
    return p;
  }

  private SysConfig sysCfgWith(String value) {
    SysConfig c = mock(SysConfig.class);
    when(c.getValue()).thenReturn(value);
    return c;
  }

  private void stubGetSysConfig(MockedStatic<ConfigUtils> cfg,
                                String appId, String country,
                                SysConfig toReturn) {
    cfg.when(() -> ConfigUtils.getSysConfig(
            eq(appId),
            eq(ConfigUtils.ALL_SYMBOL),
            eq(country),
            eq(SysConfigCode.HK_COOLING_PERIOD.name())))
       .thenReturn(toReturn);
  }

  // 1) Blank config -> early return (hits isBlank branch)
  @Test
  void returnsDefaultWhenConfigBlank() {
    ApplicationPolicy policy = policy("app", "SG");
    Instance instance = mock(Instance.class);
    SysConfig sysCfg = sysCfgWith("");   // blank

    try (MockedStatic<ConfigUtils> cfg =
             mockStatic(ConfigUtils.class, Answers.CALLS_REAL_METHODS)) {

      stubGetSysConfig(cfg, "app", "SG", sysCfg);

      Pair<Long, Long> out = ConfigUtils.calculateCoolingPeriod(policy, instance);

      assertNotNull(out);
      assertEquals(0L, out.getLeft());
      assertEquals(0L, out.getRight());

      cfg.verify(() -> ConfigUtils.getSysConfig(
          "app", ConfigUtils.ALL_SYMBOL, "SG", SysConfigCode.HK_COOLING_PERIOD.name()));
    }
  }

  // 2) Happy path (valid number)
  @Test
  void happyPath_validNumber_returnsParsedAndRemaining() {
    ApplicationPolicy policy = policy("app", "SG");
    Instance instance = mock(Instance.class);
    SysConfig sysCfg = sysCfgWith("3600");

    long fixedNow = 1_000_000L;
    long activated =   990_000L;   // remaining = 10_000

    when(instance.getActivatedOnEpochSecond()).thenReturn(activated);

    try (MockedStatic<ConfigUtils> cfg =
             mockStatic(ConfigUtils.class, Answers.CALLS_REAL_METHODS);
         MockedStatic<Instant> clock = mockStatic(Instant.class)) {

      stubGetSysConfig(cfg, "app", "SG", sysCfg);

      Instant now = mock(Instant.class);
      when(now.getEpochSecond()).thenReturn(fixedNow);
      clock.when(Instant::now).thenReturn(now);

      Pair<Long, Long> out = ConfigUtils.calculateCoolingPeriod(policy, instance);

      assertEquals(3600L, out.getLeft());
      assertEquals(fixedNow - activated, out.getRight()); // 10_000
    }
  }

  // 3) Bad number -> NumberFormatException catch
  @Test
  void invalidNumber_triggersNumberFormatCatch_returnsDefault() {
    ApplicationPolicy policy = policy("app", "SG");
    Instance instance = mock(Instance.class);
    SysConfig sysCfg = sysCfgWith("not-a-long");

    try (MockedStatic<ConfigUtils> cfg =
             mockStatic(ConfigUtils.class, Answers.CALLS_REAL_METHODS)) {

      stubGetSysConfig(cfg, "app", "SG", sysCfg);

      Pair<Long, Long> out = ConfigUtils.calculateCoolingPeriod(policy, instance);

      assertEquals(0L, out.getLeft());
      assertEquals(0L, out.getRight());
    }
  }

  // 4) Unexpected exception inside try -> generic catch
  @Test
  void unexpectedException_triggersGenericCatch_returnsDefault() {
    ApplicationPolicy policy = policy("app", "SG");
    Instance instance = mock(Instance.class);
    SysConfig sysCfg = sysCfgWith("600"); // parsed successfully

    when(instance.getActivatedOnEpochSecond())
        .thenThrow(new RuntimeException("boom"));

    try (MockedStatic<ConfigUtils> cfg =
             mockStatic(ConfigUtils.class, Answers.CALLS_REAL_METHODS);
         MockedStatic<Instant> clock = mockStatic(Instant.class)) {

      stubGetSysConfig(cfg, "app", "SG", sysCfg);

      Instant now = mock(Instant.class);
      when(now.getEpochSecond()).thenReturn(42L);
      clock.when(Instant::now).thenReturn(now);

      Pair<Long, Long> out = ConfigUtils.calculateCoolingPeriod(policy, instance);

      assertEquals(0L, out.getLeft());
      assertEquals(0L, out.getRight());
    }
  }

  // 5) Edge: future activation → negative remaining (still happy path lines)
  @Test
  void futureActivation_returnsNegativeRemaining() {
    ApplicationPolicy policy = policy("app", "SG");
    Instance instance = mock(Instance.class);
    SysConfig sysCfg = sysCfgWith("120");

    long fixedNow = 2_000L;
    long activated = 3_000L; // remaining = -1000

    when(instance.getActivatedOnEpochSecond()).thenReturn(activated);

    try (MockedStatic<ConfigUtils> cfg =
             mockStatic(ConfigUtils.class, Answers.CALLS_REAL_METHODS);
         MockedStatic<Instant> clock = mockStatic(Instant.class)) {

      stubGetSysConfig(cfg, "app", "SG", sysCfg);

      Instant now = mock(Instant.class);
      when(now.getEpochSecond()).thenReturn(fixedNow);
      clock.when(Instant::now).thenReturn(now);

      Pair<Long, Long> out = ConfigUtils.calculateCoolingPeriod(policy, instance);

      assertEquals(120L, out.getLeft());
      assertEquals(-1000L, out.getRight());
    }
  }
}