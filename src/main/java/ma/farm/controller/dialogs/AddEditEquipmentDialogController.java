package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ma.farm.dao.EquipmentDAO;
import ma.farm.model.Equipment;

import java.time.LocalDate;

/**
 * AddEditEquipmentDialogController - Handles Add/Edit Equipment Dialog
 * Used to add new equipment or edit existing equipment records
 */
public class AddEditEquipmentDialogController {

    @FXML
    private Label dialogTitle;

    @FXML
    private TextField nameField;

    @FXML
    private Label nameErrorLabel;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private Label categoryErrorLabel;

    @FXML
    private TextField quantityField;

    @FXML
    private Label quantityErrorLabel;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Label statusErrorLabel;

    @FXML
    private DatePicker purchaseDatePicker;

    @FXML
    private TextField purchasePriceField;

    @FXML
    private Label priceErrorLabel;

    @FXML
    private DatePicker lastMaintenanceDatePicker;

    @FXML
    private DatePicker nextMaintenanceDatePicker;

    @FXML
    private TextField locationField;

    @FXML
    private TextArea notesTextArea;

    private EquipmentDAO equipmentDAO;
    private Equipment currentEquipment;
    private boolean isEditMode = false;
    private Stage dialogStage;

    /**
     * Initialize method called after FXML loads
     */
    @FXML
    public void initialize() {
        equipmentDAO = new EquipmentDAO();

        // Populate category ComboBox
        categoryComboBox.getItems().addAll(
                "Feeding",
                "Cleaning",
                "Medical",
                "Other"
        );

        // Populate status ComboBox
        statusComboBox.getItems().addAll(
                "Good",
                "Fair",
                "Broken"
        );
    }

    /**
     * Set dialog stage (for closing)
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Set equipment for editing
     */
    public void setEquipment(Equipment equipment) {
        this.currentEquipment = equipment;
        this.isEditMode = true;

        // Update dialog title
        dialogTitle.setText("Modifier Équipement");

        // Populate form with existing equipment data
        nameField.setText(equipment.getName());
        categoryComboBox.setValue(equipment.getCategory());
        quantityField.setText(String.valueOf(equipment.getQuantity()));
        statusComboBox.setValue(equipment.getStatus());
        if (equipment.getPurchaseDate() != null) {
            purchaseDatePicker.setValue(equipment.getPurchaseDate());
        }
        purchasePriceField.setText(String.valueOf(equipment.getPurchasePrice()));
        if (equipment.getLastMaintenanceDate() != null) {
            lastMaintenanceDatePicker.setValue(equipment.getLastMaintenanceDate());
        }
        if (equipment.getNextMaintenanceDate() != null) {
            nextMaintenanceDatePicker.setValue(equipment.getNextMaintenanceDate());
        }
        locationField.setText(equipment.getLocation() != null ? equipment.getLocation() : "");
        notesTextArea.setText(equipment.getNotes() != null ? equipment.getNotes() : "");
    }

    /**
     * Handle save button click
     */
    @FXML
    public void handleSave() {
        // Clear previous error messages
        clearErrorLabels();

        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            // Create or update equipment object
            if (!isEditMode) {
                currentEquipment = new Equipment();
            }

            // Set values from form
            currentEquipment.setName(nameField.getText().trim());
            currentEquipment.setCategory(categoryComboBox.getValue());
            currentEquipment.setQuantity(Integer.parseInt(quantityField.getText().trim()));
            currentEquipment.setStatus(statusComboBox.getValue());
            currentEquipment.setPurchaseDate(purchaseDatePicker.getValue());

            try {
                double price = Double.parseDouble(purchasePriceField.getText().trim());
                currentEquipment.setPurchasePrice(price);
            } catch (NumberFormatException e) {
                currentEquipment.setPurchasePrice(0.0);
            }

            currentEquipment.setLastMaintenanceDate(lastMaintenanceDatePicker.getValue());
            currentEquipment.setNextMaintenanceDate(nextMaintenanceDatePicker.getValue());
            currentEquipment.setLocation(locationField.getText().trim().isEmpty() ? null : locationField.getText().trim());
            currentEquipment.setNotes(notesTextArea.getText().trim().isEmpty() ? null : notesTextArea.getText().trim());

            // Save to database
            boolean success;
            if (isEditMode) {
                success = equipmentDAO.updateEquipment(currentEquipment);
                if (success) {
                    showSuccessMessage("Équipement mis à jour avec succès!");
                } else {
                    showErrorMessage("Erreur lors de la mise à jour de l'équipement.");
                    return;
                }
            } else {
                success = equipmentDAO.addEquipment(currentEquipment);
                if (success) {
                    showSuccessMessage("Équipement ajouté avec succès!");
                } else {
                    showErrorMessage("Erreur lors de l'ajout de l'équipement.");
                    return;
                }
            }

            // Close dialog
            if (dialogStage != null) {
                dialogStage.close();
            }

        } catch (NumberFormatException e) {
            showErrorMessage("Erreur: Vérifiez les valeurs numériques.");
        } catch (Exception e) {
            System.err.println("Error saving equipment: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Erreur: " + e.getMessage());
        }
    }

    /**
     * Validate all input fields
     */
    private boolean validateInputs() {
        boolean isValid = true;

        // Validate name
        if (nameField.getText().trim().isEmpty()) {
            nameErrorLabel.setText("Le nom est requis");
            isValid = false;
        }

        // Validate category
        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().isEmpty()) {
            categoryErrorLabel.setText("La catégorie est requise");
            isValid = false;
        }

        // Validate quantity
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                quantityErrorLabel.setText("La quantité doit être supérieure à 0");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            quantityErrorLabel.setText("Veuillez entrer un nombre entier valide");
            isValid = false;
        }

        // Validate status
        if (statusComboBox.getValue() == null || statusComboBox.getValue().isEmpty()) {
            statusErrorLabel.setText("L'état est requis");
            isValid = false;
        }

        // Validate purchase price (optional but must be valid if entered)
        if (!purchasePriceField.getText().trim().isEmpty()) {
            try {
                double price = Double.parseDouble(purchasePriceField.getText().trim());
                if (price < 0) {
                    priceErrorLabel.setText("Le prix ne peut pas être négatif");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                priceErrorLabel.setText("Veuillez entrer un nombre valide");
                isValid = false;
            }
        }

        return isValid;
    }

    /**
     * Clear all error labels
     */
    private void clearErrorLabels() {
        nameErrorLabel.setText("");
        categoryErrorLabel.setText("");
        quantityErrorLabel.setText("");
        statusErrorLabel.setText("");
        priceErrorLabel.setText("");
    }

    /**
     * Show success message
     */
    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText("Opération Réussie");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error message
     */
    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Une erreur s'est produite");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handle cancel button click
     */
    @FXML
    public void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
