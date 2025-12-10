package ma.farm.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
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
    private Label alertsCountLabel;

    // FXML Components - Chart
    @FXML
    private BarChart<String, Number> eggProductionChart;

    // DAOs
    private HouseDAO houseDAO;
    private EggProductionDAO eggProductionDAO;

    /**
     * Initialize method - called automatically after FXML loads
     * Setup: Initialize DAOs, load data, populate UI
     */
    @FXML
    public void initialize() {
        System.out.println("DashboardController: Initializing...");

        try {
            // Initialize DAOs
            houseDAO = new HouseDAO();
            eggProductionDAO = new EggProductionDAO();

            System.out.println("DashboardController: DAOs initialized");

            // Load all dashboard data
            loadTotalChickens();
            loadEggsToday();
            loadAlertsCount();
            load7DayChart();

            System.out.println("DashboardController: All data loaded successfully");

        } catch (Exception e) {
            System.err.println("Error initializing dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load and display total chickens count across all houses
     */
    private void loadTotalChickens() {
        try {
            int totalChickens = houseDAO.getTotalChickenCount();
            totalChickensLabel.setText(String.valueOf(totalChickens));
            System.out.println("Total chickens loaded: " + totalChickens);
        } catch (Exception e) {
            System.err.println("Error loading total chickens: " + e.getMessage());
            totalChickensLabel.setText("Error");
            e.printStackTrace();
        }
    }

    /**
     * Load and display eggs produced today (H2 + H3)
     */
    private void loadEggsToday() {
        try {
            int eggsToday = eggProductionDAO.getEggsToday();
            eggsTodayLabel.setText(String.valueOf(eggsToday));
            System.out.println("Eggs today loaded: " + eggsToday);
        } catch (Exception e) {
            System.err.println("Error loading eggs today: " + e.getMessage());
            eggsTodayLabel.setText("Error");
            e.printStackTrace();
        }
    }

    /**
     * Load and display active alerts count
     * MVP version: Placeholder - always shows 0
     */
    private void loadAlertsCount() {
        try {
            // MVP: Just show 0 for now
            alertsCountLabel.setText("0");
            System.out.println("Alerts count: 0 (MVP placeholder)");
        } catch (Exception e) {
            System.err.println("Error loading alerts: " + e.getMessage());
            alertsCountLabel.setText("Error");
        }
    }

    /**
     * Load and populate 7-day egg production bar chart
     * Shows last 7 days of egg production
     */
    private void load7DayChart() {
        try {
            System.out.println("Loading 7-day chart...");

            // Clear existing data
            eggProductionChart.getData().clear();

            // Create series for the chart
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Œufs collectés");

            // Get last 7 days
            LocalDate[] last7Days = DateUtil.getLast7Days();

            System.out.println("Processing last 7 days of data...");

            // Loop through each day and get production data
            for (LocalDate date : last7Days) {
                // Get production for this date
                List<EggProduction> dayProduction = eggProductionDAO.getProductionByDate(date);

                // Sum up eggs from all houses for this day
                int totalEggs = 0;
                for (EggProduction production : dayProduction) {
                    totalEggs += production.getGoodEggs();
                }

                // Format date for display (e.g., "08/12")
                String dateLabel = DateUtil.formatDate(date);

                // Add data point to series
                series.getData().add(new XYChart.Data<>(dateLabel, totalEggs));

                System.out.println("  " + dateLabel + ": " + totalEggs + " eggs");
            }

            // Add series to chart
            eggProductionChart.getData().add(series);

            System.out.println("7-day chart loaded successfully");

        } catch (Exception e) {
            System.err.println("Error loading 7-day chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Refresh all dashboard data
     * Called on manual refresh or auto-refresh timer
     */
    @FXML
    public void refreshDashboard() {
        System.out.println("Refreshing dashboard...");

        try {
            loadTotalChickens();
            loadEggsToday();
            loadAlertsCount();
            load7DayChart();

            System.out.println("Dashboard refreshed successfully");
        } catch (Exception e) {
            System.err.println("Error refreshing dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}