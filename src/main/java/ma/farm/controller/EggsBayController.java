package ma.farm.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import ma.farm.dao.EggProductionDAO;
import ma.farm.model.EggProduction;
import ma.farm.util.DateUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * EggsBayController - Controls the Eggs Bay view
 * Shows: H2 and H3 egg production, total eggs in storage
 */
public class EggsBayController {

    // FXML Components - House 2 Card (Egg layers)
    @FXML
    private VBox house2Card;

    @FXML
    private Label h2NameLabel;

    @FXML
    private Label h2EggsCollectedLabel;

    @FXML
    private Label h2DeadChickensLabel;

    @FXML
    private Label h2DateLabel;

    // FXML Components - House 3 Card (Meat female)
    @FXML
    private VBox house3Card;

    @FXML
    private Label h3NameLabel;

    @FXML
    private Label h3EggsCollectedLabel;

    @FXML
    private Label h3DeadChickensLabel;

    @FXML
    private Label h3DateLabel;

    // FXML Components - Storage Card
    @FXML
    private VBox storageCard;

    @FXML
    private Label totalEggsLabel;

    @FXML
    private Label storageStatusLabel;

    // DAO
    private EggProductionDAO eggProductionDAO;

    // Constants
    private static final int MAX_STORAGE_CAPACITY = 10000; // Example capacity

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        // Initialize DAO
        eggProductionDAO = new EggProductionDAO();

        // Load egg production data
        loadHouse2Production();
        loadHouse3Production();

        // Load storage data
        loadStorageData();

        // Set today's date labels
        LocalDate today = LocalDate.now();
        if (h2DateLabel != null) {
            h2DateLabel.setText(DateUtil.formatDate(today));
        }
        if (h3DateLabel != null) {
            h3DateLabel.setText(DateUtil.formatDate(today));
        }
    }

    /**
     * Load and display House 2 egg production (Egg layers)
     */
    private void loadHouse2Production() {
        try {
            // Get today's production for House 2
            LocalDate today = LocalDate.now();
            List<EggProduction> allProductionsToday = eggProductionDAO.getProductionByDate(today);

            // Filter for House 2
            List<EggProduction> h2Productions = allProductionsToday.stream()
                    .filter(p -> p.getHouseId() == 2)
                    .collect(Collectors.toList());

            // Update labels
            if (!h2Productions.isEmpty()) {
                EggProduction production = h2Productions.get(0);

                if (h2NameLabel != null) {
                    h2NameLabel.setText("House 2 - Egg Layers");
                }

                if (h2EggsCollectedLabel != null) {
                    h2EggsCollectedLabel.setText(String.valueOf(production.getEggsCollected()));
                }

                if (h2DeadChickensLabel != null) {
                    h2DeadChickensLabel.setText(String.valueOf(production.getDeadChickens()));
                }
            } else {
                // No production recorded today
                if (h2NameLabel != null) {
                    h2NameLabel.setText("House 2 - Egg Layers");
                }
                if (h2EggsCollectedLabel != null) {
                    h2EggsCollectedLabel.setText("0");
                }
                if (h2DeadChickensLabel != null) {
                    h2DeadChickensLabel.setText("0");
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading House 2 production: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load and display House 3 egg production (Meat female)
     */
    private void loadHouse3Production() {
        try {
            // Get today's production for House 3
            LocalDate today = LocalDate.now();
            List<EggProduction> allProductionsToday = eggProductionDAO.getProductionByDate(today);

            // Filter for House 3
            List<EggProduction> h3Productions = allProductionsToday.stream()
                    .filter(p -> p.getHouseId() == 3)
                    .collect(Collectors.toList());

            // Update labels
            if (!h3Productions.isEmpty()) {
                EggProduction production = h3Productions.get(0);

                if (h3NameLabel != null) {
                    h3NameLabel.setText("House 3 - Meat Female");
                }

                if (h3EggsCollectedLabel != null) {
                    h3EggsCollectedLabel.setText(String.valueOf(production.getEggsCollected()));
                }

                if (h3DeadChickensLabel != null) {
                    h3DeadChickensLabel.setText(String.valueOf(production.getDeadChickens()));
                }
            } else {
                // No production recorded today
                if (h3NameLabel != null) {
                    h3NameLabel.setText("House 3 - Meat Female");
                }
                if (h3EggsCollectedLabel != null) {
                    h3EggsCollectedLabel.setText("0");
                }
                if (h3DeadChickensLabel != null) {
                    h3DeadChickensLabel.setText("0");
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading House 3 production: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load and display total eggs in storage
     */
    private void loadStorageData() {
        try {
            // Get total good eggs from all time (or use a date range)
            // Since there's no getTotalGoodEggs() method, we'll get all production records and sum them
            List<EggProduction> allProductions = eggProductionDAO.getAllProduction();

            int totalEggs = allProductions.stream()
                    .mapToInt(EggProduction::getGoodEggs)
                    .sum();

            // Update totalEggsLabel
            if (totalEggsLabel != null) {
                totalEggsLabel.setText(String.valueOf(totalEggs));
            }

            // Calculate storage percentage
            double percentage = calculateStoragePercentage(totalEggs);

            // Apply storage status badge
            applyStorageStatusBadge(percentage);
        } catch (Exception e) {
            System.err.println("Error loading storage data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle record egg collection button click
     * Opens dialog to record today's egg collection
     */
    @FXML
    public void handleRecordCollection() {
        // TODO: Open record collection dialog
        // This would typically open a dialog window where the user can:
        // 1. Select house (H2 or H3)
        // 2. Enter eggs collected
        // 3. Enter cracked eggs
        // 4. Enter dead chickens count
        // 5. Enter collector name (optional)
        // 6. Enter notes (optional)

        // For now, show placeholder alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Record Collection");
        alert.setHeaderText("Record Egg Collection Feature");
        alert.setContentText("This feature will open a dialog to record today's egg collection.\n\nDialog implementation is pending.");
        alert.showAndWait();

        // After dialog implementation, the code would be:
        // - Create EggProduction record
        // - Calculate good eggs automatically
        // - Save to database using eggProductionDAO.addProduction()
        // - Refresh data
    }

    /**
     * Handle remove eggs button click
     * Opens dialog to remove eggs from storage (sold/used)
     */
    @FXML
    public void handleRemoveEggs() {
        // TODO: Open remove eggs dialog
        // This would typically open a dialog window where the user can:
        // 1. Enter quantity to remove
        // 2. Select reason (sold/used/damaged)
        // 3. Enter notes

        // For now, show placeholder alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Remove Eggs");
        alert.setHeaderText("Remove Eggs from Storage Feature");
        alert.setContentText("This feature will open a dialog to remove eggs from storage.\n\nDialog implementation is pending.");
        alert.showAndWait();

        // After dialog implementation, the code would be:
        // - Validate available quantity
        // - Update storage (this might need a separate table/mechanism)
        // - Refresh data
    }

    /**
     * Calculate storage capacity percentage
     * @param currentEggs Current eggs in storage
     * @return Percentage of capacity used
     */
    private double calculateStoragePercentage(int currentEggs) {
        // Define max storage capacity
        int maxCapacity = MAX_STORAGE_CAPACITY;

        // Calculate percentage
        return (double) currentEggs / maxCapacity * 100.0;
    }

    /**
     * Apply storage status badge styling
     * @param percentage Storage capacity percentage
     */
    private void applyStorageStatusBadge(double percentage) {
        if (storageStatusLabel == null) {
            return;
        }

        // Remove previous style classes
        storageStatusLabel.getStyleClass().removeAll("storage-ok", "storage-warning", "storage-critical");

        // Update label text and styling based on percentage
        if (percentage < 50) {
            // Green if < 50%
            storageStatusLabel.setText("Storage OK (" + String.format("%.1f", percentage) + "%)");
            storageStatusLabel.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
        } else if (percentage < 80) {
            // Yellow if 50-80%
            storageStatusLabel.setText("Storage Filling (" + String.format("%.1f", percentage) + "%)");
            storageStatusLabel.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
        } else {
            // Red if > 80%
            storageStatusLabel.setText("Storage Full (" + String.format("%.1f", percentage) + "%)");
            storageStatusLabel.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
        }
    }

    /**
     * Refresh all eggs bay data
     */
    @FXML
    public void refreshData() {
        // Reload House 2 production
        loadHouse2Production();

        // Reload House 3 production
        loadHouse3Production();

        // Reload storage data
        loadStorageData();
    }
}
