package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ma.farm.dao.HouseDAO;

import java.time.LocalDate;

/**
 * Controller for the Import Chicks Dialog
 * Handles importing day-old chicks into DayOld houses
 */
public class ImportChicksDialogController {

    @FXML
    private Label maxImportLabel;
    @FXML
    private Label availableCapacityLabel;

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
    @FXML
    private Button importButton;

    private HouseDAO houseDAO;
    private boolean saved = false;
    private int maxImportLimit = 0;
    private int availableCapacity = 0;

    @FXML
    public void initialize() {
        houseDAO = new HouseDAO();

        // Get limits from database
        maxImportLimit = houseDAO.getMaxImportLimit();
        availableCapacity = houseDAO.getTotalEmptyCapacityByType(ma.farm.model.HouseType.DAY_OLD);

        // Also include capacity from partially filled houses if needed
        // For now, let's stick to empty capacity or calculate explicitly
        availableCapacity = houseDAO.getTotalDayOldCapacity() -
                houseDAO.getTotalChickenCountByType(ma.farm.model.HouseType.DAY_OLD);

        // Update labels
        maxImportLabel.setText(String.format("%,d", maxImportLimit));
        availableCapacityLabel.setText("Available capacity in DayOld houses: " +
                String.format("%,d", availableCapacity));

        // Validation listeners
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                quantityField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            validateQuantity();
        });

        priceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldVal);
            }
        });
    }

    private boolean validateQuantity() {
        String text = quantityField.getText().trim();

        if (text.isEmpty()) {
            quantityErrorLabel.setText("Please enter a quantity.");
            quantityErrorLabel.setVisible(true);
            importButton.setDisable(true);
            return false;
        }

        try {
            int quantity = Integer.parseInt(text);

            if (quantity <= 0) {
                quantityErrorLabel.setText("Quantity must be greater than 0.");
                quantityErrorLabel.setVisible(true);
                importButton.setDisable(true);
                return false;
            }

            if (quantity > maxImportLimit) {
                quantityErrorLabel
                        .setText("Cannot exceed max import limit (" + String.format("%,d", maxImportLimit) + ").");
                quantityErrorLabel.setVisible(true);
                importButton.setDisable(true);
                return false;
            }

            if (quantity > availableCapacity) {
                quantityErrorLabel
                        .setText("Not enough capacity (Available: " + String.format("%,d", availableCapacity) + ").");
                quantityErrorLabel.setVisible(true);
                importButton.setDisable(true);
                return false;
            }

            quantityErrorLabel.setVisible(false);
            importButton.setDisable(false);
            return true;

        } catch (NumberFormatException e) {
            quantityErrorLabel.setText("Invalid number.");
            quantityErrorLabel.setVisible(true);
            importButton.setDisable(true);
            return false;
        }
    }

    @FXML
    public void handleConfirm() {
        if (!validateQuantity()) {
            return;
        }

        int quantity = Integer.parseInt(quantityField.getText().trim());

        // Auto-distribute across DayOld houses
        boolean success = houseDAO.distributeChicksAcrossDayOldHouses(
                quantity,
                LocalDate.now());

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
        Stage stage = (Stage) quantityField.getScene().getWindow();
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
