Got it — you’re seeding InstanceService with rows whose timestamps come from Instant.now(), and then you query with another Instant.now()… expression. Because logging (and sometimes other libs) also touch Instant.now(Clock), tests can become flaky or NPE if only one overload is stubbed, and they’re also non-deterministic.

Below is a drop-in test pattern that (a) makes time deterministic, (b) avoids returning mock Instants (we return a real one), and (c) stubs both Instant.now() overloads so logback or anything else won’t crash. You don’t need to change your production code.

⸻

One-time test dependency (if not already present)

Make sure you have mockito-inline on test classpath so static mocking works:

<dependency>
  <groupId>org.mockito</groupId>
  <artifactId>mockito-inline</artifactId>
  <version>5.2.0</version>
  <scope>test</scope>
</dependency>


⸻

Helpers you can paste in your test class

private static final long BASE_NOW = 1_700_000_000L; // any fixed epoch second
private static final Instant FIXED_INSTANT = Instant.ofEpochSecond(BASE_NOW);

private static void freezeTime(MockedStatic<Instant> clock) {
    clock.when(Instant::now).thenReturn(FIXED_INSTANT);
    // very important: stub the overload used by logback and others
    clock.when(() -> Instant.now(Mockito.any())).thenReturn(FIXED_INSTANT);
}

// convenience to make instances with explicit timestamps
private Instance inst(String serial, int seq, long activatedOn, String status, boolean commit) {
    Instance i = new Instance();
    i.setSerialNumber(serial);
    i.setSequenceNumber(BigInteger.valueOf(seq));
    i.setActivatedOn(activatedOn);
    i.setStatus(status);
    i.setCommit(commit);
    return i;
}


⸻

Example 1 — your “single activated before” case (deterministic)

This mirrors what’s visible in your screenshot (findActivatedBeforeWhenSingleActivatedBefore()): we add four rows; the third one is the only one “activated before” the cutoff.

@Test
void findActivatedBefore_whenSingleActivatedBefore() throws Exception {
    // Arrange H2 + config the same way you already do in @BeforeEach
    AppConfig appConfig = Mockito.mock(AppConfig.class);
    H2TestUtils.h2PropertiesMocking(appConfig);
    MockingUtils.mockStatic(ConfigHandler.class, () -> ConfigHandler.getUAAConf(), appConfig);

    service = new InstanceService();
    H2TestUtils.clearSession();
    H2TestUtils.deleteAll(service);

    try (MockedStatic<Instant> clock = Mockito.mockStatic(Instant.class)) {
        freezeTime(clock); // <- key part

        // Seed data (NOTE: explicit epoch seconds, not Instant.now() inline)
        service.add(inst("FD012345", 1, 0L,                 "DEACTIVATED", false));
        service.add(inst("FD012345", 2, 0L,                 "DEACTIVATED", false));
        service.add(inst("FD012345", 3, BASE_NOW - 4*60*60, "DEACTIVATED", false)); // 4h earlier
        service.add(inst("FD012345", 4, BASE_NOW,           "ACTIVE",      false));
        service.commit();

        // Act: cutoff is 2h before now
        long cutoff = BASE_NOW - 2*60*60;
        Instance out = service.findActivatedBefore("FD012345", cutoff, false);

        // Assert
        assertNotNull(out);
        assertEquals(3, out.getSequenceNumber().intValue()); // the 3rd row is the match
    }
}


⸻

Example 2 — multiple matches; expect the latest before cutoff

If your service is supposed to return the latest activation that is still < cutoff, seed two qualifying rows and assert the later one comes back:

@Test
void findActivatedBefore_whenMultipleActivatedBefore_picksLatestBeforeCutoff() throws Exception {
    // same H2/config bootstrapping as above…
    service = new InstanceService();
    H2TestUtils.clearSession();
    H2TestUtils.deleteAll(service);

    try (MockedStatic<Instant> clock = Mockito.mockStatic(Instant.class)) {
        freezeTime(clock);

        service.add(inst("FD012345", 1, BASE_NOW - 6*60*60, "DEACTIVATED", false)); // 6h earlier
        service.add(inst("FD012345", 2, BASE_NOW - 3*60*60, "DEACTIVATED", false)); // 3h earlier
        service.add(inst("FD012345", 3, BASE_NOW - 1*60*60, "DEACTIVATED", false)); // 1h earlier (AFTER cutoff)
        service.add(inst("FD012345", 4, BASE_NOW,           "ACTIVE",      false));
        service.commit();

        long cutoff = BASE_NOW - 2*60*60; // 2h before now
        Instance out = service.findActivatedBefore("FD012345", cutoff, false);

        assertNotNull(out);
        assertEquals(2, out.getSequenceNumber().intValue()); // latest < cutoff
    }
}


⸻

Why this works (and your earlier attempt didn’t)
	•	Deterministic time: we fix BASE_NOW and precompute epoch seconds for inserted rows. We do not call Instant.now() inside service.add(…) argument lists.
	•	Real Instant, not a mock: clock.when(Instant::now).thenReturn(FIXED_INSTANT) returns a normal Instant, so any subsequent method calls (e.g., getEpochSecond()) are safe.
	•	Both overloads stubbed: Instant.now(Clock) is also stubbed; logback uses it when creating LoggingEvent timestamps. If you only stub Instant.now(), logback may still hit the other overload and get null, causing the NPE you saw.

⸻

Optional: silence logging in this test class

If your logs are noisy or you still see logging stack traces from other tests, you can mute the root logger in a @BeforeEach:

@BeforeEach
void muteLogs() {
    ((ch.qos.logback.classic.Logger)
        org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME))
        .setLevel(ch.qos.logback.classic.Level.OFF);
}


⸻

If you paste the helpers and use this pattern, your InstanceService tests should pass reliably without touching production code. If method signatures differ slightly in your project (e.g., findActivatedBefore parameters), just adjust the argument order — the key ideas (fixed epoch constants + stubbing both Instant.now overloads with a real Instant) stay the same.