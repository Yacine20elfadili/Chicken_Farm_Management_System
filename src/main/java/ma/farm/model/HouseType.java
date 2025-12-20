package ma.farm.model;

/**
 * HouseType enum - Defines the types of chicken houses
 *
 * Lifecycle flow:
 * 1. DAY_OLD (24 weeks) → Distribute to EGG_LAYER (females) or MEAT_MALE (males)
 * 2. EGG_LAYER (48 weeks) → Transfer to MEAT_FEMALE
 * 3. MEAT_FEMALE (6 weeks) → Sell
 * 4. MEAT_MALE (8 weeks) → Sell
 */
public enum HouseType {
    DAY_OLD("DayOld", 24 * 7),           // 24 weeks = 168 days
    EGG_LAYER("FemaleEggLayer", 48 * 7), // 48 weeks = 336 days
    MEAT_FEMALE("FemaleMeat", 6 * 7),    // 6 weeks = 42 days
    MEAT_MALE("MaleMeat", 8 * 7);        // 8 weeks = 56 days

    private final String displayName;
    private final int maxDurationDays;

    HouseType(String displayName, int maxDurationDays) {
        this.displayName = displayName;
        this.maxDurationDays = maxDurationDays;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the maximum duration in days that chickens should stay in this house type
     * @return duration in days
     */
    public int getMaxDurationDays() {
        return maxDurationDays;
    }

    /**
     * Gets the maximum duration in weeks
     * @return duration in weeks
     */
    public int getMaxDurationWeeks() {
        return maxDurationDays / 7;
    }

    /**
     * Gets the sell threshold percentage (60% of max duration)
     * Only applicable for MEAT_FEMALE and MEAT_MALE
     * @return threshold in days, or 0 if not applicable
     */
    public int getSellThresholdDays() {
        if (this == MEAT_FEMALE || this == MEAT_MALE) {
            return (int) (maxDurationDays * 0.6);
        }
        return 0;
    }

    /**
     * Determines the color status based on current age and max duration
     * Green: 0-50%, Orange: 50-75%, Red: 75-100%
     * @param currentAgeDays the current age in days
     * @return "green", "orange", or "red"
     */
    public String getAgeColorStatus(int currentAgeDays) {
        if (maxDurationDays == 0) return "green";

        double percentage = (double) currentAgeDays / maxDurationDays * 100;

        if (percentage <= 50) {
            return "green";
        } else if (percentage <= 75) {
            return "orange";
        } else {
            return "red";
        }
    }

    /**
     * Formats age in weeks and days (e.g., "6w 3d")
     * @param ageInDays the age in days
     * @return formatted string
     */
    public static String formatAge(int ageInDays) {
        int weeks = ageInDays / 7;
        int days = ageInDays % 7;
        return weeks + "w " + days + "d";
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Gets the HouseType enum from its display name
     * @param displayName the display name (e.g., "DayOld", "FemaleEggLayer")
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
        // Also try matching old display names for backward compatibility
        switch (displayName) {
            case "Day-old":
                return DAY_OLD;
            case "Egg Layer":
                return EGG_LAYER;
            case "Meat Female":
                return MEAT_FEMALE;
            case "Meat Male":
                return MEAT_MALE;
            default:
                return null;
        }
    }

    /**
     * Gets the section title for display in the UI
     * @return section title like "DayOld-House(s)"
     */
    public String getSectionTitle() {
        return displayName + "-House(s)";
    }

    /**
     * Generates a house name for the given index
     * @param index the house index (1-based)
     * @return house name like "DayOld-House-1"
     */
    public String generateHouseName(int index) {
        return displayName + "-House-" + index;
    }
}
