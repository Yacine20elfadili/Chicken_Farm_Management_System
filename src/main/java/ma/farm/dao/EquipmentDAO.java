package ma.farm.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ma.farm.model.Equipment;

/**
 * EquipmentDAO - Data Access Object for Equipment inventory management
 *
 * Provides database operations for the Equipment model including:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Filtering by status and category
 * - Maintenance tracking
 *
 * All database operations use prepared statements to prevent SQL injection.
 *
 * @author Chicken Farm Management System
 * @version 1.0
 */
public class EquipmentDAO {

    private final DatabaseConnection dbConnection;

    /**
     * Constructor - Initializes the DAO with a database connection instance
     */
    public EquipmentDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Maps a ResultSet row to an Equipment object
     *
     * @param rs the ResultSet positioned at the current row
     * @return an Equipment object populated with data from the ResultSet
     * @throws SQLException if a database access error occurs
     */
    private Equipment mapResultSetToEquipment(ResultSet rs)
        throws SQLException {
        Equipment equipment = new Equipment();
        equipment.setId(rs.getInt("id"));
        equipment.setName(rs.getString("name"));
        equipment.setCategory(rs.getString("category"));
        equipment.setQuantity(rs.getInt("quantity"));
        equipment.setStatus(rs.getString("status"));

        String purchaseDateStr = rs.getString("purchaseDate");
        if (purchaseDateStr != null && !purchaseDateStr.isEmpty()) {
            equipment.setPurchaseDate(LocalDate.parse(purchaseDateStr));
        }

        equipment.setPurchasePrice(rs.getDouble("purchasePrice"));

        String lastMaintenanceStr = rs.getString("lastMaintenanceDate");
        if (lastMaintenanceStr != null && !lastMaintenanceStr.isEmpty()) {
            equipment.setLastMaintenanceDate(
                LocalDate.parse(lastMaintenanceStr)
            );
        }

        String nextMaintenanceStr = rs.getString("nextMaintenanceDate");
        if (nextMaintenanceStr != null && !nextMaintenanceStr.isEmpty()) {
            equipment.setNextMaintenanceDate(
                LocalDate.parse(nextMaintenanceStr)
            );
        }

        equipment.setLocation(rs.getString("location"));
        equipment.setNotes(rs.getString("notes"));

        return equipment;
    }

    /**
     * Retrieves all equipment from the database
     *
     * @return List of all Equipment objects, or empty list if none found
     */
    public List<Equipment> getAllEquipment() {
        List<Equipment> equipmentList = new ArrayList<>();
        String query = "SELECT * FROM equipment ORDER BY name";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                equipmentList.add(mapResultSetToEquipment(rs));
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving all equipment: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return equipmentList;
    }

    /**
     * Retrieves equipment by ID
     *
     * @param id the equipment ID
     * @return Equipment object if found, null otherwise
     */
    public Equipment getEquipmentById(int id) {
        String query = "SELECT * FROM equipment WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEquipment(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving equipment by ID: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves equipment filtered by status
     *
     * @param status the equipment status to filter by (e.g., "Good", "Fair", "Broken")
     * @return List of Equipment objects matching the status, or empty list if none found
     */
    public List<Equipment> getByStatus(String status) {
        List<Equipment> equipmentList = new ArrayList<>();
        String query = "SELECT * FROM equipment WHERE status = ? ORDER BY name";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    equipmentList.add(mapResultSetToEquipment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving equipment by status: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return equipmentList;
    }

    /**
     * Retrieves equipment filtered by category
     *
     * @param category the equipment category to filter by (e.g., "Feeding", "Cleaning", "Medical")
     * @return List of Equipment objects matching the category, or empty list if none found
     */
    public List<Equipment> getByCategory(String category) {
        List<Equipment> equipmentList = new ArrayList<>();
        String query =
            "SELECT * FROM equipment WHERE category = ? ORDER BY name";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, category);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    equipmentList.add(mapResultSetToEquipment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving equipment by category: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return equipmentList;
    }

    /**
     * Retrieves all equipment that needs maintenance (nextMaintenanceDate has passed)
     *
     * @return List of Equipment objects needing maintenance, or empty list if none found
     */
    public List<Equipment> getEquipmentNeedingMaintenance() {
        List<Equipment> equipmentList = new ArrayList<>();
        String query =
            "SELECT * FROM equipment WHERE nextMaintenanceDate IS NOT NULL AND nextMaintenanceDate <= date('now') ORDER BY nextMaintenanceDate";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                equipmentList.add(mapResultSetToEquipment(rs));
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving equipment needing maintenance: " +
                    e.getMessage()
            );
            e.printStackTrace();
        }

        return equipmentList;
    }

    /**
     * Gets the count of broken equipment
     *
     * @return count of broken equipment items
     */
    public int getBrokenEquipmentCount() {
        String query =
            "SELECT COUNT(*) as count FROM equipment WHERE status = 'Broken'";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println(
                "Error counting broken equipment: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Adds a new equipment item to the database
     *
     * @param equipment the Equipment object to insert
     * @return true if insertion was successful, false otherwise
     */
    public boolean addEquipment(Equipment equipment) {
        String query =
            "INSERT INTO equipment (name, category, quantity, status, purchaseDate, purchasePrice, lastMaintenanceDate, nextMaintenanceDate, location, notes) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, equipment.getName());
            stmt.setString(2, equipment.getCategory());
            stmt.setInt(3, equipment.getQuantity());
            stmt.setString(4, equipment.getStatus());
            stmt.setString(
                5,
                equipment.getPurchaseDate() != null
                    ? equipment.getPurchaseDate().toString()
                    : null
            );
            stmt.setDouble(6, equipment.getPurchasePrice());
            stmt.setString(
                7,
                equipment.getLastMaintenanceDate() != null
                    ? equipment.getLastMaintenanceDate().toString()
                    : null
            );
            stmt.setString(
                8,
                equipment.getNextMaintenanceDate() != null
                    ? equipment.getNextMaintenanceDate().toString()
                    : null
            );
            stmt.setString(9, equipment.getLocation());
            stmt.setString(10, equipment.getNotes());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // SQLite: use last_insert_rowid() to get the generated ID
                try (
                    Statement idStmt = dbConnection
                        .getConnection()
                        .createStatement();
                    ResultSet rs = idStmt.executeQuery(
                        "SELECT last_insert_rowid()"
                    )
                ) {
                    if (rs.next()) {
                        equipment.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding equipment: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates the status of a specific equipment item
     *
     * @param id the equipment ID
     * @param status the new status (e.g., "Good", "Fair", "Broken")
     * @return true if update was successful, false otherwise
     */
    public boolean updateStatus(int id, String status) {
        String query = "UPDATE equipment SET status = ? WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, status);
            stmt.setInt(2, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println(
                "Error updating equipment status: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates an existing equipment item
     *
     * @param equipment the Equipment object with updated values
     * @return true if update was successful, false otherwise
     */
    public boolean updateEquipment(Equipment equipment) {
        String query =
            "UPDATE equipment SET name = ?, category = ?, quantity = ?, status = ?, purchaseDate = ?, " +
            "purchasePrice = ?, lastMaintenanceDate = ?, nextMaintenanceDate = ?, location = ?, notes = ? WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, equipment.getName());
            stmt.setString(2, equipment.getCategory());
            stmt.setInt(3, equipment.getQuantity());
            stmt.setString(4, equipment.getStatus());
            stmt.setString(
                5,
                equipment.getPurchaseDate() != null
                    ? equipment.getPurchaseDate().toString()
                    : null
            );
            stmt.setDouble(6, equipment.getPurchasePrice());
            stmt.setString(
                7,
                equipment.getLastMaintenanceDate() != null
                    ? equipment.getLastMaintenanceDate().toString()
                    : null
            );
            stmt.setString(
                8,
                equipment.getNextMaintenanceDate() != null
                    ? equipment.getNextMaintenanceDate().toString()
                    : null
            );
            stmt.setString(9, equipment.getLocation());
            stmt.setString(10, equipment.getNotes());
            stmt.setInt(11, equipment.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating equipment: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Records maintenance for an equipment item
     *
     * @param id the equipment ID
     * @param nextMaintenanceDate the next scheduled maintenance date
     * @return true if update was successful, false otherwise
     */
    public boolean recordMaintenance(int id, LocalDate nextMaintenanceDate) {
        String query =
            "UPDATE equipment SET lastMaintenanceDate = date('now'), nextMaintenanceDate = ?, status = 'Good' WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(
                1,
                nextMaintenanceDate != null
                    ? nextMaintenanceDate.toString()
                    : null
            );
            stmt.setInt(2, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println(
                "Error recording maintenance: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Deletes an equipment item from the database
     *
     * @param id the equipment ID to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteEquipment(int id) {
        String query = "DELETE FROM equipment WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting equipment: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Gets the total count of equipment items
     *
     * @return total number of equipment items in the database
     */
    public int getTotalEquipmentCount() {
        String query = "SELECT COUNT(*) as count FROM equipment";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting equipment: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Gets the total value of all equipment inventory
     *
     * @return total value of equipment inventory
     */
    public double getTotalEquipmentValue() {
        String query =
            "SELECT SUM(quantity * purchasePrice) as totalValue FROM equipment";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getDouble("totalValue");
            }
        } catch (SQLException e) {
            System.err.println(
                "Error calculating total equipment value: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return 0.0;
    }

    /**
     * Searches equipment by name
     *
     * @param searchTerm the search term to match against equipment names
     * @return List of Equipment objects matching the search term
     */
    public List<Equipment> searchByName(String searchTerm) {
        List<Equipment> equipmentList = new ArrayList<>();
        String query =
            "SELECT * FROM equipment WHERE name LIKE ? ORDER BY name";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    equipmentList.add(mapResultSetToEquipment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching equipment: " + e.getMessage());
            e.printStackTrace();
        }

        return equipmentList;
    }

    /**
     * Gets equipment by location
     *
     * @param location the location to filter by
     * @return List of Equipment objects at the specified location
     */
    public List<Equipment> getByLocation(String location) {
        List<Equipment> equipmentList = new ArrayList<>();
        String query =
            "SELECT * FROM equipment WHERE location = ? ORDER BY name";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, location);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    equipmentList.add(mapResultSetToEquipment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving equipment by location: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return equipmentList;
    }

    /**
     * Main method to test all CRUD operations for EquipmentDAO
     */
    public static void main(String[] args) {
        System.out.println("=== EquipmentDAO CRUD Operations Test ===\n");

        EquipmentDAO equipmentDAO = new EquipmentDAO();

        // ==================== CREATE ====================
        System.out.println("--- CREATE: Adding new equipment ---");
        Equipment newEquip = new Equipment();
        newEquip.setName("Test Water Pump");
        newEquip.setCategory("Feeding");
        newEquip.setQuantity(2);
        newEquip.setStatus("Good");
        newEquip.setPurchaseDate(LocalDate.now());
        newEquip.setPurchasePrice(450.00);
        newEquip.setLastMaintenanceDate(LocalDate.now());
        newEquip.setNextMaintenanceDate(LocalDate.now().plusMonths(3));
        newEquip.setLocation("Pump House");
        newEquip.setNotes("Test equipment for CRUD operations");

        boolean created = equipmentDAO.addEquipment(newEquip);
        if (created) {
            System.out.println(
                "✓ Equipment created successfully with ID: " + newEquip.getId()
            );
            System.out.println("  " + newEquip);
        } else {
            System.out.println("✗ Failed to create equipment");
        }

        // ==================== READ ====================
        System.out.println("\n--- READ: Get all equipment ---");
        List<Equipment> allEquip = equipmentDAO.getAllEquipment();
        System.out.println("Total equipment items: " + allEquip.size());
        for (Equipment equip : allEquip) {
            System.out.println(
                "  - " +
                    equip.getName() +
                    " (" +
                    equip.getCategory() +
                    "): " +
                    equip.getStatus()
            );
        }

        System.out.println("\n--- READ: Get equipment by ID ---");
        if (newEquip.getId() > 0) {
            Equipment fetchedEquip = equipmentDAO.getEquipmentById(
                newEquip.getId()
            );
            if (fetchedEquip != null) {
                System.out.println(
                    "✓ Found equipment: " + fetchedEquip.getName()
                );
            } else {
                System.out.println("✗ Equipment not found");
            }
        }

        System.out.println("\n--- READ: Get equipment by status (Good) ---");
        List<Equipment> goodEquip = equipmentDAO.getByStatus("Good");
        System.out.println("Good status equipment: " + goodEquip.size());
        for (Equipment equip : goodEquip) {
            System.out.println("  - " + equip.getName());
        }

        System.out.println("\n--- READ: Get equipment by status (Broken) ---");
        List<Equipment> brokenEquip = equipmentDAO.getByStatus("Broken");
        System.out.println("Broken equipment: " + brokenEquip.size());
        for (Equipment equip : brokenEquip) {
            System.out.println(
                "  - " + equip.getName() + ": " + equip.getNotes()
            );
        }

        System.out.println(
            "\n--- READ: Get equipment by category (Feeding) ---"
        );
        List<Equipment> feedingEquip = equipmentDAO.getByCategory("Feeding");
        System.out.println("Feeding equipment: " + feedingEquip.size());
        for (Equipment equip : feedingEquip) {
            System.out.println("  - " + equip.getName());
        }

        System.out.println("\n--- READ: Get equipment needing maintenance ---");
        List<Equipment> needsMaint =
            equipmentDAO.getEquipmentNeedingMaintenance();
        System.out.println(
            "Equipment needing maintenance: " + needsMaint.size()
        );
        for (Equipment equip : needsMaint) {
            System.out.println(
                "  - " +
                    equip.getName() +
                    " (due: " +
                    equip.getNextMaintenanceDate() +
                    ")"
            );
        }

        System.out.println("\n--- READ: Get broken equipment count ---");
        int brokenCount = equipmentDAO.getBrokenEquipmentCount();
        System.out.println("Broken equipment count: " + brokenCount);

        System.out.println("\n--- READ: Get total equipment value ---");
        double totalValue = equipmentDAO.getTotalEquipmentValue();
        System.out.println(
            "Total equipment value: $" + String.format("%.2f", totalValue)
        );

        System.out.println("\n--- READ: Search by name ('Feeder') ---");
        List<Equipment> searchResults = equipmentDAO.searchByName("Feeder");
        System.out.println("Search results: " + searchResults.size());
        for (Equipment equip : searchResults) {
            System.out.println("  - " + equip.getName());
        }

        // ==================== UPDATE ====================
        System.out.println("\n--- UPDATE: Update status ---");
        if (newEquip.getId() > 0) {
            boolean statusUpdated = equipmentDAO.updateStatus(
                newEquip.getId(),
                "Fair"
            );
            if (statusUpdated) {
                Equipment updatedEquip = equipmentDAO.getEquipmentById(
                    newEquip.getId()
                );
                System.out.println(
                    "✓ Status updated to: " + updatedEquip.getStatus()
                );
            } else {
                System.out.println("✗ Failed to update status");
            }
        }

        System.out.println("\n--- UPDATE: Update full equipment record ---");
        if (newEquip.getId() > 0) {
            newEquip.setName("Updated Water Pump Pro");
            newEquip.setPurchasePrice(550.00);
            newEquip.setQuantity(3);
            newEquip.setStatus("Good");
            boolean updated = equipmentDAO.updateEquipment(newEquip);
            if (updated) {
                Equipment updatedEquip = equipmentDAO.getEquipmentById(
                    newEquip.getId()
                );
                System.out.println(
                    "✓ Equipment updated: " +
                        updatedEquip.getName() +
                        " - $" +
                        updatedEquip.getPurchasePrice()
                );
            } else {
                System.out.println("✗ Failed to update equipment");
            }
        }

        System.out.println("\n--- UPDATE: Record maintenance ---");
        if (newEquip.getId() > 0) {
            boolean maintRecorded = equipmentDAO.recordMaintenance(
                newEquip.getId(),
                LocalDate.now().plusMonths(6)
            );
            if (maintRecorded) {
                Equipment updatedEquip = equipmentDAO.getEquipmentById(
                    newEquip.getId()
                );
                System.out.println(
                    "✓ Maintenance recorded. Next maintenance: " +
                        updatedEquip.getNextMaintenanceDate()
                );
            } else {
                System.out.println("✗ Failed to record maintenance");
            }
        }

        // ==================== DELETE ====================
        System.out.println("\n--- DELETE: Remove test equipment ---");
        if (newEquip.getId() > 0) {
            boolean deleted = equipmentDAO.deleteEquipment(newEquip.getId());
            if (deleted) {
                System.out.println("✓ Equipment deleted successfully");
                Equipment deletedEquip = equipmentDAO.getEquipmentById(
                    newEquip.getId()
                );
                if (deletedEquip == null) {
                    System.out.println(
                        "✓ Confirmed: Equipment no longer exists in database"
                    );
                }
            } else {
                System.out.println("✗ Failed to delete equipment");
            }
        }

        // ==================== BUSINESS LOGIC TESTS ====================
        System.out.println(
            "\n--- BUSINESS LOGIC: Test Equipment model methods ---"
        );
        Equipment testEquip = new Equipment();
        testEquip.setStatus("Broken");
        testEquip.setNextMaintenanceDate(LocalDate.now().minusDays(10));

        System.out.println(
            "isBroken() [status='Broken']: " + testEquip.isBroken()
        );
        System.out.println(
            "isOperational() [status='Broken']: " + testEquip.isOperational()
        );
        System.out.println(
            "needsMaintenance() [overdue by 10 days]: " +
                testEquip.needsMaintenance()
        );

        testEquip.setStatus("Good");
        System.out.println(
            "isBroken() [status='Good']: " + testEquip.isBroken()
        );
        System.out.println(
            "isOperational() [status='Good']: " + testEquip.isOperational()
        );

        System.out.println("\n=== EquipmentDAO Test Complete ===");
    }
}
