package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ma.farm.dao.MedicationDAO;
import ma.farm.dao.PersonnelDAO;
import ma.farm.model.Medication;
import ma.farm.model.Personnel;

import java.util.List;

/**
 * UseMedicationDialogController - Handles Use Medication Dialog
 * Asks for quantity AND worker who is using the medication
 */
public class UseMedicationDialogController {

    @FXML
    private Label medicationNameLabel;

    @FXML
    private Label currentStockLabel;

    @FXML
    private TextField quantityField;

    @FXML
    private Label quantityErrorLabel;

    @FXML
    private ComboBox<String> workerComboBox;

    @FXML
    private Label workerErrorLabel;

    private MedicationDAO medicationDAO;
    private PersonnelDAO personnelDAO;
    private Medication currentMedication;
    private Stage dialogStage;
    private boolean usageRecorded = false;

    /**
     * Initialize method called after FXML loads
     */
    @FXML
    public void initialize() {
        medicationDAO = new MedicationDAO();
        personnelDAO = new PersonnelDAO();

        // Load personnel into ComboBox
        loadPersonnel();
    }

    /**
     * Load personnel (workers) into ComboBox
     */
    private void loadPersonnel() {
        try {
            List<Personnel> personnel = personnelDAO.getAllPersonnel();

            for (Personnel person : personnel) {
                workerComboBox.getItems().add(person.getFullName());
            }
        } catch (Exception e) {
            System.err.println("Error loading personnel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Set dialog stage (for closing)
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Set medication to use
     */
    public void setMedication(Medication medication) {
        this.currentMedication = medication;

        // Display medication info
        if (medicationNameLabel != null) {
            medicationNameLabel.setText(medication.getName());
        }

        if (currentStockLabel != null) {
            currentStockLabel.setText(String.format(
                    "Stock actuel: %d %s",
                    medication.getQuantity(),
                    medication.getUnit()
            ));
        }
    }

    /**
     * Check if usage was recorded
     */
    public boolean isUsageRecorded() {
        return usageRecorded;
    }

    /**
     * Handle use button click
     */
    @FXML
    public void handleUse() {
        // Clear previous error messages
        clearErrorLabels();

        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            String worker = workerComboBox.getValue();

            // Check if sufficient stock
            if (quantity > currentMedication.getQuantity()) {
                quantityErrorLabel.setText("Quantité insuffisante en stock");
                return;
            }

            // Update quantity in database
            double newQuantity = currentMedication.getQuantity() - quantity;
            boolean success = medicationDAO.updateQuantity(currentMedication.getId(), newQuantity);

            if (success) {
                // TODO: In the future, record this usage in a medication_usage_log table
                // with worker name, quantity, and date

                showSuccessMessage(String.format(
                        "%d %s de médicament utilisé par %s",
                        quantity,
                        currentMedication.getUnit(),
                        worker
                ));

                usageRecorded = true;

                // Close dialog
                if (dialogStage != null) {
                    dialogStage.close();
                }
            } else {
                showErrorMessage("Erreur lors de la mise à jour du stock");
            }

        } catch (Exception e) {
            System.err.println("Error using medication: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Erreur: " + e.getMessage());
        }
    }

    /**
     * Validate all input fields
     */
    private boolean validateInputs() {
        boolean isValid = true;

        // Validate quantity
        if (quantityField.getText().trim().isEmpty()) {
            quantityErrorLabel.setText("La quantité est requise");
            isValid = false;
        } else {
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
        }

        // Validate worker selection
        if (workerComboBox.getValue() == null || workerComboBox.getValue().isEmpty()) {
            workerErrorLabel.setText("Veuillez sélectionner un travailleur");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Clear all error labels
     */
    private void clearErrorLabels() {
        quantityErrorLabel.setText("");
        workerErrorLabel.setText("");
    }

    /**
     * Show success message
     */
    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText("Médicament Utilisé");
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
        usageRecorded = false;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}