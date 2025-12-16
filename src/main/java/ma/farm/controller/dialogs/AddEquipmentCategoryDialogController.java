package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ma.farm.dao.EquipmentCategoryDAO;
import ma.farm.model.EquipmentCategory;

/**
 * AddEquipmentCategoryDialogController - Handles Add Equipment Category Dialog
 * Simple dialog that only asks for: name, category type, location, notes
 * Does NOT ask for quantity (quantity starts at 0)
 */
public class AddEquipmentCategoryDialogController {

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
    private TextField locationField;

    @FXML
    private Label locationErrorLabel;

    @FXML
    private TextArea notesTextArea;

    private EquipmentCategoryDAO categoryDAO;
    private Stage dialogStage;
    private boolean saveClicked = false;

    /**
     * Initialize method called after FXML loads
     */
    @FXML
    public void initialize() {
        categoryDAO = new EquipmentCategoryDAO();

        // Populate category ComboBox
        categoryComboBox.getItems().addAll(
                "Feeding",
                "Cleaning",
                "Medical",
                "Other"
        );
    }

    /**
     * Set dialog stage (for closing)
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Check if save was clicked
     */
    public boolean isSaveClicked() {
        return saveClicked;
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
            // Create new category object
            EquipmentCategory category = new EquipmentCategory();

            // Set values from form
            category.setName(nameField.getText().trim());
            category.setCategory(categoryComboBox.getValue());
            category.setLocation(locationField.getText().trim());
            category.setNotes(notesTextArea.getText().trim().isEmpty() ? null : notesTextArea.getText().trim());

            // Check if category name already exists
            if (categoryDAO.categoryNameExists(category.getName())) {
                nameErrorLabel.setText("Ce nom de catégorie existe déjà");
                return;
            }

            // Save to database
            boolean success = categoryDAO.addCategory(category);

            if (success) {
                showSuccessMessage("Catégorie d'équipement ajoutée avec succès!");
                saveClicked = true;

                // Close dialog
                if (dialogStage != null) {
                    dialogStage.close();
                }
            } else {
                showErrorMessage("Erreur lors de l'ajout de la catégorie.");
            }

        } catch (Exception e) {
            System.err.println("Error saving equipment category: " + e.getMessage());
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

        // Validate category type
        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().isEmpty()) {
            categoryErrorLabel.setText("Le type est requis");
            isValid = false;
        }

        // Validate location
        if (locationField.getText().trim().isEmpty()) {
            locationErrorLabel.setText("La localisation est requise");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Clear all error labels
     */
    private void clearErrorLabels() {
        nameErrorLabel.setText("");
        categoryErrorLabel.setText("");
        locationErrorLabel.setText("");
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
        saveClicked = false;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}