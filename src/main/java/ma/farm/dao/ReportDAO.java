package ma.farm.dao;

import ma.farm.model.Report;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ReportDAO - Data Access Object for managing Report records.
 */
public class ReportDAO {

    private DatabaseConnection dbConnection;

    public ReportDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Save a new report record.
     */
    public int saveReport(Report report) {
        String sql = """
        INSERT INTO reports
        (title, type, periodStart, periodEnd, generatedDate, format, filePath, createdBy, notes)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = dbConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, report.getTitle());
            stmt.setString(2, report.getType().name());
            stmt.setString(3, report.getPeriodStart().toString());
            stmt.setString(4, report.getPeriodEnd().toString());
            stmt.setString(5, report.getGeneratedDate().toString());
            stmt.setString(6, report.getFormat().name());
            stmt.setString(7, report.getFilePath());

            if (report.getCreatedBy() != null) {
                stmt.setInt(8, report.getCreatedBy());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            stmt.setString(9, report.getNotes());

            stmt.executeUpdate();

            // ✅ SQLite way to get generated ID
            try (Statement s = dbConnection.getConnection().createStatement();
                 ResultSet rs = s.executeQuery("SELECT last_insert_rowid()")) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error saving report: " + e.getMessage());
        }

        return -1;
    }

    /**
     * Get all reports.
     */
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports ORDER BY generatedDate DESC";

        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reports.add(extractReportFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reports: " + e.getMessage());
        }
        return reports;
    }

    /**
     * Get reports by type.
     */
    public List<Report> getReportsByType(Report.ReportType type) {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE type = ? ORDER BY generatedDate DESC";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {

            stmt.setString(1, type.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reports.add(extractReportFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reports by type: " + e.getMessage());
        }
        return reports;
    }

    /**
     * Delete a report by ID.
     */
    public boolean deleteReport(int reportId) {
        String sql = "DELETE FROM reports WHERE id = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, reportId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting report: " + e.getMessage());
        }
        return false;
    }

    private Report extractReportFromResultSet(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setId(rs.getInt("id"));
        report.setTitle(rs.getString("title"));
        report.setType(Report.ReportType.valueOf(rs.getString("type")));
        report.setPeriodStart(LocalDate.parse(rs.getString("periodStart")));
        report.setPeriodEnd(LocalDate.parse(rs.getString("periodEnd")));
        report.setGeneratedDate(LocalDate.parse(rs.getString("generatedDate")));
        report.setFormat(Report.ReportFormat.valueOf(rs.getString("format")));
        report.setFilePath(rs.getString("filePath"));
        report.setCreatedBy(rs.getObject("createdBy") != null ? rs.getInt("createdBy") : null);
        report.setNotes(rs.getString("notes"));
        report.setCreatedAt(rs.getString("created_at"));
        return report;
    }
}