package ma.farm.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for managing date operations in the dashboard.
 */
public class DateUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Formats a LocalDate into "dd/MM/yyyy" format.
     *
     * @param date LocalDate to format
     * @return formatted date as String
     */
    public static String formatDate(LocalDate date) {
        return date.format(FORMATTER);
    }

    /**
     * @return the current system date
     */
    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    /**
     * Returns the last 7 days including today.
     *
     * @return array of LocalDate (7 days)
     */
    public static LocalDate[] getLast7Days() {
        LocalDate[] days = new LocalDate[7];
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            days[i] = today.minusDays(6 - i);
        }

        return days;
    }

    /**
     * Calculates number of days between two dates.
     *
     * @param start start date
     * @param end   end date
     * @return number of days
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }
}
