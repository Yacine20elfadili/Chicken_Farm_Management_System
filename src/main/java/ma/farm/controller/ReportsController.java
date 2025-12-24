package ma.farm.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ma.farm.dao.*;
import ma.farm.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import ma.farm.util.PDFGenerator;
import java.io.File;
import ma.farm.model.Personnel;

/**
 * ReportsController - Manages the Reports page logic.
 * Generates analytical reports for Production, Financials, Inventory,
 * Consumption.
 */
public class ReportsController {

    // Filter Controls
    @FXML
    private ComboBox<String> reportTypeCombo;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button generateBtn;
    @FXML
    private Button exportPdfBtn;
    @FXML
    private Label reportTitleLabel;

    // Charts
    @FXML
    private javafx.scene.chart.BarChart<String, Number> trendChart;
    @FXML
    private javafx.scene.chart.PieChart distributionChart;

    // Report Data Table
    @FXML
    private TableView<ReportRow> reportDataTable;
    @FXML
    private TableColumn<ReportRow, String> colMetric;
    @FXML
    private TableColumn<ReportRow, String> colValue;
    @FXML
    private TableColumn<ReportRow, String> colUnit;
    @FXML
    private TableColumn<ReportRow, String> colChange;

    // History Table
    @FXML
    private TableView<Report> historyTable;
    @FXML
    private TableColumn<Report, Integer> colHistId;
    @FXML
    private TableColumn<Report, String> colHistTitle;
    @FXML
    private TableColumn<Report, String> colHistType;
    @FXML
    private TableColumn<Report, String> colHistDate;
    @FXML
    private TableColumn<Report, String> colHistPeriod;

    // DAOs
    private ReportDAO reportDAO;
    private FinancialDAO financialDAO;
    private EggProductionDAO eggProductionDAO;
    private HouseDAO houseDAO;
    private PersonnelDAO personnelDAO;

    // Data
    private ObservableList<ReportRow> reportRows = FXCollections.observableArrayList();
    private ObservableList<Report> reportHistory = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        System.out.println("ReportsController initialized");

        // Initialize DAOs
        reportDAO = new ReportDAO();
        financialDAO = new FinancialDAO();
        eggProductionDAO = new EggProductionDAO();
        houseDAO = new HouseDAO();
        personnelDAO = new PersonnelDAO();

        // Setup ComboBox
        reportTypeCombo.getItems().addAll("Production", "Financial", "Inventory", "Personnel", "Summary");
        reportTypeCombo.setValue("Summary"); // Default to Summary for cool dashboard look

        // Default dates (last 30 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));

        // Setup tables
        setupReportDataTable();
        setupHistoryTable();

        // Load history
        loadReportHistory();

        // Button actions
        generateBtn.setOnAction(e -> generateReport());
        exportPdfBtn.setOnAction(e -> exportReportToPDF());

        // Initial Generate
        generateReport();
    }

    private void exportReportToPDF() {
        try {
            String selectedType = reportTypeCombo.getValue() != null ? reportTypeCombo.getValue() : "Analytique";
            String filename = "Rapport_" + selectedType + "_" + LocalDate.now() + ".pdf";

            // Create Document
            com.itextpdf.layout.Document doc = PDFGenerator.createDocument(filename);

            // Header
            PDFGenerator.addDocumentHeader(doc,
                    "Rapport " + selectedType,
                    "RAP-" + LocalDate.now().getYear(),
                    LocalDate.now());

            // Period Filter Info
            doc.add(new com.itextpdf.layout.element.Paragraph(
                    "Période: " + startDatePicker.getValue() + " au " + endDatePicker.getValue())
                    .setMarginBottom(10));

            // Create Table
            String[] headers = { "Métrique", "Valeur", "Unité" };
            float[] widths = { 50, 25, 25 };
            com.itextpdf.layout.element.Table table = PDFGenerator.createItemsTable(headers, widths);

            // Populate Table
            for (ReportRow row : reportRows) {
                PDFGenerator.addTableRow(table, new String[] {
                        row.getMetric(),
                        row.getValue(),
                        row.getUnit()
                });
            }
            doc.add(table);

            // Footer
            PDFGenerator.addFooter(doc, "Généré par Chicken Farm Management System");
            doc.close();

            // Calculate full path for display
            String fullPath = PDFGenerator.DOCUMENTS_DIR + filename;

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Rapport Exporté");
            alert.setContentText("Fichier: " + filename + "\nEmplacement: " + PDFGenerator.DOCUMENTS_DIR);
            alert.showAndWait();

            // Try to open
            try {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(new File(fullPath));
                }
            } catch (Exception ex) {
                System.err.println("Could not open file automatically: " + ex.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'exporter le PDF: " + e.getMessage());
        }
    }

    private void setupReportDataTable() {
        colMetric.setCellValueFactory(new PropertyValueFactory<>("metric"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("value"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colChange.setCellValueFactory(new PropertyValueFactory<>("change"));
        reportDataTable.setItems(reportRows);
    }

    private void setupHistoryTable() {
        colHistId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colHistTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colHistType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().name()));
        colHistDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getGeneratedDate() != null ? data.getValue().getGeneratedDate().toString() : ""));
        colHistPeriod.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPeriodStart() + " - " + data.getValue().getPeriodEnd()));
        historyTable.setItems(reportHistory);
    }

    private void loadReportHistory() {
        reportHistory.clear();
        reportHistory.addAll(reportDAO.getAllReports());
    }

    private void generateReport() {
        String selectedType = reportTypeCombo.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null) {
            showAlert("Erreur", "Veuillez sélectionner les dates de début et de fin.");
            return;
        }
        if (start.isAfter(end)) {
            showAlert("Erreur", "La date de début doit être avant la date de fin.");
            return;
        }

        reportRows.clear();
        if (trendChart != null)
            trendChart.getData().clear();
        if (distributionChart != null)
            distributionChart.getData().clear();

        reportTitleLabel.setText("Rapport " + selectedType);

        switch (selectedType) {
            case "Production" -> generateProductionReport(start, end);
            case "Financial" -> generateFinancialReport(start, end);
            case "Inventory" -> generateInventoryReport();
            case "Personnel" -> generatePersonnelReport();
            case "Summary" -> generateSummaryReport(start, end);
            default -> generateSummaryReport(start, end);
        }

        // Save to history only on manual click? Or always?
        // Let's save.
        try {
            Report report = new Report(
                    "Rap. " + selectedType + " (" + LocalDate.now() + ")",
                    Report.ReportType.valueOf(selectedType),
                    start,
                    end);
            reportDAO.saveReport(report);
            loadReportHistory();
        } catch (Exception e) {
            // Ignore enum error if type doesn't perfectly match (e.g. Summary)
            // ReportType likely has Summary
        }
    }

    // --- Report Generation Logic ---

    private void generateProductionReport(LocalDate start, LocalDate end) {
        int totalGoodEggs = 0;
        int totalCrackedEggs = 0;
        int daysWithData = 0;

        // Chart Series
        javafx.scene.chart.XYChart.Series<String, Number> seriesGood = new javafx.scene.chart.XYChart.Series<>();
        seriesGood.setName("Oeufs Bons");

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            List<EggProduction> productions = eggProductionDAO.getProductionByDate(date);
            int dailyGood = 0;
            for (EggProduction p : productions) {
                totalGoodEggs += p.getGoodEggs();
                totalCrackedEggs += p.getCrackedEggs();
                dailyGood += p.getGoodEggs();
            }
            if (!productions.isEmpty())
                daysWithData++;

            // Add to chart (limit points if too many)
            seriesGood.getData().add(new javafx.scene.chart.XYChart.Data<>(
                    date.format(DateTimeFormatter.ofPattern("dd/MM")), dailyGood));
        }

        if (trendChart != null) {
            trendChart.getData().add(seriesGood);
            trendChart.getXAxis().setLabel("Date");
            trendChart.getYAxis().setLabel("Production (Oeufs)");
        }

        // Pie Chart: Good vs Cracked
        if (distributionChart != null) {
            ObservableList<javafx.scene.chart.PieChart.Data> pieData = FXCollections.observableArrayList(
                    new javafx.scene.chart.PieChart.Data("Bons", totalGoodEggs),
                    new javafx.scene.chart.PieChart.Data("Fêlés", totalCrackedEggs));
            distributionChart.setData(pieData);
            distributionChart.setTitle("Qualité Production");
        }

        reportRows.add(new ReportRow("Total Oeufs Bons", String.valueOf(totalGoodEggs), "oeufs", ""));
        reportRows.add(new ReportRow("Total Oeufs Fêlés", String.valueOf(totalCrackedEggs), "oeufs", ""));
        reportRows.add(new ReportRow("Moyenne Journalière",
                daysWithData > 0 ? String.format("%.1f", (double) totalGoodEggs / daysWithData) : "0", "oeufs/jour",
                ""));
    }

    private void generateFinancialReport(LocalDate start, LocalDate end) {
        List<FinancialTransaction> transactions = financialDAO.getAllTransactions();

        double totalIncome = 0;
        double totalExpense = 0;

        // Group by category for Pie Chart
        java.util.Map<String, Double> incomeByCat = new java.util.HashMap<>();
        java.util.Map<String, Double> expenseByCat = new java.util.HashMap<>();

        javafx.scene.chart.XYChart.Series<String, Number> seriesIncome = new javafx.scene.chart.XYChart.Series<>();
        seriesIncome.setName("Revenus");
        javafx.scene.chart.XYChart.Series<String, Number> seriesExpense = new javafx.scene.chart.XYChart.Series<>();
        seriesExpense.setName("Dépenses");

        // Aggregate per day for Bar Chart?
        // Or aggregate Total per Type for Bar Chart (easier comparison)

        for (FinancialTransaction tx : transactions) {
            // Filter locally since DAO returns all
            if (tx.getTransactionDate().isBefore(start) || tx.getTransactionDate().isAfter(end))
                continue;

            if ("Income".equalsIgnoreCase(String.valueOf(tx.getType()))) {
                totalIncome += tx.getAmount();
                incomeByCat.merge(tx.getCategory(), tx.getAmount(), Double::sum);
            } else {
                totalExpense += tx.getAmount();
                expenseByCat.merge(tx.getCategory(), tx.getAmount(), Double::sum);
            }
        }

        // Populate Bar Chart: Income vs Expense
        seriesIncome.getData().add(new javafx.scene.chart.XYChart.Data<>("Total", totalIncome));
        seriesExpense.getData().add(new javafx.scene.chart.XYChart.Data<>("Total", totalExpense));

        if (trendChart != null) {
            trendChart.getData().addAll(seriesIncome, seriesExpense);
            trendChart.getXAxis().setLabel("Type");
            trendChart.getYAxis().setLabel("Montant (DH)");
        }

        // Populate Pie Chart: Expense Breakdown (usually more interesting)
        if (distributionChart != null) {
            ObservableList<javafx.scene.chart.PieChart.Data> pieData = FXCollections.observableArrayList();
            expenseByCat.forEach((cat, amt) -> pieData.add(new javafx.scene.chart.PieChart.Data(cat, amt)));
            distributionChart.setData(pieData);
            distributionChart.setTitle("Répartition des Dépenses");
        }

        reportRows.add(new ReportRow("Total Recettes", String.format("%.2f", totalIncome), "DH", ""));
        reportRows.add(new ReportRow("Total Dépenses", String.format("%.2f", totalExpense), "DH", ""));
        reportRows.add(new ReportRow("Bénéfice Net", String.format("%.2f", totalIncome - totalExpense), "DH",
                totalIncome > totalExpense ? "+" : "-"));
    }

    private void generateInventoryReport() {
        int totalChickens = houseDAO.getTotalChickenCount();
        int totalHouses = houseDAO.getAllHouses().size();

        if (trendChart != null) {
            javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
            series.setName("Effectif");
            series.getData().add(new javafx.scene.chart.XYChart.Data<>("Poulets", totalChickens));
            trendChart.getData().add(series);
        }

        reportRows.add(new ReportRow("Effectif Total Poulets", String.valueOf(totalChickens), "poulets", ""));
        reportRows.add(new ReportRow("Nombre de Bâtiments", String.valueOf(totalHouses), "bâtiments", ""));
    }

    private void generatePersonnelReport() {
        List<Personnel> allPersonnel = personnelDAO.getAllPersonnel();

        int totalStaff = allPersonnel.size();
        int adminCount = 0;
        int farmCount = 0;
        double totalSalary = 0;
        double adminSalary = 0;
        double farmSalary = 0;

        for (Personnel p : allPersonnel) {
            totalSalary += p.getSalary();
            if ("administration".equalsIgnoreCase(p.getDepartment())) {
                adminCount++;
                adminSalary += p.getSalary();
            } else {
                farmCount++;
                farmSalary += p.getSalary();
            }
        }

        // Pie Chart: Distribution
        if (distributionChart != null) {
            ObservableList<javafx.scene.chart.PieChart.Data> pieData = FXCollections.observableArrayList(
                    new javafx.scene.chart.PieChart.Data("Administration", adminCount),
                    new javafx.scene.chart.PieChart.Data("Ferme", farmCount));
            distributionChart.setData(pieData);
            distributionChart.setTitle("Répartition du Personnel");
        }

        // Bar Chart: Salary Cost
        if (trendChart != null) {
            javafx.scene.chart.XYChart.Series<String, Number> seriesSalary = new javafx.scene.chart.XYChart.Series<>();
            seriesSalary.setName("Masse Salariale");
            seriesSalary.getData().add(new javafx.scene.chart.XYChart.Data<>("Admin", adminSalary));
            seriesSalary.getData().add(new javafx.scene.chart.XYChart.Data<>("Ferme", farmSalary));

            trendChart.getData().add(seriesSalary);
            trendChart.getXAxis().setLabel("Département");
            trendChart.getYAxis().setLabel("Salaire Mensuel (DH)");
        }

        reportRows.add(new ReportRow("Total Personnel", String.valueOf(totalStaff), "personnes", ""));
        reportRows.add(new ReportRow("Administration", String.valueOf(adminCount), "personnes",
                String.valueOf(adminSalary) + " DH/mois"));
        reportRows.add(new ReportRow("Ferme", String.valueOf(farmCount), "personnes",
                String.valueOf(farmSalary) + " DH/mois"));
        reportRows.add(new ReportRow("Masse Salariale Totale", String.format("%.2f", totalSalary), "DH/mois", ""));
    }

    private void generateSummaryReport(LocalDate start, LocalDate end) {
        // Combined view
        generateProductionReport(start, end);
        // Add financial specific rows without clearing
        List<FinancialTransaction> transactions = financialDAO.getAllTransactions();
        double totalIncome = 0;
        double totalExpense = 0;
        for (FinancialTransaction tx : transactions) {
            if (tx.getTransactionDate().isBefore(start) || tx.getTransactionDate().isAfter(end))
                continue;
            if ("Income".equalsIgnoreCase(String.valueOf(tx.getType())))
                totalIncome += tx.getAmount();
            else
                totalExpense += tx.getAmount();
        }
        reportRows.add(new ReportRow("Total Recettes", String.format("%.2f", totalIncome), "DH", ""));
        reportRows.add(new ReportRow("Total Dépenses", String.format("%.2f", totalExpense), "DH", ""));

        // Add Personnel Summary Line
        int totalStaff = personnelDAO.getTotalPersonnelCount();
        reportRows.add(new ReportRow("Effectif Total", String.valueOf(totalStaff), "personnes", ""));
        generateInventoryReport();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- Inner Class for Report Row Data ---
    public static class ReportRow {
        private String metric;
        private String value;
        private String unit;
        private String change;

        public ReportRow(String metric, String value, String unit, String change) {
            this.metric = metric;
            this.value = value;
            this.unit = unit;
            this.change = change;
        }

        public String getMetric() {
            return metric;
        }

        public String getValue() {
            return value;
        }

        public String getUnit() {
            return unit;
        }

        public String getChange() {
            return change;
        }
    }
}
