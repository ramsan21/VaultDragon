import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeZoneConverter {

    // Formatter for consistent output
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    public static void main(String[] args) {
        // Convert UTC to HKT
        String utcToHkt = convertUtcToHkt("2025-05-09T10:00:00");
        System.out.println("UTC to HKT: " + utcToHkt);

        // Convert HKT to UTC
        String hktToUtc = convertHktToUtc("2025-05-09T18:00:00");
        System.out.println("HKT to UTC: " + hktToUtc);
    }

    public static String convertUtcToHkt(String utcDateTimeStr) {
        LocalDateTime utcDateTime = LocalDateTime.parse(utcDateTimeStr);
        ZonedDateTime utcZoned = utcDateTime.atZone(ZoneId.of("UTC"));
        ZonedDateTime hktZoned = utcZoned.withZoneSameInstant(ZoneId.of("Asia/Hong_Kong"));
        return hktZoned.format(formatter);
    }

    public static String convertHktToUtc(String hktDateTimeStr) {
        LocalDateTime hktDateTime = LocalDateTime.parse(hktDateTimeStr);
        ZonedDateTime hktZoned = hktDateTime.atZone(ZoneId.of("Asia/Hong_Kong"));
        ZonedDateTime utcZoned = hktZoned.withZoneSameInstant(ZoneId.of("UTC"));
        return utcZoned.format(formatter);
    }
}