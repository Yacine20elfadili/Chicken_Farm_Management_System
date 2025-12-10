package ma.farm.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ma.farm.dao.HouseDAO;
import ma.farm.dao.ChickenDAO;
import ma.farm.dao.MortalityDAO;
import ma.farm.model.House;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the Chicken Bay view
 * Manages the display and interaction of chicken house data
 *
 * @author ismailouchraa
 * @version 1.0
 */
public class ChickenBayController {

    // DAOs
    private HouseDAO houseDAO;
    private ChickenDAO chickenDAO;
    private MortalityDAO mortalityDAO;

    // House 1 - Day-old
    @FXML private Label h1NameLabel;
    @FXML private Label h1TypeLabel;
    @FXML private Label h1CountLabel;
    @FXML private Label h1CapacityLabel;
    @FXML private Label h1AvgAgeLabel;
    @FXML private Label h1HealthBadge;
    @FXML private Label h1LastCleaningLabel;

    // House 2 - Egg Layer
    @FXML private Label h2NameLabel;
    @FXML private Label h2TypeLabel;
    @FXML private Label h2CountLabel;
    @FXML private Label h2CapacityLabel;
    @FXML private Label h2AvgAgeLabel;
    @FXML private Label h2HealthBadge;
    @FXML private Label h2LastCleaningLabel;

    // House 3 - Meat Female
    @FXML private Label h3NameLabel;
    @FXML private Label h3TypeLabel;
    @FXML private Label h3CountLabel;
    @FXML private Label h3CapacityLabel;
    @FXML private Label h3AvgAgeLabel;
    @FXML private Label h3HealthBadge;
    @FXML private Label h3LastCleaningLabel;

    // House 4 - Meat Male
    @FXML private Label h4NameLabel;
    @FXML private Label h4TypeLabel;
    @FXML private Label h4CountLabel;
    @FXML private Label h4CapacityLabel;
    @FXML private Label h4AvgAgeLabel;
    @FXML private Label h4HealthBadge;
    @FXML private Label h4LastCleaningLabel;

    // Mortality Statistics
    @FXML private Label deathsTodayLabel;
    @FXML private Label deathsWeekLabel;
    @FXML private Label deathsMonthLabel;

    // Date formatter
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Initializes the controller
     * Called automatically by JavaFX after FXML loading
     */
    @FXML
    public void initialize() {
        try {
            // Initialize DAOs
            houseDAO = new HouseDAO();
            chickenDAO = new ChickenDAO();
            mortalityDAO = new MortalityDAO();

            // Load all data
            loadHouseData();
            loadMortalityStats();

            System.out.println("ChickenBayController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing ChickenBayController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads data for all houses
     * Loops through houses 1-4 and loads each
     */
    private void loadHouseData() {
        try {
            for (int i = 1; i <= 4; i++) {
                loadHouseById(i);
            }
        } catch (Exception e) {
            System.err.println("Error loading house data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads data for a specific house by ID
     *
     * @param houseId the house ID (1-4)
     */
    private void loadHouseById(int houseId) {
        try {
            // Get house from database
            House house = houseDAO.getHouseById(houseId);

            if (house == null) {
                System.err.println("House " + houseId + " not found in database");
                return;
            }

            // Calculate average age (placeholder - will be implemented when ChickenDAO is ready)
            double avgAge = 0.0;
            try {
                // TODO: Implement when ChickenDAO.getAverageAge() is available
                // avgAge = chickenDAO.getAverageAge(houseId);
                avgAge = 0.0; // Placeholder
            } catch (Exception e) {
                System.err.println("Error calculating average age for house " + houseId);
            }

            // Format last cleaning date
            String lastCleaning = house.getLastCleaningDate() != null
                    ? house.getLastCleaningDate().format(DATE_FORMATTER)
                    : "Never";

            // Update UI based on house ID
            switch (houseId) {
                case 1:
                    updateHouseLabels(
                            h1NameLabel, h1TypeLabel, h1CountLabel, h1CapacityLabel,
                            h1AvgAgeLabel, h1HealthBadge, h1LastCleaningLabel,
                            house, avgAge, lastCleaning
                    );
                    break;

                case 2:
                    updateHouseLabels(
                            h2NameLabel, h2TypeLabel, h2CountLabel, h2CapacityLabel,
                            h2AvgAgeLabel, h2HealthBadge, h2LastCleaningLabel,
                            house, avgAge, lastCleaning
                    );
                    break;

                case 3:
                    updateHouseLabels(
                            h3NameLabel, h3TypeLabel, h3CountLabel, h3CapacityLabel,
                            h3AvgAgeLabel, h3HealthBadge, h3LastCleaningLabel,
                            house, avgAge, lastCleaning
                    );
                    break;

                case 4:
                    updateHouseLabels(
                            h4NameLabel, h4TypeLabel, h4CountLabel, h4CapacityLabel,
                            h4AvgAgeLabel, h4HealthBadge, h4LastCleaningLabel,
                            house, avgAge, lastCleaning
                    );
                    break;

                default:
                    System.err.println("Invalid house ID: " + houseId);
            }

        } catch (Exception e) {
            System.err.println("Error loading house " + houseId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to update all labels for a house
     */
    private void updateHouseLabels(
            Label nameLabel, Label typeLabel, Label countLabel, Label capacityLabel,
            Label avgAgeLabel, Label healthBadge, Label lastCleaningLabel,
            House house, double avgAge, String lastCleaning) {

        // Check for null labels
        if (nameLabel == null || typeLabel == null || countLabel == null ||
                capacityLabel == null || avgAgeLabel == null || healthBadge == null ||
                lastCleaningLabel == null) {
            System.err.println("One or more labels are null for house: " + house.getName());
            return;
        }

        // Update labels
        nameLabel.setText(house.getName());
        typeLabel.setText(house.getType() != null ? house.getType().getDisplayName() : "Unknown");
        countLabel.setText(String.valueOf(house.getChickenCount()));
        capacityLabel.setText(String.valueOf(house.getCapacity()));
        avgAgeLabel.setText(String.format("%.1f days", avgAge));
        lastCleaningLabel.setText(lastCleaning);

        // Update health badge
        String healthStatus = house.getHealthStatus() != null
                ? house.getHealthStatus().name()
                : "GOOD";
        healthBadge.setText(healthStatus);
        applyHealthBadge(healthBadge, healthStatus);
    }

    /**
     * Loads mortality statistics
     * Gets deaths for today, this week, and this month
     */
    private void loadMortalityStats() {
        try {
            // Get current date
            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minusDays(7);
            LocalDate monthAgo = today.minusMonths(1);

            // Get mortality counts (placeholder - will be implemented when MortalityDAO is ready)
            int deathsToday = 0;
            int deathsWeek = 0;
            int deathsMonth = 0;

            try {
                // TODO: Implement when MortalityDAO methods are available
                // deathsToday = mortalityDAO.getDeathsToday();
                // deathsWeek = mortalityDAO.getDeathsByDateRange(weekAgo, today);
                // deathsMonth = mortalityDAO.getDeathsByDateRange(monthAgo, today);

                // For now, we can use EggProductionDAO as it tracks deadChickens
                // This is a temporary solution until MortalityDAO is implemented
                deathsToday = 0; // Placeholder
                deathsWeek = 0;  // Placeholder
                deathsMonth = 0; // Placeholder
            } catch (Exception e) {
                System.err.println("Error calculating mortality stats");
            }

            // Update labels
            if (deathsTodayLabel != null) {
                deathsTodayLabel.setText(String.valueOf(deathsToday));
            }
            if (deathsWeekLabel != null) {
                deathsWeekLabel.setText(String.valueOf(deathsWeek));
            }
            if (deathsMonthLabel != null) {
                deathsMonthLabel.setText(String.valueOf(deathsMonth));
            }

        } catch (Exception e) {
            System.err.println("Error loading mortality stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Applies color styling to health badge based on status
     *
     * @param label the health badge label
     * @param status the health status (GOOD, FAIR, POOR)
     */
    private void applyHealthBadge(Label label, String status) {
        if (label == null) {
            return;
        }

        // Remove all previous style classes
        label.getStyleClass().removeAll("health-good", "health-fair", "health-poor");

        // Apply appropriate style class based on status
        switch (status.toUpperCase()) {
            case "GOOD":
                label.getStyleClass().add("health-good");
                break;
            case "FAIR":
                label.getStyleClass().add("health-fair");
                break;
            case "POOR":
                label.getStyleClass().add("health-poor");
                break;
            default:
                // Default to good if unknown status
                label.getStyleClass().add("health-good");
                System.err.println("Unknown health status: " + status);
        }
    }

    /**
     * Handles the "Add Chickens" button click
     * Opens a dialog to add chickens to a house
     */
    @FXML
    private void handleAddChickens() {
        try {
            // TODO: Implement dialog to add chickens
            // This will be implemented in a future iteration
            System.out.println("Add Chickens feature - Coming soon");

            // After adding chickens, refresh data
            // refreshData();
        } catch (Exception e) {
            System.err.println("Error handling add chickens: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the "Record Death" button click
     * Opens a dialog to record chicken mortality
     */
    @FXML
    private void handleRecordDeath() {
        try {
            // TODO: Implement dialog to record death
            // This will be implemented in a future iteration
            System.out.println("Record Death feature - Coming soon");

            // After recording death, refresh data
            // refreshData();
        } catch (Exception e) {
            System.err.println("Error handling record death: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Refreshes all data on the page
     * Call this method after any data changes
     */
    public void refreshData() {
        try {
            loadHouseData();
            loadMortalityStats();
            System.out.println("Chicken Bay data refreshed");
        } catch (Exception e) {
            System.err.println("Error refreshing data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}