package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ma.farm.dao.MedicationDAO;
import ma.farm.model.Medication;

import java.time.LocalDate;

/**
 * AddEditMedicationDialogController - Handles Add/Edit Medication Dialog
 * Used to add new medications or edit existing medication inventory
 */
public class AddEditMedicationDialogController {

    @FXML
    private Label dialogTitle;

    @FXML
    private TextField nameField;

    @FXML
    private Label nameErrorLabel;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private Label typeErrorLabel;

    @FXML
    private TextField quantityField;

    @FXML
    private Label quantityErrorLabel;

    @FXML
    private ComboBox<String> unitComboBox;

    @FXML
    private Label unitErrorLabel;

    @FXML
    private TextField pricePerUnitField;

    @FXML
    private Label priceErrorLabel;

    @FXML
    private TextField supplierField;

    @FXML
    private DatePicker purchaseDatePicker;

    @FXML
    private DatePicker expiryDatePicker;

    @FXML
    private TextField minStockField;

    @FXML
    private Label minStockErrorLabel;

    @FXML
    private TextArea usageTextArea;

    private MedicationDAO medicationDAO;
    private Medication currentMedication;
    private boolean isEditMode = false;
    private Stage dialogStage;

    /**
     * Initialize method called after FXML loads
     */
    @FXML
    public void initialize() {
        medicationDAO = new MedicationDAO();

        // Populate type ComboBox
        typeComboBox.getItems().addAll(
                "Vaccine",
                "Antibiotic",
                "Supplement"
        );

        // Populate unit ComboBox
        unitComboBox.getItems().addAll(
                "ml",
                "tablets",
                "doses",
                "bottles",
                "units"
        );
    }

    /**
     * Set dialog stage (for closing)
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Set medication for editing
     */
    public void setMedication(Medication medication) {
        this.currentMedication = medication;
        this.isEditMode = true;

        // Update dialog title
        dialogTitle.setText("Modifier Médicament");

        // Populate form with existing medication data
        nameField.setText(medication.getName());
        typeComboBox.setValue(medication.getType());
        quantityField.setText(String.valueOf(medication.getQuantity()));
        unitComboBox.setValue(medication.getUnit());
        pricePerUnitField.setText(String.valueOf(medication.getPricePerUnit()));
        supplierField.setText(medication.getSupplier() != null ? medication.getSupplier() : "");
        if (medication.getPurchaseDate() != null) {
            purchaseDatePicker.setValue(medication.getPurchaseDate());
        }
        if (medication.getExpiryDate() != null) {
            expiryDatePicker.setValue(medication.getExpiryDate());
        }
        minStockField.setText(String.valueOf(medication.getMinStockLevel()));
        usageTextArea.setText(medication.getUsage() != null ? medication.getUsage() : "");
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
            // Create or update medication object
            if (!isEditMode) {
                currentMedication = new Medication();
            }

            // Set values from form
            currentMedication.setName(nameField.getText().trim());
            currentMedication.setType(typeComboBox.getValue());
            currentMedication.setQuantity(Integer.parseInt(quantityField.getText().trim()));
            currentMedication.setUnit(unitComboBox.getValue());
            currentMedication.setPricePerUnit(Double.parseDouble(pricePerUnitField.getText().trim()));
            currentMedication.setSupplier(supplierField.getText().trim().isEmpty() ? null : supplierField.getText().trim());
            currentMedication.setPurchaseDate(purchaseDatePicker.getValue());
            currentMedication.setExpiryDate(expiryDatePicker.getValue());
            currentMedication.setMinStockLevel(Integer.parseInt(minStockField.getText().trim()));
            currentMedication.setUsage(usageTextArea.getText().trim().isEmpty() ? null : usageTextArea.getText().trim());

            // Save to database
            boolean success;
            if (isEditMode) {
                success = medicationDAO.updateMedication(currentMedication);
                if (success) {
                    showSuccessMessage("Médicament mis à jour avec succès!");
                } else {
                    showErrorMessage("Erreur lors de la mise à jour du médicament.");
                    return;
                }
            } else {
                success = medicationDAO.addMedication(currentMedication);
                if (success) {
                    showSuccessMessage("Médicament ajouté avec succès!");
                } else {
                    showErrorMessage("Erreur lors de l'ajout du médicament.");
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
            System.err.println("Error saving medication: " + e.getMessage());
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

        // Validate type
        if (typeComboBox.getValue() == null || typeComboBox.getValue().isEmpty()) {
            typeErrorLabel.setText("Le type est requis");
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

        // Validate unit
        if (unitComboBox.getValue() == null || unitComboBox.getValue().isEmpty()) {
            unitErrorLabel.setText("L'unité est requise");
            isValid = false;
        }

        // Validate price per unit
        try {
            double price = Double.parseDouble(pricePerUnitField.getText().trim());
            if (price < 0) {
                priceErrorLabel.setText("Le prix ne peut pas être négatif");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            priceErrorLabel.setText("Veuillez entrer un nombre valide");
            isValid = false;
        }

        // Validate min stock level
        try {
            int minStock = Integer.parseInt(minStockField.getText().trim());
            if (minStock < 0) {
                minStockErrorLabel.setText("Le stock minimum ne peut pas être négatif");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            minStockErrorLabel.setText("Veuillez entrer un nombre entier valide");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Clear all error labels
     */
    private void clearErrorLabels() {
        nameErrorLabel.setText("");
        typeErrorLabel.setText("");
        quantityErrorLabel.setText("");
        unitErrorLabel.setText("");
        priceErrorLabel.setText("");
        minStockErrorLabel.setText("");
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
