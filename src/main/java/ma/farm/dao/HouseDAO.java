package ma.farm.dao;

import ma.farm.model.House;
import ma.farm.model.HouseType;
import ma.farm.model.HealthStatus;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * HouseDAO - Data Access Object for House management
 *
 * Handles all database operations for chicken houses including:
 * - CRUD operations
 * - House status tracking
 * - Capacity management
 * - Health monitoring
 *
 * @author Farm Management System
 * @version 1.0
 */
public class HouseDAO {
    private DatabaseConnection dbConnection;

    public HouseDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Adds a new house to the database
     *
     * @param house the House object to add
     * @return true if successful, false otherwise
     */
    public boolean addHouse(House house) {
        System.out.println("=== HouseDAO.addHouse() called ===");
        System.out.println("House to add: " + house);

        // Check if house has pre-set ID (for specific slots 1-4)
        boolean hasPresetId = house.getId() > 0;
        String sql;

        if (hasPresetId) {
            System.out.println("Using pre-set ID: " + house.getId());
            sql = "INSERT INTO houses (id, name, type, chickenCount, capacity, healthStatus, " +
                    "lastCleaningDate, creationDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            System.out.println("Auto-generating ID");
            sql = "INSERT INTO houses (name, type, chickenCount, capacity, healthStatus, " +
                    "lastCleaningDate, creationDate) VALUES (?, ?, ?, ?, ?, ?, ?)";
        }

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            System.out.println("Database connection obtained: " + (dbConnection.getConnection() != null));

            int paramIndex = 1;

            // Set ID if pre-set
            if (hasPresetId) {
                stmt.setInt(paramIndex++, house.getId());
                System.out.println("Set id: " + house.getId());
            }

            stmt.setString(paramIndex++, house.getName());
            System.out.println("Set name: " + house.getName());

            stmt.setString(paramIndex++, house.getTypeAsString());  // Using helper method
            System.out.println("Set type: " + house.getTypeAsString());

            stmt.setInt(paramIndex++, house.getChickenCount());
            System.out.println("Set chickenCount: " + house.getChickenCount());

            stmt.setInt(paramIndex++, house.getCapacity());
            System.out.println("Set capacity: " + house.getCapacity());

            stmt.setString(paramIndex++, house.getHealthStatusAsString());  // Using helper method
            System.out.println("Set healthStatus: " + house.getHealthStatusAsString());

            stmt.setString(paramIndex++, house.getLastCleaningDate() != null ?
                    house.getLastCleaningDate().toString() : null);
            System.out.println("Set lastCleaningDate: " + house.getLastCleaningDate());

            stmt.setString(paramIndex++, house.getCreationDate() != null ?
                    house.getCreationDate().toString() : null);
            System.out.println("Set creationDate: " + house.getCreationDate());

            System.out.println("Executing SQL: " + sql);
            int rows = stmt.executeUpdate();
            System.out.println("Rows inserted: " + rows);

            if (rows == 0) {
                System.out.println("No rows inserted!");
                return false;
            }

            // Get generated ID only if not pre-set
            if (!hasPresetId) {
                try (Statement idStmt = dbConnection.getConnection().createStatement();
                     ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid() AS id")) {
                    if (rs.next()) {
                        int generatedId = rs.getInt("id");
                        house.setId(generatedId);
                        System.out.println("Generated ID: " + generatedId);
                    }
                }
            }

            System.out.println("House added successfully with ID: " + house.getId());
            return true;
        } catch (SQLException e) {
            System.err.println("=== SQL ERROR in addHouse ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing house
     *
     * @param house the House object with updated values
     * @return true if successful, false otherwise
     */
    public boolean updateHouse(House house) {
        String sql = "UPDATE houses SET name = ?, type = ?, chickenCount = ?, capacity = ?, " +
                "healthStatus = ?, lastCleaningDate = ? WHERE id = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, house.getName());
            stmt.setString(2, house.getTypeAsString());  // Using helper method
            stmt.setInt(3, house.getChickenCount());
            stmt.setInt(4, house.getCapacity());
            stmt.setString(5, house.getHealthStatusAsString());  // Using helper method
            stmt.setString(6, house.getLastCleaningDate() != null ?
                    house.getLastCleaningDate().toString() : null);
            stmt.setInt(7, house.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating house: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a house
     *
     * @param id the house ID
     * @return true if successful, false otherwise
     */
    public boolean deleteHouse(int id) {
        String sql = "DELETE FROM houses WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting house: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets a house by ID
     *
     * @param id the house ID
     * @return the House object or null if not found
     */
    public House getHouseById(int id) {
        String sql = "SELECT * FROM houses WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToHouse(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting house: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets a house by name
     *
     * @param name the house name (e.g., "H2")
     * @return the House object or null if not found
     */
    public House getHouseByName(String name) {
        String sql = "SELECT * FROM houses WHERE name = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToHouse(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting house by name: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets all houses
     *
     * @return List of all houses
     */
    public List<House> getAllHouses() {
        String sql = "SELECT * FROM houses ORDER BY name";
        List<House> houses = new ArrayList<>();

        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                houses.add(mapResultSetToHouse(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all houses: " + e.getMessage());
        }
        return houses;
    }

    /**
     * Gets houses by type
     *
     * @param type the house type
     * @return List of houses of the specified type
     */
    public List<House> getHousesByType(HouseType type) {
        String sql = "SELECT * FROM houses WHERE type = ? ORDER BY name";
        List<House> houses = new ArrayList<>();

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, type.getDisplayName());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                houses.add(mapResultSetToHouse(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting houses by type: " + e.getMessage());
        }
        return houses;
    }

    /**
     * Updates the chicken count for a house
     *
     * @param houseId the house ID
     * @param newCount the new chicken count
     * @return true if successful
     */
    public boolean updateChickenCount(int houseId, int newCount) {
        String sql = "UPDATE houses SET chickenCount = ? WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, newCount);
            stmt.setInt(2, houseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating chicken count: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates the health status for a house
     *
     * @param houseId the house ID
     * @param status the new health status
     * @return true if successful
     */
    public boolean updateHealthStatus(int houseId, HealthStatus status) {
        String sql = "UPDATE houses SET healthStatus = ? WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status.getDisplayName());
            stmt.setInt(2, houseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating health status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates the last cleaning date for a house
     *
     * @param houseId the house ID
     * @param cleaningDate the cleaning date
     * @return true if successful
     */
    public boolean updateLastCleaningDate(int houseId, LocalDate cleaningDate) {
        String sql = "UPDATE houses SET lastCleaningDate = ? WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, cleaningDate.toString());
            stmt.setInt(2, houseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating cleaning date: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the total chicken count across all houses
     *
     * @return total chicken count
     */
    public int getTotalChickenCount() {
        String sql = "SELECT COALESCE(SUM(chickenCount), 0) AS total FROM houses";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total chicken count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Gets houses that need cleaning (based on days since last cleaning)
     *
     * @param daysSinceLastCleaning threshold in days
     * @return List of houses needing cleaning
     */
    public List<House> getHousesNeedingCleaning(int daysSinceLastCleaning) {
        String sql = "SELECT * FROM houses WHERE lastCleaningDate IS NULL " +
                "OR date('now') > date(lastCleaningDate, '+' || ? || ' days') " +
                "ORDER BY lastCleaningDate";
        List<House> houses = new ArrayList<>();

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, daysSinceLastCleaning);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                houses.add(mapResultSetToHouse(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting houses needing cleaning: " + e.getMessage());
        }
        return houses;
    }

    /**
     * Helper method to map ResultSet to House object
     * Uses helper methods to convert String to enum
     */
    private House mapResultSetToHouse(ResultSet rs) throws SQLException {
        House house = new House();

        house.setId(rs.getInt("id"));
        house.setName(rs.getString("name"));

        // Use helper method to set type from String
        house.setTypeFromString(rs.getString("type"));

        house.setChickenCount(rs.getInt("chickenCount"));
        house.setCapacity(rs.getInt("capacity"));

        // Use helper method to set health status from String
        house.setHealthStatusFromString(rs.getString("healthStatus"));

        // Handle dates
        String lastCleaningStr = rs.getString("lastCleaningDate");
        if (lastCleaningStr != null && !lastCleaningStr.isEmpty()) {
            house.setLastCleaningDate(LocalDate.parse(lastCleaningStr));
        }

        String creationStr = rs.getString("creationDate");
        if (creationStr != null && !creationStr.isEmpty()) {
            house.setCreationDate(LocalDate.parse(creationStr));
        }

        return house;
    }

}
