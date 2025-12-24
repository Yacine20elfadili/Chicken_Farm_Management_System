package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ma.farm.dao.SupplierDAO;
import ma.farm.model.Supplier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SupplierDialogController {

    @FXML
    private Label dialogTitle;

    // Company Info
    @FXML
    private TextField nameField;
    @FXML
    private TextField companyNameField;
    @FXML
    private ComboBox<String> legalFormCombo;
    @FXML
    private TextField iceField;
    @FXML
    private TextField rcField;
    @FXML
    private TextArea addressArea;
    @FXML
    private TextField websiteField;

    // Contact
    @FXML
    private TextField contactPersonField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private CheckBox secondContactCheck;
    @FXML
    private TextField secondNameField;
    @FXML
    private TextField secondPhoneField;
    @FXML
    private TextField secondEmailField;

    // Category (Category will be a main dropdown, sub-categories could be
    // checkboxes in a future iteration or simplified here)
    @FXML
    private ComboBox<String> categoryCombo;
    @FXML
    private TextArea subCategoriesArea; // Simple text for now

    // Banking
    @FXML
    private TextField bankNameField;
    @FXML
    private TextField ribField;
    @FXML
    private TextField swiftField;

    // Commercial
    @FXML
    private ComboBox<String> paymentTermsCombo;
    @FXML
    private ComboBox<String> paymentMethodCombo;
    @FXML
    private TextField minOrderField;
    @FXML
    private TextField deliveryTimeField;

    @FXML
    private TextArea notesArea;
    @FXML
    private Label errorLabel;

    private SupplierDAO supplierDAO;
    private Supplier currentSupplier;
    private boolean isEditMode = false;
    private Stage dialogStage;
    private boolean saved = false;

    @FXML
    public void initialize() {
        supplierDAO = new SupplierDAO();

        legalFormCombo.getItems().addAll("SARL", "SARL AU", "SA", "SNC", "Personne Physique", "Autre");
        categoryCombo.getItems().addAll("Feed", "Medication", "Equipment", "Chicks", "Mixed", "Other");
        paymentTermsCombo.getItems().addAll("Comptant", "15 Jours", "30 Jours", "60 Jours", "90 Jours");
        paymentMethodCombo.getItems().addAll("Virement", "Chèque", "Espèces", "Traite");

        secondContactCheck.selectedProperty().addListener((obs, old, newVal) -> {
            secondNameField.setDisable(!newVal);
            secondPhoneField.setDisable(!newVal);
            secondEmailField.setDisable(!newVal);
        });

        // Disable secondary fields by default
        secondNameField.setDisable(true);
        secondPhoneField.setDisable(true);
        secondEmailField.setDisable(true);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setSupplier(Supplier supplier) {
        this.currentSupplier = supplier;
        this.isEditMode = true;
        dialogTitle.setText("Modifier Fournisseur");

        // Populate fields
        nameField.setText(supplier.getName());
        companyNameField.setText(supplier.getCompanyName());
        legalFormCombo.setValue(supplier.getLegalForm());
        iceField.setText(supplier.getIce());
        rcField.setText(supplier.getRc());
        addressArea.setText(supplier.getAddress());
        websiteField.setText(supplier.getWebsite());

        contactPersonField.setText(supplier.getContactPerson());
        emailField.setText(supplier.getEmail());
        phoneField.setText(supplier.getPhone());

        if (supplier.getSecondaryContactName() != null && !supplier.getSecondaryContactName().isEmpty()) {
            secondContactCheck.setSelected(true);
            secondNameField.setText(supplier.getSecondaryContactName());
            secondPhoneField.setText(supplier.getSecondaryContactPhone());
            secondEmailField.setText(supplier.getSecondaryContactEmail());
        }

        categoryCombo.setValue(supplier.getCategory());
        subCategoriesArea.setText(supplier.getSubCategories());

        bankNameField.setText(supplier.getBankName());
        ribField.setText(supplier.getRib());
        swiftField.setText(supplier.getSwift());

        paymentTermsCombo.setValue(supplier.getPaymentTerms());
        paymentMethodCombo.setValue(supplier.getPreferredPaymentMethod());
        minOrderField.setText(String.valueOf(supplier.getMinOrderAmount()));
        deliveryTimeField.setText(String.valueOf(supplier.getAvgDeliveryTime()));

        notesArea.setText(supplier.getNotes());
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        if (currentSupplier == null) {
            currentSupplier = new Supplier();
            currentSupplier.setCreatedAt(LocalDateTime.now());
            currentSupplier.setActive(true);
        }
        currentSupplier.setUpdatedAt(LocalDateTime.now());

        // Bind fields to model
        currentSupplier.setName(nameField.getText().trim());
        currentSupplier.setCompanyName(companyNameField.getText().trim());
        currentSupplier.setLegalForm(legalFormCombo.getValue());
        currentSupplier.setIce(iceField.getText().trim());
        currentSupplier.setRc(rcField.getText().trim());
        currentSupplier.setAddress(addressArea.getText().trim());
        currentSupplier.setWebsite(websiteField.getText().trim());

        currentSupplier.setContactPerson(contactPersonField.getText().trim());
        currentSupplier.setEmail(emailField.getText().trim());
        currentSupplier.setPhone(phoneField.getText().trim());

        if (secondContactCheck.isSelected()) {
            currentSupplier.setSecondaryContactName(secondNameField.getText().trim());
            currentSupplier.setSecondaryContactPhone(secondPhoneField.getText().trim());
            currentSupplier.setSecondaryContactEmail(secondEmailField.getText().trim());
        } else {
            currentSupplier.setSecondaryContactName(null);
            currentSupplier.setSecondaryContactPhone(null);
            currentSupplier.setSecondaryContactEmail(null);
        }

        currentSupplier.setCategory(categoryCombo.getValue());
        currentSupplier.setSubCategories(subCategoriesArea.getText().trim());

        currentSupplier.setBankName(bankNameField.getText().trim());
        currentSupplier.setRib(ribField.getText().trim());
        currentSupplier.setSwift(swiftField.getText().trim());

        currentSupplier.setPaymentTerms(paymentTermsCombo.getValue());
        currentSupplier.setPreferredPaymentMethod(paymentMethodCombo.getValue());

        try {
            if (!minOrderField.getText().isEmpty())
                currentSupplier.setMinOrderAmount(Double.parseDouble(minOrderField.getText().trim()));
            if (!deliveryTimeField.getText().isEmpty())
                currentSupplier.setAvgDeliveryTime(Integer.parseInt(deliveryTimeField.getText().trim()));
        } catch (NumberFormatException e) {
            // Should be caught by validation, but safe fallback
        }

        currentSupplier.setNotes(notesArea.getText().trim());

        boolean result;
        if (isEditMode) {
            result = supplierDAO.updateSupplier(currentSupplier);
        } else {
            result = supplierDAO.addSupplier(currentSupplier);
        }

        if (result) {
            saved = true;
            dialogStage.close();
        } else {
            errorLabel.setText("Database error. Could not save.");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean validateInput() {
        String error = "";

        if (nameField.getText().trim().isEmpty())
            error += "Name is required.\n";
        if (categoryCombo.getValue() == null)
            error += "Category is required.\n";

        // ICE should be 15 digits if provided
        String ice = iceField.getText().trim();
        if (!ice.isEmpty() && !ice.matches("\\d{15}")) {
            error += "ICE must be exactly 15 digits.\n";
        }

        // RIB should be 24 digits if provided
        String rib = ribField.getText().trim();
        if (!rib.isEmpty() && !rib.matches("\\d{24}")) {
            error += "RIB must be exactly 24 digits.\n";
        }

        // Phone format (simple check)
        String phone = phoneField.getText().trim();
        if (!phone.isEmpty() && !phone.matches("[\\d\\+\\-\\s]+")) {
            error += "Invalid phone format.\n";
        }

        try {
            if (!minOrderField.getText().trim().isEmpty())
                Double.parseDouble(minOrderField.getText().trim());
            if (!deliveryTimeField.getText().trim().isEmpty())
                Integer.parseInt(deliveryTimeField.getText().trim());
        } catch (NumberFormatException e) {
            error += "Numeric fields (Min Order, Delivery Time) must be valid numbers.\n";
        }

        if (error.isEmpty()) {
            errorLabel.setVisible(false);
            return true;
        } else {
            errorLabel.setText(error);
            errorLabel.setVisible(true);
            return false;
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Supplier getSupplier() {
        return currentSupplier;
    }
}
