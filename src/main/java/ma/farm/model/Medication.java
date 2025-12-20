package ma.farm.model;

import java.time.LocalDate;

/**
 * Medication model - Represents medication inventory
 * Used in: Storage page
 */
public class Medication {

    // Fields
    private int id;
    private String name;                    // Medication name
    private String type;                    // Vaccine, Antibiotic, Supplement
    private int quantity;                   // Number of units/doses
    private String unit;                    // ml, tablets, doses
    private double pricePerUnit;            // Cost per unit
    private String supplier;                // Supplier name
    private LocalDate purchaseDate;         // Purchase date
    private LocalDate expiryDate;           // Expiration date
    private int minStockLevel;              // Reorder threshold
    private String usage;                   // Usage instructions

    // Default constructor
    public Medication() {
    }

    // Constructor for new medication
    public Medication(String name, String type, int quantity, String unit) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
    }

    // Full constructor
    public Medication(int id, String name, String type, int quantity, String unit,
                      double pricePerUnit, String supplier, LocalDate purchaseDate,
                      LocalDate expiryDate, int minStockLevel, String usage) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.pricePerUnit = pricePerUnit;
        this.supplier = supplier;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.minStockLevel = minStockLevel;
        this.usage = usage;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    // Business methods

    /**
     * Check if medication stock is low
     * @return true if below minimum stock level
     */
    public boolean isLowStock() {
        return quantity < minStockLevel;
    }

    /**
     * Check if medication is expired
     * @return true if past expiry date
     */
    public boolean isExpired() {
        if (expiryDate == null) return false;
        return expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Check if medication is expiring soon (within 30 days)
     * @return true if expiring soon
     */
    public boolean isExpiringSoon() {
        if (expiryDate == null) return false;
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return expiryDate.isBefore(thirtyDaysFromNow) && !isExpired();
    }

    /**
     * Calculate total value of current stock
     * @return Total value in currency
     */
    public double getTotalValue() {
        return quantity * pricePerUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medication medication = (Medication) o;
        return id == medication.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Medication{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", pricePerUnit=" + pricePerUnit +
                ", supplier='" + supplier + '\'' +
                ", purchaseDate=" + purchaseDate +
                ", expiryDate=" + expiryDate +
                ", minStockLevel=" + minStockLevel +
                ", usage='" + usage + '\'' +
                '}';
    }
}