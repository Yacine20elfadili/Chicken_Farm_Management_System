package ma.farm.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * House model - Represents a chicken house/bay
 * Used in: Dashboard, Chicken Bay, Eggs Bay pages
 *
 * House Types and Durations:
 * - DayOld: 24 weeks (168 days) - then distribute to EggLayer/MeatMale
 * - FemaleEggLayer: 48 weeks (336 days) - then transfer to FemaleMeat
 * - FemaleMeat: 6 weeks (42 days) - then sell
 * - MaleMeat: 8 weeks (56 days) - then sell
 */
public class House {

    // Fields
    private int id;
    private String name; // e.g., "DayOld-House-1", "FemaleEggLayer-House-2"
    private HouseType type; // DAY_OLD, EGG_LAYER, MEAT_FEMALE, MEAT_MALE
    private int chickenCount; // Current number of chickens
    private int capacity; // Maximum capacity
    private HealthStatus healthStatus; // GOOD, FAIR, POOR
    private LocalDate lastCleaningDate;
    private LocalDate creationDate;
    private LocalDate arrivalDate; // Date when chickens arrived in this house
    private int maxImportLimit; // Calculated max import limit for DayOld houses
    private int estimatedStayWeeks; // Expected stay duration in weeks

    // Default constructor
    public House() {
    }

    // Constructor without id (for creating new houses)
    public House(String name, HouseType type, int capacity) {
        this.name = name;
        this.type = type;
        this.chickenCount = 0;
        this.capacity = capacity;
        this.healthStatus = HealthStatus.GOOD;
        this.creationDate = LocalDate.now();
        this.maxImportLimit = 0;
        this.estimatedStayWeeks = 8;
    }

    // Constructor with capacity only (for Config Houses)
    public House(HouseType type, int index, int capacity) {
        this.name = type.generateHouseName(index);
        this.type = type;
        this.chickenCount = 0;
        this.capacity = capacity;
        this.healthStatus = HealthStatus.GOOD;
        this.creationDate = LocalDate.now();
        this.maxImportLimit = 0;
        this.estimatedStayWeeks = 8;
    }

    // Full constructor
    public House(int id, String name, HouseType type, int chickenCount, int capacity,
            HealthStatus healthStatus, LocalDate lastCleaningDate, LocalDate creationDate,
            LocalDate arrivalDate) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.chickenCount = chickenCount;
        this.capacity = capacity;
        this.healthStatus = healthStatus;
        this.lastCleaningDate = lastCleaningDate;
        this.creationDate = creationDate;
        this.arrivalDate = arrivalDate;
        this.maxImportLimit = 0;
        this.estimatedStayWeeks = 8;
    }

    // ==================== Getters and Setters ====================

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

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public int getMaxImportLimit() {
        return maxImportLimit;
    }

    public void setMaxImportLimit(int maxImportLimit) {
        this.maxImportLimit = maxImportLimit;
    }

    public int getEstimatedStayWeeks() {
        return estimatedStayWeeks;
    }

    public void setEstimatedStayWeeks(int estimatedStayWeeks) {
        this.estimatedStayWeeks = estimatedStayWeeks;
    }

    // ==================== Database Helper Methods ====================

    /**
     * Gets the house type as a String for database storage
     * 
     * @return the type display name (e.g., "DayOld") or null
     */
    public String getTypeAsString() {
        return type != null ? type.getDisplayName() : null;
    }

    /**
     * Sets the house type from a String (from database)
     * 
     * @param typeStr the type display name (e.g., "DayOld")
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
     * 
     * @return the status display name (e.g., "Good") or null
     */
    public String getHealthStatusAsString() {
        return healthStatus != null ? healthStatus.getDisplayName() : null;
    }

    /**
     * Sets the health status from a String (from database)
     * 
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

    // ==================== Age and Time Calculation Methods ====================

    /**
     * Calculate the age of chickens in this house in days
     * Based on arrivalDate and current date
     * 
     * @return age in days, or 0 if no chickens or no arrival date
     */
    public int getAgeInDays() {
        if (arrivalDate == null || chickenCount == 0) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(arrivalDate, LocalDate.now());
    }

    /**
     * Get the formatted age string (e.g., "6w 3d")
     * 
     * @return formatted age string
     */
    public String getFormattedAge() {
        int ageDays = getAgeInDays();
        return HouseType.formatAge(ageDays);
    }

    /**
     * Get the color status for the age display
     * Green: 0-50%, Orange: 50-75%, Red: 75-100%
     * 
     * @return "green", "orange", or "red"
     */
    public String getAgeColorStatus() {
        if (type == null)
            return "green";
        return type.getAgeColorStatus(getAgeInDays());
    }

    /**
     * Calculate the estimated transfer/sell date based on arrival date and house
     * type duration
     * 
     * @return the estimated date, or null if no arrival date
     */
    public LocalDate getEstimatedEndDate() {
        if (arrivalDate == null || type == null) {
            return null;
        }
        return arrivalDate.plusDays(type.getMaxDurationDays());
    }

    /**
     * Get the formatted estimated end date (DD/MM/YYYY)
     * 
     * @return formatted date string or "N/A"
     */
    public String getFormattedEstimatedEndDate() {
        LocalDate endDate = getEstimatedEndDate();
        if (endDate == null) {
            return "N/A";
        }
        return String.format("%02d/%02d/%04d",
                endDate.getDayOfMonth(),
                endDate.getMonthValue(),
                endDate.getYear());
    }

    /**
     * Check if chickens have passed the sell threshold (60% of duration)
     * Only applicable for MEAT_FEMALE and MEAT_MALE
     * 
     * @return true if past threshold, false otherwise
     */
    public boolean isPastSellThreshold() {
        // ==================== PRODUCTION LOGIC (COMMENTED FOR TESTING)
        // ====================
        // if (type == null || chickenCount == 0) {
        // return false;
        // }
        // int threshold = type.getSellThresholdDays();
        // if (threshold == 0) {
        // return false; // Not a meat house
        // }
        // return getAgeInDays() >= threshold;
        // ==================== END PRODUCTION LOGIC ====================

        // TEMPORARY: Always allow sell for testing
        return true;
    }

    /**
     * Check if chickens have reached the maximum duration
     * 
     * @return true if at or past max duration
     */
    public boolean isAtMaxDuration() {
        if (type == null || arrivalDate == null) {
            return false;
        }
        return getAgeInDays() >= type.getMaxDurationDays();
    }

    /**
     * Check if chickens can be transferred (stayed at least 60% of estimated time)
     * 
     * NOTE: For development/testing, this method always returns true.
     * Uncomment the blocking logic below when ready for production.
     * 
     * @return true if transfer is allowed
     */
    public boolean canTransfer() {
        // ==================== PRODUCTION LOGIC (COMMENTED FOR TESTING)
        // ====================
        // if (type == null || arrivalDate == null || chickenCount == 0) {
        // return false;
        // }
        // int stayDays = getAgeInDays();
        // int minStayDays = (int) (estimatedStayWeeks * 7 * 0.6); // 60% of estimated
        // stay
        // return stayDays >= minStayDays;
        // ==================== END PRODUCTION LOGIC ====================

        // TEMPORARY: Always allow transfer for testing
        return true;
    }

    /**
     * Get the number of days until transfer is allowed (60% stay requirement)
     * 
     * @return days remaining until transfer allowed, or 0 if already eligible
     */
    public int getDaysUntilTransferAllowed() {
        if (type == null || arrivalDate == null) {
            return 0;
        }
        int stayDays = getAgeInDays();
        int minStayDays = (int) (estimatedStayWeeks * 7 * 0.6); // 60% of estimated stay
        int remaining = minStayDays - stayDays;
        return remaining > 0 ? remaining : 0;
    }

    /**
     * Get days remaining until estimated end date
     * 
     * @return days remaining, or 0 if no arrival date or past due
     */
    public int getDaysRemaining() {
        LocalDate endDate = getEstimatedEndDate();
        if (endDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        return days > 0 ? (int) days : 0;
    }

    // ==================== Capacity Methods ====================

    /**
     * Calculate occupancy percentage
     * 
     * @return Percentage of house capacity used
     */
    public double getOccupancyRate() {
        return capacity > 0 ? (double) chickenCount / capacity * 100 : 0;
    }

    /**
     * Check if house is at or over capacity
     * 
     * @return true if full or overcrowded
     */
    public boolean isFull() {
        return chickenCount >= capacity;
    }

    /**
     * Check if house is empty
     * 
     * @return true if no chickens
     */
    public boolean isEmpty() {
        return chickenCount == 0;
    }

    /**
     * Get available capacity
     * 
     * @return number of additional chickens that can fit
     */
    public int getAvailableCapacity() {
        return Math.max(0, capacity - chickenCount);
    }

    /**
     * Reset house to empty state (after sell/distribute/transfer)
     * Clears chicken count and arrival date
     */
    public void reset() {
        this.chickenCount = 0;
        this.arrivalDate = null;
    }

    /**
     * Add chickens to this house
     * 
     * @param count   number of chickens to add
     * @param arrival the arrival date
     * @return true if successful (doesn't exceed capacity)
     */
    public boolean addChickens(int count, LocalDate arrival) {
        if (count <= 0) {
            return false;
        }
        if (chickenCount + count > capacity) {
            return false;
        }
        this.chickenCount += count;
        if (this.arrivalDate == null) {
            this.arrivalDate = arrival;
        }
        return true;
    }

    /**
     * Remove chickens from this house (for mortality or partial sell)
     * 
     * @param count number of chickens to remove
     * @return true if successful
     */
    public boolean removeChickens(int count) {
        if (count <= 0 || count > chickenCount) {
            return false;
        }
        this.chickenCount -= count;
        if (this.chickenCount == 0) {
            this.arrivalDate = null; // Reset arrival date when empty
        }
        return true;
    }

    // ==================== Display Helper Methods ====================

    /**
     * Get a short display name for dropdowns
     * 
     * @return name like "DayOld-House-1"
     */
    public String getDisplayName() {
        return name;
    }

    /**
     * Get card title (just the house number part)
     * 
     * @return title like "House-1"
     */
    public String getCardTitle() {
        if (name == null)
            return "House";
        // Extract the House-X part from names like "DayOld-House-1"
        int lastDash = name.lastIndexOf("-House-");
        if (lastDash >= 0) {
            return name.substring(lastDash + 1); // Returns "House-1"
        }
        return name;
    }

    // ==================== Object Methods ====================

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
                ", arrivalDate=" + arrivalDate +
                ", creationDate=" + creationDate +
                '}';
    }
}
