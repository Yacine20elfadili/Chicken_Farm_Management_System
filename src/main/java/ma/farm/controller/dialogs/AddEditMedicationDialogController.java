package ma.farm.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ma.farm.dao.FinancialDAO;
import ma.farm.dao.MedicationDAO;
import ma.farm.dao.SupplierDAO;
import ma.farm.model.FinancialTransaction;
import ma.farm.model.Medication;
import ma.farm.model.Supplier;

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
    private ComboBox<Supplier> supplierCombo;

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
    private SupplierDAO supplierDAO;
    private FinancialDAO financialDAO;
    private Medication currentMedication;
    private boolean isEditMode = false;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        medicationDAO = new MedicationDAO();
        supplierDAO = new SupplierDAO();
        financialDAO = new FinancialDAO();

        typeComboBox.getItems().addAll("Vaccine", "Antibiotic", "Supplement");
        unitComboBox.getItems().addAll("ml", "tablets", "doses", "bottles", "units");

        // Load Suppliers
        supplierCombo
                .setItems(FXCollections.observableArrayList(supplierDAO.getActiveSuppliersByCategory("Medication")));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMedication(Medication medication) {
        this.currentMedication = medication;
        this.isEditMode = true;

        dialogTitle.setText("Modifier Médicament");

        nameField.setText(medication.getName());
        typeComboBox.setValue(medication.getType());
        quantityField.setText(String.valueOf(medication.getQuantity()));
        unitComboBox.setValue(medication.getUnit());
        pricePerUnitField.setText(String.valueOf(medication.getPricePerUnit()));

        // Set Supplier in ComboBox
        if (medication.getSupplier() != null) {
            for (Supplier s : supplierCombo.getItems()) {
                if (s.getName().equals(medication.getSupplier())) {
                    supplierCombo.setValue(s);
                    break;
                }
            }
        }

        if (medication.getPurchaseDate() != null) {
            purchaseDatePicker.setValue(medication.getPurchaseDate());
        }
        if (medication.getExpiryDate() != null) {
            expiryDatePicker.setValue(medication.getExpiryDate());
        }
        minStockField.setText(String.valueOf(medication.getMinStockLevel()));
        usageTextArea.setText(medication.getUsage() != null ? medication.getUsage() : "");
    }

    @FXML
    public void handleSave() {
        clearErrorLabels();

        if (!validateInputs()) {
            return;
        }

        try {
            if (!isEditMode) {
                currentMedication = new Medication();
            }

            currentMedication.setName(nameField.getText().trim());
            currentMedication.setType(typeComboBox.getValue());
            currentMedication.setQuantity(Integer.parseInt(quantityField.getText().trim()));
            currentMedication.setUnit(unitComboBox.getValue());
            currentMedication.setPricePerUnit(Double.parseDouble(pricePerUnitField.getText().trim()));

            Supplier selectedSupplier = supplierCombo.getValue();
            currentMedication.setSupplier(selectedSupplier != null ? selectedSupplier.getName() : null);

            currentMedication.setPurchaseDate(purchaseDatePicker.getValue());
            currentMedication.setExpiryDate(expiryDatePicker.getValue());
            currentMedication.setMinStockLevel(Integer.parseInt(minStockField.getText().trim()));
            currentMedication
                    .setUsage(usageTextArea.getText().trim().isEmpty() ? null : usageTextArea.getText().trim());

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
                    // Log financial transaction for new medication purchases
                    try {
                        FinancialTransaction tx = new FinancialTransaction();
                        tx.setTransactionDate(LocalDate.now());
                        tx.setType("Expense");
                        tx.setCategory("Achat Médicaments");

                        double qty = currentMedication.getQuantity(); // Dosage/Units
                        // Note: Medication might not have pricePerUnit directly or it might be price.
                        // Assuming price field exists and logic holds.
                        double price = currentMedication.getPricePerUnit(); // Use pricePerUnit for calculation
                        double total = qty * price;

                        tx.setAmount(total);
                        tx.setPaymentMethod("Cash"); // Default, could be made configurable
                        tx.setDescription("Achat Médicaments: " + currentMedication.getName() + " (" + qty + " "
                                + currentMedication.getUnit() + ")");

                        if (selectedSupplier != null) {
                            tx.setRelatedEntityType("Supplier");
                            tx.setRelatedEntityId(selectedSupplier.getId());
                        }

                        financialDAO.addTransaction(tx);
                        System.out.println("Logged expense for medication: " + total + " DH");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Failed to log financial transaction for medication");
                    }
                    showSuccessMessage("Médicament ajouté avec succès!");
                } else {
                    showErrorMessage("Erreur lors de l'ajout du médicament.");
                    return;
                }
            }

            if (dialogStage != null) {
                dialogStage.close();
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
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                quantityErrorLabel.setText("La quantité doit être supérieure à 0");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            quantityErrorLabel.setText("Veuillez entrer un nombre entier valide");
            isValid = false;
        }

        if (unitComboBox.getValue() == null || unitComboBox.getValue().isEmpty()) {
            unitErrorLabel.setText("L'unité est requise");
            isValid = false;
        }

        try {
            if (Double.parseDouble(pricePerUnitField.getText().trim()) < 0) {
                priceErrorLabel.setText("Le prix ne peut pas être négatif");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            priceErrorLabel.setText("Veuillez entrer un nombre valide");
            isValid = false;
        }

        try {
            if (Integer.parseInt(minStockField.getText().trim()) < 0) {
                minStockErrorLabel.setText("Le stock minimum ne peut pas être négatif");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            minStockErrorLabel.setText("Veuillez entrer un nombre entier valide");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrorLabels() {
        nameErrorLabel.setText("");
        typeErrorLabel.setText("");
        quantityErrorLabel.setText("");
        unitErrorLabel.setText("");
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
