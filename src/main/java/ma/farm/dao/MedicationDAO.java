package ma.farm.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ma.farm.model.Medication;

/**
 * MedicationDAO - Data Access Object for Medication inventory management
 *
 * Provides database operations for the Medication model including:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Filtering by type and stock levels
 * - Low stock alerts and expiry tracking
 *
 * All database operations use prepared statements to prevent SQL injection.
 *
 * @author Chicken Farm Management System
 * @version 1.0
 */
public class MedicationDAO {

    private final DatabaseConnection dbConnection;

    /**
     * Constructor - Initializes the DAO with a database connection instance
     */
    public MedicationDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Maps a ResultSet row to a Medication object
     *
     * @param rs the ResultSet positioned at the current row
     * @return a Medication object populated with data from the ResultSet
     * @throws SQLException if a database access error occurs
     */
    private Medication mapResultSetToMedication(ResultSet rs)
        throws SQLException {
        Medication medication = new Medication();
        medication.setId(rs.getInt("id"));
        medication.setName(rs.getString("name"));
        medication.setType(rs.getString("type"));
        medication.setQuantity(rs.getInt("quantity"));
        medication.setUnit(rs.getString("unit"));
        medication.setPricePerUnit(rs.getDouble("pricePerUnit"));
        medication.setSupplier(rs.getString("supplier"));

        String purchaseDateStr = rs.getString("purchaseDate");
        if (purchaseDateStr != null && !purchaseDateStr.isEmpty()) {
            medication.setPurchaseDate(LocalDate.parse(purchaseDateStr));
        }

        String expiryStr = rs.getString("expiryDate");
        if (expiryStr != null && !expiryStr.isEmpty()) {
            medication.setExpiryDate(LocalDate.parse(expiryStr));
        }

        medication.setMinStockLevel(rs.getInt("minStockLevel"));
        medication.setUsage(rs.getString("usage"));

        return medication;
    }

    /**
     * Retrieves all medications from the database
     *
     * @return List of all Medication objects, or empty list if none found
     */
    public List<Medication> getAllMedications() {
        List<Medication> medicationList = new ArrayList<>();
        String query = "SELECT * FROM medications ORDER BY name";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                medicationList.add(mapResultSetToMedication(rs));
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving all medications: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return medicationList;
    }

    /**
     * Retrieves medication by ID
     *
     * @param id the medication ID
     * @return Medication object if found, null otherwise
     */
    public Medication getMedicationById(int id) {
        String query = "SELECT * FROM medications WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMedication(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving medication by ID: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves medications filtered by type
     *
     * @param type the medication type to filter by (e.g., "Vaccine", "Antibiotic", "Supplement")
     * @return List of Medication objects matching the type, or empty list if none found
     */
    public List<Medication> getMedicationsByType(String type) {
        List<Medication> medicationList = new ArrayList<>();
        String query = "SELECT * FROM medications WHERE type = ? ORDER BY name";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, type);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medicationList.add(mapResultSetToMedication(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving medications by type: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return medicationList;
    }

    /**
     * Retrieves all medications that are below their minimum stock level
     *
     * @return List of Medication objects with low stock, or empty list if none found
     */
    public List<Medication> getLowStockMedications() {
        List<Medication> medicationList = new ArrayList<>();
        String query =
            "SELECT * FROM medications WHERE quantity < minStockLevel ORDER BY (minStockLevel - quantity) DESC";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                medicationList.add(mapResultSetToMedication(rs));
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving low stock medications: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return medicationList;
    }

    /**
     * Gets the count of medications that are below minimum stock level
     *
     * @return count of low stock medications
     */
    public int getLowStockCount() {
        String query =
            "SELECT COUNT(*) as count FROM medications WHERE quantity < minStockLevel";

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
                "Error counting low stock medications: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Adds a new medication to the database
     *
     * @param medication the Medication object to insert
     * @return true if insertion was successful, false otherwise
     */
    public boolean addMedication(Medication medication) {
        String query =
            "INSERT INTO medications (name, type, quantity, unit, pricePerUnit, supplier, purchaseDate, expiryDate, minStockLevel, usage) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, medication.getName());
            stmt.setString(2, medication.getType());
            stmt.setInt(3, medication.getQuantity());
            stmt.setString(4, medication.getUnit());
            stmt.setDouble(5, medication.getPricePerUnit());
            stmt.setString(6, medication.getSupplier());
            stmt.setString(
                7,
                medication.getPurchaseDate() != null
                    ? medication.getPurchaseDate().toString()
                    : null
            );
            stmt.setString(
                8,
                medication.getExpiryDate() != null
                    ? medication.getExpiryDate().toString()
                    : null
            );
            stmt.setInt(9, medication.getMinStockLevel());
            stmt.setString(10, medication.getUsage());

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
                        medication.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding medication: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates the quantity of a specific medication
     *
     * @param id the medication ID
     * @param quantity the new quantity
     * @return true if update was successful, false otherwise
     */
    public boolean updateQuantity(int id, int quantity) {
        String query = "UPDATE medications SET quantity = ? WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println(
                "Error updating medication quantity: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates an existing medication
     *
     * @param medication the Medication object with updated values
     * @return true if update was successful, false otherwise
     */
    public boolean updateMedication(Medication medication) {
        String query =
            "UPDATE medications SET name = ?, type = ?, quantity = ?, unit = ?, pricePerUnit = ?, " +
            "supplier = ?, purchaseDate = ?, expiryDate = ?, minStockLevel = ?, usage = ? WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, medication.getName());
            stmt.setString(2, medication.getType());
            stmt.setInt(3, medication.getQuantity());
            stmt.setString(4, medication.getUnit());
            stmt.setDouble(5, medication.getPricePerUnit());
            stmt.setString(6, medication.getSupplier());
            stmt.setString(
                7,
                medication.getPurchaseDate() != null
                    ? medication.getPurchaseDate().toString()
                    : null
            );
            stmt.setString(
                8,
                medication.getExpiryDate() != null
                    ? medication.getExpiryDate().toString()
                    : null
            );
            stmt.setInt(9, medication.getMinStockLevel());
            stmt.setString(10, medication.getUsage());
            stmt.setInt(11, medication.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating medication: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Deletes a medication from the database
     *
     * @param id the medication ID to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteMedication(int id) {
        String query = "DELETE FROM medications WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting medication: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Gets the total count of medications
     *
     * @return total number of medications in the database
     */
    public int getTotalMedicationCount() {
        String query = "SELECT COUNT(*) as count FROM medications";

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
            System.err.println("Error counting medications: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Gets the total value of all medication inventory
     *
     * @return total value of medication inventory
     */
    public double getTotalMedicationValue() {
        String query =
            "SELECT SUM(quantity * pricePerUnit) as totalValue FROM medications";

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
                "Error calculating total medication value: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return 0.0;
    }

    /**
     * Gets medications that are expired
     *
     * @return List of Medication objects that are expired
     */
    public List<Medication> getExpiredMedications() {
        List<Medication> medicationList = new ArrayList<>();
        String query =
            "SELECT * FROM medications WHERE expiryDate IS NOT NULL AND expiryDate < date('now') ORDER BY expiryDate";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                medicationList.add(mapResultSetToMedication(rs));
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving expired medications: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return medicationList;
    }

    /**
     * Gets medications that are expiring within a specified number of days
     *
     * @param days number of days to check for expiry
     * @return List of Medication objects that are expiring soon
     */
    public List<Medication> getExpiringMedications(int days) {
        List<Medication> medicationList = new ArrayList<>();
        String query =
            "SELECT * FROM medications WHERE expiryDate IS NOT NULL AND expiryDate <= date('now', '+' || ? || ' days') AND expiryDate >= date('now') ORDER BY expiryDate";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setInt(1, days);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medicationList.add(mapResultSetToMedication(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving expiring medications: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return medicationList;
    }

    /**
     * Searches medications by name
     *
     * @param searchTerm the search term to match against medication names
     * @return List of Medication objects matching the search term
     */
    public List<Medication> searchByName(String searchTerm) {
        List<Medication> medicationList = new ArrayList<>();
        String query =
            "SELECT * FROM medications WHERE name LIKE ? ORDER BY name";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medicationList.add(mapResultSetToMedication(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error searching medications: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return medicationList;
    }

    /**
     * Main method to test all CRUD operations for MedicationDAO
     */
    public static void main(String[] args) {
        System.out.println("=== MedicationDAO CRUD Operations Test ===\n");

        MedicationDAO medicationDAO = new MedicationDAO();

        // ==================== CREATE ====================
        System.out.println("--- CREATE: Adding new medication ---");
        Medication newMed = new Medication();
        newMed.setName("Test Vitamin Complex");
        newMed.setType("Supplement");
        newMed.setQuantity(100);
        newMed.setUnit("tablets");
        newMed.setPricePerUnit(5.50);
        newMed.setSupplier("Test Pharma Inc.");
        newMed.setPurchaseDate(LocalDate.now());
        newMed.setExpiryDate(LocalDate.now().plusYears(1));
        newMed.setMinStockLevel(20);
        newMed.setUsage("1 tablet per 10 birds daily");

        boolean created = medicationDAO.addMedication(newMed);
        if (created) {
            System.out.println(
                "✓ Medication created successfully with ID: " + newMed.getId()
            );
            System.out.println("  " + newMed);
        } else {
            System.out.println("✗ Failed to create medication");
        }

        // ==================== READ ====================
        System.out.println("\n--- READ: Get all medications ---");
        List<Medication> allMeds = medicationDAO.getAllMedications();
        System.out.println("Total medications: " + allMeds.size());
        for (Medication med : allMeds) {
            System.out.println(
                "  - " +
                    med.getName() +
                    " (" +
                    med.getType() +
                    "): " +
                    med.getQuantity() +
                    " " +
                    med.getUnit()
            );
        }

        System.out.println("\n--- READ: Get medication by ID ---");
        if (newMed.getId() > 0) {
            Medication fetchedMed = medicationDAO.getMedicationById(
                newMed.getId()
            );
            if (fetchedMed != null) {
                System.out.println(
                    "✓ Found medication: " + fetchedMed.getName()
                );
            } else {
                System.out.println("✗ Medication not found");
            }
        }

        System.out.println("\n--- READ: Get medications by type (Vaccine) ---");
        List<Medication> vaccines = medicationDAO.getMedicationsByType(
            "Vaccine"
        );
        System.out.println("Vaccine count: " + vaccines.size());
        for (Medication med : vaccines) {
            System.out.println(
                "  - " +
                    med.getName() +
                    ": " +
                    med.getQuantity() +
                    " " +
                    med.getUnit()
            );
        }

        System.out.println("\n--- READ: Get low stock medications ---");
        List<Medication> lowStockMeds = medicationDAO.getLowStockMedications();
        System.out.println("Low stock medications: " + lowStockMeds.size());
        for (Medication med : lowStockMeds) {
            System.out.println(
                "  - " +
                    med.getName() +
                    ": " +
                    med.getQuantity() +
                    " (min: " +
                    med.getMinStockLevel() +
                    ")"
            );
        }

        System.out.println("\n--- READ: Get low stock count ---");
        int lowStockCount = medicationDAO.getLowStockCount();
        System.out.println("Low stock count: " + lowStockCount);

        System.out.println("\n--- READ: Get total medication value ---");
        double totalValue = medicationDAO.getTotalMedicationValue();
        System.out.println(
            "Total medication inventory value: $" +
                String.format("%.2f", totalValue)
        );

        System.out.println("\n--- READ: Get expired medications ---");
        List<Medication> expiredMeds = medicationDAO.getExpiredMedications();
        System.out.println("Expired medications: " + expiredMeds.size());
        for (Medication med : expiredMeds) {
            System.out.println(
                "  - " + med.getName() + " expired: " + med.getExpiryDate()
            );
        }

        System.out.println("\n--- READ: Search by name ('Vaccine') ---");
        List<Medication> searchResults = medicationDAO.searchByName("Vaccine");
        System.out.println("Search results: " + searchResults.size());
        for (Medication med : searchResults) {
            System.out.println("  - " + med.getName());
        }

        // ==================== UPDATE ====================
        System.out.println("\n--- UPDATE: Update quantity ---");
        if (newMed.getId() > 0) {
            boolean quantityUpdated = medicationDAO.updateQuantity(
                newMed.getId(),
                150
            );
            if (quantityUpdated) {
                Medication updatedMed = medicationDAO.getMedicationById(
                    newMed.getId()
                );
                System.out.println(
                    "✓ Quantity updated to: " + updatedMed.getQuantity()
                );
            } else {
                System.out.println("✗ Failed to update quantity");
            }
        }

        System.out.println("\n--- UPDATE: Update full medication record ---");
        if (newMed.getId() > 0) {
            newMed.setName("Updated Vitamin Complex Plus");
            newMed.setPricePerUnit(6.99);
            newMed.setQuantity(200);
            boolean updated = medicationDAO.updateMedication(newMed);
            if (updated) {
                Medication updatedMed = medicationDAO.getMedicationById(
                    newMed.getId()
                );
                System.out.println(
                    "✓ Medication updated: " +
                        updatedMed.getName() +
                        " - $" +
                        updatedMed.getPricePerUnit() +
                        "/" +
                        updatedMed.getUnit()
                );
            } else {
                System.out.println("✗ Failed to update medication");
            }
        }

        // ==================== DELETE ====================
        System.out.println("\n--- DELETE: Remove test medication ---");
        if (newMed.getId() > 0) {
            boolean deleted = medicationDAO.deleteMedication(newMed.getId());
            if (deleted) {
                System.out.println("✓ Medication deleted successfully");
                Medication deletedMed = medicationDAO.getMedicationById(
                    newMed.getId()
                );
                if (deletedMed == null) {
                    System.out.println(
                        "✓ Confirmed: Medication no longer exists in database"
                    );
                }
            } else {
                System.out.println("✗ Failed to delete medication");
            }
        }

        // ==================== BUSINESS LOGIC TESTS ====================
        System.out.println(
            "\n--- BUSINESS LOGIC: Test Medication model methods ---"
        );
        Medication testMed = new Medication();
        testMed.setQuantity(5);
        testMed.setMinStockLevel(10);
        testMed.setPricePerUnit(25.0);
        testMed.setExpiryDate(LocalDate.now().minusDays(1));

        System.out.println(
            "isLowStock() [5 < 10 min]: " + testMed.isLowStock()
        );
        System.out.println(
            "isExpired() [expired yesterday]: " + testMed.isExpired()
        );
        System.out.println(
            "getTotalValue() [5 * $25]: $" + testMed.getTotalValue()
        );

        System.out.println("\n=== MedicationDAO Test Complete ===");
    }
}
