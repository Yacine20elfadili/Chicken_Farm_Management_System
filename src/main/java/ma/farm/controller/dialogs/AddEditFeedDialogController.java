package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ma.farm.dao.FeedDAO;
import ma.farm.model.Feed;

import java.time.LocalDate;

/**
 * AddEditFeedDialogController - Handles Add/Edit Feed Dialog
 * Used to add new feed items or edit existing feed inventory
 */
public class AddEditFeedDialogController {

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
    private TextField pricePerKgField;

    @FXML
    private Label priceErrorLabel;

    @FXML
    private TextField supplierField;

    @FXML
    private DatePicker expiryDatePicker;

    @FXML
    private TextField minStockField;

    @FXML
    private Label minStockErrorLabel;

    private FeedDAO feedDAO;
    private Feed currentFeed;
    private boolean isEditMode = false;
    private Stage dialogStage;

    /**
     * Initialize method called after FXML loads
     */
    @FXML
    public void initialize() {
        feedDAO = new FeedDAO();

        // Populate type ComboBox with feed types
        typeComboBox.getItems().addAll(
                "Day-old",
                "Layer",
                "Meat Growth"
        );
    }

    /**
     * Set dialog stage (for closing)
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Set feed for editing
     */
    public void setFeed(Feed feed) {
        this.currentFeed = feed;
        this.isEditMode = true;

        // Update dialog title
        dialogTitle.setText("Modifier Aliment");

        // Populate form with existing feed data
        nameField.setText(feed.getName());
        typeComboBox.setValue(feed.getType());
        quantityField.setText(String.valueOf(feed.getQuantityKg()));
        pricePerKgField.setText(String.valueOf(feed.getPricePerKg()));
        supplierField.setText(feed.getSupplier() != null ? feed.getSupplier() : "");
        if (feed.getExpiryDate() != null) {
            expiryDatePicker.setValue(feed.getExpiryDate());
        }
        minStockField.setText(String.valueOf(feed.getMinStockLevel()));
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
            // Create or update feed object
            if (!isEditMode) {
                currentFeed = new Feed();
            }

            // Set values from form
            currentFeed.setName(nameField.getText().trim());
            currentFeed.setType(typeComboBox.getValue());
            currentFeed.setQuantityKg(Double.parseDouble(quantityField.getText().trim()));
            currentFeed.setPricePerKg(Double.parseDouble(pricePerKgField.getText().trim()));
            currentFeed.setSupplier(supplierField.getText().trim().isEmpty() ? null : supplierField.getText().trim());
            currentFeed.setExpiryDate(expiryDatePicker.getValue());
            currentFeed.setMinStockLevel(Double.parseDouble(minStockField.getText().trim()));

            // If adding new feed, set restock date to today
            if (!isEditMode) {
                currentFeed.setLastRestockDate(LocalDate.now());
            }

            // Save to database
            boolean success;
            if (isEditMode) {
                success = feedDAO.updateFeed(currentFeed);
                if (success) {
                    showSuccessMessage("Aliment mis à jour avec succès!");
                } else {
                    showErrorMessage("Erreur lors de la mise à jour de l'aliment.");
                    return;
                }
            } else {
                success = feedDAO.addFeed(currentFeed);
                if (success) {
                    showSuccessMessage("Aliment ajouté avec succès!");
                } else {
                    showErrorMessage("Erreur lors de l'ajout de l'aliment.");
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
            System.err.println("Error saving feed: " + e.getMessage());
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
            double quantity = Double.parseDouble(quantityField.getText().trim());
            if (quantity <= 0) {
                quantityErrorLabel.setText("La quantité doit être supérieure à 0");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            quantityErrorLabel.setText("Veuillez entrer un nombre valide");
            isValid = false;
        }

        // Validate price per kg
        try {
            double price = Double.parseDouble(pricePerKgField.getText().trim());
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
            double minStock = Double.parseDouble(minStockField.getText().trim());
            if (minStock < 0) {
                minStockErrorLabel.setText("Le stock minimum ne peut pas être négatif");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            minStockErrorLabel.setText("Veuillez entrer un nombre valide");
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
