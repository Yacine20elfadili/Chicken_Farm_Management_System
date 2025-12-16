package ma.farm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * EquipmentItem model - Represents an individual equipment item
 * Used in: Storage page
 * Example: One specific shovel with its own status and maintenance history
 */
public class EquipmentItem {

    // Fields
    private int id;
    private int categoryId;                 // Foreign key to EquipmentCategory
    private String status;                  // Good, Fair, Broken
    private LocalDate purchaseDate;         // When purchased
    private double purchasePrice;           // Original cost
    private LocalDate lastMaintenanceDate;  // Last maintenance
    private LocalDate nextMaintenanceDate;  // Scheduled maintenance
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== Constructors =====

    /**
     * Default constructor
     */
    public EquipmentItem() {
    }

    /**
     * Constructor for new item (minimal fields)
     */
    public EquipmentItem(int categoryId, String status, LocalDate purchaseDate, double purchasePrice) {
        this.categoryId = categoryId;
        this.status = status;
        this.purchaseDate = purchaseDate;
        this.purchasePrice = purchasePrice;
    }

    /**
     * Full constructor (all fields)
     */
    public EquipmentItem(int id, int categoryId, String status, LocalDate purchaseDate,
                         double purchasePrice, LocalDate lastMaintenanceDate,
                         LocalDate nextMaintenanceDate, LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.status = status;
        this.purchaseDate = purchaseDate;
        this.purchasePrice = purchasePrice;
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.nextMaintenanceDate = nextMaintenanceDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ===== Getters and Setters =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public LocalDate getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public LocalDate getNextMaintenanceDate() {
        return nextMaintenanceDate;
    }

    public void setNextMaintenanceDate(LocalDate nextMaintenanceDate) {
        this.nextMaintenanceDate = nextMaintenanceDate;
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

    // ===== Utility Methods =====

    /**
     * Check if equipment is broken
     */
    public boolean isBroken() {
        return "Broken".equalsIgnoreCase(status);
    }

    /**
     * Check if equipment is in fair condition
     */
    public boolean isFair() {
        return "Fair".equalsIgnoreCase(status);
    }

    /**
     * Check if equipment is in good condition
     */
    public boolean isGood() {
        return "Good".equalsIgnoreCase(status);
    }

    /**
     * Check if equipment is operational
     */
    public boolean isOperational() {
        return !isBroken();
    }

    /**
     * Check if maintenance is overdue
     */
    public boolean isMaintenanceOverdue() {
        if (nextMaintenanceDate == null) {
            return false;
        }
        return nextMaintenanceDate.isBefore(LocalDate.now());
    }

    /**
     * Check if maintenance is due soon (within 7 days)
     */
    public boolean isMaintenanceDueSoon() {
        if (nextMaintenanceDate == null) {
            return false;
        }
        LocalDate weekFromNow = LocalDate.now().plusDays(7);
        return nextMaintenanceDate.isBefore(weekFromNow) &&
               nextMaintenanceDate.isAfter(LocalDate.now().minusDays(1));
    }

    /**
     * Get days until next maintenance
     */
    public long getDaysUntilMaintenance() {
        if (nextMaintenanceDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextMaintenanceDate);
    }

    @Override
    public String toString() {
        return "EquipmentItem{" +
                "id=" + id +
                ", categoryId=" + categoryId +
                ", status='" + status + '\'' +
                ", purchasePrice=" + purchasePrice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquipmentItem that = (EquipmentItem) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}