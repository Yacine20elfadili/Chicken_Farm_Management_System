// ChickenDAO.java
package ma.farm.dao;

import ma.farm.model.Chicken;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChickenDAO {

    private final DatabaseConnection dbConnection;

    public ChickenDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    // Create a new chicken batch
    public boolean createChickenBatch(Chicken chicken) {
        String sql = "INSERT INTO chickens (houseId, batchNumber, quantity, arrivalDate, " +
                "ageInDays, gender, healthStatus, averageWeight, nextTransferDate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, chicken.getHouseId());
            pstmt.setString(2, chicken.getBatchNumber());
            pstmt.setInt(3, chicken.getQuantity());
            pstmt.setString(4, chicken.getArrivalDate().toString());
            pstmt.setInt(5, chicken.getAgeInDays());
            pstmt.setString(6, chicken.getGender());
            pstmt.setString(7, chicken.getHealthStatus());
            if (chicken.getAverageWeight() != null) {
                pstmt.setDouble(8, chicken.getAverageWeight());
            } else {
                pstmt.setNull(8, Types.DOUBLE);
            }
            if (chicken.getNextTransferDate() != null) {
                pstmt.setString(9, chicken.getNextTransferDate().toString());
            } else {
                pstmt.setNull(9, Types.VARCHAR);
            }

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error creating chicken batch: " + e.getMessage());
            return false;
        }
    }

    // Get chicken batch by ID
    public Chicken getChickenById(int id) {
        String sql = "SELECT * FROM chickens WHERE id = ?";
        Chicken chicken = null;

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                chicken = resultSetToChicken(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error getting chicken by ID: " + e.getMessage());
        }

        return chicken;
    }

    // Get all chickens
    public List<Chicken> getAllChickens() {
        String sql = "SELECT * FROM chickens ORDER BY arrivalDate DESC";
        List<Chicken> chickens = new ArrayList<>();

        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                chickens.add(resultSetToChicken(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting all chickens: " + e.getMessage());
        }

        return chickens;
    }

    // Get chickens by house
    public List<Chicken> getChickensByHouse(int houseId) {
        String sql = "SELECT * FROM chickens WHERE houseId = ? ORDER BY arrivalDate DESC";
        List<Chicken> chickens = new ArrayList<>();

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, houseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                chickens.add(resultSetToChicken(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting chickens by house: " + e.getMessage());
        }

        return chickens;
    }

    // Get chickens by batch number
    public List<Chicken> getChickensByBatch(String batchNumber) {
        String sql = "SELECT * FROM chickens WHERE batchNumber = ? ORDER BY arrivalDate DESC";
        List<Chicken> chickens = new ArrayList<>();

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setString(1, batchNumber);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                chickens.add(resultSetToChicken(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting chickens by batch: " + e.getMessage());
        }

        return chickens;
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


    // Get chickens due for transfer
    public List<Chicken> getChickensDueForTransfer(LocalDate date) {
        String sql = "SELECT * FROM chickens WHERE nextTransferDate <= ? AND nextTransferDate IS NOT NULL ORDER BY nextTransferDate ASC";
        List<Chicken> chickens = new ArrayList<>();

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setString(1, date.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                chickens.add(resultSetToChicken(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting chickens due for transfer: " + e.getMessage());
        }

        return chickens;
    }

    // Update chicken batch
    public boolean updateChickenBatch(Chicken chicken) {
        String sql = "UPDATE chickens SET houseId = ?, batchNumber = ?, quantity = ?, " +
                "arrivalDate = ?, ageInDays = ?, gender = ?, healthStatus = ?, " +
                "averageWeight = ?, nextTransferDate = ? WHERE id = ?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, chicken.getHouseId());
            pstmt.setString(2, chicken.getBatchNumber());
            pstmt.setInt(3, chicken.getQuantity());
            pstmt.setString(4, chicken.getArrivalDate().toString());
            pstmt.setInt(5, chicken.getAgeInDays());
            pstmt.setString(6, chicken.getGender());
            pstmt.setString(7, chicken.getHealthStatus());
            if (chicken.getAverageWeight() != null) {
                pstmt.setDouble(8, chicken.getAverageWeight());
            } else {
                pstmt.setNull(8, Types.DOUBLE);
            }
            if (chicken.getNextTransferDate() != null) {
                pstmt.setString(9, chicken.getNextTransferDate().toString());
            } else {
                pstmt.setNull(9, Types.VARCHAR);
            }
            pstmt.setInt(10, chicken.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating chicken batch: " + e.getMessage());
            return false;
        }
    }

    // Delete chicken batch
    public boolean deleteChickenBatch(int id) {
        String sql = "DELETE FROM chickens WHERE id = ?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting chicken batch: " + e.getMessage());
            return false;
        }
    }

    // Get total chickens in house
    public int getTotalChickensInHouse(int houseId) {
        String sql = "SELECT SUM(quantity) as total FROM chickens WHERE houseId = ?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, houseId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error getting total chickens in house: " + e.getMessage());
        }

        return 0;
    }

    // Get chickens by health status
    public List<Chicken> getChickensByHealthStatus(String healthStatus) {
        String sql = "SELECT * FROM chickens WHERE healthStatus = ? ORDER BY arrivalDate DESC";
        List<Chicken> chickens = new ArrayList<>();

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setString(1, healthStatus);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                chickens.add(resultSetToChicken(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting chickens by health status: " + e.getMessage());
        }

        return chickens;
    }

    // Get statistics
    public ChickenStatistics getChickenStatistics() {
        String sql = "SELECT " +
                "COUNT(*) as totalBatches, " +
                "SUM(quantity) as totalChickens, " +
                "AVG(averageWeight) as avgWeight, " +
                "MIN(arrivalDate) as earliestArrival, " +
                "MAX(arrivalDate) as latestArrival " +
                "FROM chickens";

        ChickenStatistics stats = new ChickenStatistics();

        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                stats.setTotalBatches(rs.getInt("totalBatches"));
                stats.setTotalChickens(rs.getInt("totalChickens"));
                stats.setAverageWeight(rs.getDouble("avgWeight"));
                stats.setEarliestArrival(rs.getString("earliestArrival"));
                stats.setLatestArrival(rs.getString("latestArrival"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting chicken statistics: " + e.getMessage());
        }

        return stats;
    }

    // Helper method to convert ResultSet to Chicken object
    private Chicken resultSetToChicken(ResultSet rs) throws SQLException {
        Chicken chicken = new Chicken();
        chicken.setId(rs.getInt("id"));
        chicken.setHouseId(rs.getInt("houseId"));
        chicken.setBatchNumber(rs.getString("batchNumber"));
        chicken.setQuantity(rs.getInt("quantity"));

        String arrivalDateStr = rs.getString("arrivalDate");
        if (arrivalDateStr != null) {
            chicken.setArrivalDate(LocalDate.parse(arrivalDateStr));
        }

        chicken.setAgeInDays(rs.getInt("ageInDays"));
        chicken.setGender(rs.getString("gender"));
        chicken.setHealthStatus(rs.getString("healthStatus"));
        chicken.setAverageWeight(rs.getDouble("averageWeight"));

        String nextTransferDateStr = rs.getString("nextTransferDate");
        if (nextTransferDateStr != null) {
            chicken.setNextTransferDate(LocalDate.parse(nextTransferDateStr));
        }

        return chicken;
    }

    // Statistics inner class
    public static class ChickenStatistics {
        private int totalBatches;
        private int totalChickens;
        private double averageWeight;
        private String earliestArrival;
        private String latestArrival;

        // Getters and Setters
        public int getTotalBatches() { return totalBatches; }
        public void setTotalBatches(int totalBatches) { this.totalBatches = totalBatches; }

        public int getTotalChickens() { return totalChickens; }
        public void setTotalChickens(int totalChickens) { this.totalChickens = totalChickens; }

        public double getAverageWeight() { return averageWeight; }
        public void setAverageWeight(double averageWeight) { this.averageWeight = averageWeight; }

        public String getEarliestArrival() { return earliestArrival; }
        public void setEarliestArrival(String earliestArrival) { this.earliestArrival = earliestArrival; }

        public String getLatestArrival() { return latestArrival; }
        public void setLatestArrival(String latestArrival) { this.latestArrival = latestArrival; }

        @Override
        public String toString() {
            return String.format("Total Batches: %d, Total Chickens: %d, Average Weight: %.2f kg",
                    totalBatches, totalChickens, averageWeight);
        }
    }
}