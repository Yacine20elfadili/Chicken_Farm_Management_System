package ma.farm.model;

public enum HealthStatus {
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor");
    
    private final String displayName;
    
    HealthStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Gets the HealthStatus enum from its display name
     * @param displayName the display name (e.g., "Good")
     * @return the corresponding HealthStatus or null if not found
     */
    public static HealthStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return null;
        }
        for (HealthStatus status : values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        return null;
    }
}
