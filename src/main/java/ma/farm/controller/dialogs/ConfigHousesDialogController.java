package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ma.farm.dao.HouseDAO;
import ma.farm.model.House;
import ma.farm.model.HouseType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Config Houses Dialog
 * Allows configuring the number of houses per type and their capacities
 */
public class ConfigHousesDialogController {

    @FXML
    private HBox warningBox;

    @FXML
    private Label warningLabel;

    @FXML
    private Spinner<Integer> dayOldSpinner;

    @FXML
    private Spinner<Integer> eggLayerSpinner;

    @FXML
    private Spinner<Integer> femaleMeatSpinner;

    @FXML
    private Spinner<Integer> maleMeatSpinner;

    @FXML
    private VBox dayOldCapacityContainer;

    @FXML
    private VBox eggLayerCapacityContainer;

    @FXML
    private VBox femaleMeatCapacityContainer;

    @FXML
    private VBox maleMeatCapacityContainer;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private HouseDAO houseDAO;
    private boolean saved = false;

    // Maps to hold capacity text fields for each house type
    private Map<HouseType, List<TextField>> capacityFields = new HashMap<>();

    // Constants
    private static final int MIN_HOUSES = 1;
    private static final int MAX_HOUSES = 5;
    private static final int DEFAULT_CAPACITY = 500;

    @FXML
    public void initialize() {
        houseDAO = new HouseDAO();

        // Initialize capacity field maps
        capacityFields.put(HouseType.DAY_OLD, new ArrayList<>());
        capacityFields.put(HouseType.EGG_LAYER, new ArrayList<>());
        capacityFields.put(HouseType.MEAT_FEMALE, new ArrayList<>());
        capacityFields.put(HouseType.MEAT_MALE, new ArrayList<>());

        // Check if any house has chickens
        boolean hasChickens = houseDAO.hasAnyChickens();
        if (hasChickens) {
            warningBox.setVisible(true);
            warningBox.setManaged(true);
            saveButton.setDisable(true);
        }

        // Initialize spinners
        initializeSpinner(dayOldSpinner, HouseType.DAY_OLD, dayOldCapacityContainer);
        initializeSpinner(eggLayerSpinner, HouseType.EGG_LAYER, eggLayerCapacityContainer);
        initializeSpinner(femaleMeatSpinner, HouseType.MEAT_FEMALE, femaleMeatCapacityContainer);
        initializeSpinner(maleMeatSpinner, HouseType.MEAT_MALE, maleMeatCapacityContainer);

        // Load existing configuration
        loadExistingConfiguration();
    }

    private void initializeSpinner(Spinner<Integer> spinner, HouseType type, VBox container) {
        SpinnerValueFactory<Integer> valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_HOUSES, MAX_HOUSES, MIN_HOUSES);
        spinner.setValueFactory(valueFactory);
        spinner.setEditable(false);

        // Listen for value changes
        spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateCapacityFields(type, container, newVal);
        });

        // Initialize with 1 house
        updateCapacityFields(type, container, MIN_HOUSES);
    }

    private void loadExistingConfiguration() {
        // Load existing house counts and capacities
        for (HouseType type : HouseType.values()) {
            List<House> houses = houseDAO.getHousesByType(type);
            if (!houses.isEmpty()) {
                Spinner<Integer> spinner = getSpinnerForType(type);
                VBox container = getContainerForType(type);

                if (spinner != null && container != null) {
                    spinner.getValueFactory().setValue(houses.size());
                    updateCapacityFields(type, container, houses.size());

                    // Set existing capacities
                    List<TextField> fields = capacityFields.get(type);
                    for (int i = 0; i < houses.size() && i < fields.size(); i++) {
                        fields.get(i).setText(String.valueOf(houses.get(i).getCapacity()));
                    }
                }
            }
        }
    }

    private Spinner<Integer> getSpinnerForType(HouseType type) {
        switch (type) {
            case DAY_OLD:
                return dayOldSpinner;
            case EGG_LAYER:
                return eggLayerSpinner;
            case MEAT_FEMALE:
                return femaleMeatSpinner;
            case MEAT_MALE:
                return maleMeatSpinner;
            default:
                return null;
        }
    }

    private VBox getContainerForType(HouseType type) {
        switch (type) {
            case DAY_OLD:
                return dayOldCapacityContainer;
            case EGG_LAYER:
                return eggLayerCapacityContainer;
            case MEAT_FEMALE:
                return femaleMeatCapacityContainer;
            case MEAT_MALE:
                return maleMeatCapacityContainer;
            default:
                return null;
        }
    }

    private void updateCapacityFields(HouseType type, VBox container, int count) {
        container.getChildren().clear();
        List<TextField> fields = capacityFields.get(type);

        // Store old values to preserve them
        List<String> oldValues = new ArrayList<>();
        for (TextField field : fields) {
            oldValues.add(field.getText());
        }
        fields.clear();

        for (int i = 1; i <= count; i++) {
            HBox row = new HBox(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label label = new Label(type.generateHouseName(i) + " capacity:");
            label.setPrefWidth(180);
            label.setStyle("-fx-font-size: 12px;");

            TextField capacityField = new TextField();
            capacityField.setPrefWidth(100);
            capacityField.setPromptText("e.g., 500");

            // Restore old value if exists, otherwise use default
            if (i - 1 < oldValues.size() && !oldValues.get(i - 1).isEmpty()) {
                capacityField.setText(oldValues.get(i - 1));
            } else {
                capacityField.setText(String.valueOf(DEFAULT_CAPACITY));
            }

            // Add numeric validation
            capacityField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*")) {
                    capacityField.setText(newVal.replaceAll("[^\\d]", ""));
                }
            });

            fields.add(capacityField);
            row.getChildren().addAll(label, capacityField);
            container.getChildren().add(row);
        }
    }

    @FXML
    public void handleSave() {
        // Validate all capacity fields
        if (!validateCapacities()) {
            return;
        }

        // Delete all existing houses
        if (!houseDAO.deleteAllHouses()) {
            showError("Failed to clear existing houses. Please try again.");
            return;
        }

        // Create new houses for each type
        boolean success = true;
        for (HouseType type : HouseType.values()) {
            List<TextField> fields = capacityFields.get(type);
            for (int i = 0; i < fields.size(); i++) {
                int capacity = Integer.parseInt(fields.get(i).getText());
                House house = new House(type, i + 1, capacity);

                if (!houseDAO.addHouse(house)) {
                    success = false;
                    break;
                }
            }
            if (!success) break;
        }

        if (success) {
            saved = true;
            closeDialog();
        } else {
            showError("Failed to save house configuration. Please try again.");
        }
    }

    private boolean validateCapacities() {
        for (HouseType type : HouseType.values()) {
            List<TextField> fields = capacityFields.get(type);
            for (int i = 0; i < fields.size(); i++) {
                String text = fields.get(i).getText().trim();
                if (text.isEmpty()) {
                    showError("Please enter capacity for " + type.generateHouseName(i + 1));
                    fields.get(i).requestFocus();
                    return false;
                }
                try {
                    int capacity = Integer.parseInt(text);
                    if (capacity <= 0) {
                        showError("Capacity must be greater than 0 for " + type.generateHouseName(i + 1));
                        fields.get(i).requestFocus();
                        return false;
                    }
                    if (capacity > 10000) {
                        showError("Capacity cannot exceed 10,000 for " + type.generateHouseName(i + 1));
                        fields.get(i).requestFocus();
                        return false;
                    }
                } catch (NumberFormatException e) {
                    showError("Invalid capacity value for " + type.generateHouseName(i + 1));
                    fields.get(i).requestFocus();
                    return false;
                }
            }
        }
        return true;
    }

    @FXML
    public void handleCancel() {
        saved = false;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isSaved() {
        return saved;
    }
}
