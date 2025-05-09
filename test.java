import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeZoneConverter {

    // Custom formatter for input/output
    private static final DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");

    public static void main(String[] args) {
        // Convert UTC to HKT
        String utcToHkt = convertUtcToHkt("09-05-25 10:00:00");
        System.out.println("UTC to HKT: " + utcToHkt);

        // Convert HKT to UTC
        String hktToUtc = convertHktToUtc("09-05-25 18:00:00");
        System.out.println("HKT to UTC: " + hktToUtc);
    }

    public static String convertUtcToHkt(String utcDateTimeStr) {
        LocalDateTime utcDateTime = LocalDateTime.parse(utcDateTimeStr, customFormatter);
        ZonedDateTime utcZoned = utcDateTime.atZone(ZoneId.of("UTC"));
        ZonedDateTime hktZoned = utcZoned.withZoneSameInstant(ZoneId.of("Asia/Hong_Kong"));
        return hktZoned.format(DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss z"));
    }

    public static String convertHktToUtc(String hktDateTimeStr) {
        LocalDateTime hktDateTime = LocalDateTime.parse(hktDateTimeStr, customFormatter);
        ZonedDateTime hktZoned = hktDateTime.atZone(ZoneId.of("Asia/Hong_Kong"));
        ZonedDateTime utcZoned = hktZoned.withZoneSameInstant(ZoneId.of("UTC"));
        return utcZoned.format(DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss z"));
    }
}