package ma.farm.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilTest {

    // ========== Basic ==========
    @Test
    void testIsEmpty() {
        assertTrue(ValidationUtil.isEmpty(null));
        assertTrue(ValidationUtil.isEmpty("   "));
        assertFalse(ValidationUtil.isEmpty("test"));
    }

    @Test
    void testSanitize() {
        assertEquals("", ValidationUtil.sanitize(null));
        assertEquals("test", ValidationUtil.sanitize("  test "));
    }

    @Test
    void testParseInteger() {
        assertEquals(123, ValidationUtil.parseInteger("123"));
        assertNull(ValidationUtil.parseInteger("abc"));
        assertNull(ValidationUtil.parseInteger(null));
    }

    // ========== Email & Password ==========
    @Test
    void testIsValidEmail() {
        assertTrue(ValidationUtil.isValidEmail("test@email.com"));
        assertFalse(ValidationUtil.isValidEmail("testemail.com"));
        assertFalse(ValidationUtil.isValidEmail(""));
    }

    @Test
    void testIsValidPassword() {
        assertTrue(ValidationUtil.isValidPassword("123456"));
        assertFalse(ValidationUtil.isValidPassword("123"));
        assertFalse(ValidationUtil.isValidPassword(null));
    }

    // ========== Company ==========
    @Test
    void testIsValidCompanyName() {
        assertTrue(ValidationUtil.isValidCompanyName("My Company"));
        assertFalse(ValidationUtil.isValidCompanyName("AB"));
    }

    @Test
    void testIsValidLegalForm() {
        assertTrue(ValidationUtil.isValidLegalForm("SARL"));
        assertTrue(ValidationUtil.isValidLegalForm("SA"));
        assertFalse(ValidationUtil.isValidLegalForm("INVALID"));
    }

    @Test
    void testIsValidCapitalSocial() {
        assertTrue(ValidationUtil.isValidCapitalSocial(10000, "SARL"));
        assertFalse(ValidationUtil.isValidCapitalSocial(5000, "SARL"));

        assertTrue(ValidationUtil.isValidCapitalSocial(300000, "SA"));
        assertFalse(ValidationUtil.isValidCapitalSocial(100000, "SA"));
    }

    @Test
    void testIsValidCapitalSocialStr() {
        assertTrue(ValidationUtil.isValidCapitalSocialStr("1000"));
        assertFalse(ValidationUtil.isValidCapitalSocialStr("-5"));
        assertFalse(ValidationUtil.isValidCapitalSocialStr("abc"));
    }

    // ========== Moroccan IDs ==========
    @Test
    void testIsValidICE() {
        assertTrue(ValidationUtil.isValidICE("123456789012345"));
        assertFalse(ValidationUtil.isValidICE("12345"));
    }

    @Test
    void testIsValidRC() {
        assertTrue(ValidationUtil.isValidRC("RC 123 Casablanca"));
        assertFalse(ValidationUtil.isValidRC("R"));
    }

    @Test
    void testIsValidIF() {
        assertTrue(ValidationUtil.isValidIF("1234567"));
        assertFalse(ValidationUtil.isValidIF("ABC"));
    }

    @Test
    void testIsValidPatente() {
        assertTrue(ValidationUtil.isValidPatente("123456"));
        assertFalse(ValidationUtil.isValidPatente("-10"));
    }

    @Test
    void testIsValidCNSS() {
        assertTrue(ValidationUtil.isValidCNSS(""));
        assertTrue(ValidationUtil.isValidCNSS("1234567"));
        assertFalse(ValidationUtil.isValidCNSS("ABC"));
    }

    @Test
    void testIsValidONSSA() {
        assertTrue(ValidationUtil.isValidONSSA("ONSSA123"));
        assertFalse(ValidationUtil.isValidONSSA("123"));
    }

    // ========== Address ==========
    @Test
    void testIsValidAddress() {
        assertTrue(ValidationUtil.isValidAddress("123 Rue Hassan II"));
        assertFalse(ValidationUtil.isValidAddress("Rue"));
    }

    @Test
    void testIsValidCity() {
        assertTrue(ValidationUtil.isValidCity("Rabat"));
        assertFalse(ValidationUtil.isValidCity("A"));
    }

    @Test
    void testIsValidPostalCode() {
        assertTrue(ValidationUtil.isValidPostalCode("20000"));
        assertFalse(ValidationUtil.isValidPostalCode("2000"));
    }

    // ========== Banking ==========
    @Test
    void testIsValidRIB() {
        assertTrue(ValidationUtil.isValidRIB("123456789012345678901234"));
        assertFalse(ValidationUtil.isValidRIB("123"));
    }

    @Test
    void testIsValidBankName() {
        assertTrue(ValidationUtil.isValidBankName("CIH"));
        assertFalse(ValidationUtil.isValidBankName("AB"));
    }

    // ========== Contact ==========
    @Test
    void testIsValidMoroccanPhone() {
        assertTrue(ValidationUtil.isValidMoroccanPhone("+212612345678"));
        assertFalse(ValidationUtil.isValidMoroccanPhone("061234567"));
    }

    @Test
    void testIsValidPhoneNumber() {
        assertTrue(ValidationUtil.isValidPhoneNumber("0612345678"));
        assertTrue(ValidationUtil.isValidPhoneNumber("+212612345678"));
        assertFalse(ValidationUtil.isValidPhoneNumber("123"));
    }

    @Test
    void testIsValidWebsite() {
        assertTrue(ValidationUtil.isValidWebsite(""));
        assertTrue(ValidationUtil.isValidWebsite("https://example.com"));
        assertFalse(ValidationUtil.isValidWebsite("htp:/bad"));
    }
}
