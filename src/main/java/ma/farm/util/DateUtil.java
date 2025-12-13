package ma.farm.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DateUtil {

    // Date formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Formats a LocalDate to "dd/MM/yyyy" format
     *
     * @param date the date to format
     * @return formatted date string
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formats a LocalDate to "MMM yyyy" format (e.g., "Dec 2025")
     *
     * @param date the date to format
     * @return formatted month-year string
     */
    public static String formatMonthYear(LocalDate date) {
        if (date == null) return "";
        return date.format(MONTH_FORMATTER);
    }

    /**
     * Parses a date string in "dd/MM/yyyy" format to LocalDate
     *
     * @param dateStr the date string to parse
     * @return LocalDate or null if parsing fails
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            System.err.println("Error parsing date: " + dateStr);
            return null;
        }
    }

    /**
     * Calculates the number of days between a past date and today
     *
     * @param pastDate the past date
     * @return number of days since that date (0 if date is today)
     */
    public static long daysSince(LocalDate pastDate) {
        if (pastDate == null) return 0;
        return ChronoUnit.DAYS.between(pastDate, LocalDate.now());
    }

    /**
     * Calculates the number of days between today and a future date
     *
     * @param futureDate the future date
     * @return number of days until that date (0 if date is today, negative if date is in past)
     */
    public static long daysUntil(LocalDate futureDate) {
        if (futureDate == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), futureDate);
    }

    /**
     * Calculates the number of days between two dates
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return number of days between dates
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return 0;
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Checks if a given date is today
     *
     * @param date the date to check
     * @return true if date is today
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) return false;
        return date.equals(LocalDate.now());
    }

    /**
     * Checks if a given date is in the past
     *
     * @param date the date to check
     * @return true if date is before today
     */
    public static boolean isPast(LocalDate date) {
        if (date == null) return false;
        return date.isBefore(LocalDate.now());
    }

    /**
     * Checks if a given date is in the future
     *
     * @param date the date to check
     * @return true if date is after today
     */
    public static boolean isFuture(LocalDate date) {
        if (date == null) return false;
        return date.isAfter(LocalDate.now());
    }

    /**
     * Checks if a given date is today or in the future
     *
     * @param date the date to check
     * @return true if date is today or later
     */
    public static boolean isTodayOrFuture(LocalDate date) {
        if (date == null) return false;
        return !date.isBefore(LocalDate.now());
    }

    /**
     * Checks if a given date is today or in the past
     *
     * @param date the date to check
     * @return true if date is today or earlier
     */
    public static boolean isTodayOrPast(LocalDate date) {
        if (date == null) return false;
        return !date.isAfter(LocalDate.now());
    }

    /**
     * Gets the last 7 days including today
     * Returns array in descending order (today is index 0)
     *
     * @return array of 7 LocalDate objects
     */
    public static LocalDate[] getLast7Days() {
        LocalDate[] dates = new LocalDate[7];
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            dates[i] = today.minusDays(i);
        }

        return dates;
    }

    /**
     * Gets the last N days including today
     *
     * @param days number of days to retrieve
     * @return List of LocalDate objects in descending order (today is first)
     */
    public static List<LocalDate> getLastNDays(int days) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < days; i++) {
            dateList.add(today.minusDays(i));
        }

        return dateList;
    }

    /**
     * Gets all dates in a date range (inclusive)
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return List of all dates between (and including) start and end
     */
    public static List<LocalDate> getDateRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            dateList.add(current);
            current = current.plusDays(1);
        }

        return dateList;
    }

    /**
     * Gets the age in days for a given birth date
     *
     * @param birthDate the birth date
     * @return age in days
     */
    public static long getAgeInDays(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return daysSince(birthDate);
    }

    /**
     * Checks if a date is within a given range
     *
     * @param date      the date to check
     * @param startDate the range start
     * @param endDate   the range end
     * @return true if date is between start and end (inclusive)
     */
    public static boolean isBetween(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) return false;
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Gets the start of the current week (Monday)
     *
     * @return LocalDate for Monday of the current week
     */
    public static LocalDate getStartOfWeek() {
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        return today.minusDays(dayOfWeek - 1);
    }

    /**
     * Gets the end of the current week (Sunday)
     *
     * @return LocalDate for Sunday of the current week
     */
    public static LocalDate getEndOfWeek() {
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        return today.plusDays(7 - dayOfWeek);
    }

    /**
     * Gets the start of the current month
     *
     * @return LocalDate for the first day of the current month
     */
    public static LocalDate getStartOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /**
     * Gets the end of the current month
     *
     * @return LocalDate for the last day of the current month
     */
    public static LocalDate getEndOfMonth() {
        LocalDate today = LocalDate.now();
        return today.withDayOfMonth(today.lengthOfMonth());
    }

    /**
     * Gets the start of the current year
     *
     * @return LocalDate for January 1st of the current year
     */
    public static LocalDate getStartOfYear() {
        return LocalDate.now().withDayOfYear(1);
    }

    /**
     * Gets the end of the current year
     *
     * @return LocalDate for December 31st of the current year
     */
    public static LocalDate getEndOfYear() {
        LocalDate today = LocalDate.now();
        return today.withDayOfYear(today.lengthOfYear());
    }

    /**
     * Converts an ISO date string (yyyy-MM-dd) to formatted date
     *
     * @param isoDateStr ISO format date string
     * @return formatted date string (dd/MM/yyyy)
     */
    public static String convertFromISO(String isoDateStr) {
        if (isoDateStr == null || isoDateStr.isEmpty()) return "";
        try {
            LocalDate date = LocalDate.parse(isoDateStr, ISO_FORMATTER);
            return formatDate(date);
        } catch (Exception e) {
            System.err.println("Error converting ISO date: " + isoDateStr);
            return "";
        }
    }

    /**
     * Converts a formatted date string to ISO format
     *
     * @param formattedDateStr formatted date string (dd/MM/yyyy)
     * @return ISO format date string (yyyy-MM-dd)
     */
    public static String convertToISO(String formattedDateStr) {
        if (formattedDateStr == null || formattedDateStr.isEmpty()) return "";
        try {
            LocalDate date = parseDate(formattedDateStr);
            return date.format(ISO_FORMATTER);
        } catch (Exception e) {
            System.err.println("Error converting to ISO format: " + formattedDateStr);
            return "";
        }
    }

    /**
     * Gets a human-readable relative date string
     * Examples: "Today", "Yesterday", "2 days ago", "Tomorrow", "In 3 days"
     *
     * @param date the date to format
     * @return human-readable relative date
     */
    public static String getRelativeDate(LocalDate date) {
        if (date == null) return "";

        long daysFromNow = daysUntil(date);

        if (daysFromNow == 0) return "Today";
        if (daysFromNow == 1) return "Tomorrow";
        if (daysFromNow == -1) return "Yesterday";
        if (daysFromNow > 1) return "In " + daysFromNow + " days";
        if (daysFromNow < -1) return Math.abs(daysFromNow) + " days ago";

        return formatDate(date);
    }

    /**
     * Formats a LocalDate to short date format "dd/MM"
     * Used for chart labels and compact date displays
     *
     * @param date the date to format
     * @return formatted short date string (dd/MM)
     */
    public static String formatShortDate(LocalDate date) {
        if (date == null) return "";
        DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("dd/MM");
        return date.format(shortFormatter);
    }
}