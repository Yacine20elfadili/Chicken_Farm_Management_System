package ma.farm.model;

/**
 * PersonnelType Enum - Defines all possible personnel types in the system
 *
 * Two main categories:
 * 1. ADMINISTRATION: Farm Owner, Cashier, Admin Staff
 * 2. FARM: Supervisors (Veterinary, Inventory, Farmhand) and their Subordinates
 *
 * Business Rules:
 * - Only ONE Farm Owner can exist
 * - Only ONE Cashier can exist
 * - 1-4 Admin Staff can exist (default has all 4 positions)
 * - Only ONE supervisor per type can exist (Veterinary, Inventory, Farmhand)
 * - Multiple subordinates can exist under each supervisor
 * - Subordinates MUST be linked to their respective supervisor
 *
 * Used in: Personnel management page
 *
 * @author Chicken Farm Management System
 * @version 2.0
 */
public enum PersonnelType {

    // ============================================================
    // ADMINISTRATION DEPARTMENT
    // ============================================================

    FARM_OWNER("farm_owner", "administration", "Propriétaire / Directeur Général",
            "Farm Owner / General Manager - Overall farm management", true, false),

    CASHIER("cashier", "administration", "Caissier",
            "Cashier - Bridge between admin and farm operations", true, false),

    ADMIN_STAFF("admin_staff", "administration", "Personnel Administratif",
            "Admin Staff - Accounting, HR, Legal, Sales positions", false, false),

    // ============================================================
    // FARM DEPARTMENT - SUPERVISORS
    // ============================================================

    VETERINARY_SUPERVISOR("veterinary_supervisor", "farm", "Superviseur Vétérinaire",
            "Veterinary Supervisor - Manages animal health team", true, true),

    INVENTORY_SUPERVISOR("inventory_supervisor", "farm", "Superviseur Inventaire & Approvisionnement",
            "Inventory & Supply Supervisor - Manages inventory team", true, true),

    FARMHAND_SUPERVISOR("farmhand_supervisor", "farm", "Superviseur Ouvriers Agricoles",
            "Farmhand Supervisor - Manages farmhand workers", true, true),

    // ============================================================
    // FARM DEPARTMENT - SUBORDINATES
    // ============================================================

    VETERINARY_SUBORDINATE("veterinary_subordinate", "farm", "Subordonné Vétérinaire",
            "Veterinary Subordinate - Works under veterinary supervisor", false, false),

    INVENTORY_SUBORDINATE("inventory_subordinate", "farm", "Subordonné Inventaire",
            "Inventory Subordinate - Works under inventory supervisor", false, false),

    FARMHAND_SUBORDINATE("farmhand_subordinate", "farm", "Subordonné Ouvrier Agricole",
            "Farmhand Subordinate - Works under farmhand supervisor", false, false);

    // ============================================================
    // FIELDS
    // ============================================================

    private final String code;
    private final String department;
    private final String displayNameFr;
    private final String description;
    private final boolean isSingleton; // Only one can exist
    private final boolean isSupervisor; // Can have subordinates

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Constructor for PersonnelType enum
     *
     * @param code the database/internal code matching jobTitles.name
     * @param department "administration" or "farm"
     * @param displayNameFr the French display name for UI
     * @param description the English description
     * @param isSingleton true if only one person of this type can exist
     * @param isSupervisor true if this type can have subordinates
     */
    PersonnelType(String code, String department, String displayNameFr,
                  String description, boolean isSingleton, boolean isSupervisor) {
        this.code = code;
        this.department = department;
        this.displayNameFr = displayNameFr;
        this.description = description;
        this.isSingleton = isSingleton;
        this.isSupervisor = isSupervisor;
    }

    // ============================================================
    // GETTERS
    // ============================================================

    /**
     * Gets the database/internal code (matches jobTitles.name in database)
     * @return the code (e.g., "farm_owner", "veterinary_supervisor")
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the department
     * @return "administration" or "farm"
     */
    public String getDepartment() {
        return department;
    }

    /**
     * Gets the French display name for UI
     * @return the French name
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
     * Checks if only one person of this type can exist
     * @return true for Farm Owner, Cashier, and all Supervisors
     */
    public boolean isSingleton() {
        return isSingleton;
    }

    /**
     * Checks if this type is a supervisor (can have subordinates)
     * @return true for Veterinary, Inventory, and Farmhand Supervisors
     */
    public boolean isSupervisor() {
        return isSupervisor;
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    /**
     * Checks if this type belongs to administration department
     * @return true if administration
     */
    public boolean isAdministration() {
        return "administration".equals(department);
    }

    /**
     * Checks if this type belongs to farm department
     * @return true if farm
     */
    public boolean isFarm() {
        return "farm".equals(department);
    }

    /**
     * Checks if this type is a subordinate (requires a supervisor)
     * @return true for all subordinate types
     */
    public boolean isSubordinate() {
        return this == VETERINARY_SUBORDINATE ||
               this == INVENTORY_SUBORDINATE ||
               this == FARMHAND_SUBORDINATE;
    }

    /**
     * Gets the corresponding supervisor type for a subordinate
     * @return the supervisor PersonnelType, or null if not a subordinate
     */
    public PersonnelType getSupervisorType() {
        switch (this) {
            case VETERINARY_SUBORDINATE:
                return VETERINARY_SUPERVISOR;
            case INVENTORY_SUBORDINATE:
                return INVENTORY_SUPERVISOR;
            case FARMHAND_SUBORDINATE:
                return FARMHAND_SUPERVISOR;
            default:
                return null;
        }
    }

    /**
     * Gets the corresponding subordinate type for a supervisor
     * @return the subordinate PersonnelType, or null if not a supervisor
     */
    public PersonnelType getSubordinateType() {
        switch (this) {
            case VETERINARY_SUPERVISOR:
                return VETERINARY_SUBORDINATE;
            case INVENTORY_SUPERVISOR:
                return INVENTORY_SUBORDINATE;
            case FARMHAND_SUPERVISOR:
                return FARMHAND_SUBORDINATE;
            default:
                return null;
        }
    }

    /**
     * Converts a code string to PersonnelType enum
     *
     * @param code the personnel type code (e.g., "farm_owner")
     * @return the corresponding PersonnelType, or null if not found
     */
    public static PersonnelType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        String normalizedCode = code.toLowerCase().trim();
        for (PersonnelType type : values()) {
            if (type.code.equals(normalizedCode)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Gets all administration types
     * @return array of FARM_OWNER, CASHIER, ADMIN_STAFF
     */
    public static PersonnelType[] getAdministrationTypes() {
        return new PersonnelType[]{FARM_OWNER, CASHIER, ADMIN_STAFF};
    }

    /**
     * Gets all farm supervisor types
     * @return array of VETERINARY_SUPERVISOR, INVENTORY_SUPERVISOR, FARMHAND_SUPERVISOR
     */
    public static PersonnelType[] getFarmSupervisorTypes() {
        return new PersonnelType[]{VETERINARY_SUPERVISOR, INVENTORY_SUPERVISOR, FARMHAND_SUPERVISOR};
    }

    /**
     * Gets all farm subordinate types
     * @return array of VETERINARY_SUBORDINATE, INVENTORY_SUBORDINATE, FARMHAND_SUBORDINATE
     */
    public static PersonnelType[] getFarmSubordinateTypes() {
        return new PersonnelType[]{VETERINARY_SUBORDINATE, INVENTORY_SUBORDINATE, FARMHAND_SUBORDINATE};
    }

    /**
     * Gets all farm types (supervisors + subordinates)
     * @return array of all farm personnel types
     */
    public static PersonnelType[] getFarmTypes() {
        return new PersonnelType[]{
            VETERINARY_SUPERVISOR, INVENTORY_SUPERVISOR, FARMHAND_SUPERVISOR,
            VETERINARY_SUBORDINATE, INVENTORY_SUBORDINATE, FARMHAND_SUBORDINATE
        };
    }

    @Override
    public String toString() {
        return displayNameFr;
    }
}
