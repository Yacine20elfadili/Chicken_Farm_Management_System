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
 * - House configuration (dynamic houses per type)
 * - Capacity management
 * - Chicken lifecycle operations (import, distribute, transfer, sell)
 *
 * @author Farm Management System
 * @version 2.0
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
        String sql = "INSERT INTO houses (name, type, chickenCount, capacity, healthStatus, " +
                "lastCleaningDate, creationDate, arrivalDate, maxImportLimit, estimatedStayWeeks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, house.getName());
            stmt.setString(2, house.getTypeAsString());
            stmt.setInt(3, house.getChickenCount());
            stmt.setInt(4, house.getCapacity());
            stmt.setString(5, house.getHealthStatusAsString());
            stmt.setString(6, house.getLastCleaningDate() != null ? house.getLastCleaningDate().toString() : null);
            stmt.setString(7,
                    house.getCreationDate() != null ? house.getCreationDate().toString() : LocalDate.now().toString());
            stmt.setString(8, house.getArrivalDate() != null ? house.getArrivalDate().toString() : null);
            stmt.setInt(9, house.getMaxImportLimit());
            stmt.setInt(10, house.getEstimatedStayWeeks());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                // Get generated ID
                try (Statement idStmt = dbConnection.getConnection().createStatement();
                        ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid() AS id")) {
                    if (rs.next()) {
                        house.setId(rs.getInt("id"));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding house: " + e.getMessage());
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
                "healthStatus = ?, lastCleaningDate = ?, arrivalDate = ?, maxImportLimit = ?, estimatedStayWeeks = ? WHERE id = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, house.getName());
            stmt.setString(2, house.getTypeAsString());
            stmt.setInt(3, house.getChickenCount());
            stmt.setInt(4, house.getCapacity());
            stmt.setString(5, house.getHealthStatusAsString());
            stmt.setString(6, house.getLastCleaningDate() != null ? house.getLastCleaningDate().toString() : null);
            stmt.setString(7, house.getArrivalDate() != null ? house.getArrivalDate().toString() : null);
            stmt.setInt(8, house.getMaxImportLimit());
            stmt.setInt(9, house.getEstimatedStayWeeks());
            stmt.setInt(10, house.getId());

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
     * Deletes all houses from the database
     * Used for reconfiguration when no houses contain chickens
     *
     * @return true if successful
     */
    public boolean deleteAllHouses() {
        String sql = "DELETE FROM houses";
        try (Statement stmt = dbConnection.getConnection().createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting all houses: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes all houses of a specific type
     *
     * @param type the house type to delete
     * @return true if successful
     */
    public boolean deleteHousesByType(HouseType type) {
        String sql = "DELETE FROM houses WHERE type = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, type.getDisplayName());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting houses by type: " + e.getMessage());
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
     * @param name the house name (e.g., "DayOld-House-1")
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
        String sql = "SELECT * FROM houses ORDER BY type, name";
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
     * Gets empty houses by type (chickenCount = 0)
     *
     * @param type the house type
     * @return List of empty houses of the specified type
     */
    public List<House> getEmptyHousesByType(HouseType type) {
        String sql = "SELECT * FROM houses WHERE type = ? AND chickenCount = 0 ORDER BY name";
        List<House> houses = new ArrayList<>();

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, type.getDisplayName());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                houses.add(mapResultSetToHouse(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting empty houses by type: " + e.getMessage());
        }
        return houses;
    }

    /**
     * Gets non-empty houses by type (chickenCount > 0)
     *
     * @param type the house type
     * @return List of occupied houses of the specified type
     */
    public List<House> getOccupiedHousesByType(HouseType type) {
        String sql = "SELECT * FROM houses WHERE type = ? AND chickenCount > 0 ORDER BY name";
        List<House> houses = new ArrayList<>();

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, type.getDisplayName());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                houses.add(mapResultSetToHouse(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting occupied houses by type: " + e.getMessage());
        }
        return houses;
    }

    /**
     * Gets the count of houses by type
     *
     * @param type the house type
     * @return count of houses
     */
    public int getHouseCountByType(HouseType type) {
        String sql = "SELECT COUNT(*) FROM houses WHERE type = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, type.getDisplayName());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting houses by type: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Gets total available capacity for empty houses of a type
     *
     * @param type the house type
     * @return total available capacity
     */
    public int getTotalEmptyCapacityByType(HouseType type) {
        String sql = "SELECT COALESCE(SUM(capacity), 0) FROM houses WHERE type = ? AND chickenCount = 0";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, type.getDisplayName());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total empty capacity: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Checks if any house contains chickens
     *
     * @return true if at least one house has chickens
     */
    public boolean hasAnyChickens() {
        String sql = "SELECT COUNT(*) FROM houses WHERE chickenCount > 0";
        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking for chickens: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates the chicken count for a house
     *
     * @param houseId  the house ID
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
     * Updates the arrival date for a house
     *
     * @param houseId     the house ID
     * @param arrivalDate the arrival date
     * @return true if successful
     */
    public boolean updateArrivalDate(int houseId, LocalDate arrivalDate) {
        String sql = "UPDATE houses SET arrivalDate = ? WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, arrivalDate != null ? arrivalDate.toString() : null);
            stmt.setInt(2, houseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating arrival date: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds chickens to a house (for import or transfer operations)
     *
     * @param houseId     the house ID
     * @param count       number of chickens to add
     * @param arrivalDate the arrival date
     * @return true if successful
     */
    public boolean addChickensToHouse(int houseId, int count, LocalDate arrivalDate) {
        String sql = "UPDATE houses SET chickenCount = chickenCount + ?, arrivalDate = ? WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, count);
            stmt.setString(2, arrivalDate.toString());
            stmt.setInt(3, houseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding chickens to house: " + e.getMessage());
            return false;
        }
    }

    /**
     * Removes chickens from a house (for mortality or sell operations)
     *
     * @param houseId the house ID
     * @param count   number of chickens to remove
     * @return true if successful
     */
    public boolean removeChickensFromHouse(int houseId, int count) {
        // First get current count to check
        House house = getHouseById(houseId);
        if (house == null || house.getChickenCount() < count) {
            return false;
        }

        int newCount = house.getChickenCount() - count;
        String sql;

        if (newCount == 0) {
            // Reset house when empty
            sql = "UPDATE houses SET chickenCount = 0, arrivalDate = NULL WHERE id = ?";
            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
                stmt.setInt(1, houseId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Error resetting house: " + e.getMessage());
                return false;
            }
        } else {
            sql = "UPDATE houses SET chickenCount = ? WHERE id = ?";
            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
                stmt.setInt(1, newCount);
                stmt.setInt(2, houseId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Error removing chickens from house: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Resets a house to empty state (after distribute/transfer/sell all)
     *
     * @param houseId the house ID
     * @return true if successful
     */
    public boolean resetHouse(int houseId) {
        String sql = "UPDATE houses SET chickenCount = 0, arrivalDate = NULL WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, houseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error resetting house: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates the health status for a house
     *
     * @param houseId the house ID
     * @param status  the new health status
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
     * @param houseId      the house ID
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
     * Gets total chicken count by house type
     *
     * @param type the house type
     * @return total chicken count for that type
     */
    public int getTotalChickenCountByType(HouseType type) {
        String sql = "SELECT COALESCE(SUM(chickenCount), 0) FROM houses WHERE type = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, type.getDisplayName());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting chicken count by type: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Check if houses are configured (at least one house exists)
     *
     * @return true if at least one house exists
     */
    public boolean areHousesConfigured() {
        String sql = "SELECT COUNT(*) FROM houses";
        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking house configuration: " + e.getMessage());
        }
        return false;
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

        String arrivalStr = rs.getString("arrivalDate");
        if (arrivalStr != null && !arrivalStr.isEmpty()) {
            house.setArrivalDate(LocalDate.parse(arrivalStr));
        }

        // New fields for allocation
        house.setMaxImportLimit(rs.getInt("maxImportLimit"));
        house.setEstimatedStayWeeks(rs.getInt("estimatedStayWeeks"));

        return house;
    }

    /**
     * Gets the maximum import limit (minimum across all DayOld houses)
     *
     * @return max import limit, or 0 if no DayOld houses
     */
    public int getMaxImportLimit() {
        String sql = "SELECT MIN(maxImportLimit) FROM houses WHERE type = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, HouseType.DAY_OLD.getDisplayName());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting max import limit: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Updates the max import limit for all DayOld houses
     *
     * @param limit the max import limit
     * @return true if successful
     */
    public boolean updateMaxImportLimitForDayOld(int limit) {
        String sql = "UPDATE houses SET maxImportLimit = ? WHERE type = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setString(2, HouseType.DAY_OLD.getDisplayName());
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.err.println("Error updating max import limit: " + e.getMessage());
            return false;
        }
    }

    /**
     * Distributes chicks across all empty DayOld houses
     *
     * @param totalChicks total number of chicks to distribute
     * @param arrivalDate the arrival date
     * @return true if successful, false if not enough capacity
     */
    public boolean distributeChicksAcrossDayOldHouses(int totalChicks, LocalDate arrivalDate) {
        List<House> dayOldHouses = getEmptyHousesByType(HouseType.DAY_OLD);

        if (dayOldHouses.isEmpty()) {
            // Also try partially filled houses
            dayOldHouses = getHousesByType(HouseType.DAY_OLD);
        }

        // Calculate total available capacity
        int totalAvailable = 0;
        for (House house : dayOldHouses) {
            totalAvailable += house.getAvailableCapacity();
        }

        if (totalAvailable < totalChicks) {
            System.err.println("Not enough capacity: need " + totalChicks + ", have " + totalAvailable);
            return false;
        }

        // Distribute evenly across houses
        int remaining = totalChicks;
        for (House house : dayOldHouses) {
            if (remaining <= 0)
                break;

            int available = house.getAvailableCapacity();
            int toAdd = Math.min(remaining, available);

            if (toAdd > 0) {
                if (!addChickensToHouse(house.getId(), toAdd, arrivalDate)) {
                    System.err.println("Failed to add chickens to house: " + house.getName());
                    return false;
                }
                remaining -= toAdd;
            }
        }

        return remaining == 0;
    }

    /**
     * Gets the total capacity of all DayOld houses
     *
     * @return total DayOld capacity
     */
    public int getTotalDayOldCapacity() {
        String sql = "SELECT COALESCE(SUM(capacity), 0) FROM houses WHERE type = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, HouseType.DAY_OLD.getDisplayName());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total DayOld capacity: " + e.getMessage());
        }
        return 0;
    }
}
