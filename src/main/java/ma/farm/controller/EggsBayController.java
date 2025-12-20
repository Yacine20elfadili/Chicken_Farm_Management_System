package ma.farm.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ma.farm.controller.dialogs.RecordEggCollectionDialogController;
import ma.farm.controller.dialogs.SellEggsDialogController;
import ma.farm.dao.EggProductionDAO;
import ma.farm.dao.HouseDAO;
import ma.farm.model.EggProduction;
import ma.farm.model.House;
import ma.farm.model.HouseType;
import ma.farm.util.DateUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * EggsBayController - Controls the Eggs Bay view
 * Shows: All houses that lay eggs (EGG_LAYER and MEAT_FEMALE)
 * Statistics: Production and cracked eggs (Today, Week, Month)
 * Storage: Total eggs in inventory
 * CRUD: Record daily egg collection
 */
public class EggsBayController {

    // FXML Components - House Cards Container
    @FXML
    private HBox houseCardsContainer;

    // FXML Components - Statistics Cards
    @FXML
    private VBox productionStatsCard;

    @FXML
    private Label productionTodayLabel;

    @FXML
    private Label productionWeekLabel;

    @FXML
    private Label productionMonthLabel;

    @FXML
    private VBox crackedStatsCard;

    @FXML
    private Label crackedTodayLabel;

    @FXML
    private Label crackedWeekLabel;

    @FXML
    private Label crackedMonthLabel;

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

    @FXML
    private Button sellEggsButton;

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
        System.out.println("=== EggsBayController: Initializing ===");

        // Initialize DAOs
        eggProductionDAO = new EggProductionDAO();
        houseDAO = new HouseDAO();

        // Check if there are any egg-laying houses first
        checkAndDisplayEggLayingHouses();

        // Load statistics
        loadStatistics();

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
                    .filter(house -> house.getType() == HouseType.EGG_LAYER ||
                                   house.getType() == HouseType.MEAT_FEMALE)
                    .collect(Collectors.toList());

            System.out.println("Found " + eggLayingHouses.size() + " egg-laying houses");

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
     * Dynamically creates cards for ALL egg-laying houses (not limited to H2/H3)
     */
    private void loadEggLayingHousesData(List<House> eggLayingHouses) {
        // Clear previous cards
        if (houseCardsContainer != null) {
            houseCardsContainer.getChildren().clear();
        }

        // Set today's date
        LocalDate today = LocalDate.now();
        String todayFormatted = DateUtil.formatDate(today);

        // Create a card for each egg-laying house
        for (House house : eggLayingHouses) {
            VBox houseCard = createHouseCard(house, todayFormatted);
            houseCardsContainer.getChildren().add(houseCard);
        }

        System.out.println("Created " + eggLayingHouses.size() + " house cards");
    }

    /**
     * Create a house card dynamically for a given house
     * @param house the house to create a card for
     * @param todayFormatted today's date formatted
     * @return a VBox containing the house card
     */
    private VBox createHouseCard(House house, String todayFormatted) {
        VBox card = new VBox();
        card.setStyle("-fx-border-color: #ddd; -fx-border-radius: 8; -fx-padding: 15; -fx-spacing: 10;");
        card.setPrefWidth(300);
        card.getStyleClass().add("metric-card");

        // House name label - FIXED: Use house.getName() instead of house.getId()
        Label nameLabel = new Label(house.getName() + " - " + house.getType().getDisplayName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Eggs collected today
        HBox eggsBox = new HBox();
        eggsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        eggsBox.setSpacing(10);
        Label eggsLabel = new Label("Œufs du jour :");
        Label eggsValueLabel = new Label("0");
        eggsValueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3eff51;");
        eggsBox.getChildren().addAll(eggsLabel, new javafx.scene.layout.Region(), eggsValueLabel);

        // Cracked eggs today
        HBox crackedBox = new HBox();
        crackedBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        crackedBox.setSpacing(10);
        Label crackedLabel = new Label("Cracked :");
        Label crackedValueLabel = new Label("0");
        crackedValueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ff6b6b;");
        crackedBox.getChildren().addAll(crackedLabel, new javafx.scene.layout.Region(), crackedValueLabel);

        // Date
        HBox dateBox = new HBox();
        dateBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        dateBox.setSpacing(10);
        Label dateLabel = new Label("Date :");
        Label dateValueLabel = new Label(todayFormatted);
        dateValueLabel.setStyle("-fx-font-size: 12px;");
        dateBox.getChildren().addAll(dateLabel, new javafx.scene.layout.Region(), dateValueLabel);

        card.getChildren().addAll(nameLabel, eggsBox, crackedBox, dateBox);

        // Load production data for this house
        try {
            LocalDate today = LocalDate.now();
            List<EggProduction> productions = eggProductionDAO.getProductionByDate(today);

            for (EggProduction prod : productions) {
                if (prod.getHouseId() == house.getId()) {
                    eggsValueLabel.setText(String.valueOf(prod.getEggsCollected()));
                    crackedValueLabel.setText(String.valueOf(prod.getCrackedEggs()));
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading production data for house " + house.getId() + ": " + e.getMessage());
        }

        HBox.setHgrow(card, javafx.scene.layout.Priority.ALWAYS);
        return card;
    }

    /**
     * Load and display production and cracked statistics
     *
     * Œufs Produits = eggsCollected (total eggs collected)
     * Œufs Cracked = crackedEggs
     */
    private void loadStatistics() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate weekStart = today.minusDays(6); // Last 7 days including today
            LocalDate monthStart = today.minusDays(29); // Last 30 days including today

            // Get all production records
            List<EggProduction> allProductions = eggProductionDAO.getAllProduction();

            // Today's statistics
            int eggsTodayProduced = 0;
            int eggsTodayCracked = 0;

            // Week statistics
            int eggsWeekProduced = 0;
            int eggsWeekCracked = 0;

            // Month statistics
            int eggsMonthProduced = 0;
            int eggsMonthCracked = 0;

            for (EggProduction prod : allProductions) {
                LocalDate prodDate = prod.getProductionDate();
                if (prodDate == null) continue;

                // Today
                if (prodDate.equals(today)) {
                    eggsTodayProduced += prod.getEggsCollected();
                    eggsTodayCracked += prod.getCrackedEggs();
                }

                // This week (last 7 days)
                if (!prodDate.isBefore(weekStart) && !prodDate.isAfter(today)) {
                    eggsWeekProduced += prod.getEggsCollected();
                    eggsWeekCracked += prod.getCrackedEggs();
                }

                // This month (last 30 days)
                if (!prodDate.isBefore(monthStart) && !prodDate.isAfter(today)) {
                    eggsMonthProduced += prod.getEggsCollected();
                    eggsMonthCracked += prod.getCrackedEggs();
                }
            }

            // Update UI - Production
            if (productionTodayLabel != null) {
                productionTodayLabel.setText(String.format("%,d", eggsTodayProduced));
            }
            if (productionWeekLabel != null) {
                productionWeekLabel.setText(String.format("%,d", eggsWeekProduced));
            }
            if (productionMonthLabel != null) {
                productionMonthLabel.setText(String.format("%,d", eggsMonthProduced));
            }

            // Update UI - Cracked
            if (crackedTodayLabel != null) {
                crackedTodayLabel.setText(String.format("%,d", eggsTodayCracked));
            }
            if (crackedWeekLabel != null) {
                crackedWeekLabel.setText(String.format("%,d", eggsWeekCracked));
            }
            if (crackedMonthLabel != null) {
                crackedMonthLabel.setText(String.format("%,d", eggsMonthCracked));
            }

            System.out.println("Statistics loaded - Today: " + eggsTodayProduced + " produced, " +
                             eggsTodayCracked + " cracked");

        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load and display total eggs in storage
     *
     * Stock = SUM(eggsCollected) - SUM(crackedEggs) from all houses all time
     * This equals SUM(goodEggs) since goodEggs = eggsCollected - crackedEggs
     */
    private void loadStorageData() {
        try {
            // Get all production records
            List<EggProduction> allProductions = eggProductionDAO.getAllProduction();

            // Calculate total stock = sum of all good eggs (collected - cracked)
            int totalEggsCollected = 0;
            int totalCracked = 0;

            for (EggProduction prod : allProductions) {
                totalEggsCollected += prod.getEggsCollected();
                totalCracked += prod.getCrackedEggs();
            }

            // Stock = Total Collected - Total Cracked
            int totalStock = totalEggsCollected - totalCracked;
            if (totalStock < 0) {
                totalStock = 0;
            }

            // Update totalEggsLabel
            if (totalEggsLabel != null) {
                totalEggsLabel.setText(String.format("%,d", totalStock));
            }

            // Calculate storage percentage
            double percentage = calculateStoragePercentage(totalStock);

            // Apply storage status badge
            applyStorageStatusBadge(percentage);

            System.out.println("Storage loaded - Collected: " + totalEggsCollected + ", Cracked: " + totalCracked +
                             ", Stock: " + totalStock + " (" + String.format("%.1f", percentage) + "%)");

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
        try {
            // Load the dialog FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/dialogs/RecordEggCollectionDialog.fxml")
            );
            Parent dialogContent = loader.load();
            RecordEggCollectionDialogController dialogController = loader.getController();

            // Create and show the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Enregistrer la Collecte d'Œufs");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogContent));
            dialogStage.setResizable(true);
            dialogStage.setMinWidth(520);
            dialogStage.setMinHeight(550);
            dialogStage.showAndWait();

            // Check if save was clicked
            if (dialogController.isSaved()) {
                System.out.println("Egg collection recorded successfully");
                // Refresh data
                refreshData();

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("Collecte Enregistrée");
                alert.setContentText("La collecte d'œufs a été enregistrée avec succès.");
                alert.showAndWait();
            }

        } catch (IOException e) {
            System.err.println("Error opening record collection dialog: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur");
            alert.setContentText("Impossible d'ouvrir le formulaire de collecte.");
            alert.showAndWait();
        }
    }

    /**
     * Handle sell eggs button click
     * Opens dialog to sell eggs from storage
     */
    @FXML
    public void handleSellEggs() {
        try {
            // Load the dialog FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/dialogs/SellEggsDialog.fxml")
            );
            Parent dialogContent = loader.load();
            SellEggsDialogController dialogController = loader.getController();

            // Create and show the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Vendre des Œufs");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogContent));
            dialogStage.setResizable(true);
            dialogStage.setMinWidth(750);
            dialogStage.setMinHeight(480);
            dialogStage.showAndWait();

            // Check if save was clicked
            if (dialogController.isSaved()) {
                int soldQuantity = dialogController.getSoldQuantity();
                System.out.println("Eggs sold successfully: " + soldQuantity);

                // Refresh data
                refreshData();

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("Vente Enregistrée");
                alert.setContentText(soldQuantity + " œufs ont été vendus avec succès.");
                alert.showAndWait();
            }

        } catch (IOException e) {
            System.err.println("Error opening sell eggs dialog: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur");
            alert.setContentText("Impossible d'ouvrir le formulaire de vente.");
            alert.showAndWait();
        }
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
            storageStatusLabel.setText("Stock OK (" + String.format("%.1f", percentage) + "%)");
            storageStatusLabel.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
        } else if (percentage < 80) {
            // Yellow if 50-80%
            storageStatusLabel.setText("Stock Remplissage (" + String.format("%.1f", percentage) + "%)");
            storageStatusLabel.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
        } else {
            // Red if > 80%
            storageStatusLabel.setText("Stock Complet (" + String.format("%.1f", percentage) + "%)");
            storageStatusLabel.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
        }
    }

    /**
     * Refresh all eggs bay data
     */
    @FXML
    public void refreshData() {
        System.out.println("Refreshing EggsBay data...");

        // Re-check egg laying houses and reload data
        checkAndDisplayEggLayingHouses();

        // Reload statistics
        loadStatistics();

        // Reload storage data
        loadStorageData();
    }
}
