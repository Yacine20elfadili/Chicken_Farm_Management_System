package ma.farm.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import ma.farm.dao.CustomerDAO;
import ma.farm.dao.FinancialDAO;
import ma.farm.dao.HouseDAO;
import ma.farm.model.Customer;
import ma.farm.model.FinancialTransaction;
import ma.farm.model.House;

import java.time.LocalDate;

/**
 * Controller for the Sell Chickens Dialog
 * Handles selling chickens from FemaleMeat or MaleMeat houses
 */
public class SellChickensDialogController {

    @FXML
    private Label sourceHouseLabel;

    @FXML
    private Label availableChickensLabel;

    @FXML
    private TextField quantityField;

    @FXML
    private Label quantityErrorLabel;

    @FXML
    private DatePicker saleDatePicker;

    @FXML
    private TextField priceField;

    @FXML
    private ComboBox<Customer> customerCombo;

    @FXML
    private Label errorLabel;

    @FXML
    private Button confirmButton;

    private HouseDAO houseDAO;
    private CustomerDAO customerDAO;
    private FinancialDAO financialDAO;
    private House sourceHouse;
    private boolean saved = false;

    @FXML
    public void initialize() {
        houseDAO = new HouseDAO();
        customerDAO = new CustomerDAO();
        financialDAO = new FinancialDAO();

        // Set default date to today
        saleDatePicker.setValue(LocalDate.now());

        // Load active customers
        loadCustomers();

        // Set up number-only validation for quantity field
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                quantityField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            validateQuantity();
        });

        // Set up price field validation (allow decimals)
        priceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldVal);
            }
        });
    }

    private void loadCustomers() {
        customerCombo.setItems(FXCollections.observableArrayList(customerDAO.getActiveCustomers()));
        customerCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer c) {
                return c != null ? c.getName() : "";
            }

            @Override
            public Customer fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Sets the source house from which chickens will be sold
     * Must be called before showing the dialog
     */
    public void setSourceHouse(House house) {
        this.sourceHouse = house;

        // Update UI with source house info
        sourceHouseLabel.setText("Selling from: " + house.getName());
        availableChickensLabel.setText(String.valueOf(house.getChickenCount()));
    }

    private boolean validateQuantity() {
        String quantityText = quantityField.getText().trim();

        if (quantityText.isEmpty()) {
            quantityErrorLabel.setVisible(false);
            return false;
        }

        try {
            int quantity = Integer.parseInt(quantityText);

            if (quantity <= 0) {
                showQuantityError("Quantity must be greater than 0");
                return false;
            }

            if (sourceHouse != null && quantity > sourceHouse.getChickenCount()) {
                showQuantityError("Quantity exceeds available chickens (" + sourceHouse.getChickenCount() + ")");
                return false;
            }

            quantityErrorLabel.setVisible(false);
            return true;

        } catch (NumberFormatException e) {
            showQuantityError("Please enter a valid number");
            return false;
        }
    }

    private void showQuantityError(String message) {
        quantityErrorLabel.setText(message);
        quantityErrorLabel.setVisible(true);
    }

    @FXML
    public void handleSellAll() {
        if (sourceHouse != null) {
            quantityField.setText(String.valueOf(sourceHouse.getChickenCount()));
        }
    }

    @FXML
    public void handleConfirm() {
        if (sourceHouse == null) {
            showError("No source house specified.");
            return;
        }

        // Validate quantity
        String quantityText = quantityField.getText().trim();
        if (quantityText.isEmpty()) {
            showError("Please enter the quantity to sell.");
            return;
        }

        if (!validateQuantity()) {
            return;
        }

        // Customer is REQUIRED
        Customer selectedCustomer = customerCombo.getValue();
        if (selectedCustomer == null) {
            showError("Veuillez sélectionner un client.");
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

        int quantity = Integer.parseInt(quantityText);

        // Validate date
        LocalDate saleDate = saleDatePicker.getValue();
        if (saleDate == null) {
            showError("Please select a sale date.");
            return;
        }

        // Perform the sale
        boolean success;
        if (quantity == sourceHouse.getChickenCount()) {
            // Selling all - reset the house
            success = houseDAO.resetHouse(sourceHouse.getId());
        } else {
            // Partial sale - just reduce count
            success = houseDAO.removeChickensFromHouse(sourceHouse.getId(), quantity);
        }

        if (success) {
            // Log Income transaction linked to customer
            double totalAmount = quantity * price;

            FinancialTransaction tx = new FinancialTransaction();
            tx.setTransactionDate(saleDate);
            tx.setType("Income");
            tx.setCategory("Vente Poulets");
            tx.setAmount(totalAmount);
            tx.setPaymentMethod("Cash");
            tx.setDescription("Vente " + quantity + " poulets à " + selectedCustomer.getName());

            tx.setRelatedEntityType("Customer");
            tx.setRelatedEntityId(selectedCustomer.getId());

            // Update customer loyalty
            customerDAO.recordVisit(selectedCustomer.getId(), totalAmount);

            financialDAO.addTransaction(tx);

            System.out.println("Sale successful. Qty: " + quantity + ", Amount: " + totalAmount + " DH");
            saved = true;
            closeDialog();
        } else {
            showError("Failed to complete the sale. Please try again.");
        }
    }

    @FXML
    public void handleCancel() {
        saved = false;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
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
