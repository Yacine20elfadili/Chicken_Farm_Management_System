package ma.farm.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validation utility class for Moroccan business registration fields.
 * Provides validation for ICE, RC, CNSS, ONSSA, RIB, phone, and other legal
 * requirements.
 */
public class ValidationUtil {

    // Email validation regex pattern
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    // Moroccan phone pattern (+212 format)
    private static final String MOROCCAN_PHONE_REGEX = "^\\+212[\\s-]?[0-9]{3}[\\s-]?[0-9]{6}$|^\\+212[0-9]{9}$";
    private static final Pattern MOROCCAN_PHONE_PATTERN = Pattern.compile(MOROCCAN_PHONE_REGEX);

    // Website URL pattern
    private static final String WEBSITE_REGEX = "^(https?://)?([\\w.-]+)\\.([a-z]{2,})(:[0-9]+)?(/.*)?$";
    private static final Pattern WEBSITE_PATTERN = Pattern.compile(WEBSITE_REGEX, Pattern.CASE_INSENSITIVE);

    // Legal forms allowed in Morocco
    private static final List<String> LEGAL_FORMS = Arrays.asList(
            "SARL", "SA", "SNC", "SCS", "SCA", "Entreprise Individuelle");

    // ============== Basic Validations ==============

    /**
     * Checks if a string is null or empty (after trimming)
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Sanitizes input by trimming whitespace
     */
    public static String sanitize(String input) {
        return input == null ? "" : input.trim();
    }

    /**
     * Parses a string to Integer, returns null if invalid
     */
    public static Integer parseInteger(String value) {
        if (isEmpty(value))
            return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ============== Email & Password ==============

    /**
     * Validates if a string is a valid email format
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email))
            return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates password meets minimum requirements (at least 6 characters)
     */
    public static boolean isValidPassword(String password) {
        return !isEmpty(password) && password.length() >= 6;
    }

    // ============== Company Information ==============

    /**
     * Validates company name (not null/empty, length 3-200)
     */
    public static boolean isValidCompanyName(String name) {
        if (isEmpty(name))
            return false;
        String trimmed = name.trim();
        return trimmed.length() >= 3 && trimmed.length() <= 200;
    }

    /**
     * Validates legal form is one of the allowed Moroccan forms
     */
    public static boolean isValidLegalForm(String legalForm) {
        if (isEmpty(legalForm))
            return false;
        return LEGAL_FORMS.contains(legalForm.trim());
    }

    /**
     * Returns the list of valid legal forms
     */
    public static List<String> getLegalForms() {
        return LEGAL_FORMS;
    }

    /**
     * Validates capital social based on legal form
     * - SARL: minimum 10,000 MAD
     * - SA: minimum 300,000 MAD
     * - Others: must be positive
     */
    public static boolean isValidCapitalSocial(int capital, String legalForm) {
        if (capital <= 0)
            return false;
        if ("SA".equals(legalForm)) {
            return capital >= 300000;
        } else if ("SARL".equals(legalForm)) {
            return capital >= 10000;
        }
        return true; // Other legal forms just need positive capital
    }

    /**
     * Validates capital social string input
     */
    public static boolean isValidCapitalSocialStr(String capitalStr) {
        if (isEmpty(capitalStr))
            return false;
        try {
            int val = Integer.parseInt(capitalStr.trim());
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ============== Moroccan Legal Identifiers ==============

    /**
     * Validates ICE (Identifiant Commun Entreprise)
     * Must be exactly 15 numeric digits
     * ⚠️ MANDATORY for all invoices by Moroccan law
     */
    public static boolean isValidICE(String ice) {
        if (isEmpty(ice))
            return false;
        String trimmed = ice.trim();
        return trimmed.matches("\\d{15}");
    }

    /**
     * Validates RC (Registre de Commerce)
     * Format: "RC [number] [city]" or just number + city
     */
    public static boolean isValidRC(String rc) {
        if (isEmpty(rc))
            return false;
        String trimmed = rc.trim();
        // Must have at least 3 characters
        return trimmed.length() >= 3;
    }

    /**
     * Validates IF (Identifiant Fiscal)
     * Must be 7-8 numeric digits
     */
    public static boolean isValidIF(String fiscalId) {
        if (isEmpty(fiscalId))
            return false;
        String trimmed = fiscalId.trim();
        return trimmed.matches("\\d{7,8}");
    }

    /**
     * Validates Patente (Professional tax number)
     * Must be a positive integer, typically 6-8 digits
     */
    public static boolean isValidPatente(String patenteStr) {
        if (isEmpty(patenteStr))
            return false;
        try {
            int val = Integer.parseInt(patenteStr.trim());
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates CNSS number
     * Must be 7-9 numeric digits (optional field - can be empty)
     * ⚠️ Required if company has employees
     */
    public static boolean isValidCNSS(String cnss) {
        if (isEmpty(cnss))
            return true; // Optional
        String trimmed = cnss.trim();
        return trimmed.matches("\\d{7,9}");
    }

    /**
     * Validates ONSSA authorization number
     * Alphanumeric, minimum 5 characters
     * ⚠️ MANDATORY for poultry farms
     */
    public static boolean isValidONSSA(String onssa) {
        if (isEmpty(onssa))
            return false;
        String trimmed = onssa.trim();
        return trimmed.length() >= 5;
    }

    // ============== Address ==============

    /**
     * Validates address (not null/empty, at least 10 characters)
     */
    public static boolean isValidAddress(String address) {
        if (isEmpty(address))
            return false;
        return address.trim().length() >= 10;
    }

    /**
     * Validates city name (not null/empty, at least 2 characters)
     */
    public static boolean isValidCity(String city) {
        if (isEmpty(city))
            return false;
        return city.trim().length() >= 2;
    }

    /**
     * Validates Moroccan postal code (exactly 5 digits)
     */
    public static boolean isValidPostalCode(String postalCode) {
        if (isEmpty(postalCode))
            return false;
        String trimmed = postalCode.trim();
        return trimmed.matches("\\d{5}");
    }

    // ============== Banking ==============

    /**
     * Validates RIB (24 digits)
     * ⚠️ CRITICAL - Required for all legal payments
     */
    public static boolean isValidRIB(String rib) {
        if (isEmpty(rib))
            return false;
        String trimmed = rib.trim();
        return trimmed.matches("\\d{24}");
    }

    /**
     * Validates bank name (not null/empty, at least 3 characters)
     */
    public static boolean isValidBankName(String bankName) {
        if (isEmpty(bankName))
            return false;
        return bankName.trim().length() >= 3;
    }

    // ============== Contact ==============

    /**
     * Validates Moroccan phone number
     * Format: +212 XXX-XXXXXX or +212XXXXXXXXX
     */
    public static boolean isValidMoroccanPhone(String phone) {
        if (isEmpty(phone))
            return false;
        return MOROCCAN_PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates phone number (10 digits, starts with '0')
     * Alternative format for local Moroccan numbers
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (isEmpty(phone))
            return false;
        String trimmed = phone.trim();
        // Accept both +212 format and local 0XXXXXXXXX format
        if (trimmed.startsWith("+212")) {
            return isValidMoroccanPhone(trimmed);
        }
        return trimmed.matches("0\\d{9}");
    }

    /**
     * Validates website URL (optional - returns true if empty)
     */
    public static boolean isValidWebsite(String website) {
        if (isEmpty(website))
            return true; // Optional field
        return WEBSITE_PATTERN.matcher(website.trim()).matches();
    }
}