package ma.farm.dao;

import ma.farm.model.EggProduction;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * EggProductionDAO - Data Access Object for EggProduction management
 *
 * Provides database operations for the EggProduction model including:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Daily production tracking and retrieval
 * - Historical data queries (specific date, date ranges, trends)
 * - Production statistics and aggregations
 * - House-specific egg production analysis
 *
 * All database operations use prepared statements to prevent SQL injection.
 * Primarily handles data for egg-laying houses (H2, H3).
 *
 * @author Farm Management System
 * @version 1.0
 */
public class EggProductionDAO {
    private DatabaseConnection dbConnection;

    /**
     * Constructor - Initializes the DAO with a database connection instance
     */
    public EggProductionDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Adds a new egg production record to the database
     *
     * @param production the EggProduction object to create
     * @return true if the record was added successfully, false otherwise
     */
    public boolean addProduction(EggProduction production) {
        String sql = "INSERT INTO egg_production (houseId, productionDate, eggsCollected, crackedEggs, goodEggs, deadChickens, collectedBy, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, production.getHouseId());
            // Convert LocalDate to String for SQLite
            stmt.setString(2, production.getProductionDate().toString());
            stmt.setInt(3, production.getEggsCollected());
            stmt.setInt(4, production.getCrackedEggs());
            stmt.setInt(5, production.getGoodEggs());
            stmt.setInt(6, production.getDeadChickens());
            stmt.setString(7, production.getCollectedBy());
            stmt.setString(8, production.getNotes());

            int rows = stmt.executeUpdate();
            if (rows == 0) return false;

            // Get inserted ID
            try (Statement idStmt = dbConnection.getConnection().createStatement();
                 ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid() AS id")) {
                if (rs.next()) {
                    production.setId(rs.getInt("id"));
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error adding egg production: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing egg production record
     *
     * @param production the EggProduction object with updated values (must have id set)
     * @return true if the update was successful, false otherwise
     */
    public boolean updateProduction(EggProduction production) {
        String sql = "UPDATE egg_production SET houseId = ?, productionDate = ?, eggsCollected = ?, " +
                "crackedEggs = ?, goodEggs = ?, deadChickens = ?, collectedBy = ?, notes = ? WHERE id = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, production.getHouseId());
            // Convert LocalDate to String for SQLite
            stmt.setString(2, production.getProductionDate().toString());
            stmt.setInt(3, production.getEggsCollected());
            stmt.setInt(4, production.getCrackedEggs());
            stmt.setInt(5, production.getGoodEggs());
            stmt.setInt(6, production.getDeadChickens());
            stmt.setString(7, production.getCollectedBy());
            stmt.setString(8, production.getNotes());
            stmt.setInt(9, production.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating egg production: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes an egg production record
     *
     * @param id the ID of the record to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteProduction(int id) {
        String sql = "DELETE FROM egg_production WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting egg production: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves a specific egg production record by ID
     *
     * @param id the ID of the record to retrieve
     * @return an EggProduction object if found, null otherwise
     */
    public EggProduction getProductionById(int id) {
        String sql = "SELECT * FROM egg_production WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEggProduction(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting egg production: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves egg production records for a specific date
     *
     * @param date the production date to retrieve
     * @return a List of EggProduction records for the specified date
     */
    public List<EggProduction> getProductionByDate(LocalDate date) {
        String sql = "SELECT * FROM egg_production WHERE productionDate = ? ORDER BY houseId";
        List<EggProduction> productionList = new ArrayList<>();
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            // Convert LocalDate to String for SQLite
            stmt.setString(1, date.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                productionList.add(mapResultSetToEggProduction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting production by date: " + e.getMessage());
            e.printStackTrace();
        }
        return productionList;
    }

    /**
     * Retrieves production records for a specific house
     *
     * @param houseId the ID of the house
     * @return a List of all EggProduction records for that house
     */
    public List<EggProduction> getProductionByHouse(int houseId) {
        String sql = "SELECT * FROM egg_production WHERE houseId = ? ORDER BY productionDate DESC";
        List<EggProduction> productionList = new ArrayList<>();
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, houseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                productionList.add(mapResultSetToEggProduction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting production by house: " + e.getMessage());
            e.printStackTrace();
        }
        return productionList;
    }

    /**
     * Retrieves egg production records for the past 7 days (including today)
     *
     * Useful for trend analysis and recent performance tracking.
     *
     * @return a List of EggProduction records from the past 7 days
     */
    public List<EggProduction> getLast7Days() {
        String sql = "SELECT * FROM egg_production WHERE productionDate >= date('now', '-7 days') " +
                "ORDER BY productionDate DESC, houseId";
        List<EggProduction> productionList = new ArrayList<>();
        try (Statement stmt = dbConnection.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                productionList.add(mapResultSetToEggProduction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting last 7 days production: " + e.getMessage());
            e.printStackTrace();
        }
        return productionList;
    }

    /**
     * Retrieves the total good eggs produced today
     *
     * Sums goodEggs from egg-laying houses (typically H2 and H3) for today.
     *
     * @return the total number of good eggs collected today
     */
    public int getEggsToday() {
        String sql = "SELECT COALESCE(SUM(goodEggs), 0) AS total FROM egg_production " +
                "WHERE productionDate = DATE('now')";
        try (Statement stmt = dbConnection.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting eggs today: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the total eggs collected within a date range
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return the total number of eggs collected in the date range
     */
    public int getTotalEggsByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(goodEggs), 0) AS total FROM egg_production " +
                "WHERE productionDate BETWEEN ? AND ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            // Convert LocalDate to String for SQLite
            stmt.setString(1, startDate.toString());
            stmt.setString(2, endDate.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting eggs by date range: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the average efficiency rate for a specific house
     *
     * Efficiency rate = (goodEggs / eggsCollected) × 100
     *
     * @param houseId the ID of the house
     * @return the average efficiency rate as a percentage
     */
    public double getAverageEfficiencyByHouse(int houseId) {
        String sql = "SELECT COALESCE(AVG(CASE WHEN eggsCollected > 0 THEN (CAST(goodEggs AS FLOAT) / eggsCollected * 100) ELSE 0 END), 0) AS avgEfficiency " +
                "FROM egg_production WHERE houseId = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, houseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avgEfficiency");
            }
        } catch (SQLException e) {
            System.err.println("Error getting average efficiency: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Counts the number of production records for a specific house
     *
     * @param houseId the ID of the house
     * @return the count of production records
     */
    public int getProductionCountByHouse(int houseId) {
        String sql = "SELECT COUNT(*) AS count FROM egg_production WHERE houseId = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, houseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting production records: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the total dead chickens recorded for a house within a date range
     *
     * Used to track flock health and adjust house chicken counts.
     *
     * @param houseId the ID of the house
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return the total number of dead chickens recorded
     */
    public int getTotalDeadChickens(int houseId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(deadChickens), 0) AS total FROM egg_production " +
                "WHERE houseId = ? AND productionDate BETWEEN ? AND ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, houseId);
            // Convert LocalDate to String for SQLite
            stmt.setString(2, startDate.toString());
            stmt.setString(3, endDate.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total dead chickens: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves all egg production records
     *
     * @return a List of all EggProduction records
     */
    public List<EggProduction> getAllProduction() {
        String sql = "SELECT * FROM egg_production ORDER BY productionDate DESC";
        List<EggProduction> productionList = new ArrayList<>();
        try (Statement stmt = dbConnection.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                productionList.add(mapResultSetToEggProduction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all production records: " + e.getMessage());
            e.printStackTrace();
        }
        return productionList;
    }

    /**
     * Helper method to map a ResultSet row to an EggProduction object
     * FIXED for SQLite: Uses getString() for dates instead of getDate()
     *
     * @param rs the ResultSet positioned at the desired row
     * @return an EggProduction object with all fields populated
     * @throws SQLException if column access fails
     */
    private EggProduction mapResultSetToEggProduction(ResultSet rs) throws SQLException {
        LocalDate productionDate = null;

        // SQLite stores dates as TEXT, so use getString() and parse
        String dateStr = rs.getString("productionDate");
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                productionDate = LocalDate.parse(dateStr);
            } catch (Exception e) {
                System.err.println("Error parsing date: " + dateStr);
                e.printStackTrace();
            }
        }

        return new EggProduction(
                rs.getInt("id"),
                rs.getInt("houseId"),
                productionDate,
                rs.getInt("eggsCollected"),
                rs.getInt("crackedEggs"),
                rs.getInt("goodEggs"),
                rs.getInt("deadChickens"),
                rs.getString("collectedBy"),
                rs.getString("notes")
        );
    }

    /**
     * Main method for testing EggProductionDAO operations
     */
    public static void main(String[] args) {
        System.out.println("=== EggProductionDAO Test Suite ===\n");

        // Initialize DAO
        EggProductionDAO dao = new EggProductionDAO();

        // Test database connection
        testDatabaseConnection();

        // Run test suite
        testCreateProduction(dao);
        testReadProduction(dao);
        testUpdateProduction(dao);
        testQueryMethods(dao);
        testStatistics(dao);
        testDeleteProduction(dao);

        System.out.println("\n=== Test Suite Completed ===");
    }

    /**
     * Test 1: Database Connection
     */
    private static void testDatabaseConnection() {
        System.out.println("--- Test 1: Database Connection ---");
        try {
            DatabaseConnection dbConn = DatabaseConnection.getInstance();
            if (dbConn != null && dbConn.getConnection() != null) {
                System.out.println("✓ Database connection successful");
                System.out.println("  Database path: " + DatabaseConnection.getDatabasePath());
            } else {
                System.out.println("✗ Database connection failed");
            }
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * Test 2: Create Production Records
     */
    private static void testCreateProduction(EggProductionDAO dao) {
        System.out.println("--- Test 2: Create Production Records ---");

        // Create test record for House H2
        EggProduction production1 = new EggProduction(
                2,                          // House ID (H2)
                LocalDate.now(),           // Today
                4500,                      // Eggs collected
                120,                       // Cracked eggs
                3                          // Dead chickens
        );
        production1.setCollectedBy("Ahmed");
        production1.setNotes("Test record - Normal production day");

        boolean created1 = dao.addProduction(production1);
        if (created1) {
            System.out.println("✓ Record 1 created successfully (ID: " + production1.getId() + ")");
            System.out.println("  House: H2");
            System.out.println("  Date: " + production1.getProductionDate());
            System.out.println("  Eggs Collected: " + production1.getEggsCollected());
            System.out.println("  Good Eggs: " + production1.getGoodEggs());
            System.out.println("  Efficiency: " + String.format("%.2f%%", production1.getEfficiencyRate()));
        } else {
            System.out.println("✗ Failed to create record 1");
        }

        // Create test record for House H3
        EggProduction production2 = new EggProduction(
                3,                          // House ID (H3)
                LocalDate.now(),           // Today
                3200,                      // Eggs collected
                85,                        // Cracked eggs
                1                          // Dead chickens
        );
        production2.setCollectedBy("Fatima");
        production2.setNotes("Test record - Good quality eggs");

        boolean created2 = dao.addProduction(production2);
        if (created2) {
            System.out.println("✓ Record 2 created successfully (ID: " + production2.getId() + ")");
            System.out.println("  House: H3");
            System.out.println("  Efficiency: " + String.format("%.2f%%", production2.getEfficiencyRate()));
        } else {
            System.out.println("✗ Failed to create record 2");
        }

        // Create record for yesterday
        EggProduction production3 = new EggProduction(
                2,                          // House ID (H2)
                LocalDate.now().minusDays(1), // Yesterday
                4350,                      // Eggs collected
                105,                       // Cracked eggs
                2                          // Dead chickens
        );
        production3.setCollectedBy("Hassan");
        production3.setNotes("Test record - Yesterday's production");

        boolean created3 = dao.addProduction(production3);
        if (created3) {
            System.out.println("✓ Record 3 created successfully (ID: " + production3.getId() + ")");
        } else {
            System.out.println("✗ Failed to create record 3");
        }

        System.out.println();
    }

    /**
     * Test 3: Read Production Records
     */
    private static void testReadProduction(EggProductionDAO dao) {
        System.out.println("--- Test 3: Read Production Records ---");

        // Test get by ID
        EggProduction production = dao.getProductionById(1);
        if (production != null) {
            System.out.println("✓ Read by ID successful");
            System.out.println("  ID: " + production.getId());
            System.out.println("  House: H" + production.getHouseId());
            System.out.println("  Date: " + production.getProductionDate());
            System.out.println("  Collected By: " + production.getCollectedBy());
        } else {
            System.out.println("✗ Read by ID failed (Record might not exist yet)");
        }

        // Test get all
        List<EggProduction> allProduction = dao.getAllProduction();
        System.out.println("✓ Retrieved all records: " + allProduction.size() + " total");

        // Display first 3 records
        if (allProduction.size() > 0) {
            System.out.println("\n  Recent records:");
            allProduction.stream()
                    .limit(3)
                    .forEach(p -> System.out.println("    - " + p.getProductionDate() +
                            " | H" + p.getHouseId() +
                            " | " + p.getEggsCollected() + " eggs (" +
                            String.format("%.1f%%", p.getEfficiencyRate()) + ")"));
        }

        System.out.println();
    }

    /**
     * Test 4: Update Production Record
     */
    private static void testUpdateProduction(EggProductionDAO dao) {
        System.out.println("--- Test 4: Update Production Record ---");

        // Get first record
        EggProduction production = dao.getProductionById(1);
        if (production != null) {
            int originalEggs = production.getEggsCollected();
            int originalCracked = production.getCrackedEggs();

            // Update values
            production.setEggsCollected(4600);
            production.setCrackedEggs(100);
            production.setNotes("UPDATED: Recounted eggs");
            production.calculateGoodEggs();

            boolean updated = dao.updateProduction(production);
            if (updated) {
                System.out.println("✓ Update successful");
                System.out.println("  Original eggs: " + originalEggs + " → Updated: " + production.getEggsCollected());
                System.out.println("  Original cracked: " + originalCracked + " → Updated: " + production.getCrackedEggs());
                System.out.println("  New good eggs: " + production.getGoodEggs());
            } else {
                System.out.println("✗ Update failed");
            }
        } else {
            System.out.println("✗ No record found to update");
        }

        System.out.println();
    }

    /**
     * Test 5: Query Methods
     */
    private static void testQueryMethods(EggProductionDAO dao) {
        System.out.println("--- Test 5: Query Methods ---");

        // Test get by date
        List<EggProduction> todayProduction = dao.getProductionByDate(LocalDate.now());
        System.out.println("✓ Today's production records: " + todayProduction.size());
        for (EggProduction p : todayProduction) {
            System.out.println("  - House H" + p.getHouseId() + ": " +
                    p.getEggsCollected() + " eggs (" +
                    String.format("%.1f%%", p.getEfficiencyRate()) + " efficiency)");
        }

        // Test get by house
        List<EggProduction> house2Production = dao.getProductionByHouse(2);
        System.out.println("✓ House H2 records: " + house2Production.size());

        // Test last 7 days
        List<EggProduction> last7Days = dao.getLast7Days();
        System.out.println("✓ Last 7 days records: " + last7Days.size());

        // Test production count
        int count = dao.getProductionCountByHouse(2);
        System.out.println("✓ Production count for H2: " + count);

        System.out.println();
    }

    /**
     * Test 6: Statistics Methods
     */
    private static void testStatistics(EggProductionDAO dao) {
        System.out.println("--- Test 6: Statistics Methods ---");

        // Test eggs today
        int eggsToday = dao.getEggsToday();
        System.out.println("✓ Total good eggs today: " + eggsToday);

        // Test date range total
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        int weekTotal = dao.getTotalEggsByDateRange(weekAgo, LocalDate.now());
        System.out.println("✓ Total eggs last 7 days: " + weekTotal);

        // Test average efficiency
        double avgEfficiency = dao.getAverageEfficiencyByHouse(2);
        System.out.println("✓ Average efficiency for H2: " + String.format("%.2f%%", avgEfficiency));

        // Test dead chickens
        int deadChickens = dao.getTotalDeadChickens(2, weekAgo, LocalDate.now());
        System.out.println("✓ Total dead chickens (H2, last 7 days): " + deadChickens);

        // Efficiency interpretation
        if (avgEfficiency >= 95) {
            System.out.println("  Status: Excellent quality control ✓");
        } else if (avgEfficiency >= 90) {
            System.out.println("  Status: Good quality ✓");
        } else if (avgEfficiency >= 85) {
            System.out.println("  Status: Acceptable, monitor closely ⚠");
        } else {
            System.out.println("  Status: Poor quality, investigate issues ⚠");
        }

        System.out.println();
    }

    /**
     * Test 7: Delete Production Record
     */
    private static void testDeleteProduction(EggProductionDAO dao) {
        System.out.println("--- Test 7: Delete Production Record ---");

        // Create a temporary record for deletion
        EggProduction tempProduction = new EggProduction(
                2,
                LocalDate.now().minusDays(30), // Old record
                1000,
                50,
                0
        );
        tempProduction.setNotes("Temporary test record for deletion");

        boolean created = dao.addProduction(tempProduction);
        if (created) {
            int tempId = tempProduction.getId();
            System.out.println("✓ Created temporary record (ID: " + tempId + ")");

            // Delete the record
            boolean deleted = dao.deleteProduction(tempId);
            if (deleted) {
                System.out.println("✓ Delete successful");

                // Verify deletion
                EggProduction checkDeleted = dao.getProductionById(tempId);
                if (checkDeleted == null) {
                    System.out.println("✓ Deletion verified - record not found");
                } else {
                    System.out.println("✗ Deletion failed - record still exists");
                }
            } else {
                System.out.println("✗ Delete failed");
            }
        } else {
            System.out.println("✗ Could not create temporary record for deletion test");
        }

        System.out.println();
    }
}