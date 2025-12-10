package ma.farm.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * DateUtil - Utility class for date operations
 * Used across: Dashboard, Chicken Bay, Eggs Bay, Storage, Tasks, Personnel pages
 */
public class DateUtil {

    // Date formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");

    /**
     * Format LocalDate to string (dd/MM/yyyy)
     *
     * @param date The date to format
     * @return Formatted date string (e.g., "08/12/2025")
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Format LocalDate to short string (dd/MM)
     * Useful for chart labels
     *
     * @param date The date to format
     * @return Formatted short date string (e.g., "08/12")
     */
    public static String formatShortDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(SHORT_DATE_FORMATTER);
    }

    /**
     * Get current date
     *
     * @return Today's date
     */
    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    /**
     * Calculate days between two dates
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Number of days between dates
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calculate days since a date
     *
     * @param date The past date
     * @return Number of days since that date
     */
    public static long daysSince(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(date, LocalDate.now());
    }

    /**
     * Calculate days until a date
     *
     * @param date The future date
     * @return Number of days until that date
     */
    public static long daysUntil(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    /**
     * Get last 7 days as a list (for charts/reports)
     * Used in: Dashboard (7-day egg production chart)
     *
     * Returns dates in chronological order (oldest to newest)
     * Index 0 = 6 days ago, Index 6 = today
     *
     * @return Array of last 7 dates including today
     */
    public static LocalDate[] getLast7Days() {
        LocalDate[] dates = new LocalDate[7];
        LocalDate today = LocalDate.now();

        // Start from 6 days ago up to today
        for (int i = 0; i < 7; i++) {
            dates[i] = today.minusDays(6 - i);
        }

        return dates;
    }

    /**
     * Get last N days as a list
     *
     * @param days Number of days
     * @return Array of last N dates including today
     */
    public static LocalDate[] getLastNDays(int days) {
        if (days <= 0) {
            return new LocalDate[0];
        }

        LocalDate[] dates = new LocalDate[days];
        LocalDate today = LocalDate.now();

        for (int i = 0; i < days; i++) {
            dates[i] = today.minusDays(days - 1 - i);
        }

        return dates;
    }

    /**
     * Check if date is today
     *
     * @param date Date to check
     * @return true if date is today
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.equals(LocalDate.now());
    }

    /**
     * Check if date is in the past
     *
     * @param date Date to check
     * @return true if date is before today
     */
    public static boolean isPast(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isBefore(LocalDate.now());
    }

    /**
     * Check if date is in the future
     *
     * @param date Date to check
     * @return true if date is after today
     */
    public static boolean isFuture(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isAfter(LocalDate.now());
    }

    /**
     * Add days to a date
     *
     * @param date The starting date
     * @param days Number of days to add
     * @return New date after adding days
     */
    public static LocalDate addDays(LocalDate date, int days) {
        if (date == null) {
            return null;
        }
        return date.plusDays(days);
    }

    /**
     * Subtract days from a date
     *
     * @param date The starting date
     * @param days Number of days to subtract
     * @return New date after subtracting days
     */
    public static LocalDate subtractDays(LocalDate date, int days) {
        if (date == null) {
            return null;
        }
        return date.minusDays(days);
    }

    /**
     * Parse string to LocalDate (dd/MM/yyyy)
     *
     * @param dateString The date string to parse
     * @return LocalDate object or null if parsing fails
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (Exception e) {
            System.err.println("Error parsing date: " + dateString);
            return null;
        }
    }

    /**
     * Get date N days ago
     *
     * @param days Number of days back
     * @return Date N days in the past
     */
    public static LocalDate getDaysAgo(int days) {
        return LocalDate.now().minusDays(days);
    }

    /**
     * Format date for display in UI (human readable)
     * Example: "Today", "Yesterday", "3 days ago", or "15/01/2024"
     *
     * @param date Date to format
     * @return Human-readable date string
     */
    public static String formatForDisplay(LocalDate date) {
        if (date == null) {
            return "";
        }

        LocalDate today = LocalDate.now();
        long daysDiff = ChronoUnit.DAYS.between(date, today);

        if (daysDiff == 0) {
            return "Today";
        } else if (daysDiff == 1) {
            return "Yesterday";
        } else if (daysDiff > 1 && daysDiff <= 7) {
            return daysDiff + " days ago";
        } else {
            return formatDate(date);
        }
    }

    /**
     * Calculate age in days from arrival date
     * Used in: Chicken Bay (chicken age calculation)
     *
     * @param arrivalDate Date chicken arrived
     * @return Age in days
     */
    public static int calculateAgeInDays(LocalDate arrivalDate) {
        if (arrivalDate == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(arrivalDate, LocalDate.now());
    }

    /**
     * Check if date is within a range
     *
     * @param date Date to check
     * @param startDate Range start
     * @param endDate Range end
     * @return true if date is within range (inclusive)
     */
    public static boolean isWithinRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Validate date string format (dd/MM/yyyy)
     *
     * @param dateString String to validate
     * @return true if valid format
     */
    public static boolean isValidDateFormat(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return false;
        }

        try {
            LocalDate.parse(dateString, DATE_FORMATTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get start of current week (Monday)
     *
     * @return Date of Monday of current week
     */
    public static LocalDate getStartOfWeek() {
        return LocalDate.now().with(java.time.DayOfWeek.MONDAY);
    }

    /**
     * Get end of current week (Sunday)
     *
     * @return Date of Sunday of current week
     */
    public static LocalDate getEndOfWeek() {
        return LocalDate.now().with(java.time.DayOfWeek.SUNDAY);
    }

    /**
     * Get start of current month
     *
     * @return First day of current month
     */
    public static LocalDate getStartOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /**
     * Get end of current month
     *
     * @return Last day of current month
     */
    public static LocalDate getEndOfMonth() {
        return LocalDate.now().withDayOfMonth(
                LocalDate.now().lengthOfMonth()
        );
    }
}