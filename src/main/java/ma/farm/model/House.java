package ma.farm.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * House model - Represents a chicken house/bay
 * Used in: Dashboard, Chicken Bay, Eggs Bay pages
 */
public class House {

    // Fields
    private int id;
    private String name;                    // H1, H2, H3, H4
    private HouseType type;                 // DAY_OLD, EGG_LAYER, MEAT_FEMALE, MEAT_MALE
    private int chickenCount;               // Current number of chickens
    private int capacity;                   // Maximum capacity
    private HealthStatus healthStatus;      // GOOD, FAIR, POOR
    private LocalDate lastCleaningDate;
    private LocalDate creationDate;

    // Default constructor
    public House() {
    }

    // Constructor without id (for creating new houses)
    public House(String name, HouseType type, int capacity) {
        this.name = name;
        this.type = type;
        this.chickenCount = 0;
        this.capacity = capacity;
    }

    // Full constructor
    public House(int id, String name, HouseType type, int chickenCount, int capacity,
                 HealthStatus healthStatus, LocalDate lastCleaningDate, LocalDate creationDate) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.chickenCount = chickenCount;
        this.capacity = capacity;
        this.healthStatus = healthStatus;
        this.lastCleaningDate = lastCleaningDate;
        this.creationDate = creationDate;
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

    public HouseType getType() {
        return type;
    }

    public void setType(HouseType type) {
        this.type = type;
    }

    public int getChickenCount() {
        return chickenCount;
    }

    public void setChickenCount(int chickenCount) {
        this.chickenCount = chickenCount;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    public LocalDate getLastCleaningDate() {
        return lastCleaningDate;
    }

    public void setLastCleaningDate(LocalDate lastCleaningDate) {
        this.lastCleaningDate = lastCleaningDate;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    // ==================== Database Helper Methods ====================

    /**
     * Gets the house type as a String for database storage
     * @return the type display name (e.g., "Egg Layer") or null
     */
    public String getTypeAsString() {
        return type != null ? type.getDisplayName() : null;
    }

    /**
     * Sets the house type from a String (from database)
     * @param typeStr the type display name (e.g., "Egg Layer")
     */
    public void setTypeFromString(String typeStr) {
        if (typeStr != null && !typeStr.isEmpty()) {
            this.type = HouseType.fromDisplayName(typeStr);
            if (this.type == null) {
                System.err.println("Invalid house type: " + typeStr);
            }
        } else {
            this.type = null;
        }
    }

    /**
     * Gets the health status as a String for database storage
     * @return the status display name (e.g., "Good") or null
     */
    public String getHealthStatusAsString() {
        return healthStatus != null ? healthStatus.getDisplayName() : null;
    }

    /**
     * Sets the health status from a String (from database)
     * @param statusStr the status display name (e.g., "Good")
     */
    public void setHealthStatusFromString(String statusStr) {
        if (statusStr != null && !statusStr.isEmpty()) {
            this.healthStatus = HealthStatus.fromDisplayName(statusStr);
            if (this.healthStatus == null) {
                System.err.println("Invalid health status: " + statusStr);
            }
        } else {
            this.healthStatus = null;
        }
    }

    // Business methods

    /**
     * Calculate occupancy percentage
     * @return Percentage of house capacity used
     */
    public double getOccupancyRate() {
        return capacity > 0 ? (double) chickenCount / capacity * 100 : 0;
    }

    /**
     * Check if house is at or over capacity
     * @return true if full or overcrowded
     */
    public boolean isFull() {
        return chickenCount >= capacity;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof House && id == ((House) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "House{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", chickenCount=" + chickenCount +
                ", capacity=" + capacity +
                ", healthStatus=" + healthStatus +
                ", lastCleaningDate=" + lastCleaningDate +
                ", creationDate=" + creationDate +
                '}';
    }
}