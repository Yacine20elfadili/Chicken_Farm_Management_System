package ma.farm.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Label;
import ma.farm.dao.DatabaseConnection;
import ma.farm.dao.HouseDAO;
import ma.farm.dao.EggProductionDAO;
import ma.farm.util.DateUtil;
import ma.farm.dao.HouseDAO;
import ma.farm.dao.EggProductionDAO;

import java.sql.Connection;

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

    }

    /**
     * Load and display total chickens count across all houses
     */
    private void loadTotalChickens() {
        // TODO: Get total chickens from HouseDAO

        // TODO: Update totalChickensLabel
    }

    /**
     * Load and display eggs produced today (H2 + H3)
     */
    private void loadEggsToday() {
        // TODO: Get today's egg production from EggProductionDAO

        // TODO: Calculate total (H2 + H3)

        // TODO: Update eggsTodayLabel
    }

    /**
     * Load and display active alerts count
     * Alerts: Low stock, health issues, overdue tasks, etc.
     */
    private void loadAlertsCount() {
        // TODO: Calculate alerts from various sources

        // TODO: Update alertsCountLabel
    }

    /**
     * Load and populate 7-day egg production bar chart
     * Shows last 7 days of egg production
     */
    private void load7DayChart() {
        // TODO: Get last 7 days using DateUtil.getLast7Days()

        // TODO: Get egg production data for each day

        // TODO: Populate BarChart with data

        // TODO: Style chart axes and bars
    }

    /**
     * Refresh all dashboard data
     * Called on manual refresh or auto-refresh timer
     */
    @FXML
    public void refreshDashboard() {
        // TODO: Reload all data

        // TODO: Update UI components
    }

    /**
     * Handle navigation to other pages
     * Note: Navigation is handled by MainWindowController
     */
}