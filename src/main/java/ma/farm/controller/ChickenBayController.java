package ma.farm.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ma.farm.controller.dialogs.*;
import ma.farm.dao.HouseDAO;
import ma.farm.dao.MortalityDAO;
import ma.farm.model.House;
import ma.farm.model.HouseType;

import java.util.List;

/**
 * ChickenBayController - Controls the Chicken Bay view
 *
 * New design with:
 * - Top: Title + Config Houses + Record Death buttons
 * - Mortality statistics row
 * - 4 sections: DayOld, FemaleEggLayer, FemaleMeat, MaleMeat
 * - Dynamic house cards per section
 */
public class ChickenBayController {

    // Mortality Stats Labels
    @FXML
    private Label deathsTodayLabel;

    @FXML
    private Label deathsWeekLabel;

    @FXML
    private Label deathsMonthLabel;

    @FXML
    private Label deathsTotalLabel;

    // Section containers
    @FXML
    private VBox dayOldSection;

    @FXML
    private FlowPane dayOldCardsContainer;

    @FXML
    private Label dayOldEmptyLabel;

    @FXML
    private VBox eggLayerSection;

    @FXML
    private FlowPane eggLayerCardsContainer;

    @FXML
    private Label eggLayerEmptyLabel;

    @FXML
    private VBox femaleMeatSection;

    @FXML
    private FlowPane femaleMeatCardsContainer;

    @FXML
    private Label femaleMeatEmptyLabel;

    @FXML
    private VBox maleMeatSection;

    @FXML
    private FlowPane maleMeatCardsContainer;

    @FXML
    private Label maleMeatEmptyLabel;

    // Import button (only for DayOld section)
    @FXML
    private Button importButton;

    // DAOs
    private HouseDAO houseDAO;
    private MortalityDAO mortalityDAO;

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        houseDAO = new HouseDAO();
        mortalityDAO = new MortalityDAO();

        // Load all data
        refreshData();
    }

    /**
     * Refresh all data on the page
     */
    @FXML
    public void refreshData() {
        loadMortalityStats();
        loadHousesBySection();
        updateImportButtonState();
    }

    /**
     * Load mortality statistics
     */
    private void loadMortalityStats() {
        try {
            MortalityDAO.MortalityStatistics stats = mortalityDAO.getMortalityStatistics();

            if (deathsTodayLabel != null) {
                deathsTodayLabel.setText(String.valueOf(stats.getTodayDeaths()));
            }
            if (deathsWeekLabel != null) {
                deathsWeekLabel.setText(String.valueOf(stats.getThisWeekDeaths()));
            }
            if (deathsMonthLabel != null) {
                deathsMonthLabel.setText(String.valueOf(stats.getThisMonthDeaths()));
            }
            if (deathsTotalLabel != null) {
                deathsTotalLabel.setText(String.valueOf(stats.getTotalDeaths()));
            }
        } catch (Exception e) {
            System.err.println("Error loading mortality stats: " + e.getMessage());
        }
    }

    /**
     * Load houses and display them in their respective sections
     */
    private void loadHousesBySection() {
        // Clear all containers
        if (dayOldCardsContainer != null) dayOldCardsContainer.getChildren().clear();
        if (eggLayerCardsContainer != null) eggLayerCardsContainer.getChildren().clear();
        if (femaleMeatCardsContainer != null) femaleMeatCardsContainer.getChildren().clear();
        if (maleMeatCardsContainer != null) maleMeatCardsContainer.getChildren().clear();

        // Check if houses are configured
        boolean housesConfigured = houseDAO.areHousesConfigured();

        if (!housesConfigured) {
            // Show empty messages for all sections
            showEmptyMessage(dayOldEmptyLabel, true);
            showEmptyMessage(eggLayerEmptyLabel, true);
            showEmptyMessage(femaleMeatEmptyLabel, true);
            showEmptyMessage(maleMeatEmptyLabel, true);
            return;
        }

        // Load each section
        loadSectionHouses(HouseType.DAY_OLD, dayOldCardsContainer, dayOldEmptyLabel);
        loadSectionHouses(HouseType.EGG_LAYER, eggLayerCardsContainer, eggLayerEmptyLabel);
        loadSectionHouses(HouseType.MEAT_FEMALE, femaleMeatCardsContainer, femaleMeatEmptyLabel);
        loadSectionHouses(HouseType.MEAT_MALE, maleMeatCardsContainer, maleMeatEmptyLabel);
    }

    /**
     * Load houses for a specific section
     */
    private void loadSectionHouses(HouseType type, FlowPane container, Label emptyLabel) {
        if (container == null) return;

        List<House> houses = houseDAO.getHousesByType(type);

        if (houses.isEmpty()) {
            showEmptyMessage(emptyLabel, true);
        } else {
            showEmptyMessage(emptyLabel, false);
            for (House house : houses) {
                VBox card = createHouseCard(house);
                container.getChildren().add(card);
            }
        }
    }

    /**
     * Show or hide empty message label
     */
    private void showEmptyMessage(Label label, boolean show) {
        if (label != null) {
            label.setVisible(show);
            label.setManaged(show);
        }
    }

    /**
     * Create a house card for display
     */
    private VBox createHouseCard(House house) {
        VBox card = new VBox(8);
        card.setPrefWidth(220);
        card.setPadding(new Insets(15));

        // Determine if house is empty (grayed out)
        boolean isEmpty = house.isEmpty();

        if (isEmpty) {
            card.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8; " +
                         "-fx-border-color: #ddd; -fx-border-radius: 8; -fx-border-style: dashed; -fx-opacity: 0.7;");
        } else {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                         "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        }

        // Card Title (House name)
        Label titleLabel = new Label(house.getCardTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        if (isEmpty) {
            // Empty house display
            Label emptyLabel = new Label("Empty");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");

            Label capacityLabel = new Label("Capacity: " + house.getCapacity());
            capacityLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

            card.getChildren().addAll(titleLabel, emptyLabel, capacityLabel);
        } else {
            // Occupied house display
            // Chicken count
            HBox countRow = createInfoRow("Chickens:", String.valueOf(house.getChickenCount()));

            // Age with color coding
            String ageText = house.getFormattedAge();
            String ageColor = getAgeColor(house.getAgeColorStatus());
            HBox ageRow = createInfoRow("Age:", ageText);
            // The row contains: [Label (index 0), Region spacer (index 1), Label value (index 2)]
            Label ageValueLabel = (Label) ageRow.getChildren().get(2);
            ageValueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + ageColor + ";");

            // Estimated date
            String actionLabel = getActionLabel(house.getType());
            HBox dateRow = createInfoRow(actionLabel + ":", house.getFormattedEstimatedEndDate());

            card.getChildren().addAll(titleLabel, countRow, ageRow, dateRow);

            // Add action button based on house type
            Button actionButton = createActionButton(house);
            if (actionButton != null) {
                HBox buttonRow = new HBox();
                buttonRow.setAlignment(Pos.CENTER_RIGHT);
                buttonRow.setPadding(new Insets(5, 0, 0, 0));
                buttonRow.getChildren().add(actionButton);
                card.getChildren().add(buttonRow);
            }
        }

        return card;
    }

    /**
     * Create an info row (label: value)
     */
    private HBox createInfoRow(String labelText, String valueText) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label value = new Label(valueText);
        value.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        row.getChildren().addAll(label, spacer, value);
        return row;
    }

    /**
     * Get color for age display based on status
     */
    private String getAgeColor(String status) {
        switch (status) {
            case "green":
                return "#28a745";
            case "orange":
                return "#ff9800";
            case "red":
                return "#dc3545";
            default:
                return "#333";
        }
    }

    /**
     * Get action label based on house type
     */
    private String getActionLabel(HouseType type) {
        switch (type) {
            case DAY_OLD:
                return "Distribute";
            case EGG_LAYER:
                return "Transfer";
            case MEAT_FEMALE:
            case MEAT_MALE:
                return "Sell";
            default:
                return "Action";
        }
    }

    /**
     * Create action button for a house based on its type
     */
    private Button createActionButton(House house) {
        if (house.isEmpty()) {
            return null;
        }

        Button button = new Button();
        button.setStyle("-fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 10;");

        switch (house.getType()) {
            case DAY_OLD:
                button.setText("Distribute");
                button.setStyle(button.getStyle() + " -fx-background-color: #2196f3; -fx-text-fill: white;");

                // Check if there are empty houses in both EggLayer and MaleMeat
                boolean hasEmptyEggLayer = !houseDAO.getEmptyHousesByType(HouseType.EGG_LAYER).isEmpty();
                boolean hasEmptyMaleMeat = !houseDAO.getEmptyHousesByType(HouseType.MEAT_MALE).isEmpty();
                button.setDisable(!hasEmptyEggLayer || !hasEmptyMaleMeat);

                button.setOnAction(e -> handleDistribute(house));
                break;

            case EGG_LAYER:
                button.setText("Transfer");
                button.setStyle(button.getStyle() + " -fx-background-color: #9c27b0; -fx-text-fill: white;");

                // Check if there are empty FemaleMeat houses with enough capacity
                int totalFemaleMeatCapacity = houseDAO.getTotalEmptyCapacityByType(HouseType.MEAT_FEMALE);
                button.setDisable(totalFemaleMeatCapacity < house.getChickenCount());

                button.setOnAction(e -> handleTransfer(house));
                break;

            case MEAT_FEMALE:
            case MEAT_MALE:
                button.setText("Sell");
                button.setStyle(button.getStyle() + " -fx-background-color: #4caf50; -fx-text-fill: white;");

                // Check if past 60% threshold
                button.setDisable(!house.isPastSellThreshold());

                button.setOnAction(e -> handleSell(house));
                break;

            default:
                return null;
        }

        return button;
    }

    /**
     * Update Import button state based on DayOld house availability
     */
    private void updateImportButtonState() {
        if (importButton == null) return;

        // Check if there are any DayOld houses with available capacity
        List<House> dayOldHouses = houseDAO.getHousesByType(HouseType.DAY_OLD);
        boolean hasAvailableCapacity = false;

        for (House house : dayOldHouses) {
            if (!house.isFull()) {
                hasAvailableCapacity = true;
                break;
            }
        }

        importButton.setDisable(!hasAvailableCapacity || dayOldHouses.isEmpty());
    }

    // ==================== Button Handlers ====================

    /**
     * Handle Config Houses button click
     */
    @FXML
    public void handleConfigHouses() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/ConfigHousesDialog.fxml"));
            Parent root = loader.load();

            ConfigHousesDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Configure Houses");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(true);
            dialogStage.setMinWidth(550);
            dialogStage.setMinHeight(650);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                refreshData();
                showSuccessAlert("Houses configured successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error opening Config Houses dialog: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Failed to open Config Houses dialog: " + e.getMessage());
        }
    }

    /**
     * Handle Record Death button click
     */
    @FXML
    public void handleRecordDeath() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/RecordMortalityDialog.fxml"));
            Parent root = loader.load();

            RecordMortalityDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Record Mortality");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(true);
            dialogStage.setMinWidth(550);
            dialogStage.setMinHeight(700);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                refreshData();
                showSuccessAlert("Mortality recorded successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error opening Record Mortality dialog: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Failed to open Record Mortality dialog: " + e.getMessage());
        }
    }

    /**
     * Handle Import button click (DayOld section)
     */
    @FXML
    public void handleImport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/ImportChicksDialog.fxml"));
            Parent root = loader.load();

            ImportChicksDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Import Day-Old Chicks");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(true);
            dialogStage.setMinWidth(500);
            dialogStage.setMinHeight(450);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                refreshData();
                showSuccessAlert("Chicks imported successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error opening Import Chicks dialog: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Failed to open Import Chicks dialog: " + e.getMessage());
        }
    }

    /**
     * Handle Distribute button click (DayOld house card)
     */
    private void handleDistribute(House house) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/DistributeChicksDialog.fxml"));
            Parent root = loader.load();

            DistributeChicksDialogController controller = loader.getController();
            controller.setSourceHouse(house);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Distribute Chicks");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(true);
            dialogStage.setMinWidth(600);
            dialogStage.setMinHeight(700);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                refreshData();
                showSuccessAlert("Chicks distributed successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error opening Distribute Chicks dialog: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Failed to open Distribute Chicks dialog: " + e.getMessage());
        }
    }

    /**
     * Handle Transfer button click (FemaleEggLayer house card)
     */
    private void handleTransfer(House house) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/TransferChickensDialog.fxml"));
            Parent root = loader.load();

            TransferChickensDialogController controller = loader.getController();
            controller.setSourceHouse(house);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Transfer Chickens");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(true);
            dialogStage.setMinWidth(550);
            dialogStage.setMinHeight(550);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                refreshData();
                showSuccessAlert("Chickens transferred successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error opening Transfer Chickens dialog: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Failed to open Transfer Chickens dialog: " + e.getMessage());
        }
    }

    /**
     * Handle Sell button click (FemaleMeat or MaleMeat house card)
     */
    private void handleSell(House house) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/SellChickensDialog.fxml"));
            Parent root = loader.load();

            SellChickensDialogController controller = loader.getController();
            controller.setSourceHouse(house);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Sell Chickens");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(true);
            dialogStage.setMinWidth(500);
            dialogStage.setMinHeight(500);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                refreshData();
                showSuccessAlert("Chickens sold successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error opening Sell Chickens dialog: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Failed to open Sell Chickens dialog: " + e.getMessage());
        }
    }

    // ==================== Alert Helpers ====================

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
