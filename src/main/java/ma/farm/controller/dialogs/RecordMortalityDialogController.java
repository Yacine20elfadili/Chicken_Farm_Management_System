package ma.farm.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ma.farm.dao.HouseDAO;
import ma.farm.dao.MortalityDAO;
import ma.farm.model.House;
import ma.farm.model.Mortality;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for Record Mortality Dialog
 * Handles recording chicken deaths and updating house counts
 */
public class RecordMortalityDialogController {

    @FXML
    private ComboBox<House> houseComboBox;

    @FXML
    private DatePicker deathDatePicker;

    @FXML
    private TextField deathCountField;

    @FXML
    private ComboBox<String> causeComboBox;

    @FXML
    private TextArea symptomsArea;

    @FXML
    private CheckBox isOutbreakCheckBox;

    @FXML
    private TextField recordedByField;

    @FXML
    private TextArea notesArea;

    @FXML
    private Label errorLabel;

    private HouseDAO houseDAO;
    private MortalityDAO mortalityDAO;
    private boolean saved = false;

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        // Initialize DAOs
        houseDAO = new HouseDAO();
        mortalityDAO = new MortalityDAO();

        // Set today's date as default
        deathDatePicker.setValue(LocalDate.now());

        // Load houses into combo box
        loadHouses();

        // Populate common causes of death
        ObservableList<String> causes = FXCollections.observableArrayList(
                "Disease",
                "Old Age",
                "Malnutrition",
                "Heat Stress",
                "Cold Stress",
                "Predator Attack",
                "Injury",
                "Unknown",
                "Other"
        );
        causeComboBox.setItems(causes);

        // Add numeric validation to death count field
        addNumericValidation(deathCountField);

        // Configure house combo box display
        houseComboBox.setButtonCell(new ListCell<House>() {
            @Override
            protected void updateItem(House house, boolean empty) {
                super.updateItem(house, empty);
                if (empty || house == null) {
                    setText(null);
                } else {
                    setText(house.getName() + " (" + house.getType().getDisplayName() + ")");
                }
            }
        });

        houseComboBox.setCellFactory(param -> new ListCell<House>() {
            @Override
            protected void updateItem(House house, boolean empty) {
                super.updateItem(house, empty);
                if (empty || house == null) {
                    setText(null);
                } else {
                    setText(house.getName() + " (" + house.getType().getDisplayName() + ") - "
                            + house.getChickenCount() + " chickens");
                }
            }
        });
    }

    /**
     * Load all houses into combo box
     */
    private void loadHouses() {
        try {
            List<House> houses = houseDAO.getAllHouses();
            ObservableList<House> houseList = FXCollections.observableArrayList(houses);
            houseComboBox.setItems(houseList);
        } catch (Exception e) {
            System.err.println("Error loading houses: " + e.getMessage());
            e.printStackTrace();
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
            House selectedHouse = houseComboBox.getValue();
            int deathCount = Integer.parseInt(deathCountField.getText().trim());

            // Check if death count exceeds current chicken count
            if (deathCount > selectedHouse.getChickenCount()) {
                showError("Death count (" + deathCount + ") cannot exceed current chicken count ("
                        + selectedHouse.getChickenCount() + ").");
                return;
            }

            // Create new Mortality object
            Mortality mortality = new Mortality();
            mortality.setHouseId(selectedHouse.getId());
            mortality.setDeathDate(deathDatePicker.getValue());
            mortality.setCount(deathCount);
            mortality.setCause(causeComboBox.getValue());
            mortality.setSymptoms(symptomsArea.getText() != null ? symptomsArea.getText().trim() : "");
            mortality.setIsOutbreak(isOutbreakCheckBox.isSelected());
            mortality.setRecordedBy(recordedByField.getText() != null ? recordedByField.getText().trim() : "");
            mortality.setNotes(notesArea.getText() != null ? notesArea.getText().trim() : "");

            // Save mortality record to database
            int mortalityId = mortalityDAO.recordMortality(mortality);

            if (mortalityId > 0) {
                // Update house chicken count (decrease by death count)
                int newChickenCount = selectedHouse.getChickenCount() - deathCount;
                boolean countUpdated = houseDAO.updateChickenCount(selectedHouse.getId(), newChickenCount);

                if (countUpdated) {
                    saved = true;
                    closeDialog();
                } else {
                    showError("Mortality recorded but failed to update house chicken count.");
                }
            } else {
                showError("Failed to record mortality. Please try again.");
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid number for death count.");
        } catch (Exception e) {
            showError("Error recording mortality: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validate all input fields
     */
    private boolean validateInputs() {
        // Validate house selection
        if (houseComboBox.getValue() == null) {
            showError("Please select a house.");
            houseComboBox.requestFocus();
            return false;
        }

        // Validate death date
        if (deathDatePicker.getValue() == null) {
            showError("Please select a death date.");
            deathDatePicker.requestFocus();
            return false;
        }

        // Check if date is not in the future
        if (deathDatePicker.getValue().isAfter(LocalDate.now())) {
            showError("Death date cannot be in the future.");
            deathDatePicker.requestFocus();
            return false;
        }

        // Validate death count
        if (deathCountField.getText() == null || deathCountField.getText().trim().isEmpty()) {
            showError("Please enter the number of deaths.");
            deathCountField.requestFocus();
            return false;
        }

        try {
            int deathCount = Integer.parseInt(deathCountField.getText().trim());
            if (deathCount <= 0) {
                showError("Death count must be greater than 0.");
                deathCountField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid number for death count.");
            deathCountField.requestFocus();
            return false;
        }

        // Validate cause
        if (causeComboBox.getValue() == null || causeComboBox.getValue().trim().isEmpty()) {
            showError("Please select or enter a cause of death.");
            causeComboBox.requestFocus();
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
        Stage stage = (Stage) houseComboBox.getScene().getWindow();
        stage.close();
    }

    /**
     * Check if mortality was saved successfully
     */
    public boolean isSaved() {
        return saved;
    }
}
