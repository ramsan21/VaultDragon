Perfect — since commenting out log.info(...) makes the test pass, your production logic is fine and the failure is purely logback interacting badly with your static Instant mocking.
You don’t need to disable logging in the real code — you can suppress logging only inside your tests.

Here are a few clean ways to do that 👇

⸻

✅ Option 1 – Globally disable Logback for this test class

Add this at the top of your test class (or in a @BeforeAll / @BeforeEach):

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

@BeforeEach
void muteLogging() {
    Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.OFF);
}

and optionally re-enable after tests:

@AfterEach
void unmuteLogging() {
    Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.INFO); // or whatever your normal level is
}

This silences all Logback logging in the test JVM — log.info(...) will be called, but nothing will be rendered or timestamped, so no Instant.now() calls occur.

⸻

✅ Option 2 – Temporarily redirect logs to a null appender

If you prefer not to modify the logger level:

@BeforeEach
void disableLogging() {
    Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    root.detachAndStopAllAppenders();
}

This removes all appenders (console/file) from Logback, effectively discarding logs.

⸻

✅ Option 3 – Use a dedicated logger config for tests

Create src/test/resources/logback-test.xml with minimal content:

<configuration>
  <root level="OFF">
    <appender-ref ref="CONSOLE"/>
  </root>
</configuration>

Logback automatically prefers logback-test.xml when running tests, so no code change is needed at all.

⸻

👉 Recommended

Option 3 is the cleanest and permanent solution for tests.
If you just want a quick fix right now, copy Option 1 (setLevel(Level.OFF)) into your test setup — it’ll prevent the log.info() line from touching Instant.now() and remove your NPE instantly, without altering any production class.