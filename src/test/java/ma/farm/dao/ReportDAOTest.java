package ma.farm.dao;

import ma.farm.model.Report;
import ma.farm.model.Report.ReportFormat;
import ma.farm.model.Report.ReportType;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReportDAOTest {

    private static ReportDAO reportDAO;

    @BeforeAll
    static void setupDatabase() throws Exception {
        reportDAO = new ReportDAO();
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reports (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title VARCHAR(100) NOT NULL,
                    type VARCHAR(50) NOT NULL,
                    periodStart DATE NOT NULL,
                    periodEnd DATE NOT NULL,
                    generatedDate DATE NOT NULL,
                    format VARCHAR(20),
                    filePath TEXT,
                    createdBy INTEGER,
                    notes TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);
        }
    }

    @Test
    @Order(1)
    void testSaveReport() {
        Report report = new Report(
                "Monthly Production",
                ReportType.Production,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31)
        );
        report.setFormat(ReportFormat.View);
        report.setNotes("January production report");

        int generatedId = reportDAO.saveReport(report);

        assertTrue(generatedId > 0, "Report ID should be generated");
    }

    @Test
    @Order(2)
    void testGetAllReports() {
        List<Report> reports = reportDAO.getAllReports();

        assertNotNull(reports);
        assertFalse(reports.isEmpty(), "Reports list should not be empty");
    }

    @Test
    @Order(3)
    void testGetReportsByType() {
        List<Report> productionReports =
                reportDAO.getReportsByType(ReportType.Production);

        assertNotNull(productionReports);
        assertFalse(productionReports.isEmpty());

        for (Report r : productionReports) {
            assertEquals(ReportType.Production, r.getType());
        }
    }

    @Test
    @Order(4)
    void testDeleteReport() {
        List<Report> reports = reportDAO.getAllReports();
        assertFalse(reports.isEmpty());

        int reportId = reports.get(0).getId();
        boolean deleted = reportDAO.deleteReport(reportId);

        assertTrue(deleted, "Report should be deleted successfully");
    }
}
