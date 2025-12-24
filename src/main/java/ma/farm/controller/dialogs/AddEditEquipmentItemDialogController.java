package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ma.farm.dao.EquipmentItemDAO;
import ma.farm.dao.FinancialDAO;
import ma.farm.dao.SupplierDAO;
import ma.farm.model.EquipmentItem;
import ma.farm.model.FinancialTransaction;
import ma.farm.model.Supplier;
import javafx.util.StringConverter;
import java.util.List;
import java.util.stream.Collectors;

import java.time.LocalDate;

/**
 * AddEditEquipmentItemDialogController - Handles Add/Edit Equipment Item Dialog
 * Used for adding/editing individual equipment items within a category
 */
public class AddEditEquipmentItemDialogController {

    @FXML
    private Label dialogTitle;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Label statusErrorLabel;

    @FXML
    private DatePicker purchaseDatePicker;

    @FXML
    private Label purchaseDateErrorLabel;

    @FXML
    private TextField purchasePriceField;

    @FXML
    private Label priceErrorLabel;

    @FXML
    private ComboBox<Supplier> supplierComboBox; // Added

    @FXML
    private DatePicker lastMaintenanceDatePicker;

    @FXML
    private DatePicker nextMaintenanceDatePicker;

    @FXML
    private Label maintenanceErrorLabel;

    private EquipmentItemDAO itemDAO;
    private SupplierDAO supplierDAO;
    private FinancialDAO financialDAO;
    private EquipmentItem currentItem;
    private int categoryId;
    private boolean isEditMode = false;
    private Stage dialogStage;
    private boolean saveClicked = false;

    /**
     * Initialize method called after FXML loads
     */
    @FXML
    public void initialize() {
        itemDAO = new EquipmentItemDAO();
        supplierDAO = new SupplierDAO();
        financialDAO = new FinancialDAO();

        // Populate status ComboBox
        statusComboBox.getItems().addAll(
                "Good",
                "Fair",
                "Broken",
                "Active", "Maintenance", "Retired");
        statusComboBox.setValue("Good"); // Default value

        // Initialize and load suppliers
        supplierDAO = new SupplierDAO();
        loadSuppliers();
    }

    private void loadSuppliers() {
        List<Supplier> allSuppliers = supplierDAO.getAllSuppliers();
        // Filter for Equipment or Mixed or Other
        List<Supplier> equipmentSuppliers = allSuppliers.stream()
                .filter(s -> "Equipment".equalsIgnoreCase(s.getCategory()) ||
                        "Mixed".equalsIgnoreCase(s.getCategory()) ||
                        "Other".equalsIgnoreCase(s.getCategory()))
                .filter(Supplier::isActive)
                .collect(Collectors.toList());

        supplierComboBox.getItems().setAll(equipmentSuppliers);

        // String Converter for Display
        supplierComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Supplier s) {
                return s != null ? s.getName() : "";
            }

            @Override
            public Supplier fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Set dialog stage (for closing)
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Set category ID for new item
     */
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
        this.isEditMode = false;
        dialogTitle.setText("Ajouter Équipement");
    }

    /**
     * Set item for editing
     */
    public void setItem(EquipmentItem item) {
        this.currentItem = item;
        this.categoryId = item.getCategoryId();
        this.isEditMode = true;

        // Update dialog title
        dialogTitle.setText("Modifier Équipement");

        // Populate form with existing item data
        statusComboBox.setValue(item.getStatus());
        if (item.getPurchaseDate() != null) {
            purchaseDatePicker.setValue(item.getPurchaseDate());
        }
        purchasePriceField.setText(String.valueOf(item.getPurchasePrice()));
        if (item.getLastMaintenanceDate() != null) {
            lastMaintenanceDatePicker.setValue(item.getLastMaintenanceDate());
        }
        if (item.getNextMaintenanceDate() != null) {
            nextMaintenanceDatePicker.setValue(item.getNextMaintenanceDate());
        }

        // Set Supplier
        if (item.getSupplierId() > 0) {
            supplierComboBox.getItems().stream()
                    .filter(s -> s.getId() == item.getSupplierId())
                    .findFirst()
                    .ifPresent(s -> supplierComboBox.setValue(s));
        }
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
            // Create or update item object
            if (!isEditMode) {
                currentItem = new EquipmentItem();
                currentItem.setCategoryId(categoryId);
            }

            // Set values from form
            currentItem.setStatus(statusComboBox.getValue());
            currentItem.setPurchaseDate(purchaseDatePicker.getValue());

            // Set Supplier
            if (supplierComboBox.getValue() != null) {
                currentItem.setSupplierId(supplierComboBox.getValue().getId());
            } else {
                currentItem.setSupplierId(0);
            }

            try {
                double price = Double.parseDouble(purchasePriceField.getText().trim());
                currentItem.setPurchasePrice(price);
            } catch (NumberFormatException e) {
                currentItem.setPurchasePrice(0.0);
            }

            currentItem.setLastMaintenanceDate(lastMaintenanceDatePicker.getValue());
            currentItem.setNextMaintenanceDate(nextMaintenanceDatePicker.getValue());

            // Save to database
            boolean success;
            if (isEditMode) {
                success = itemDAO.updateItem(currentItem);
                if (success) {
                    showSuccessMessage("Équipement mis à jour avec succès!");
                } else {
                    showErrorMessage("Erreur lors de la mise à jour de l'équipement.");
                    return;
                }
            } else {
                success = itemDAO.addItem(currentItem);
                if (success) {
                    // Log financial transaction for new equipment purchase
                    try {
                        FinancialTransaction tx = new FinancialTransaction();
                        tx.setTransactionDate(LocalDate.now());
                        tx.setType("Expense");
                        tx.setCategory("Achat Équipement");

                        double amount = currentItem.getPurchasePrice();
                        tx.setAmount(amount);
                        tx.setPaymentMethod("Cash");
                        tx.setDescription("Achat Équipement"); // Can be refined if category name known

                        if (currentItem.getSupplierId() > 0) {
                            tx.setRelatedEntityType("Supplier");
                            tx.setRelatedEntityId(currentItem.getSupplierId());
                        }

                        financialDAO.addTransaction(tx);
                        System.out.println("Logged expense for equipment: " + amount + " DH");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Failed to log financial transaction for equipment");
                    }
                    showSuccessMessage("Équipement ajouté avec succès!");
                } else {
                    showErrorMessage("Erreur lors de l'ajout de l'équipement.");
                    return;
                }
            }

            saveClicked = true;

            // Close dialog
            if (dialogStage != null) {
                dialogStage.close();
            }

        } catch (Exception e) {
            System.err.println("Error saving equipment item: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Erreur: " + e.getMessage());
        }
    }

    /**
     * Validate all input fields
     */
    private boolean validateInputs() {
        boolean isValid = true;

        // Validate status
        if (statusComboBox.getValue() == null || statusComboBox.getValue().isEmpty()) {
            statusErrorLabel.setText("L'état est requis");
            isValid = false;
        }

        // Validate purchase date
        if (purchaseDatePicker.getValue() == null) {
            purchaseDateErrorLabel.setText("La date d'achat est requise");
            isValid = false;
        }

        // Validate purchase price
        if (purchasePriceField.getText().trim().isEmpty()) {
            priceErrorLabel.setText("Le prix d'achat est requis");
            isValid = false;
        } else {
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

        // Validate maintenance dates (if both are provided)
        if (lastMaintenanceDatePicker.getValue() != null && nextMaintenanceDatePicker.getValue() != null) {
            if (nextMaintenanceDatePicker.getValue().isBefore(lastMaintenanceDatePicker.getValue())) {
                maintenanceErrorLabel.setText("La prochaine maintenance doit être après la dernière");
                isValid = false;
            }
        }

        return isValid;
    }

    /**
     * Clear all error labels
     */
    private void clearErrorLabels() {
        statusErrorLabel.setText("");
        purchaseDateErrorLabel.setText("");
        priceErrorLabel.setText("");
        maintenanceErrorLabel.setText("");
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