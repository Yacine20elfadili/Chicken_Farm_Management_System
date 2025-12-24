package ma.farm.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import ma.farm.dao.HouseDAO;
import ma.farm.dao.EggProductionDAO;
import ma.farm.model.EggProduction;
import ma.farm.util.DateUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * DashboardController - Controls the main dashboard view
 * Shows: Total chickens, eggs today, alerts, 7-day egg chart
 */
public class DashboardController {

    // FXML Components - Summary Cards
    @FXML
    private Label totalChickensLabel;

    @FXML
    private Label eggsTodayLabel;

    @FXML
    private Label totalIncomeLabel;
    @FXML
    private Label totalExpenseLabel;

    // FXML Components - Chart
    @FXML
    private BarChart<String, Number> eggProductionChart;

    // DAOs
    private HouseDAO houseDAO;
    private EggProductionDAO eggProductionDAO;
    private ma.farm.dao.FinancialDAO financialDAO;

    /**
     * Initialize method - called automatically after FXML loads
     * Setup: Initialize DAOs, load data, populate UI
     */
    @FXML
    public void initialize() {
        System.out.println("=== DashboardController: Initializing ===");
        System.out.println("Current date: " + LocalDate.now());

        try {
            // Initialize DAOs
            houseDAO = new HouseDAO();
            eggProductionDAO = new EggProductionDAO();
            financialDAO = new ma.farm.dao.FinancialDAO();

            System.out.println("DashboardController: DAOs initialized");

            // Configure chart before loading data
            configureChart();

            // Load all dashboard data
            loadTotalChickens();
            loadEggsToday();
            loadFinancialSummary();
            load7DayChart();

            System.out.println("=== DashboardController: All data loaded successfully ===\n");

        } catch (Exception e) {
            System.err.println("Error initializing dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ... (configureChart remains same) ...

    /**
     * Load and display financial summary (Income vs Expense)
     */
    private void loadFinancialSummary() {
        try {
            double totalIncome = 0;
            double totalExpense = 0;

            // Fetch all transactions
            // Note: For better performance in large DBs, add efficient SUM aggregation
            // methods to DAO.
            List<ma.farm.model.FinancialTransaction> transactions = financialDAO.getAllTransactions();

            for (ma.farm.model.FinancialTransaction tx : transactions) {
                if ("Income".equalsIgnoreCase(String.valueOf(tx.getType()))) {
                    totalIncome += tx.getAmount();
                } else {
                    totalExpense += tx.getAmount();
                }
            }

            if (totalIncomeLabel != null) {
                totalIncomeLabel.setText(String.format("Rec: %.2f DH", totalIncome));
            }
            if (totalExpenseLabel != null) {
                totalExpenseLabel.setText(String.format("Dép: %.2f DH", totalExpense));
            }

            System.out.println("✓ Financial summary loaded: In=" + totalIncome + ", Out=" + totalExpense);

        } catch (Exception e) {
            System.err.println("✗ Error loading financial summary: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ...

    @FXML
    public void refreshDashboard() {
        System.out.println("\n=== Refreshing dashboard ===");

        try {
            loadTotalChickens();
            loadEggsToday();
            loadFinancialSummary();
            load7DayChart();

            System.out.println("=== Dashboard refreshed successfully ===\n");
        } catch (Exception e) {
            System.err.println("✗ Error refreshing dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configure chart appearance and behavior
     */
    private void configureChart() {
        if (eggProductionChart == null) {
            System.err.println("✗ Error: eggProductionChart is null!");
            return;
        }

        System.out.println("Configuring chart...");

        // Set chart properties
        eggProductionChart.setTitle("");
        eggProductionChart.setLegendVisible(true);
        eggProductionChart.setAnimated(true);

        // Configure axes
        CategoryAxis xAxis = (CategoryAxis) eggProductionChart.getXAxis();
        if (xAxis != null) {
            xAxis.setLabel("Jours");
            xAxis.setAnimated(false);
        }

        NumberAxis yAxis = (NumberAxis) eggProductionChart.getYAxis();
        if (yAxis != null) {
            yAxis.setLabel("Œufs collectés");
            yAxis.setAutoRanging(true);
            yAxis.setForceZeroInRange(true);
            yAxis.setAnimated(false);
        }

        // Set minimum height to ensure visibility
        eggProductionChart.setMinHeight(300);
        eggProductionChart.setPrefHeight(350);

        // Apply inline styling to ensure visibility
        eggProductionChart.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;");

        System.out.println("✓ Chart configured");
    }

    /**
     * Load and display total chickens count across all houses
     */
    private void loadTotalChickens() {
        try {
            int totalChickens = houseDAO.getTotalChickenCount();
            if (totalChickensLabel != null) {
                totalChickensLabel.setText(String.valueOf(totalChickens));
            }
            System.out.println("✓ Total chickens loaded: " + totalChickens);
        } catch (Exception e) {
            System.err.println("✗ Error loading total chickens: " + e.getMessage());
            if (totalChickensLabel != null) {
                totalChickensLabel.setText("Error");
            }
            e.printStackTrace();
        }
    }

    /**
     * Load and display eggs produced today (H2 + H3)
     */
    private void loadEggsToday() {
        try {
            LocalDate today = LocalDate.now();
            System.out.println("Loading eggs for date: " + today);

            // Get production for today
            List<EggProduction> todayProduction = eggProductionDAO.getProductionByDate(today);

            System.out.println("Found " + todayProduction.size() + " production records for today");

            // Calculate total eggs
            int eggsToday = 0;
            for (EggProduction production : todayProduction) {
                eggsToday += production.getGoodEggs();
                System.out
                        .println("  House " + production.getHouseId() + ": " + production.getGoodEggs() + " good eggs");
            }

            if (eggsTodayLabel != null) {
                eggsTodayLabel.setText(String.valueOf(eggsToday));
            }

            if (eggsToday == 0) {
                System.out.println("⚠ Warning: No egg production data found for today (" + today + ")");
                System.out.println("  This might be because there's no data in the database for this date.");
            } else {
                System.out.println("✓ Eggs today loaded: " + eggsToday);
            }
        } catch (Exception e) {
            System.err.println("✗ Error loading eggs today: " + e.getMessage());
            if (eggsTodayLabel != null) {
                eggsTodayLabel.setText("Error");
            }
            e.printStackTrace();
        }
    }

    /**
     * Load and populate 7-day egg production bar chart
     * Shows last 7 days of egg production
     */
    private void load7DayChart() {
        try {
            System.out.println("\n--- Loading 7-day chart ---");

            if (eggProductionChart == null) {
                System.err.println("✗ Error: eggProductionChart is null!");
                return;
            }

            // Clear existing data
            eggProductionChart.getData().clear();

            // Create series for the chart
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Œufs collectés");

            // Get last 7 days
            LocalDate[] last7Days = DateUtil.getLast7Days();

            System.out.println("Processing last 7 days of data:");
            System.out.println("Date Range: " + last7Days[0] + " to " + last7Days[6]);

            int totalDataPoints = 0;
            int maxEggs = 0;

            // Loop through each day and get production data
            for (LocalDate date : last7Days) {
                // Get production for this date
                List<EggProduction> dayProduction = eggProductionDAO.getProductionByDate(date);

                // Sum up eggs from all houses for this day
                int totalEggs = 0;
                for (EggProduction production : dayProduction) {
                    totalEggs += production.getGoodEggs();
                }

                if (totalEggs > maxEggs) {
                    maxEggs = totalEggs;
                }

                // Format date for display (dd/MM)
                String dateLabel = DateUtil.formatShortDate(date);

                // Create data point
                XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(dateLabel, totalEggs);

                // Add data point to series
                series.getData().add(dataPoint);

                System.out.println("  " + date + " (" + dateLabel + "): " + totalEggs + " eggs (" + dayProduction.size()
                        + " records)");

                if (totalEggs > 0) {
                    totalDataPoints++;
                }
            }

            // Add series to chart
            eggProductionChart.getData().add(series);

            System.out.println("Series added with " + series.getData().size() + " data points");
            System.out.println("Max eggs value: " + maxEggs);

            // Force chart refresh
            eggProductionChart.layout();
            eggProductionChart.requestLayout();

            if (totalDataPoints == 0) {
                System.out.println("⚠ Warning: No egg production data found for the last 7 days!");
                System.out.println("  Chart will be empty. Check if database has data for recent dates.");
            } else {
                System.out.println("✓ 7-day chart loaded successfully (" + totalDataPoints + " days with data)");
            }

            // Check if chart is visible
            System.out.println("Chart visible: " + eggProductionChart.isVisible());
            System.out.println("Chart managed: " + eggProductionChart.isManaged());
            System.out.println("Chart width: " + eggProductionChart.getWidth());
            System.out.println("Chart height: " + eggProductionChart.getHeight());

        } catch (Exception e) {
            System.err.println("✗ Error loading 7-day chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
