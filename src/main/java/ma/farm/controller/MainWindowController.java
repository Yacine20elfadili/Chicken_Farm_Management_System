package ma.farm.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindowController {

    @FXML
    private AnchorPane contentArea;

    @FXML
    private SidebarController sidebarController;

    /**
     * Initialize method - called automatically after FXML is loaded
     */
    @FXML
    public void initialize() {
        sidebarController.setMainController(this);
        // Load Dashboard by default
        showDashboard();
    }

    /**
     * Load Dashboard page
     */
    @FXML
    public void showDashboard() {
        loadPage("/fxml/DashboardView.fxml");
    }

    /**
     * Load Chicken Bay page
     */
    @FXML
    public void showChickenBay() {
        loadPage("/fxml/ChickenBayView.fxml");
    }

    /**
     * Load Eggs Bay page
     */
    @FXML
    public void showEggsBay() {
        loadPage("/fxml/EggsBayView.fxml");
    }

    /**
     * Load Storage page
     */
    @FXML
    public void showStorage() {
        loadPage("/fxml/StorageView.fxml");
    }

    /**
     * Load Tasks page
     */
    @FXML
    public void showTasks() {
        loadPage("/fxml/TasksView.fxml");
    }

    /**
     * Load Personnel page
     */
    @FXML
    public void showPersonnel() {
        loadPage("/fxml/PersonnelView.fxml");
    }

    @FXML
    public void showSuppliers() {
        loadPage("/fxml/SuppliersView.fxml");
    }

    @FXML
    public void showCustomers() {
        loadPage("/fxml/CustomersView.fxml");
    }

    @FXML
    public void showFarmDocument() {
        loadPage("/fxml/FarmDocumentView.fxml");
    }

    @FXML
    public void showFinancialTracking() {
        loadPage("/fxml/FinancialTrackingView.fxml");
    }

    @FXML
    public void showReports() {
        loadPage("/fxml/ReportsView.fxml");
    }

    @FXML
    public void showSettings() {
        loadPage("/fxml/SettingsView.fxml");
    }

    /**
     * Handle logout - return to login page
     */
    @FXML
    public void handleLogout() {
        try {
            // Load login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent loginRoot = loader.load();

            // Get current stage
            Stage stage = (Stage) contentArea.getScene().getWindow();

            // Set login scene
            Scene loginScene = new Scene(loginRoot);
            stage.setScene(loginScene);
            stage.setTitle("Chicken Farm Management - Login");

            System.out.println("Logged out successfully!");

        } catch (IOException e) {
            System.err.println("Error loading login page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load a page into the content area
     * 
     * @param fxmlPath Path to the FXML file
     */
    private void loadPage(String fxmlPath) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            // Clear current content
            contentArea.getChildren().clear();

            // Add new content
            contentArea.getChildren().add(page);

            // Make it fill the entire area
            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);

            System.out.println("Loaded page: " + fxmlPath);

        } catch (IOException e) {
            System.err.println("Error loading page: " + fxmlPath);
            e.printStackTrace();
        }
    }
}