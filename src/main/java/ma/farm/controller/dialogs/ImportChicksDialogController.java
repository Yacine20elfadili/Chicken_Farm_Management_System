package ma.farm.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ma.farm.dao.HouseDAO;
import ma.farm.model.House;
import ma.farm.model.HouseType;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for the Import Chicks Dialog
 * Handles importing day-old chicks into DayOld houses
 */
public class ImportChicksDialogController {

    @FXML
    private ComboBox<House> houseComboBox;

    @FXML
    private Label capacityInfoLabel;

    @FXML
    private TextField quantityField;

    @FXML
    private Label quantityErrorLabel;

    @FXML
    private TextField supplierField;

    @FXML
    private TextField priceField;

    @FXML
    private Label errorLabel;

    private HouseDAO houseDAO;
    private boolean saved = false;

    @FXML
    public void initialize() {
        houseDAO = new HouseDAO();

        // Load empty DayOld houses
        loadEmptyHouses();

        // Set up house selection listener
        houseComboBox.setOnAction(e -> updateCapacityInfo());

        // Set up quantity field validation
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                quantityField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            validateQuantity();
        });

        // Set up price field validation (allow decimals)
        priceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldVal);
            }
        });
    }

    private void loadEmptyHouses() {
        List<House> emptyHouses = houseDAO.getEmptyHousesByType(HouseType.DAY_OLD);

        // Also include houses with available capacity (not just empty)
        List<House> allDayOldHouses = houseDAO.getHousesByType(HouseType.DAY_OLD);
        for (House house : allDayOldHouses) {
            if (!house.isFull() && !emptyHouses.contains(house)) {
                emptyHouses.add(house);
            }
        }

        houseComboBox.setItems(FXCollections.observableArrayList(emptyHouses));

        // Custom cell factory to display house names
        houseComboBox.setCellFactory(lv -> new ListCell<House>() {
            @Override
            protected void updateItem(House house, boolean empty) {
                super.updateItem(house, empty);
                if (empty || house == null) {
                    setText(null);
                } else {
                    setText(house.getName() + " (Capacity: " + house.getCapacity() +
                           ", Current: " + house.getChickenCount() + ")");
                }
            }
        });

        houseComboBox.setButtonCell(new ListCell<House>() {
            @Override
            protected void updateItem(House house, boolean empty) {
                super.updateItem(house, empty);
                if (empty || house == null) {
                    setText(null);
                } else {
                    setText(house.getName());
                }
            }
        });

        if (emptyHouses.isEmpty()) {
            capacityInfoLabel.setText("No available DayOld houses!");
            capacityInfoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #dc3545;");
        }
    }

    private void updateCapacityInfo() {
        House selected = houseComboBox.getValue();
        if (selected != null) {
            int available = selected.getAvailableCapacity();
            capacityInfoLabel.setText("Available capacity: " + available + " chicks");
            capacityInfoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #28a745;");
        } else {
            capacityInfoLabel.setText("Available capacity: -");
            capacityInfoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        }
    }

    private boolean validateQuantity() {
        House selected = houseComboBox.getValue();
        String quantityText = quantityField.getText().trim();

        if (quantityText.isEmpty()) {
            quantityErrorLabel.setVisible(false);
            return false;
        }

        try {
            int quantity = Integer.parseInt(quantityText);

            if (quantity <= 0) {
                showQuantityError("Quantity must be greater than 0");
                return false;
            }

            if (selected != null) {
                int available = selected.getAvailableCapacity();
                if (quantity > available) {
                    showQuantityError("Quantity exceeds available capacity (" + available + ")");
                    return false;
                }
            }

            quantityErrorLabel.setVisible(false);
            return true;

        } catch (NumberFormatException e) {
            showQuantityError("Please enter a valid number");
            return false;
        }
    }

    private void showQuantityError(String message) {
        quantityErrorLabel.setText(message);
        quantityErrorLabel.setVisible(true);
    }

    @FXML
    public void handleConfirm() {
        // Validate house selection
        House selectedHouse = houseComboBox.getValue();
        if (selectedHouse == null) {
            showError("Please select a house.");
            return;
        }

        // Validate quantity
        String quantityText = quantityField.getText().trim();
        if (quantityText.isEmpty()) {
            showError("Please enter the quantity of chicks to import.");
            return;
        }

        if (!validateQuantity()) {
            return;
        }

        int quantity = Integer.parseInt(quantityText);

        // Perform the import
        boolean success = houseDAO.addChickensToHouse(
            selectedHouse.getId(),
            quantity,
            LocalDate.now()
        );

        if (success) {
            saved = true;
            closeDialog();
        } else {
            showError("Failed to import chicks. Please try again.");
        }
    }

    @FXML
    public void handleCancel() {
        saved = false;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) houseComboBox.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
