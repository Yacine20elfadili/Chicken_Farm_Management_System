package ma.farm.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User model - Represents a registered business user
 * Contains Moroccan business registration fields (ICE, RC, CNSS, ONSSA, etc.)
 */
public class User {
    private int id;

    // Account Credentials
    private String email;
    private String password;

    // Company Information
    private String companyName;
    private String legalForm; // SARL, SA, SNC, SCS, SCA, Entreprise Individuelle
    private int capitalSocial; // Capital in MAD

    // Legal Identifiers (Moroccan)
    private String ice; // Identifiant Commun Entreprise (15 digits)
    private String rc; // Registre de Commerce
    private String fiscalId; // Identifiant Fiscal (IF)
    private int patente; // Professional tax number
    private String cnss; // CNSS number (optional)
    private String onssa; // ONSSA authorization (mandatory for poultry)

    // Address
    private String address; // Full street address
    private String city;
    private String postalCode; // 5 digits

    // Banking
    private String bankRIB; // 24 digits
    private String bankName;

    // Contact
    private String phoneNumber; // +212 format
    private String website; // Optional

    // Timestamps
    private LocalDateTime creationDate;
    private LocalDateTime updatedAt;

    // Default constructor
    public User() {
    }

    /**
     * Constructor for user sign up (without id and timestamps).
     */
    public User(String email, String password, String companyName, String legalForm, int capitalSocial,
            String ice, String rc, String fiscalId, int patente, String cnss, String onssa,
            String address, String city, String postalCode,
            String bankRIB, String bankName, String phoneNumber, String website) {
        this.email = email;
        this.password = password;
        this.companyName = companyName;
        this.legalForm = legalForm;
        this.capitalSocial = capitalSocial;
        this.ice = ice;
        this.rc = rc;
        this.fiscalId = fiscalId;
        this.patente = patente;
        this.cnss = cnss;
        this.onssa = onssa;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
        this.bankRIB = bankRIB;
        this.bankName = bankName;
        this.phoneNumber = phoneNumber;
        this.website = website;
        this.creationDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Full constructor for User (used when loading from database).
     */
    public User(int id, String email, String password, String companyName, String legalForm, int capitalSocial,
            String ice, String rc, String fiscalId, int patente, String cnss, String onssa,
            String address, String city, String postalCode,
            String bankRIB, String bankName, String phoneNumber, String website,
            LocalDateTime creationDate, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.companyName = companyName;
        this.legalForm = legalForm;
        this.capitalSocial = capitalSocial;
        this.ice = ice;
        this.rc = rc;
        this.fiscalId = fiscalId;
        this.patente = patente;
        this.cnss = cnss;
        this.onssa = onssa;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
        this.bankRIB = bankRIB;
        this.bankName = bankName;
        this.phoneNumber = phoneNumber;
        this.website = website;
        this.creationDate = creationDate;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLegalForm() {
        return legalForm;
    }

    public void setLegalForm(String legalForm) {
        this.legalForm = legalForm;
    }

    public int getCapitalSocial() {
        return capitalSocial;
    }

    public void setCapitalSocial(int capitalSocial) {
        this.capitalSocial = capitalSocial;
    }

    public String getIce() {
        return ice;
    }

    public void setIce(String ice) {
        this.ice = ice;
    }

    public String getRc() {
        return rc;
    }

    public void setRc(String rc) {
        this.rc = rc;
    }

    public String getFiscalId() {
        return fiscalId;
    }

    public void setFiscalId(String fiscalId) {
        this.fiscalId = fiscalId;
    }

    public int getPatente() {
        return patente;
    }

    public void setPatente(int patente) {
        this.patente = patente;
    }

    public String getCnss() {
        return cnss;
    }

    public void setCnss(String cnss) {
        this.cnss = cnss;
    }

    public String getOnssa() {
        return onssa;
    }

    public void setOnssa(String onssa) {
        this.onssa = onssa;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getBankRIB() {
        return bankRIB;
    }

    public void setBankRIB(String bankRIB) {
        this.bankRIB = bankRIB;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", companyName='" + companyName + '\'' +
                ", legalForm='" + legalForm + '\'' +
                ", ice='" + ice + '\'' +
                ", city='" + city + '\'' +
                ", creationDate=" + creationDate +
                '}';
    }
}