package ma.farm.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ma.farm.dao.HouseDAO;
import ma.farm.model.HealthStatus;
import ma.farm.model.House;
import ma.farm.model.HouseType;

import java.time.LocalDate;

/**
 * Controller for Edit House Dialog
 * Handles editing an existing house in the system
 */
public class EditHouseDialogController {

    @FXML
    private Label houseIdLabel;

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
    private DatePicker lastCleaningDatePicker;

    @FXML
    private Label errorLabel;

    private HouseDAO houseDAO;
    private House house;
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

        // Add numeric validation to capacity field
        addNumericValidation(capacityField);
    }

    /**
     * Set the house to edit
     * @param house The house object to edit
     */
    public void setHouse(House house) {
        this.house = house;
        loadHouseData();
    }

    /**
     * Load house data into form fields
     */
    private void loadHouseData() {
        if (house == null) {
            return;
        }

        // Store house ID (hidden)
        houseIdLabel.setText(String.valueOf(house.getId()));

        // Load data into fields
        nameField.setText(house.getName());
        typeComboBox.setValue(house.getType());
        capacityField.setText(String.valueOf(house.getCapacity()));
        chickenCountField.setText(String.valueOf(house.getChickenCount()));
        healthStatusComboBox.setValue(house.getHealthStatus());

        // Set last cleaning date
        if (house.getLastCleaningDate() != null) {
            lastCleaningDatePicker.setValue(house.getLastCleaningDate());
        } else {
            lastCleaningDatePicker.setValue(LocalDate.now());
        }
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
        // Hide previous error
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            // Update house object
            house.setName(nameField.getText().trim());
            // Note: Type is disabled and cannot be changed
            house.setCapacity(Integer.parseInt(capacityField.getText().trim()));
            // Note: Chicken count is read-only and managed by chicken additions/deaths
            house.setHealthStatus(healthStatusComboBox.getValue());
            house.setLastCleaningDate(lastCleaningDatePicker.getValue());

            // Save to database
            boolean success = houseDAO.updateHouse(house);

            if (success) {
                saved = true;
                closeDialog();
            } else {
                showError("Failed to update house. Please try again.");
            }
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for capacity.");
        } catch (Exception e) {
            showError("Error updating house: " + e.getMessage());
            e.printStackTrace();
        }
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

            // Check if capacity is less than current chicken count
            int currentCount = Integer.parseInt(chickenCountField.getText());
            if (capacity < currentCount) {
                showError("Capacity cannot be less than current chicken count (" + currentCount + ").");
                capacityField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid number for capacity.");
            capacityField.requestFocus();
            return false;
        }

        // Validate health status
        if (healthStatusComboBox.getValue() == null) {
            showError("Please select a health status.");
            healthStatusComboBox.requestFocus();
            return false;
        }

        // Validate last cleaning date
        if (lastCleaningDatePicker.getValue() == null) {
            showError("Please select a last cleaning date.");
            lastCleaningDatePicker.requestFocus();
            return false;
        }

        // Check if date is not in the future
        if (lastCleaningDatePicker.getValue().isAfter(LocalDate.now())) {
            showError("Last cleaning date cannot be in the future.");
            lastCleaningDatePicker.requestFocus();
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

    /**
     * Get the updated house
     */
    public House getHouse() {
        return house;
    }
}
