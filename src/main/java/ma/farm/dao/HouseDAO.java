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
    /**
     * Main method for comprehensive testing of HouseDAO
     * Tests all major functionalities: CRUD, queries, updates, and statistics.
     * Assumes a clean database state before running; may leave test data behind.
     */
    public static void main(String[] args) {
        System.out.println("=== Comprehensive HouseDAO Test Suite ===\n");

        HouseDAO dao = new HouseDAO();
        int testHouse1Id;
        int testHouse2Id;
        int testHouse3Id;
        int testHouse4Id;

        try {
            // Test 1: Create Houses for all HouseType variants
            System.out.println("--- Test 1: Create Houses ---");
            House house1 = new House("H1", HouseType.DAY_OLD, 10000);
            house1.setChickenCount(9500);
            house1.setHealthStatus(HealthStatus.GOOD);
            house1.setLastCleaningDate(LocalDate.now().minusDays(3));
            house1.setCreationDate(LocalDate.now());

            boolean created1 = dao.addHouse(house1);
            testHouse1Id = house1.getId();
            if (created1) {
                System.out.println("✓ House H1 (Day-old) created successfully (ID: " + testHouse1Id + ")");
            } else {
                System.err.println("✗ Failed to create House H1");
            }

            House house2 = new House("H2", HouseType.EGG_LAYER, 5000);
            house2.setChickenCount(4800);
            house2.setHealthStatus(HealthStatus.GOOD);
            house2.setLastCleaningDate(LocalDate.now().minusDays(5));
            house2.setCreationDate(LocalDate.now());

            boolean created2 = dao.addHouse(house2);
            testHouse2Id = house2.getId();
            if (created2) {
                System.out.println("✓ House H2 (Egg Layer) created successfully (ID: " + testHouse2Id + ")");
            } else {
                System.err.println("✗ Failed to create House H2");
            }

            House house3 = new House("H3", HouseType.MEAT_FEMALE, 4500);
            house3.setChickenCount(4200);
            house3.setHealthStatus(HealthStatus.FAIR);
            house3.setLastCleaningDate(LocalDate.now().minusDays(10));
            house3.setCreationDate(LocalDate.now());

            boolean created3 = dao.addHouse(house3);
            testHouse3Id = house3.getId();
            if (created3) {
                System.out.println("✓ House H3 (Meat Female) created successfully (ID: " + testHouse3Id + ")");
            } else {
                System.err.println("✗ Failed to create House H3");
            }

            House house4 = new House("H4", HouseType.MEAT_MALE, 6000);
            house4.setChickenCount(5800);
            house4.setHealthStatus(HealthStatus.GOOD);
            house4.setLastCleaningDate(LocalDate.now().minusDays(1));
            house4.setCreationDate(LocalDate.now());

            boolean created4 = dao.addHouse(house4);
            testHouse4Id = house4.getId();
            if (created4) {
                System.out.println("✓ House H4 (Meat Male) created successfully (ID: " + testHouse4Id + ")");
            } else {
                System.err.println("✗ Failed to create House H4");
            }

            // Test 2: Read Houses (All, By ID, By Name, By Type)
            System.out.println("\n--- Test 2: Read Houses ---");
            List<House> allHouses = dao.getAllHouses();
            System.out.println("✓ Retrieved all houses: " + allHouses.size() + " houses");

            if (!allHouses.isEmpty()) {
                for (House h : allHouses) {
                    String typeDisplay = h.getType() != null ? h.getType().getDisplayName() : "Unknown";
                    String healthDisplay = h.getHealthStatus() != null ? h.getHealthStatus().getDisplayName() : "Unknown";
                    System.out.println("  - " + h.getName() + " (ID: " + h.getId() + "): " +
                            typeDisplay + " (" + h.getChickenCount() + "/" +
                            h.getCapacity() + ") - " + healthDisplay +
                            " - Last cleaned: " + (h.getLastCleaningDate() != null ? h.getLastCleaningDate() : "Never"));
                }
            }

            House retrievedById = dao.getHouseById(testHouse1Id);
            if (retrievedById != null && "H1".equals(retrievedById.getName())) {
                System.out.println("✓ Successfully retrieved House H1 by ID");
            } else {
                System.err.println("✗ Failed to retrieve House H1 by ID");
            }

            House retrievedByName = dao.getHouseByName("H4");
            if (retrievedByName != null && testHouse4Id == retrievedByName.getId()) {
                System.out.println("✓ Successfully retrieved House H4 by name");
            } else {
                System.err.println("✗ Failed to retrieve House H4 by name");
            }

            List<House> dayOlds = dao.getHousesByType(HouseType.DAY_OLD);
            System.out.println("✓ Retrieved Day-old houses: " + dayOlds.size());

            List<House> eggLayers = dao.getHousesByType(HouseType.EGG_LAYER);
            System.out.println("✓ Retrieved Egg Layer houses: " + eggLayers.size());

            List<House> meatFemales = dao.getHousesByType(HouseType.MEAT_FEMALE);
            System.out.println("✓ Retrieved Meat Female houses: " + meatFemales.size());

            List<House> meatMales = dao.getHousesByType(HouseType.MEAT_MALE);
            System.out.println("✓ Retrieved Meat Male houses: " + meatMales.size());

            // Test 3: Update Operations (Chicken Count, Health Status, Cleaning Date, Full Update)
            System.out.println("\n--- Test 3: Update Operations ---");
            boolean chickenUpdate = dao.updateChickenCount(testHouse2Id, 4750);
            if (chickenUpdate) {
                System.out.println("✓ Updated chicken count for H2 to 4750");
            } else {
                System.err.println("✗ Failed to update chicken count for H2");
            }

            boolean healthUpdate = dao.updateHealthStatus(testHouse3Id, HealthStatus.GOOD);
            if (healthUpdate) {
                System.out.println("✓ Updated health status for H3 to GOOD");
            } else {
                System.err.println("✗ Failed to update health status for H3");
            }

            LocalDate newCleaningDate = LocalDate.now();
            boolean cleaningUpdate = dao.updateLastCleaningDate(testHouse1Id, newCleaningDate);
            if (cleaningUpdate) {
                System.out.println("✓ Updated last cleaning date for H1 to " + newCleaningDate);
            } else {
                System.err.println("✗ Failed to update cleaning date for H1");
            }

            // Test full house update
            house2.setName("H2-Updated");
            house2.setCapacity(5500);
            boolean fullUpdate = dao.updateHouse(house2);
            if (fullUpdate) {
                System.out.println("✓ Full update of H2 successful");
                // Verify update
                House updatedHouse = dao.getHouseById(testHouse2Id);
                if ("H2-Updated".equals(updatedHouse.getName()) && updatedHouse.getCapacity() == 5500) {
                    System.out.println("✓ Verified full update via retrieval");
                }
            } else {
                System.err.println("✗ Failed full update of H2");
            }

            // Test 4: Statistics and Queries
            System.out.println("\n--- Test 4: Statistics and Advanced Queries ---");
            int totalChickens = dao.getTotalChickenCount();
            System.out.println("✓ Total chickens across all houses: " + totalChickens);

            List<House> housesNeedingCleaning = dao.getHousesNeedingCleaning(7); // Houses needing cleaning within 7 days
            System.out.println("✓ Houses needing cleaning (within 7 days): " + housesNeedingCleaning.size());
            if (!housesNeedingCleaning.isEmpty()) {
                for (House h : housesNeedingCleaning) {
                    System.out.println("  - " + h.getName() + " (last cleaned: " + (h.getLastCleaningDate() != null ? h.getLastCleaningDate() : "Never") + ")");
                }
            }

            // Test 5: Delete House
            System.out.println("\n--- Test 5: Delete House ---");
            boolean deleted = dao.deleteHouse(testHouse3Id);
            if (deleted) {
                System.out.println("✓ Deleted House H3 successfully");
                // Verify deletion
                House deletedCheck = dao.getHouseById(testHouse3Id);
                if (deletedCheck == null) {
                    System.out.println("✓ Verified deletion of H3 (not found)");
                }
            } else {
                System.err.println("✗ Failed to delete House H3");
            }

            // Final verification: Remaining houses
            List<House> finalHouses = dao.getAllHouses();
            System.out.println("\n✓ Final count of houses: " + finalHouses.size());

            System.out.println("\n=== Test Suite Complete ===");
            System.out.println("Note: Test data (e.g., H1, H2-Updated, H4) may remain in the database. Clean up as needed.");

        } catch (Exception e) {
            System.err.println("✗ Unexpected error during testing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
