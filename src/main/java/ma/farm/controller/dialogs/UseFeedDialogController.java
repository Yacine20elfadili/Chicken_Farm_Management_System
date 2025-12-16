package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ma.farm.dao.FeedDAO;
import ma.farm.dao.PersonnelDAO;
import ma.farm.model.Feed;
import ma.farm.model.Personnel;

import java.util.List;

/**
 * UseFeedDialogController - Handles Use Feed Dialog
 * Asks for quantity AND worker who is using the feed
 */
public class UseFeedDialogController {

    @FXML
    private Label feedNameLabel;

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

    private FeedDAO feedDAO;
    private PersonnelDAO personnelDAO;
    private Feed currentFeed;
    private Stage dialogStage;
    private boolean usageRecorded = false;

    /**
     * Initialize method called after FXML loads
     */
    @FXML
    public void initialize() {
        feedDAO = new FeedDAO();
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
     * Set feed to use
     */
    public void setFeed(Feed feed) {
        this.currentFeed = feed;

        // Display feed info
        if (feedNameLabel != null) {
            feedNameLabel.setText(feed.getName());
        }

        if (currentStockLabel != null) {
            currentStockLabel.setText(String.format("Stock actuel: %.1f kg", feed.getQuantityKg()));
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
            double quantity = Double.parseDouble(quantityField.getText().trim());
            String worker = workerComboBox.getValue();

            // Check if sufficient stock
            if (quantity > currentFeed.getQuantityKg()) {
                quantityErrorLabel.setText("Quantité insuffisante en stock");
                return;
            }

            // Update quantity in database
            double newQuantity = currentFeed.getQuantityKg() - quantity;
            boolean success = feedDAO.updateQuantity(currentFeed.getId(), newQuantity);

            if (success) {
                // TODO: In the future, record this usage in a feed_usage_log table
                // with worker name, quantity, and date

                showSuccessMessage(String.format(
                        "%.1f kg d'aliment utilisé par %s",
                        quantity,
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
            System.err.println("Error using feed: " + e.getMessage());
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
                double quantity = Double.parseDouble(quantityField.getText().trim());
                if (quantity <= 0) {
                    quantityErrorLabel.setText("La quantité doit être supérieure à 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                quantityErrorLabel.setText("Veuillez entrer un nombre valide");
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
        alert.setHeaderText("Aliment Utilisé");
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