package ma.farm.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ma.farm.dao.FinancialDAO;
import ma.farm.dao.HouseDAO;
import ma.farm.dao.SupplierDAO;
import ma.farm.model.FinancialTransaction;
import ma.farm.model.Supplier;

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
    private ComboBox<Supplier> supplierCombo;
    @FXML
    private TextField priceField;

    @FXML
    private Label errorLabel;
    @FXML
    private Button importButton;

    private HouseDAO houseDAO;
    private SupplierDAO supplierDAO;
    private FinancialDAO financialDAO;
    private boolean saved = false;
    private int maxImportLimit = 0;
    private int availableCapacity = 0;

    @FXML
    public void initialize() {
        houseDAO = new HouseDAO();
        supplierDAO = new SupplierDAO();
        financialDAO = new FinancialDAO();

        // Get limits from database
        maxImportLimit = houseDAO.getMaxImportLimit();
        availableCapacity = houseDAO.getTotalEmptyCapacityByType(ma.farm.model.HouseType.DAY_OLD);

        availableCapacity = houseDAO.getTotalDayOldCapacity() -
                houseDAO.getTotalChickenCountByType(ma.farm.model.HouseType.DAY_OLD);

        maxImportLabel.setText(String.format("%,d", maxImportLimit));
        availableCapacityLabel.setText("Available capacity in DayOld houses: " +
                String.format("%,d", availableCapacity));

        // Load Suppliers (Category = Chicks or similar logic if implemented)
        // For now loading all active suppliers or assume user selects appropriate one
        // Ideally should filter by "Chicks" but category might be "Poussin" or mixed
        supplierCombo.setItems(FXCollections.observableArrayList(supplierDAO.getActiveSuppliersByCategory("Chicks")));

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

        // Supplier is REQUIRED
        if (supplierCombo.getValue() == null) {
            showError("Veuillez sélectionner un fournisseur.");
            return;
        }

        // Price is REQUIRED
        String priceText = priceField.getText().trim();
        if (priceText.isEmpty()) {
            showError("Veuillez entrer un prix unitaire.");
            return;
        }

        double price = 0.0;
        try {
            price = Double.parseDouble(priceText);
            if (price <= 0) {
                showError("Le prix doit être supérieur à 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Le prix est invalide.");
            return;
        }

        int quantity = Integer.parseInt(quantityField.getText().trim());
        Supplier selectedSupplier = supplierCombo.getValue();

        boolean success = houseDAO.distributeChicksAcrossDayOldHouses(
                quantity,
                LocalDate.now());

        if (success) {
            // Log expense transaction linked to supplier
            double totalAmount = quantity * price;

            FinancialTransaction tx = new FinancialTransaction();
            tx.setTransactionDate(LocalDate.now());
            tx.setType("Expense");
            tx.setCategory("Achat Poussins");
            tx.setAmount(totalAmount);
            tx.setPaymentMethod("Cash");
            tx.setDescription("Import " + quantity + " poussins de " + selectedSupplier.getName());
            tx.setRelatedEntityType("Supplier");
            tx.setRelatedEntityId(selectedSupplier.getId());
            financialDAO.addTransaction(tx);

            System.out.println("Import successful. Supplier: " + selectedSupplier.getName() + ", Qty: " + quantity
                    + ", Amount: " + totalAmount + " DH");
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
