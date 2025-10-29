@Test
void happyPath_validNumber_returnsParsedAndRemaining() {
    ApplicationPolicy policy = policy("app", "SG");
    SysConfig sysCfg = sysCfgWith("3600");
    Instance instance = mock(Instance.class);
    when(instance.getActivatedOn()).thenReturn(990_000L);

    long fixedNow = 1_000_000L;
    Instant fixedInstant = Instant.ofEpochSecond(fixedNow);

    try (MockedStatic<ConfigUtils> cfg = mockStatic(ConfigUtils.class, Answers.CALLS_REAL_METHODS);
         MockedStatic<Instant> clock = mockStatic(Instant.class)) {

        // your existing stub
        stubGetSysConfig(cfg, "app", "SG", sysCfg);

        // IMPORTANT: return a real Instant and stub BOTH overloads
        clock.when(Instant::now).thenReturn(fixedInstant);
        clock.when(() -> Instant.now(Mockito.any())).thenReturn(fixedInstant);

        Pair<Long, Long> out = ConfigUtils.calculateCoolingPeriod(policy, instance);

        assertEquals(3600L, out.getLeft());
        assertEquals(fixedNow - 990_000L, out.getRight());
    }
}