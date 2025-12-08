package ma.farm.model;

public enum HouseType {
    DAY_OLD("Day-old"),
    EGG_LAYER("Egg Layer"),
    MEAT_FEMALE("Meat Female"),
    MEAT_MALE("Meat Male");
    
    private final String displayName;
    
    HouseType(String displayName) {
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
     * Gets the HouseType enum from its display name
     * @param displayName the display name (e.g., "Day-old")
     * @return the corresponding HouseType or null if not found
     */
    public static HouseType fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return null;
        }
        for (HouseType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        return null;
    }
}
