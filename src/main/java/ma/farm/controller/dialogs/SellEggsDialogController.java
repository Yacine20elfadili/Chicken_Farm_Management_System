package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ma.farm.dao.EggProductionDAO;
import ma.farm.dao.HouseDAO;
import ma.farm.model.EggProduction;
import ma.farm.model.House;
import ma.farm.model.HouseType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for the Sell Eggs Dialog
 * Handles selling eggs by reducing quantities in the egg_production table
 */
public class SellEggsDialogController {

    @FXML
    private VBox housesListContainer;

    @FXML
    private Label totalToSellLabel;

    @FXML
    private DatePicker saleDatePicker;

    @FXML
    private TextField priceField;

    @FXML
    private TextField customerField;

    @FXML
    private Button confirmButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label errorLabel;

    // DAOs
    private EggProductionDAO eggProductionDAO;
    private HouseDAO houseDAO;

    // Track houses and their input fields
    private List<House> eggLayingHouses;
    private Map<Integer, TextField> houseQuantityFields; // houseId -> TextField
    private Map<Integer, Integer> houseAvailableEggs;    // houseId -> available eggs

    private boolean saved = false;
    private int totalSoldQuantity = 0;

    @FXML
    public void initialize() {
        System.out.println("=== SellEggsDialogController: Initializing ===");

        eggProductionDAO = new EggProductionDAO();
        houseDAO = new HouseDAO();

        houseQuantityFields = new HashMap<>();
        houseAvailableEggs = new HashMap<>();

        // Set default date to today
        saleDatePicker.setValue(LocalDate.now());

        // Disable confirm button initially
        confirmButton.setDisable(true);

        // Hide error label initially
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }

        // Load egg-laying houses
        loadEggLayingHouses();

        // Set up price field validation (allow decimals)
        priceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldVal);
            }
        });
    }

    /**
     * Load all egg-laying houses and create input rows for each
     */
    private void loadEggLayingHouses() {
        try {
            List<House> allHouses = houseDAO.getAllHouses();

            // Filter to only egg-laying houses (EGG_LAYER and MEAT_FEMALE)
            eggLayingHouses = allHouses.stream()
                    .filter(house -> house.getType() == HouseType.EGG_LAYER ||
                            house.getType() == HouseType.MEAT_FEMALE)
                    .collect(Collectors.toList());

            if (eggLayingHouses.isEmpty()) {
                showError("Aucune maison pondeuse configurée");
                confirmButton.setDisable(true);
                return;
            }

            // Clear container
            housesListContainer.getChildren().clear();

            // Create a row for each house
            for (House house : eggLayingHouses) {
                HBox houseRow = createHouseRow(house);
                housesListContainer.getChildren().add(houseRow);
            }

            System.out.println("Loaded " + eggLayingHouses.size() + " egg-laying houses for sale dialog");

        } catch (Exception e) {
            System.err.println("Error loading egg-laying houses: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors du chargement des maisons");
        }
    }

    /**
     * Calculate available eggs for a house (sum of all goodEggs from egg_production)
     */
    private int calculateAvailableEggs(int houseId) {
        try {
            List<EggProduction> productions = eggProductionDAO.getProductionByHouse(houseId);
            return productions.stream()
                    .mapToInt(EggProduction::getGoodEggs)
                    .sum();
        } catch (Exception e) {
            System.err.println("Error calculating available eggs for house " + houseId + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * Create a row for a house with name, available eggs, and input field
     */
    private HBox createHouseRow(House house) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(10);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 5; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");

        // Calculate available eggs for this house
        int availableEggs = calculateAvailableEggs(house.getId());
        houseAvailableEggs.put(house.getId(), availableEggs);

        // House name label
        Label nameLabel = new Label(house.getName());
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        nameLabel.setPrefWidth(180);

        // Available eggs label
        Label availableLabel = new Label("[" + availableEggs + "]");
        if (availableEggs > 0) {
            availableLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #28a745; -fx-font-weight: bold;");
        } else {
            availableLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc3545; -fx-font-weight: bold;");
        }
        availableLabel.setPrefWidth(70);

        // Arrow
        Label arrowLabel = new Label("→");
        arrowLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Quantity input field
        TextField quantityField = new TextField("0");
        quantityField.setPrefWidth(80);
        quantityField.setAlignment(Pos.CENTER);
        quantityField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ced4da; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 5 8; -fx-font-size: 13px;");

        // Disable field if no eggs available
        if (availableEggs <= 0) {
            quantityField.setDisable(true);
            quantityField.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ced4da; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 5 8; -fx-font-size: 13px;");
        }

        // Store reference
        houseQuantityFields.put(house.getId(), quantityField);

        // Add input validation and update listener
        final int maxAvailable = availableEggs;
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Only allow numbers
            if (!newVal.matches("\\d*")) {
                quantityField.setText(newVal.replaceAll("[^\\d]", ""));
                return;
            }

            // Validate against available
            if (!newVal.isEmpty()) {
                try {
                    int qty = Integer.parseInt(newVal);
                    if (qty > maxAvailable) {
                        quantityField.setText(String.valueOf(maxAvailable));
                        return;
                    }
                } catch (NumberFormatException e) {
                    quantityField.setText("0");
                }
            }

            // Update total and button state
            updateTotalAndButton();
        });

        // Focus listener to select all text
        quantityField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                quantityField.selectAll();
            }
        });

        row.getChildren().addAll(nameLabel, availableLabel, arrowLabel, spacer, quantityField);

        return row;
    }

    /**
     * Update the total eggs to sell and enable/disable confirm button
     */
    private void updateTotalAndButton() {
        int total = 0;

        for (Map.Entry<Integer, TextField> entry : houseQuantityFields.entrySet()) {
            String text = entry.getValue().getText().trim();
            if (!text.isEmpty()) {
                try {
                    total += Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    // Ignore invalid values
                }
            }
        }

        totalToSellLabel.setText(String.valueOf(total));

        // Enable confirm button only if total > 0
        confirmButton.setDisable(total <= 0);
    }

    @FXML
    public void handleConfirm() {
        hideError();

        // Validate date
        LocalDate saleDate = saleDatePicker.getValue();
        if (saleDate == null) {
            showError("Veuillez sélectionner une date de vente.");
            return;
        }

        // Collect all houses with quantities > 0 and validate
        Map<Integer, Integer> houseSales = new HashMap<>(); // houseId -> quantity to sell
        totalSoldQuantity = 0;

        for (House house : eggLayingHouses) {
            TextField field = houseQuantityFields.get(house.getId());
            String text = field.getText().trim();

            if (!text.isEmpty()) {
                try {
                    int qty = Integer.parseInt(text);
                    if (qty > 0) {
                        // Validate against available
                        int available = houseAvailableEggs.get(house.getId());
                        if (qty > available) {
                            showError("Quantité pour " + house.getName() + " dépasse le stock disponible (" + available + ")");
                            return;
                        }

                        houseSales.put(house.getId(), qty);
                        totalSoldQuantity += qty;
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid values
                }
            }
        }

        if (houseSales.isEmpty()) {
            showError("Veuillez entrer au moins une quantité à vendre.");
            return;
        }

        // Process each house sale - reduce eggs from egg_production records
        boolean allSuccess = true;
        for (Map.Entry<Integer, Integer> entry : houseSales.entrySet()) {
            int houseId = entry.getKey();
            int qtyToSell = entry.getValue();

            boolean success = reduceEggsFromHouse(houseId, qtyToSell);
            if (!success) {
                allSuccess = false;
                System.err.println("Failed to reduce eggs for house " + houseId);
            }
        }

        if (allSuccess) {
            saved = true;
            System.out.println("Egg sales recorded successfully: " + totalSoldQuantity + " eggs from " + houseSales.size() + " houses");
            closeDialog();
        } else {
            showError("Certaines ventes n'ont pas pu être enregistrées. Veuillez réessayer.");
        }
    }

    /**
     * Reduce eggs from a house's production records
     * Strategy: Reduce from the most recent production records first (LIFO)
     */
    private boolean reduceEggsFromHouse(int houseId, int quantityToSell) {
        try {
            // Get all production records for this house, ordered by date DESC (most recent first)
            List<EggProduction> productions = eggProductionDAO.getProductionByHouse(houseId);

            if (productions.isEmpty()) {
                System.err.println("No production records found for house " + houseId);
                return false;
            }

            int remainingToSell = quantityToSell;

            for (EggProduction prod : productions) {
                if (remainingToSell <= 0) {
                    break;
                }

                int goodEggs = prod.getGoodEggs();
                if (goodEggs <= 0) {
                    continue;
                }

                // Calculate how many eggs to reduce from this record
                int reduceFromThisRecord = Math.min(remainingToSell, goodEggs);

                // Reduce eggsCollected (goodEggs will be recalculated automatically)
                int newEggsCollected = prod.getEggsCollected() - reduceFromThisRecord;
                if (newEggsCollected < prod.getCrackedEggs()) {
                    // Can't have eggsCollected < crackedEggs, adjust
                    newEggsCollected = prod.getCrackedEggs();
                    reduceFromThisRecord = prod.getEggsCollected() - newEggsCollected;
                }

                prod.setEggsCollected(newEggsCollected);
                prod.calculateGoodEggs();

                // Update the record in database
                boolean updated = eggProductionDAO.updateProduction(prod);
                if (updated) {
                    remainingToSell -= reduceFromThisRecord;
                    System.out.println("Reduced " + reduceFromThisRecord + " eggs from production record " + prod.getId() +
                                     " (house " + houseId + "). New eggsCollected: " + newEggsCollected);
                } else {
                    System.err.println("Failed to update production record " + prod.getId());
                }
            }

            if (remainingToSell > 0) {
                System.err.println("Warning: Could not fully reduce " + quantityToSell + " eggs from house " + houseId +
                                 ". Remaining: " + remainingToSell);
                return false;
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error reducing eggs from house " + houseId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    public void handleCancel() {
        System.out.println("Cancel button clicked");
        saved = false;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
        System.err.println("Error: " + message);
    }

    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public int getSoldQuantity() {
        return totalSoldQuantity;
    }

    public LocalDate getSaleDate() {
        return saleDatePicker.getValue();
    }
}
