import org.joda.time.Instant;
import org.joda.time.Interval;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeOffsetClockTest {

    @InjectMocks
    private TimeOffsetClock clock;

    @Test
    void testNow() {
        // Set up the test
        long offset = 1000L;
        clock = new TimeOffsetClock(offset);

        // Perform the test
        Instant now = clock.now();

        // Verify that the clock's time matches the expected time
        Instant expectedNow = new Instant(System.currentTimeMillis() + offset);
        assertTrue(now.isEqual(expectedNow));
    }

    @Test
    void testIsCurrentTimeInInterval() {
        // Set up the test
        long offset = 1000L;
        clock = new TimeOffsetClock(offset);

        // Define an interval
        Instant start = new Instant(System.currentTimeMillis() - 2000L);
        Instant end = new Instant(System.currentTimeMillis() + 2000L);

        // Perform the test with a valid interval
        boolean result = clock.isCurrentTimeInInterval(start, end);

        // Verify that the result is true for a valid interval
        assertTrue(result);

        // Perform the test with an invalid interval
        Instant invalidStart = new Instant(System.currentTimeMillis() + 3000L);
        Instant invalidEnd = new Instant(System.currentTimeMillis() + 4000L);
        result = clock.isCurrentTimeInInterval(invalidStart, invalidEnd);

        // Verify that the result is false for an invalid interval
        assertFalse(result);
    }
}
