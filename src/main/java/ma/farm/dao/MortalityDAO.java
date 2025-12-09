// MortalityDAO.java - Updated for SQLite compatibility
package ma.farm.dao;

import ma.farm.model.Mortality;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MortalityDAO {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DatabaseConnection dbConnection;

    public MortalityDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    // Alternative recordMortality method for SQLite without getGeneratedKeys support
    public int recordMortality(Mortality mortality) {
        String sql = "INSERT INTO mortality (houseId, deathDate, count, cause, " +
                "symptoms, isOutbreak, recordedBy, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String getLastIdSql = "SELECT last_insert_rowid() as id";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             Statement stmt = conn.createStatement()) {

            pstmt.setInt(1, mortality.getHouseId());
            pstmt.setString(2, mortality.getDeathDate().format(DATE_FORMATTER));
            pstmt.setInt(3, mortality.getCount());
            pstmt.setString(4, mortality.getCause());
            pstmt.setString(5, mortality.getSymptoms());
            pstmt.setInt(6, mortality.getIsOutbreak() ? 1 : 0);
            pstmt.setString(7, mortality.getRecordedBy());
            pstmt.setString(8, mortality.getNotes());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.executeQuery(getLastIdSql)) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            }

            return -1;

        } catch (SQLException e) {
            System.err.println("Error recording mortality: " + e.getMessage());
            return -1;
        }
    }

    // Get deaths recorded today
    public List<Mortality> getTodayDeaths() {
        LocalDate today = LocalDate.now();
        return getMortalityByDate(today);
    }

    // Get deaths by specific date
    public List<Mortality> getMortalityByDate(LocalDate date) {
        String sql = "SELECT * FROM mortality WHERE deathDate = ? ORDER BY recorded_at DESC";
        List<Mortality> records = new ArrayList<>();

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setString(1, date.format(DATE_FORMATTER));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(resultSetToMortality(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting mortality by date: " + e.getMessage());
        }

        return records;
    }

    // Get total deaths this week
    public int getTotalDeathsThisWeek() {
        String sql = "SELECT SUM(count) as total FROM mortality " +
                "WHERE deathDate >= date('now', '-7 days')";

        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error getting total deaths this week: " + e.getMessage());
        }

        return 0;
    }

    // Get total deaths this month
    public int getTotalDeathsThisMonth() {
        String sql = "SELECT SUM(count) as total FROM mortality " +
                "WHERE strftime('%Y-%m', deathDate) = strftime('%Y-%m', 'now')";

        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error getting total deaths this month: " + e.getMessage());
        }

        return 0;
    }

    // Get total deaths in a house
    public int getTotalDeathsInHouse(int houseId) {
        String sql = "SELECT SUM(count) as total FROM mortality WHERE houseId = ?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, houseId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error getting total deaths in house: " + e.getMessage());
        }

        return 0;
    }

    // Get mortality by house with date range
    public List<Mortality> getMortalityByHouse(int houseId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM mortality WHERE houseId = ? " +
                "AND deathDate >= ? AND deathDate <= ? " +
                "ORDER BY deathDate DESC";
        List<Mortality> records = new ArrayList<>();

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, houseId);
            pstmt.setString(2, startDate.format(DATE_FORMATTER));
            pstmt.setString(3, endDate.format(DATE_FORMATTER));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(resultSetToMortality(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting mortality by house with date range: " + e.getMessage());
        }

        return records;
    }

    // Get all mortality records
    public List<Mortality> getAllMortalityRecords() {
        String sql = "SELECT * FROM mortality ORDER BY deathDate DESC, recorded_at DESC";
        List<Mortality> records = new ArrayList<>();

        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                records.add(resultSetToMortality(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting all mortality records: " + e.getMessage());
        }

        return records;
    }

    // Get mortality by house
    public List<Mortality> getMortalityByHouse(int houseId) {
        String sql = "SELECT * FROM mortality WHERE houseId = ? ORDER BY deathDate DESC";
        List<Mortality> records = new ArrayList<>();

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, houseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(resultSetToMortality(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting mortality by house: " + e.getMessage());
        }

        return records;
    }

    // Get mortality by date range
    public List<Mortality> getMortalityByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM mortality WHERE deathDate BETWEEN ? AND ? ORDER BY deathDate DESC";
        List<Mortality> records = new ArrayList<>();

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setString(1, startDate.format(DATE_FORMATTER));
            pstmt.setString(2, endDate.format(DATE_FORMATTER));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(resultSetToMortality(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting mortality by date range: " + e.getMessage());
        }

        return records;
    }

    // Get mortality by cause
    public List<Mortality> getMortalityByCause(String cause) {
        String sql = "SELECT * FROM mortality WHERE cause = ? ORDER BY deathDate DESC";
        List<Mortality> records = new ArrayList<>();

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setString(1, cause);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(resultSetToMortality(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting mortality by cause: " + e.getMessage());
        }

        return records;
    }

    // Get outbreak records
    public List<Mortality> getOutbreakRecords() {
        String sql = "SELECT * FROM mortality WHERE isOutbreak = 1 ORDER BY deathDate DESC";
        List<Mortality> records = new ArrayList<>();

        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                records.add(resultSetToMortality(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting outbreak records: " + e.getMessage());
        }

        return records;
    }

    // Update mortality record
    public boolean updateMortality(Mortality mortality) {
        String sql = "UPDATE mortality SET houseId = ?, deathDate = ?, count = ?, " +
                "cause = ?, symptoms = ?, isOutbreak = ?, recordedBy = ?, notes = ? " +
                "WHERE id = ?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, mortality.getHouseId());
            pstmt.setString(2, mortality.getDeathDate().format(DATE_FORMATTER));
            pstmt.setInt(3, mortality.getCount());
            pstmt.setString(4, mortality.getCause());
            pstmt.setString(5, mortality.getSymptoms());
            pstmt.setInt(6, mortality.getIsOutbreak() ? 1 : 0);
            pstmt.setString(7, mortality.getRecordedBy());
            pstmt.setString(8, mortality.getNotes());
            pstmt.setInt(9, mortality.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating mortality record: " + e.getMessage());
            return false;
        }
    }

    // Delete mortality record
    public boolean deleteMortality(int id) {
        String sql = "DELETE FROM mortality WHERE id = ?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting mortality record: " + e.getMessage());
            return false;
        }
    }

    // Helper method to convert ResultSet to Mortality object with proper date handling
    private Mortality resultSetToMortality(ResultSet rs) throws SQLException {
        Mortality mortality = new Mortality();
        mortality.setId(rs.getInt("id"));
        mortality.setHouseId(rs.getInt("houseId"));

        String deathDateStr = rs.getString("deathDate");
        if (deathDateStr != null && !deathDateStr.isEmpty()) {
            try {
                mortality.setDeathDate(LocalDate.parse(deathDateStr, DATE_FORMATTER));
            } catch (Exception e) {
                System.err.println("Error parsing date: " + deathDateStr + " - " + e.getMessage());
                mortality.setDeathDate(LocalDate.now());
            }
        } else {
            mortality.setDeathDate(LocalDate.now());
        }

        mortality.setCount(rs.getInt("count"));
        mortality.setCause(rs.getString("cause"));
        mortality.setSymptoms(rs.getString("symptoms"));
        mortality.setIsOutbreak(rs.getInt("isOutbreak") == 1);
        mortality.setRecordedBy(rs.getString("recordedBy"));
        mortality.setNotes(rs.getString("notes"));
        mortality.setRecordedAt(rs.getString("recorded_at"));
        return mortality;
    }

    // Get mortality statistics
    public MortalityStatistics getMortalityStatistics() {
        MortalityStatistics stats = new MortalityStatistics();

        // Get total records
        String sqlTotal = "SELECT COUNT(*) as totalRecords, SUM(count) as totalDeaths FROM mortality";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sqlTotal)) {

            if (rs.next()) {
                stats.setTotalRecords(rs.getInt("totalRecords"));
                stats.setTotalDeaths(rs.getInt("totalDeaths"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting total mortality statistics: " + e.getMessage());
        }

        // Get today's deaths
        stats.setTodayDeaths(getTodayDeathsCount());

        // Get this week's deaths
        stats.setThisWeekDeaths(getTotalDeathsThisWeek());

        // Get this month's deaths
        stats.setThisMonthDeaths(getTotalDeathsThisMonth());

        // Get outbreak count
        String sqlOutbreak = "SELECT COUNT(*) as outbreakCount FROM mortality WHERE isOutbreak = 1";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sqlOutbreak)) {

            if (rs.next()) {
                stats.setOutbreakCount(rs.getInt("outbreakCount"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting outbreak count: " + e.getMessage());
        }

        return stats;
    }

    // Helper method to get today's death count
    private int getTodayDeathsCount() {
        LocalDate today = LocalDate.now();
        String sql = "SELECT SUM(count) as total FROM mortality WHERE deathDate = ?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {

            pstmt.setString(1, today.format(DATE_FORMATTER));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error getting today's death count: " + e.getMessage());
        }

        return 0;
    }

    // Statistics class
    public static class MortalityStatistics {
        private int totalRecords;
        private int totalDeaths;
        private int todayDeaths;
        private int thisWeekDeaths;
        private int thisMonthDeaths;
        private int outbreakCount;

        // Getters and Setters
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

        public int getTotalDeaths() { return totalDeaths; }
        public void setTotalDeaths(int totalDeaths) { this.totalDeaths = totalDeaths; }

        public int getTodayDeaths() { return todayDeaths; }
        public void setTodayDeaths(int todayDeaths) { this.todayDeaths = todayDeaths; }

        public int getThisWeekDeaths() { return thisWeekDeaths; }
        public void setThisWeekDeaths(int thisWeekDeaths) { this.thisWeekDeaths = thisWeekDeaths; }

        public int getThisMonthDeaths() { return thisMonthDeaths; }
        public void setThisMonthDeaths(int thisMonthDeaths) { this.thisMonthDeaths = thisMonthDeaths; }

        public int getOutbreakCount() { return outbreakCount; }
        public void setOutbreakCount(int outbreakCount) { this.outbreakCount = outbreakCount; }

        @Override
        public String toString() {
            return String.format(
                    "Mortality Statistics:\n" +
                            "  Total Records: %d\n" +
                            "  Total Deaths: %d\n" +
                            "  Today's Deaths: %d\n" +
                            "  This Week's Deaths: %d\n" +
                            "  This Month's Deaths: %d\n" +
                            "  Outbreaks: %d",
                    totalRecords, totalDeaths, todayDeaths, thisWeekDeaths, thisMonthDeaths, outbreakCount
            );
        }
    }
}