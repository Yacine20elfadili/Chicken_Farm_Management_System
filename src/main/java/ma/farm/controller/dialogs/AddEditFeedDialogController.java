package ma.farm.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ma.farm.dao.FeedDAO;
import ma.farm.dao.FinancialDAO;
import ma.farm.dao.SupplierDAO;
import ma.farm.model.Feed;
import ma.farm.model.FinancialTransaction;
import ma.farm.model.Supplier;

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
    private ComboBox<Supplier> supplierCombo;

    @FXML
    private DatePicker expiryDatePicker;
    @FXML
    private TextField minStockField;
    @FXML
    private Label minStockErrorLabel;

    private FeedDAO feedDAO;
    private SupplierDAO supplierDAO;
    private FinancialDAO financialDAO;
    private Feed currentFeed;
    private boolean isEditMode = false;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        feedDAO = new FeedDAO();
        supplierDAO = new SupplierDAO();
        financialDAO = new FinancialDAO();

        typeComboBox.getItems().addAll("Day-old", "Layer", "Meat Growth");

        // Load Suppliers
        supplierCombo.setItems(FXCollections.observableArrayList(supplierDAO.getActiveSuppliersByCategory("Feed")));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setFeed(Feed feed) {
        this.currentFeed = feed;
        this.isEditMode = true;

        dialogTitle.setText("Modifier Aliment");

        nameField.setText(feed.getName());
        typeComboBox.setValue(feed.getType());
        quantityField.setText(String.valueOf(feed.getQuantityKg()));
        pricePerKgField.setText(String.valueOf(feed.getPricePerKg()));

        // Set Supplier in ComboBox
        if (feed.getSupplier() != null) {
            for (Supplier s : supplierCombo.getItems()) {
                if (s.getName().equals(feed.getSupplier())) {
                    supplierCombo.setValue(s);
                    break;
                }
            }
        }

        if (feed.getExpiryDate() != null) {
            expiryDatePicker.setValue(feed.getExpiryDate());
        }
        minStockField.setText(String.valueOf(feed.getMinStockLevel()));
    }

    @FXML
    public void handleSave() {
        clearErrorLabels();

        if (!validateInputs()) {
            return;
        }

        try {
            if (!isEditMode) {
                currentFeed = new Feed();
            }

            currentFeed.setName(nameField.getText().trim());
            currentFeed.setType(typeComboBox.getValue());
            currentFeed.setQuantityKg(Double.parseDouble(quantityField.getText().trim()));
            currentFeed.setPricePerKg(Double.parseDouble(pricePerKgField.getText().trim()));

            Supplier selectedSupplier = supplierCombo.getValue();
            currentFeed.setSupplier(selectedSupplier != null ? selectedSupplier.getName() : null);

            currentFeed.setExpiryDate(expiryDatePicker.getValue());
            currentFeed.setMinStockLevel(Double.parseDouble(minStockField.getText().trim()));

            if (!isEditMode) {
                currentFeed.setLastRestockDate(LocalDate.now());
            }

            boolean success;
            if (isEditMode) {
                success = feedDAO.updateFeed(currentFeed);
            } else {
                success = feedDAO.addFeed(currentFeed);
            }

            if (success) {
                // Log financial transaction for new feed purchases (not edits)
                if (!isEditMode) {
                    try {
                        FinancialTransaction tx = new FinancialTransaction();
                        tx.setTransactionDate(LocalDate.now());
                        tx.setType("Expense");
                        tx.setCategory("Achat Aliments");

                        double qty = currentFeed.getQuantityKg();
                        double price = currentFeed.getPricePerKg();
                        double total = qty * price;

                        tx.setAmount(total);
                        tx.setPaymentMethod("Cash");
                        tx.setDescription("Achat Aliments: " + currentFeed.getName() + " (" + qty + " kg)");

                        if (selectedSupplier != null) {
                            tx.setRelatedEntityType("Supplier");
                            tx.setRelatedEntityId(selectedSupplier.getId());
                        }

                        financialDAO.addTransaction(tx);
                        System.out.println("Logged expense for feed: " + total + " DH");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Failed to log financial transaction for feed");
                    }
                }

                showSuccessMessage(isEditMode ? "Aliment mis à jour avec succès!" : "Aliment ajouté avec succès!");
                if (dialogStage != null)
                    dialogStage.close();
            } else {
                showErrorMessage("Erreur lors de l'enregistrement.");
            }

        } catch (NumberFormatException e) {
            showErrorMessage("Erreur: Vérifiez les valeurs numériques.");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Erreur: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (nameField.getText().trim().isEmpty()) {
            nameErrorLabel.setText("Le nom est requis");
            isValid = false;
        }

        if (typeComboBox.getValue() == null || typeComboBox.getValue().isEmpty()) {
            typeErrorLabel.setText("Le type est requis");
            isValid = false;
        }

        try {
            if (Double.parseDouble(quantityField.getText().trim()) <= 0) {
                quantityErrorLabel.setText("La quantité doit être positive");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            quantityErrorLabel.setText("Nombre invalide");
            isValid = false;
        }

        try {
            if (Double.parseDouble(pricePerKgField.getText().trim()) < 0) {
                priceErrorLabel.setText("Le prix ne peut pas être négatif");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            priceErrorLabel.setText("Nombre invalide");
            isValid = false;
        }

        try {
            if (Double.parseDouble(minStockField.getText().trim()) < 0) {
                minStockErrorLabel.setText("Stock min invalide");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            minStockErrorLabel.setText("Nombre invalide");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrorLabels() {
        nameErrorLabel.setText("");
        typeErrorLabel.setText("");
        quantityErrorLabel.setText("");
        priceErrorLabel.setText("");
        minStockErrorLabel.setText("");
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
