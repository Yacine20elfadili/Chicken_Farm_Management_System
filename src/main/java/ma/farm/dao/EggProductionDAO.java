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
     * Update an existing egg production record
     * @param production the production to update
     * @return true if update successful
     */
    public boolean updateProduction(EggProduction production) {
        String sql = "UPDATE egg_production SET eggsCollected = ?, crackedEggs = ?, " +
                     "goodEggs = ?, notes = ?, collectedBy = ? WHERE id = ?";
        
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, production.getEggsCollected());
            stmt.setInt(2, production.getCrackedEggs());
            stmt.setInt(3, production.getGoodEggs());
            stmt.setString(4, production.getNotes());
            stmt.setString(5, production.getCollectedBy());
            stmt.setInt(6, production.getId());
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Production record updated: " + rowsAffected + " rows");
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating production: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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

}