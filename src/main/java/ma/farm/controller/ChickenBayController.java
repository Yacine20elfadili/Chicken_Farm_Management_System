package ma.farm.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import ma.farm.dao.ChickenDAO;
import ma.farm.dao.HouseDAO;
import ma.farm.dao.MortalityDAO;
import ma.farm.model.Chicken;
import ma.farm.model.House;
import ma.farm.util.DateUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * ChickenBayController - Controls the Chicken Bay view
 * Shows: 4 house cards (H1-H4), mortality statistics
 */
public class ChickenBayController {

    // FXML Components - House Cards
    @FXML
    private VBox house1Card;

    @FXML
    private VBox house2Card;

    @FXML
    private VBox house3Card;

    @FXML
    private VBox house4Card;

    // House 1 Labels (Day-old chicks)
    @FXML
    private Label h1NameLabel;

    @FXML
    private Label h1TypeLabel;

    @FXML
    private Label h1CountLabel;

    @FXML
    private Label h1AgeLabel;

    @FXML
    private Label h1HealthLabel;

    @FXML
    private Label h1TransferLabel;

    // House 2 Labels (Egg layers)
    @FXML
    private Label h2NameLabel;

    @FXML
    private Label h2TypeLabel;

    @FXML
    private Label h2CountLabel;

    @FXML
    private Label h2AgeLabel;

    @FXML
    private Label h2HealthLabel;

    @FXML
    private Label h2TransferLabel;

    // House 3 Labels (Meat female)
    @FXML
    private Label h3NameLabel;

    @FXML
    private Label h3TypeLabel;

    @FXML
    private Label h3CountLabel;

    @FXML
    private Label h3AgeLabel;

    @FXML
    private Label h3HealthLabel;

    @FXML
    private Label h3TransferLabel;

    // House 4 Labels (Meat male)
    @FXML
    private Label h4NameLabel;

    @FXML
    private Label h4TypeLabel;

    @FXML
    private Label h4CountLabel;

    @FXML
    private Label h4AgeLabel;

    @FXML
    private Label h4HealthLabel;

    @FXML
    private Label h4TransferLabel;

    // Mortality Card Labels
    @FXML
    private Label deathsTodayLabel;

    @FXML
    private Label deathsWeekLabel;

    @FXML
    private Label deathsMonthLabel;

    // DAOs
    private HouseDAO houseDAO;
    private ChickenDAO chickenDAO;
    private MortalityDAO mortalityDAO;

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        // Initialize DAOs
        houseDAO = new HouseDAO();
        chickenDAO = new ChickenDAO();
        mortalityDAO = new MortalityDAO();

        // Load all house data
        loadHouseData();

        // Load mortality statistics
        loadMortalityStats();
    }

    /**
     * Load and display data for all 4 houses
     */
    private void loadHouseData() {
        // Load House 1 data
        loadHouseById(1);

        // Load House 2 data
        loadHouseById(2);

        // Load House 3 data
        loadHouseById(3);

        // Load House 4 data
        loadHouseById(4);
    }

    /**
     * Load and display data for a specific house
     * @param houseId The house ID (1-4)
     */
    private void loadHouseById(int houseId) {
        try {
            // Get house from HouseDAO
            House house = houseDAO.getHouseById(houseId);

            if (house == null) {
                System.err.println("House " + houseId + " not found in database");
                return;
            }

            // Get chickens from ChickenDAO
            List<Chicken> chickens = chickenDAO.getChickensByHouse(houseId);

            // Calculate average age
            int averageAge = 0;
            if (!chickens.isEmpty()) {
                int totalAge = 0;
                for (Chicken chicken : chickens) {
                    totalAge += chicken.getAgeInDays();
                }
                averageAge = totalAge / chickens.size();
            }

            // Calculate days until transfer (from first chicken batch)
            int daysUntilTransfer = 0;
            if (!chickens.isEmpty() && chickens.get(0).getNextTransferDate() != null) {
                daysUntilTransfer = (int) DateUtil.daysUntil(chickens.get(0).getNextTransferDate());
            }

            // Update corresponding labels based on house ID
            switch (houseId) {
                case 1:
                    updateHouseLabels(
                            h1NameLabel, h1TypeLabel, h1CountLabel,
                            h1AgeLabel, h1HealthLabel, h1TransferLabel,
                            house, averageAge, daysUntilTransfer
                    );
                    break;
                case 2:
                    updateHouseLabels(
                            h2NameLabel, h2TypeLabel, h2CountLabel,
                            h2AgeLabel, h2HealthLabel, h2TransferLabel,
                            house, averageAge, daysUntilTransfer
                    );
                    break;
                case 3:
                    updateHouseLabels(
                            h3NameLabel, h3TypeLabel, h3CountLabel,
                            h3AgeLabel, h3HealthLabel, h3TransferLabel,
                            house, averageAge, daysUntilTransfer
                    );
                    break;
                case 4:
                    updateHouseLabels(
                            h4NameLabel, h4TypeLabel, h4CountLabel,
                            h4AgeLabel, h4HealthLabel, h4TransferLabel,
                            house, averageAge, daysUntilTransfer
                    );
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error loading house " + houseId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update house labels with data
     */
    private void updateHouseLabels(Label nameLabel, Label typeLabel, Label countLabel,
                                   Label ageLabel, Label healthLabel, Label transferLabel,
                                   House house, int averageAge, int daysUntilTransfer) {
        // Update labels
        if (nameLabel != null) nameLabel.setText(house.getName());
        if (typeLabel != null) typeLabel.setText(house.getType() != null ? house.getType().getDisplayName() : "Unknown");
        if (countLabel != null) countLabel.setText(String.valueOf(house.getChickenCount()));
        if (ageLabel != null) ageLabel.setText(averageAge + " days");
        if (healthLabel != null) {
            String healthStatus = house.getHealthStatus() != null ? house.getHealthStatus().getDisplayName() : "Unknown";
            healthLabel.setText(healthStatus);
            applyHealthBadge(healthLabel, healthStatus);
        }
        if (transferLabel != null) {
            if (daysUntilTransfer > 0) {
                transferLabel.setText(daysUntilTransfer + " days");
            } else if (daysUntilTransfer == 0) {
                transferLabel.setText("Today");
            } else {
                transferLabel.setText("Overdue");
            }
        }
    }

    /**
     * Load and display mortality statistics
     */
    private void loadMortalityStats() {
        try {
            // Get deaths today from MortalityDAO
            MortalityDAO.MortalityStatistics stats = mortalityDAO.getMortalityStatistics();

            // Update mortality labels
            if (deathsTodayLabel != null) {
                deathsTodayLabel.setText(String.valueOf(stats.getTodayDeaths()));
            }

            if (deathsWeekLabel != null) {
                deathsWeekLabel.setText(String.valueOf(stats.getThisWeekDeaths()));
            }

            if (deathsMonthLabel != null) {
                deathsMonthLabel.setText(String.valueOf(stats.getThisMonthDeaths()));
            }
        } catch (Exception e) {
            System.err.println("Error loading mortality stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle add chickens button click
     * Opens dialog to add new chickens to a house
     */
    @FXML
    public void handleAddChickens() {
        // TODO: Open add chickens dialog
        // This would typically open a dialog window where the user can:
        // 1. Select house (H1-H4)
        // 2. Enter quantity
        // 3. Enter arrival date
        // 4. Enter batch number
        // 5. Enter other details (gender, health status, etc.)

        // For now, show placeholder alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Chickens");
        alert.setHeaderText("Add Chickens Feature");
        alert.setContentText("This feature will open a dialog to add new chickens to a house.\n\nDialog implementation is pending.");
        alert.showAndWait();

        // After dialog implementation, the code would be:
        // - Create new Chicken record
        // - Save to database using chickenDAO.createChickenBatch()
        // - Update house chicken count using houseDAO.updateChickenCount()
        // - Refresh house data
    }

    /**
     * Handle record death button click
     * Opens dialog to record chicken deaths
     */
    @FXML
    public void handleRecordDeath() {
        // TODO: Open record death dialog
        // This would typically open a dialog window where the user can:
        // 1. Select house (H1-H4)
        // 2. Enter death count
        // 3. Enter cause of death
        // 4. Enter symptoms (optional)
        // 5. Mark as outbreak if applicable

        // For now, show placeholder alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Record Death");
        alert.setHeaderText("Record Death Feature");
        alert.setContentText("This feature will open a dialog to record chicken deaths.\n\nDialog implementation is pending.");
        alert.showAndWait();

        // After dialog implementation, the code would be:
        // - Create Mortality record
        // - Save to database using mortalityDAO.recordMortality()
        // - Update house chicken count
        // - Refresh data
    }

    /**
     * Apply health status badge styling
     * @param label The label to style
     * @param status Health status (Good/Fair/Poor)
     */
    private void applyHealthBadge(Label label, String status) {
        if (label == null || status == null) {
            return;
        }

        // Remove previous style classes
        label.getStyleClass().removeAll("health-good", "health-fair", "health-poor");

        // Apply color based on status
        switch (status.toLowerCase()) {
            case "good":
                label.getStyleClass().add("health-good");
                label.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
                break;
            case "fair":
                label.getStyleClass().add("health-fair");
                label.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
                break;
            case "poor":
                label.getStyleClass().add("health-poor");
                label.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
                break;
            default:
                label.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
        }
    }

    /**
     * Refresh all chicken bay data
     */
    @FXML
    public void refreshData() {
        // Reload house data
        loadHouseData();

        // Reload mortality stats
        loadMortalityStats();
    }
}
