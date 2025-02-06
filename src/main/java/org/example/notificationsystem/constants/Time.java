package org.example.notificationsystem.constants;

import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

/**
 * Constants related to Time
 * <ul>
 *     <li>ZoneId INDIA_ZONE_ID = ZoneId.of("Asia/Kolkata");</li>
 *     <li>ZoneId UTC_ZONE_ID = ZoneId.of("UTC");</li>
 *     <li>DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");</li>
 *     <li>String MAX_DATE = "05-02-3025 12:37:00";</li>
 *     <li>DateTimeFormatter ELASTICSEARCH_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss.SSS'Z'").withZone(UTC_ZONE_ID);</li>
 * </ul>
 *
 * @author Malladi Pradyumna
 */
public final class Time {
    public static ZoneId INDIA_ZONE_ID = ZoneId.of("Asia/Kolkata");
    public static ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
    public static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    public static String MAX_DATE = "05-02-3025 12:37:00";
    public static DateTimeFormatter ELASTICSEARCH_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss.SSS'Z'").withZone(UTC_ZONE_ID);
}