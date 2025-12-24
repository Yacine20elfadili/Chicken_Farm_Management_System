package ma.farm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Customer {
    private int id;
    private String name;
    private String companyName;
    private String type; // "Company" or "Individual"
    private String legalForm; // SARL, SA, etc. (Company only)
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String ice; // Required for Company
    private String rc; // Registre de Commerce (Company only)
    private String website;

    // Secondary Contact
    private String secondaryContactName;
    private String secondaryContactPhone;

    // Banking Info
    private String bankName;
    private String rib;

    // Commercial Terms
    private String paymentTerms; // Immediate, Net 15, Net 30, Net 60
    private String usualPurchases; // What they typically buy
    private String deliverySchedule; // e.g., "Every Tuesday, Thursday"

    // Financial
    private double outstandingBalance;

    // Loyalty Tracking
    private double totalPurchases;
    private int visitCount;
    private LocalDate lastVisitDate;

    // Status
    private boolean isActive;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Customer() {
        this.isActive = true;
        this.paymentTerms = "Immediate";
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLegalForm() {
        return legalForm;
    }

    public void setLegalForm(String legalForm) {
        this.legalForm = legalForm;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getSecondaryContactName() {
        return secondaryContactName;
    }

    public void setSecondaryContactName(String secondaryContactName) {
        this.secondaryContactName = secondaryContactName;
    }

    public String getSecondaryContactPhone() {
        return secondaryContactPhone;
    }

    public void setSecondaryContactPhone(String secondaryContactPhone) {
        this.secondaryContactPhone = secondaryContactPhone;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getUsualPurchases() {
        return usualPurchases;
    }

    public void setUsualPurchases(String usualPurchases) {
        this.usualPurchases = usualPurchases;
    }

    public String getDeliverySchedule() {
        return deliverySchedule;
    }

    public void setDeliverySchedule(String deliverySchedule) {
        this.deliverySchedule = deliverySchedule;
    }

    public double getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(double outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public double getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(double totalPurchases) {
        this.totalPurchases = totalPurchases;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public LocalDate getLastVisitDate() {
        return lastVisitDate;
    }

    public void setLastVisitDate(LocalDate lastVisitDate) {
        this.lastVisitDate = lastVisitDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // === Utility Methods ===

    /**
     * Check if this customer is a company (vs individual).
     */
    public boolean isCompany() {
        return "Company".equalsIgnoreCase(type);
    }

    /**
     * Check if this is a repeat customer (more than 3 visits).
     */
    public boolean isRepeatCustomer() {
        return visitCount > 3;
    }

    /**
     * Get display name (for ComboBox).
     */
    @Override
    public String toString() {
        String displayType = isCompany() ? "Entreprise" : "Particulier";
        return name + " (" + displayType + ")";
    }
}
