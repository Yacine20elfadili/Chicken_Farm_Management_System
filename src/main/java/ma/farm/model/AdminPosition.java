package ma.farm.model;

/**
 * AdminPosition Enum - Defines the positions that Admin Staff can hold
 *
 * Admin Staff can have 1 to 4 positions.
 * The default admin staff starts with all 4 positions.
 * Additional staff can have 1-3 positions (not all 4).
 * Each position can only be assigned to ONE person at a time.
 *
 * Used in: Personnel management for admin_staff job title
 *
 * @author Chicken Farm Management System
 * @version 2.0
 */
public enum AdminPosition {

    ACCOUNTING("accounting", "Comptabilité", "Handles financial records and accounting"),
    HR("hr", "Ressources Humaines", "Handles human resources and employee management"),
    LEGAL("legal", "Juridique/Paperasse", "Handles legal matters and paperwork"),
    SALES("sales", "Ventes & Opérations", "Handles sales and business operations");

    private final String code;
    private final String displayNameFr;
    private final String description;

    /**
     * Constructor for AdminPosition enum
     *
     * @param code the database/internal code for the position
     * @param displayNameFr the French display name for UI
     * @param description the English description of the position
     */
    AdminPosition(String code, String displayNameFr, String description) {
        this.code = code;
        this.displayNameFr = displayNameFr;
        this.description = description;
    }

    /**
     * Gets the database/internal code
     * @return the position code (e.g., "accounting", "hr")
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the French display name for UI
     * @return the French name (e.g., "Comptabilité", "Ressources Humaines")
     */
    public String getDisplayNameFr() {
        return displayNameFr;
    }

    /**
     * Gets the English description
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Converts a code string to AdminPosition enum
     *
     * @param code the position code (e.g., "accounting")
     * @return the corresponding AdminPosition, or null if not found
     */
    public static AdminPosition fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        String normalizedCode = code.toLowerCase().trim();
        for (AdminPosition position : values()) {
            if (position.code.equals(normalizedCode)) {
                return position;
            }
        }
        return null;
    }

    /**
     * Converts a comma-separated string of position codes to an array of AdminPositions
     *
     * @param positionsStr comma-separated position codes (e.g., "accounting,hr,legal")
     * @return array of AdminPosition enums
     */
    public static AdminPosition[] fromCommaSeparatedString(String positionsStr) {
        if (positionsStr == null || positionsStr.trim().isEmpty()) {
            return new AdminPosition[0];
        }

        String[] codes = positionsStr.split(",");
        java.util.List<AdminPosition> positions = new java.util.ArrayList<>();

        for (String code : codes) {
            AdminPosition position = fromCode(code.trim());
            if (position != null) {
                positions.add(position);
            }
        }

        return positions.toArray(new AdminPosition[0]);
    }

    /**
     * Converts an array of AdminPositions to a comma-separated string
     *
     * @param positions array of AdminPosition enums
     * @return comma-separated string of position codes (e.g., "accounting,hr,legal")
     */
    public static String toCommaSeparatedString(AdminPosition[] positions) {
        if (positions == null || positions.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < positions.length; i++) {
            sb.append(positions[i].getCode());
            if (i < positions.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * Gets all position codes as a comma-separated string
     * Used for default admin staff who has all 4 positions
     *
     * @return "accounting,hr,legal,sales"
     */
    public static String getAllPositionsAsString() {
        return "accounting,hr,legal,sales";
    }

    /**
     * Gets the total number of positions available
     * @return 4
     */
    public static int getTotalPositionCount() {
        return values().length;
    }

    @Override
    public String toString() {
        return displayNameFr;
    }
}
