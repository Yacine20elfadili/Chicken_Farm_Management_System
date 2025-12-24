package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ma.farm.dao.CustomerDAO;
import ma.farm.model.Customer;

public class CustomerDialogController {

    @FXML
    private Label dialogTitle;

    @FXML
    private RadioButton companyRadio;
    @FXML
    private RadioButton individualRadio;
    @FXML
    private ToggleGroup typeGroup;

    // Company Section
    @FXML
    private VBox companySection;
    @FXML
    private TextField companyNameField;
    @FXML
    private ComboBox<String> legalFormCombo;
    @FXML
    private TextField iceField;
    @FXML
    private TextField rcField;
    @FXML
    private TextField websiteField;

    // Contact Section
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField addressField;

    // Secondary Contact
    @FXML
    private TextField secondaryNameField;
    @FXML
    private TextField secondaryPhoneField;

    // Terms Section
    @FXML
    private VBox termsSection;
    @FXML
    private ComboBox<String> paymentTermsCombo;
    @FXML
    private TextField deliveryScheduleField;
    @FXML
    private TextField usualPurchasesField;

    // Notes
    @FXML
    private TextArea notesArea;

    private CustomerDAO customerDAO;
    private Customer currentCustomer;
    private Stage dialogStage;
    private boolean saved = false;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        customerDAO = new CustomerDAO();

        // Setup Legal Form combo
        legalFormCombo.getItems().addAll("SARL", "SA", "SNC", "SCS", "Auto-entrepreneur", "Autre");

        // Setup Payment Terms combo
        paymentTermsCombo.getItems().addAll("Immediate", "Net 15", "Net 30", "Net 60");
        paymentTermsCombo.setValue("Immediate");

        // Type toggle listener
        typeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            updateFieldVisibility();
        });

        updateFieldVisibility();
    }

    private void updateFieldVisibility() {
        boolean isCompany = companyRadio.isSelected();
        companySection.setVisible(isCompany);
        companySection.setManaged(isCompany);

        // For individuals, payment is always immediate
        if (!isCompany) {
            paymentTermsCombo.setValue("Immediate");
            paymentTermsCombo.setDisable(true);
        } else {
            paymentTermsCombo.setDisable(false);
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCustomer(Customer customer) {
        this.currentCustomer = customer;
        this.isEditMode = true;
        dialogTitle.setText("✏️ Modifier Client");

        // Set type
        if ("Company".equals(customer.getType())) {
            companyRadio.setSelected(true);
        } else {
            individualRadio.setSelected(true);
        }
        updateFieldVisibility();

        // Populate fields
        companyNameField.setText(customer.getCompanyName());
        if (customer.getLegalForm() != null) {
            legalFormCombo.setValue(customer.getLegalForm());
        }
        iceField.setText(customer.getIce());
        rcField.setText(customer.getRc());
        websiteField.setText(customer.getWebsite());

        nameField.setText(customer.getName());
        phoneField.setText(customer.getPhone());
        emailField.setText(customer.getEmail());
        addressField.setText(customer.getAddress());

        secondaryNameField.setText(customer.getSecondaryContactName());
        secondaryPhoneField.setText(customer.getSecondaryContactPhone());

        if (customer.getPaymentTerms() != null) {
            paymentTermsCombo.setValue(customer.getPaymentTerms());
        }
        deliveryScheduleField.setText(customer.getDeliverySchedule());
        usualPurchasesField.setText(customer.getUsualPurchases());

        notesArea.setText(customer.getNotes());
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    public void handleSave() {
        if (!validateInputs()) {
            return;
        }

        if (!isEditMode) {
            currentCustomer = new Customer();
        }

        // Set type
        currentCustomer.setType(companyRadio.isSelected() ? "Company" : "Individual");

        // Company fields
        if (companyRadio.isSelected()) {
            currentCustomer.setCompanyName(companyNameField.getText().trim());
            currentCustomer.setLegalForm(legalFormCombo.getValue());
            currentCustomer.setIce(iceField.getText().trim());
            currentCustomer.setRc(rcField.getText().trim());
            currentCustomer.setWebsite(websiteField.getText().trim());
        } else {
            // Clear company fields for individuals
            currentCustomer.setCompanyName(null);
            currentCustomer.setLegalForm(null);
            currentCustomer.setIce(null);
            currentCustomer.setRc(null);
            currentCustomer.setWebsite(null);
        }

        // Contact fields
        currentCustomer.setName(nameField.getText().trim());
        currentCustomer.setPhone(phoneField.getText().trim());
        currentCustomer.setEmail(emailField.getText().trim());
        currentCustomer.setAddress(addressField.getText().trim());

        // Secondary contact
        currentCustomer.setSecondaryContactName(secondaryNameField.getText().trim());
        currentCustomer.setSecondaryContactPhone(secondaryPhoneField.getText().trim());

        // Terms
        currentCustomer.setPaymentTerms(paymentTermsCombo.getValue());
        currentCustomer.setDeliverySchedule(deliveryScheduleField.getText().trim());
        currentCustomer.setUsualPurchases(usualPurchasesField.getText().trim());

        // Notes
        currentCustomer.setNotes(notesArea.getText());

        // Save
        boolean success;
        if (isEditMode) {
            success = customerDAO.updateCustomer(currentCustomer);
        } else {
            currentCustomer.setActive(true);
            success = customerDAO.addCustomer(currentCustomer);
        }

        if (success) {
            saved = true;
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    isEditMode ? "Client modifié avec succès!" : "Client ajouté avec succès!");
            dialogStage.close();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "L'opération a échoué.");
        }
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        // Name is always required
        if (nameField.getText().trim().isEmpty()) {
            errors.append("• Le nom du contact est requis.\n");
        }
        if (phoneField.getText().trim().isEmpty()) {
            errors.append("• Le numéro de téléphone est requis.\n");
        }

        // Company-specific validation
        if (companyRadio.isSelected()) {
            if (companyNameField.getText().trim().isEmpty()) {
                errors.append("• Le nom de la société est requis.\n");
            }
            if (iceField.getText().trim().isEmpty()) {
                errors.append("• L'ICE est obligatoire pour les entreprises.\n");
            } else if (!iceField.getText().trim().matches("\\d{15}")) {
                errors.append("• L'ICE doit contenir exactement 15 chiffres.\n");
            }
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation", errors.toString());
            return false;
        }

        return true;
    }

    @FXML
    public void handleCancel() {
        saved = false;
        dialogStage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
