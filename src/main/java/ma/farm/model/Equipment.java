package ma.farm.model;

import java.time.LocalDate;

/**
 * Equipment model - Represents farm equipment inventory
 * Used in: Storage page
 */
public class Equipment {

    // Fields
    private int id;
    private String name;                    // Equipment name
    private String category;                // Feeding, Cleaning, Medical, Other
    private int quantity;                   // Number of units
    private String status;                  // Good, Fair, Broken
    private LocalDate purchaseDate;         // Purchase date
    private double purchasePrice;           // Original cost
    private LocalDate lastMaintenanceDate;  // Last maintenance
    private LocalDate nextMaintenanceDate;  // Scheduled maintenance
    private String location;                // Where stored
    private String notes;                   // Additional info

    // Default constructor
    public Equipment() {
    }

    // Constructor for new equipment
    public Equipment(String name, String category, int quantity, String status) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.status = status;
    }

    // Full constructor
    public Equipment(int id, String name, String category, int quantity, String status,
                     LocalDate purchaseDate, double purchasePrice, LocalDate lastMaintenanceDate,
                     LocalDate nextMaintenanceDate, String location, String notes) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.status = status;
        this.purchaseDate = purchaseDate;
        this.purchasePrice = purchasePrice;
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.nextMaintenanceDate = nextMaintenanceDate;
        this.location = location;
        this.notes = notes;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Business methods

    /**
     * Check if equipment needs maintenance
     * @return true if maintenance is due
     */
    public boolean needsMaintenance() {
        if (nextMaintenanceDate == null) {
            return false;
        }
        return nextMaintenanceDate.isBefore(LocalDate.now());
    }

    /**
     * Check if equipment is broken
     * @return true if status is "Broken"
     */
    public boolean isBroken() {
        return "Broken".equals(status);
    }

    /**
     * Check if equipment is operational
     * @return true if status is "Good" or "Fair"
     */
    public boolean isOperational() {
        return !isBroken();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipment equipment = (Equipment) o;
        return id == equipment.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Equipment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                ", status='" + status + '\'' +
                ", purchaseDate=" + purchaseDate +
                ", purchasePrice=" + purchasePrice +
                ", lastMaintenanceDate=" + lastMaintenanceDate +
                ", nextMaintenanceDate=" + nextMaintenanceDate +
                ", location='" + location + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}