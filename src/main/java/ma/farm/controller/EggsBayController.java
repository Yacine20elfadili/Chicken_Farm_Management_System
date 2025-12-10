package ma.farm.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ma.farm.dao.EggProductionDAO;
import ma.farm.model.EggProduction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the Eggs Bay view
 * Manages the display and interaction of egg production data
 *
 * @author ismailouchraa
 * @version 1.0
 */
public class EggsBayController {

    // DAO
    private EggProductionDAO eggProductionDAO;

    // House 2 (Egg Layer) Labels
    @FXML private Label h2DateLabel;
    @FXML private Label h2EggsCollectedLabel;
    @FXML private Label h2CrackedEggsLabel;
    @FXML private Label h2GoodEggsLabel;
    @FXML private Label h2DeadChickensLabel;
    @FXML private Label h2EfficiencyLabel;
    @FXML private Label h2CollectedByLabel;

    // House 3 (Egg Layer) Labels
    @FXML private Label h3DateLabel;
    @FXML private Label h3EggsCollectedLabel;
    @FXML private Label h3CrackedEggsLabel;
    @FXML private Label h3GoodEggsLabel;
    @FXML private Label h3DeadChickensLabel;
    @FXML private Label h3EfficiencyLabel;
    @FXML private Label h3CollectedByLabel;

    // Storage Labels
    @FXML private Label totalEggsLabel;
    @FXML private Label storageStatusLabel;
    @FXML private Label lastUpdateLabel;

    // Date formatter
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Initializes the controller
     * Called automatically by JavaFX after FXML loading
     */
    @FXML
    public void initialize() {
        try {
            // Initialize DAO
            eggProductionDAO = new EggProductionDAO();

            // Load all production data
            loadHouse2Production();
            loadHouse3Production();
            loadStorageData();

            System.out.println("EggsBayController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing EggsBayController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads today's production data for House 2
     */
    private void loadHouse2Production() {
        try {
            LocalDate today = LocalDate.now();

            // Get today's production records for House 2
            List<EggProduction> productions = eggProductionDAO.getProductionByDate(today);

            // Find House 2 production
            EggProduction h2Production = productions.stream()
                    .filter(p -> p.getHouseId() == 2)
                    .findFirst()
                    .orElse(null);

            if (h2Production != null) {
                // Update labels with production data
                updateProductionLabels(
                        h2DateLabel, h2EggsCollectedLabel, h2CrackedEggsLabel,
                        h2GoodEggsLabel, h2DeadChickensLabel, h2EfficiencyLabel,
                        h2CollectedByLabel, h2Production
                );
            } else {
                // No production data for today
                setNoDataLabels(
                        h2DateLabel, h2EggsCollectedLabel, h2CrackedEggsLabel,
                        h2GoodEggsLabel, h2DeadChickensLabel, h2EfficiencyLabel,
                        h2CollectedByLabel, today
                );
            }

        } catch (Exception e) {
            System.err.println("Error loading House 2 production: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads today's production data for House 3
     */
    private void loadHouse3Production() {
        try {
            LocalDate today = LocalDate.now();

            // Get today's production records for House 3
            List<EggProduction> productions = eggProductionDAO.getProductionByDate(today);

            // Find House 3 production
            EggProduction h3Production = productions.stream()
                    .filter(p -> p.getHouseId() == 3)
                    .findFirst()
                    .orElse(null);

            if (h3Production != null) {
                // Update labels with production data
                updateProductionLabels(
                        h3DateLabel, h3EggsCollectedLabel, h3CrackedEggsLabel,
                        h3GoodEggsLabel, h3DeadChickensLabel, h3EfficiencyLabel,
                        h3CollectedByLabel, h3Production
                );
            } else {
                // No production data for today
                setNoDataLabels(
                        h3DateLabel, h3EggsCollectedLabel, h3CrackedEggsLabel,
                        h3GoodEggsLabel, h3DeadChickensLabel, h3EfficiencyLabel,
                        h3CollectedByLabel, today
                );
            }

        } catch (Exception e) {
            System.err.println("Error loading House 3 production: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to update production labels
     */
    private void updateProductionLabels(
            Label dateLabel, Label collectedLabel, Label crackedLabel,
            Label goodLabel, Label deadLabel, Label efficiencyLabel,
            Label collectorLabel, EggProduction production) {

        // Check for null labels
        if (dateLabel == null || collectedLabel == null || crackedLabel == null ||
                goodLabel == null || deadLabel == null || efficiencyLabel == null ||
                collectorLabel == null) {
            System.err.println("One or more labels are null for production update");
            return;
        }

        // Update date
        String formattedDate = production.getProductionDate() != null
                ? production.getProductionDate().format(DATE_FORMATTER)
                : "N/A";
        dateLabel.setText(formattedDate);

        // Update egg counts
        collectedLabel.setText(String.valueOf(production.getEggsCollected()));
        crackedLabel.setText(String.valueOf(production.getCrackedEggs()));
        goodLabel.setText(String.valueOf(production.getGoodEggs()));

        // Update dead chickens
        deadLabel.setText(String.valueOf(production.getDeadChickens()));

        // Update efficiency (with percentage)
        double efficiency = production.getEfficiencyRate();
        efficiencyLabel.setText(String.format("%.1f%%", efficiency));

        // Apply color based on efficiency
        applyEfficiencyColor(efficiencyLabel, efficiency);

        // Update collector name
        String collector = production.getCollectedBy();
        collectorLabel.setText(collector != null ? collector : "Unknown");
    }

    /**
     * Helper method to set "No Data" labels
     */
    private void setNoDataLabels(
            Label dateLabel, Label collectedLabel, Label crackedLabel,
            Label goodLabel, Label deadLabel, Label efficiencyLabel,
            Label collectorLabel, LocalDate date) {

        if (dateLabel != null) {
            dateLabel.setText(date.format(DATE_FORMATTER));
        }
        if (collectedLabel != null) {
            collectedLabel.setText("0");
        }
        if (crackedLabel != null) {
            crackedLabel.setText("0");
        }
        if (goodLabel != null) {
            goodLabel.setText("0");
        }
        if (deadLabel != null) {
            deadLabel.setText("0");
        }
        if (efficiencyLabel != null) {
            efficiencyLabel.setText("0.0%");
            efficiencyLabel.getStyleClass().removeAll("efficiency-good", "efficiency-fair", "efficiency-poor");
        }
        if (collectorLabel != null) {
            collectorLabel.setText("No data");
        }
    }

    /**
     * Applies color styling to efficiency label based on value
     *
     * @param label the efficiency label
     * @param efficiency the efficiency percentage
     */
    private void applyEfficiencyColor(Label label, double efficiency) {
        if (label == null) {
            return;
        }

        // Remove all previous style classes
        label.getStyleClass().removeAll("efficiency-good", "efficiency-fair", "efficiency-poor");

        // Apply style based on efficiency threshold
        if (efficiency >= 95.0) {
            label.getStyleClass().add("efficiency-good");
        } else if (efficiency >= 90.0) {
            label.getStyleClass().add("efficiency-fair");
        } else {
            label.getStyleClass().add("efficiency-poor");
        }
    }

    /**
     * Loads total egg storage data
     * Calculates total good eggs from all production records
     */
    private void loadStorageData() {
        try {
            // Get all production records
            List<EggProduction> allProductions = eggProductionDAO.getAllProduction();

            // Calculate total good eggs
            int totalGoodEggs = allProductions.stream()
                    .mapToInt(EggProduction::getGoodEggs)
                    .sum();

            // Update total eggs label
            if (totalEggsLabel != null) {
                totalEggsLabel.setText(String.format("%,d", totalGoodEggs));
            }

            // Update storage status based on total
            if (storageStatusLabel != null) {
                String status = getStorageStatus(totalGoodEggs);
                storageStatusLabel.setText(status);
                applyStorageStatusColor(storageStatusLabel, totalGoodEggs);
            }

            // Update last update time
            if (lastUpdateLabel != null) {
                lastUpdateLabel.setText("Last updated: " +
                        LocalDate.now().format(DATE_FORMATTER));
            }

        } catch (Exception e) {
            System.err.println("Error loading storage data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets storage status message based on total eggs
     *
     * @param totalEggs total number of eggs
     * @return status message
     */
    private String getStorageStatus(int totalEggs) {
        if (totalEggs >= 50000) {
            return "Excellent Stock";
        } else if (totalEggs >= 30000) {
            return "Good Stock";
        } else if (totalEggs >= 15000) {
            return "Moderate Stock";
        } else if (totalEggs >= 5000) {
            return "Low Stock";
        } else {
            return "Very Low Stock";
        }
    }

    /**
     * Applies color styling to storage status label
     *
     * @param label the storage status label
     * @param totalEggs total number of eggs
     */
    private void applyStorageStatusColor(Label label, int totalEggs) {
        if (label == null) {
            return;
        }

        // Remove all previous style classes
        label.getStyleClass().removeAll("storage-excellent", "storage-good",
                "storage-moderate", "storage-low", "storage-critical");

        // Apply style based on stock level
        if (totalEggs >= 50000) {
            label.getStyleClass().add("storage-excellent");
        } else if (totalEggs >= 30000) {
            label.getStyleClass().add("storage-good");
        } else if (totalEggs >= 15000) {
            label.getStyleClass().add("storage-moderate");
        } else if (totalEggs >= 5000) {
            label.getStyleClass().add("storage-low");
        } else {
            label.getStyleClass().add("storage-critical");
        }
    }

    /**
     * Handles the "Record Collection" button click
     * Opens a dialog to record new egg collection
     */
    @FXML
    private void handleRecordCollection() {
        try {
            // TODO: Implement dialog to record egg collection
            // This will be implemented in a future iteration
            System.out.println("Record Collection feature - Coming soon");

            // After recording collection, refresh data
            // refreshData();
        } catch (Exception e) {
            System.err.println("Error handling record collection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the "Remove Eggs" button click
     * Opens a dialog to remove eggs from storage (sales, damaged, etc.)
     */
    @FXML
    private void handleRemoveEggs() {
        try {
            // TODO: Implement dialog to remove eggs from storage
            // This will be implemented in a future iteration
            System.out.println("Remove Eggs feature - Coming soon");

            // After removing eggs, refresh data
            // refreshData();
        } catch (Exception e) {
            System.err.println("Error handling remove eggs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Refreshes all data on the page
     * Call this method after any data changes
     */
    public void refreshData() {
        try {
            loadHouse2Production();
            loadHouse3Production();
            loadStorageData();
            System.out.println("Eggs Bay data refreshed");
        } catch (Exception e) {
            System.err.println("Error refreshing data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}