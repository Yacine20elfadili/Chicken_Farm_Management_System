package ma.farm.model;

import java.time.LocalDate;

/**
 * Feed model - Represents feed inventory
 * Used in: Storage page
 */
public class Feed {

    // Fields
    private int id;
    private String name;                    // Feed name
    private String type;                    // Day-old, Layer, Meat Growth
    private double quantityKg;              // Current stock in kg
    private double pricePerKg;              // Cost per kg
    private String supplier;                // Supplier name
    private LocalDate lastRestockDate;      // Last restock date
    private LocalDate expiryDate;           // Expiration date
    private double minStockLevel;           // Reorder threshold

    // Default constructor
    public Feed() {
    }

    // Constructor for new feed entry
    public Feed(String name, String type, double quantityKg, double pricePerKg) {
        this.name = name;
        this.type = type;
        this.quantityKg = quantityKg;
        this.pricePerKg = pricePerKg;
    }

    // Full constructor
    public Feed(int id, String name, String type, double quantityKg, double pricePerKg,
                String supplier, LocalDate lastRestockDate, LocalDate expiryDate,
                double minStockLevel) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.quantityKg = quantityKg;
        this.pricePerKg = pricePerKg;
        this.supplier = supplier;
        this.lastRestockDate = lastRestockDate;
        this.expiryDate = expiryDate;
        this.minStockLevel = minStockLevel;
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

    public double getQuantityKg() {
        return quantityKg;
    }

    public void setQuantityKg(double quantityKg) {
        this.quantityKg = quantityKg;
    }

    public double getPricePerKg() {
        return pricePerKg;
    }

    public void setPricePerKg(double pricePerKg) {
        this.pricePerKg = pricePerKg;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public LocalDate getLastRestockDate() {
        return lastRestockDate;
    }

    public void setLastRestockDate(LocalDate lastRestockDate) {
        this.lastRestockDate = lastRestockDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public double getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(double minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    // Business methods

    /**
     * Check if feed stock is low
     * @return true if below minimum stock level
     */
    public boolean isLowStock() {
        return quantityKg < minStockLevel;
    }

    /**
     * Check if feed is expired or near expiry
     * @return true if expired or expires within 7 days
     */
    public boolean isExpiredOrNearExpiry() {
        if (expiryDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return expiryDate.isBefore(today) || expiryDate.isBefore(today.plusDays(7));
    }

    /**
     * Calculate total value of current stock
     * @return Total value in currency
     */
    public double getTotalValue() {
        return quantityKg * pricePerKg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feed feed = (Feed) o;
        return id == feed.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Feed{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", quantityKg=" + quantityKg +
                ", pricePerKg=" + pricePerKg +
                ", supplier='" + supplier + '\'' +
                ", lastRestockDate=" + lastRestockDate +
                ", expiryDate=" + expiryDate +
                ", minStockLevel=" + minStockLevel +
                '}';
    }
}