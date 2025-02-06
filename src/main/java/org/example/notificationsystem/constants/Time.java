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
    /**
     * India's Zone ID. Helpful for interconversion of Date object to show the time zone's local time .
     * */
    public static ZoneId INDIA_ZONE_ID = ZoneId.of("Asia/Kolkata");
    /**
     * UTC Zone Id. Helpful for interconversion of Date object to show the time zone's local time .
     */
    public static ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
    /**
     * Helpful to parse the Time String in ElasticSearchRequest
     */
    public static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    /**
     * Because Date(MAX) gives an overflow (!!!!)
     * */
    public static String MAX_DATE = "05-02-3025 12:37:00";
    /**
     * Used for converting the timestamp as stored in elasticsearch to a Date object
     * */
    public static DateTimeFormatter ELASTICSEARCH_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss.SSS'Z'").withZone(UTC_ZONE_ID);
}