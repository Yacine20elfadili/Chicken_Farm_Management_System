package ma.farm.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ma.farm.dao.EggProductionDAO;
import ma.farm.dao.HouseDAO;
import ma.farm.model.EggProduction;
import ma.farm.model.House;
import ma.farm.model.HouseType;
import ma.farm.util.DateUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * EggsBayController - Controls the Eggs Bay view
 * Shows: Houses that can lay eggs (EGG_LAYER and MEAT_FEMALE), total eggs in storage
 */
public class EggsBayController {

    // FXML Components - House Cards Container
    @FXML
    private HBox houseCardsContainer;

    // FXML Components - House 2 Card
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

    // FXML Components - House 3 Card
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

    // FXML Components - No Egg Houses Message
    @FXML
    private VBox noEggHousesMessage;

    // FXML Components - Storage Card
    @FXML
    private VBox storageCard;

    @FXML
    private Label totalEggsLabel;

    @FXML
    private Label storageStatusLabel;

    // DAOs
    private EggProductionDAO eggProductionDAO;
    private HouseDAO houseDAO;

    // Constants
    private static final int MAX_STORAGE_CAPACITY = 10000; // Example capacity

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        // Initialize DAOs
        eggProductionDAO = new EggProductionDAO();
        houseDAO = new HouseDAO();

        // Check if there are any egg-laying houses first
        checkAndDisplayEggLayingHouses();

        // Load storage data
        loadStorageData();
    }

    /**
     * Check if there are egg-laying houses and display accordingly
     */
    private void checkAndDisplayEggLayingHouses() {
        try {
            // Get all houses from database
            List<House> allHouses = houseDAO.getAllHouses();
            
            // Filter houses that can lay eggs (EGG_LAYER and MEAT_FEMALE)
            List<House> eggLayingHouses = allHouses.stream()
                    .filter(house -> house.getType() == HouseType.EGG_LAYER || house.getType() == HouseType.MEAT_FEMALE)
                    .collect(Collectors.toList());

            if (eggLayingHouses.isEmpty()) {
                // No egg-laying houses found - show message
                showNoEggHousesMessage();
            } else {
                // Egg-laying houses found - show cards and load data
                showHouseCards();
                loadEggLayingHousesData(eggLayingHouses);
            }

        } catch (Exception e) {
            System.err.println("Error checking egg-laying houses: " + e.getMessage());
            e.printStackTrace();
            // On error, show the house cards with default "non configuré" values
            showHouseCards();
        }
    }

    /**
     * Show the "No egg laying houses" message and hide house cards
     */
    private void showNoEggHousesMessage() {
        // Hide house cards
        if (houseCardsContainer != null) {
            houseCardsContainer.setVisible(false);
            houseCardsContainer.setManaged(false);
        }

        // Show no data message
        if (noEggHousesMessage != null) {
            noEggHousesMessage.setVisible(true);
            noEggHousesMessage.setManaged(true);
        }

        System.out.println("No egg-laying houses configured - showing message");
    }

    /**
     * Show house cards and hide the "No egg laying houses" message
     */
    private void showHouseCards() {
        // Show house cards
        if (houseCardsContainer != null) {
            houseCardsContainer.setVisible(true);
            houseCardsContainer.setManaged(true);
        }

        // Hide no data message
        if (noEggHousesMessage != null) {
            noEggHousesMessage.setVisible(false);
            noEggHousesMessage.setManaged(false);
        }
    }

    /**
     * Load data for egg-laying houses
     */
    private void loadEggLayingHousesData(List<House> eggLayingHouses) {
        // Set today's date
        LocalDate today = LocalDate.now();
        String todayFormatted = DateUtil.formatDate(today);

        // Check each house and load data if it exists
        for (House house : eggLayingHouses) {
            if (house.getId() == 2) {
                loadHouseProductionData(house, h2NameLabel, h2EggsCollectedLabel, h2DeadChickensLabel, h2DateLabel, todayFormatted);
            } else if (house.getId() == 3) {
                loadHouseProductionData(house, h3NameLabel, h3EggsCollectedLabel, h3DeadChickensLabel, h3DateLabel, todayFormatted);
            }
            // Add more house IDs as needed
        }

        // For houses that don't exist, keep the "Non configuré" defaults
        // This is already set in the FXML, so no action needed
    }

    /**
     * Load production data for a specific house
     */
    private void loadHouseProductionData(House house, Label nameLabel, Label eggsLabel, Label mortalityLabel, Label dateLabel, String todayFormatted) {
        try {
            // Update house name label
            if (nameLabel != null) {
                String houseName = String.format("Bâtiment %d - %s", house.getId(), house.getType().getDisplayName());
                nameLabel.setText(houseName);
            }

            // Get today's production for this house
            LocalDate today = LocalDate.now();
            List<EggProduction> allProductionsToday = eggProductionDAO.getProductionByDate(today);

            // Filter for this specific house
            List<EggProduction> houseProductions = allProductionsToday.stream()
                    .filter(p -> p.getHouseId() == house.getId())
                    .collect(Collectors.toList());

            // Update production labels
            if (!houseProductions.isEmpty()) {
                EggProduction production = houseProductions.get(0);

                if (eggsLabel != null) {
                    eggsLabel.setText(String.valueOf(production.getEggsCollected()));
                }

                if (mortalityLabel != null) {
                    mortalityLabel.setText(String.valueOf(production.getDeadChickens()));
                }
            } else {
                // No production data for today - show 0
                if (eggsLabel != null) {
                    eggsLabel.setText("0");
                }
                if (mortalityLabel != null) {
                    mortalityLabel.setText("0");
                }
            }

            // Update date label
            if (dateLabel != null) {
                dateLabel.setText(todayFormatted);
            }

        } catch (Exception e) {
            System.err.println("Error loading production data for house " + house.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load and display total eggs in storage
     */
    private void loadStorageData() {
        try {
            // Get total good eggs from all time (or use a date range)
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Record Collection");
        alert.setHeaderText("Record Egg Collection Feature");
        alert.setContentText("This feature will open a dialog to record today's egg collection.\n\nDialog implementation is pending.");
        alert.showAndWait();
    }

    /**
     * Handle remove eggs button click
     * Opens dialog to remove eggs from storage (sold/used)
     */
    @FXML
    public void handleRemoveEggs() {
        // TODO: Open remove eggs dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Remove Eggs");
        alert.setHeaderText("Remove Eggs from Storage Feature");
        alert.setContentText("This feature will open a dialog to remove eggs from storage.\n\nDialog implementation is pending.");
        alert.showAndWait();
    }

    /**
     * Calculate storage capacity percentage
     * @param currentEggs Current eggs in storage
     * @return Percentage of capacity used
     */
    private double calculateStoragePercentage(int currentEggs) {
        return (double) currentEggs / MAX_STORAGE_CAPACITY * 100.0;
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
        // Re-check egg laying houses and reload data
        checkAndDisplayEggLayingHouses();

        // Reload storage data
        loadStorageData();
    }
}
