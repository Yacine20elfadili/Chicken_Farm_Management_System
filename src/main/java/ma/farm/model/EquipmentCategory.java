package ma.farm.model;

import java.time.LocalDateTime;

/**
 * EquipmentCategory model - Represents a category/type of equipment
 * Used in: Storage page
 * Example: "Shovels", "Water Pumps", "Feeders"
 */
public class EquipmentCategory {

    // Fields
    private int id;
    private String name;                    // Category name (e.g., "Shovels")
    private String category;                // Type: Feeding, Cleaning, Medical, Other
    private String location;                // Where stored/used
    private String notes;                   // Additional info
    private int itemCount;                  // Count of items (calculated from DB)
    private LocalDateTime createdAt;        // Record creation timestamp
    private LocalDateTime updatedAt;        // Record last update timestamp

    // ===== Constructors =====

    /**
     * Default constructor
     */
    public EquipmentCategory() {
        this.itemCount = 0;
    }

    /**
     * Constructor for new category (without timestamps)
     */
    public EquipmentCategory(String name, String category, String location, String notes) {
        this.name = name;
        this.category = category;
        this.location = location;
        this.notes = notes;
        this.itemCount = 0;
    }

    /**
     * Full constructor (with all fields including timestamps)
     */
    public EquipmentCategory(int id, String name, String category, String location, String notes,
                            int itemCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.location = location;
        this.notes = notes;
        this.itemCount = itemCount;
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

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
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
     * Get display name with item count
     */
    public String getDisplayName() {
        return name + " (" + itemCount + ")";
    }

    /**
     * Check if category has any items
     */
    public boolean hasItems() {
        return itemCount > 0;
    }

    @Override
    public String toString() {
        return "EquipmentCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", location='" + location + '\'' +
                ", itemCount=" + itemCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquipmentCategory that = (EquipmentCategory) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}