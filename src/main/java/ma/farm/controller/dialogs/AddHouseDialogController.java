package ma.farm.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ma.farm.dao.HouseDAO;
import ma.farm.model.HealthStatus;
import ma.farm.model.House;
import ma.farm.model.HouseType;

import java.time.LocalDate;

/**
 * Controller for Add House Dialog
 * Handles adding a new house to the system
 */
public class AddHouseDialogController {

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<HouseType> typeComboBox;

    @FXML
    private TextField capacityField;

    @FXML
    private TextField chickenCountField;

    @FXML
    private ComboBox<HealthStatus> healthStatusComboBox;

    @FXML
    private Label errorLabel;

    private HouseDAO houseDAO;
    private boolean saved = false;

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        // Initialize DAO
        houseDAO = new HouseDAO();

        // Populate type combo box with all house types
        typeComboBox.setItems(FXCollections.observableArrayList(HouseType.values()));

        // Populate health status combo box
        healthStatusComboBox.setItems(FXCollections.observableArrayList(HealthStatus.values()));

        // Set default values
        healthStatusComboBox.setValue(HealthStatus.GOOD);
        chickenCountField.setText("0");

        // Add numeric validation to capacity and count fields
        addNumericValidation(capacityField);
        addNumericValidation(chickenCountField);
    }

    /**
     * Add numeric-only validation to text field
     */
    private void addNumericValidation(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    /**
     * Handle save button click
     */
    @FXML
    public void handleSave() {
        System.out.println("=== ADD HOUSE: handleSave() called ===");

        // Hide previous error
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Validate inputs
        if (!validateInputs()) {
            System.out.println("ADD HOUSE: Validation failed");
            return;
        }

        System.out.println("ADD HOUSE: Validation passed");

        try {
            // Find available house ID (1-4)
            int availableId = findAvailableHouseId();
            if (availableId == -1) {
                System.out.println("ADD HOUSE: No available house slots (max 4)");
                showError("Maximum 4 houses allowed. Please delete an existing house first.");
                return;
            }

            System.out.println("ADD HOUSE: Found available ID: " + availableId);

            // Create new House object
            House house = new House();
            house.setId(availableId);  // Set specific ID (1-4)
            house.setName(nameField.getText().trim());
            house.setType(typeComboBox.getValue());
            house.setCapacity(Integer.parseInt(capacityField.getText().trim()));
            house.setChickenCount(Integer.parseInt(chickenCountField.getText().trim()));
            house.setHealthStatus(healthStatusComboBox.getValue());
            house.setCreationDate(LocalDate.now());
            house.setLastCleaningDate(LocalDate.now());

            System.out.println("ADD HOUSE: House object created: " + house);
            System.out.println("ADD HOUSE: Calling houseDAO.addHouse()...");

            // Save to database
            boolean success = houseDAO.addHouse(house);

            System.out.println("ADD HOUSE: DAO result = " + success);

            if (success) {
                saved = true;
                System.out.println("ADD HOUSE: House saved successfully with ID: " + house.getId());
                closeDialog();
            } else {
                System.out.println("ADD HOUSE: Failed to save house");
                showError("Failed to add house. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("ADD HOUSE: NumberFormatException: " + e.getMessage());
            showError("Please enter valid numbers for capacity and chicken count.");
        } catch (Exception e) {
            System.out.println("ADD HOUSE: Exception: " + e.getMessage());
            showError("Error adding house: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Find first available house ID from 1 to 4
     * @return available ID (1-4), or -1 if all slots are taken
     */
    private int findAvailableHouseId() {
        for (int id = 1; id <= 4; id++) {
            House existing = houseDAO.getHouseById(id);
            if (existing == null) {
                System.out.println("ADD HOUSE: ID " + id + " is available");
                return id;
            }
            System.out.println("ADD HOUSE: ID " + id + " is taken");
        }
        return -1; // All slots (1-4) are taken
    }

    /**
     * Validate all input fields
     */
    private boolean validateInputs() {
        // Validate name
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            showError("Please enter a house name.");
            nameField.requestFocus();
            return false;
        }

        // Validate type
        if (typeComboBox.getValue() == null) {
            showError("Please select a house type.");
            typeComboBox.requestFocus();
            return false;
        }

        // Validate capacity
        if (capacityField.getText() == null || capacityField.getText().trim().isEmpty()) {
            showError("Please enter the house capacity.");
            capacityField.requestFocus();
            return false;
        }

        try {
            int capacity = Integer.parseInt(capacityField.getText().trim());
            if (capacity <= 0) {
                showError("Capacity must be greater than 0.");
                capacityField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid number for capacity.");
            capacityField.requestFocus();
            return false;
        }

        // Validate chicken count
        if (chickenCountField.getText() == null || chickenCountField.getText().trim().isEmpty()) {
            showError("Please enter the initial chicken count.");
            chickenCountField.requestFocus();
            return false;
        }

        try {
            int chickenCount = Integer.parseInt(chickenCountField.getText().trim());
            int capacity = Integer.parseInt(capacityField.getText().trim());

            if (chickenCount < 0) {
                showError("Chicken count cannot be negative.");
                chickenCountField.requestFocus();
                return false;
            }

            if (chickenCount > capacity) {
                showError("Chicken count cannot exceed capacity.");
                chickenCountField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid number for chicken count.");
            chickenCountField.requestFocus();
            return false;
        }

        // Validate health status
        if (healthStatusComboBox.getValue() == null) {
            showError("Please select a health status.");
            healthStatusComboBox.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Handle cancel button click
     */
    @FXML
    public void handleCancel() {
        closeDialog();
    }

    /**
     * Close the dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    /**
     * Check if house was saved successfully
     */
    public boolean isSaved() {
        return saved;
    }
}
