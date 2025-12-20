package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ma.farm.dao.EggProductionDAO;
import ma.farm.dao.HouseDAO;
import ma.farm.model.EggProduction;
import ma.farm.model.House;
import ma.farm.model.HouseType;
import ma.farm.util.DateUtil;
import ma.farm.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RecordEggCollectionDialogController - Controls the Record Egg Collection dialog
 * Allows users to record daily egg collection for egg-laying houses
 */
public class RecordEggCollectionDialogController {

    @FXML
    private ComboBox<House> houseSelector;

    @FXML
    private TextField eggsCollectedField;

    @FXML
    private TextField crackedEggsField;

    @FXML
    private DatePicker collectionDatePicker;

    @FXML
    private TextArea notesArea;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label errorLabel;

    // DAOs
    private EggProductionDAO eggProductionDAO;
    private HouseDAO houseDAO;

    // Flag to track if save was clicked
    private boolean saved = false;

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        System.out.println("=== RecordEggCollectionDialogController: Initializing ===");

        // Initialize DAOs
        eggProductionDAO = new EggProductionDAO();
        houseDAO = new HouseDAO();

        // Hide error label initially
        errorLabel.setVisible(false);

        // Set default date to today
        collectionDatePicker.setValue(LocalDate.now());

        // Load egg-laying houses into combo box
        loadEggLayingHouses();

        // Set up button handlers
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());

        // Add numeric input validation to egg fields
        setupNumericValidation();
    }

    /**
     * Load all egg-laying houses into the house selector combo box
     */
    private void loadEggLayingHouses() {
        try {
            List<House> allHouses = houseDAO.getAllHouses();

            // Filter to only egg-laying houses
            List<House> eggLayingHouses = allHouses.stream()
                    .filter(house -> house.getType() == HouseType.EGG_LAYER || 
                                   house.getType() == HouseType.MEAT_FEMALE)
                    .collect(Collectors.toList());

            if (eggLayingHouses.isEmpty()) {
                showError("Aucune maison pondeuse configurée");
                saveButton.setDisable(true);
                return;
            }

            // Populate combo box
            houseSelector.getItems().addAll(eggLayingHouses);
            houseSelector.getSelectionModel().selectFirst();

            // FIX #1: Set custom cell factory to display house name instead of toString()
            houseSelector.setCellFactory(param -> new ListCell<House>() {
                @Override
                protected void updateItem(House house, boolean empty) {
                    super.updateItem(house, empty);
                    if (empty || house == null) {
                        setText(null);
                    } else {
                        setText(house.getName() + " - " + house.getType().getDisplayName());
                    }
                }
            });

            // Also set the button cell to display house name
            houseSelector.setButtonCell(new ListCell<House>() {
                @Override
                protected void updateItem(House house, boolean empty) {
                    super.updateItem(house, empty);
                    if (empty || house == null) {
                        setText(null);
                    } else {
                        setText(house.getName() + " - " + house.getType().getDisplayName());
                    }
                }
            });

            System.out.println("Loaded " + eggLayingHouses.size() + " egg-laying houses");

        } catch (Exception e) {
            System.err.println("Error loading egg-laying houses: " + e.getMessage());
            showError("Erreur lors du chargement des maisons");
        }
    }

    /**
     * Setup numeric validation for egg input fields
     */
    private void setupNumericValidation() {
        // Only allow numbers in eggs collected field
        eggsCollectedField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                eggsCollectedField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Only allow numbers in cracked eggs field
        crackedEggsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                crackedEggsField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    /**
     * Handle save button click
     */
    @FXML
    private void handleSave() {
        System.out.println("Save button clicked");

        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            // Get selected house
            House selectedHouse = houseSelector.getValue();
            if (selectedHouse == null) {
                showError("Veuillez sélectionner une maison");
                return;
            }

            // Get input values
            int eggsCollected = Integer.parseInt(eggsCollectedField.getText());
            int crackedEggs = Integer.parseInt(crackedEggsField.getText());
            LocalDate collectionDate = collectionDatePicker.getValue();
            String notes = notesArea.getText();

            // Validate cracked eggs don't exceed collected eggs
            if (crackedEggs > eggsCollected) {
                showError("Le nombre d'œufs cracked ne peut pas dépasser le nombre d'œufs collectés");
                return;
            }

            // FIX: Check if a record already exists for this house on this date
            boolean recordExists = false;
            EggProduction existingProduction = null;
            
            try {
                List<EggProduction> existingRecords = eggProductionDAO.getProductionByDate(collectionDate);
                for (EggProduction prod : existingRecords) {
                    if (prod.getHouseId() == selectedHouse.getId()) {
                        recordExists = true;
                        existingProduction = prod;
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error checking existing records: " + e.getMessage());
            }

            boolean success;

            if (recordExists && existingProduction != null) {
                // UPDATE existing record - ADD eggs to what was already collected
                System.out.println("Record exists for this house on this date - UPDATING");
                
                int newEggsCollected = existingProduction.getEggsCollected() + eggsCollected;
                int newCrackedEggs = existingProduction.getCrackedEggs() + crackedEggs;
                
                existingProduction.setEggsCollected(newEggsCollected);
                existingProduction.setCrackedEggs(newCrackedEggs);
                existingProduction.calculateGoodEggs();
                
                // Update notes if provided
                if (!notes.isEmpty()) {
                    String existingNotes = existingProduction.getNotes();
                    if (existingNotes != null && !existingNotes.isEmpty()) {
                        existingProduction.setNotes(existingNotes + "\n" + notes);
                    } else {
                        existingProduction.setNotes(notes);
                    }
                }
                
                // Call UPDATE method in DAO
                success = eggProductionDAO.updateProduction(existingProduction);
                System.out.println("Updated production: " + newEggsCollected + " eggs, " + newCrackedEggs + " cracked");
                
            } else {
                // CREATE new record
                System.out.println("No existing record - CREATING new");
                
                EggProduction production = new EggProduction(
                        selectedHouse.getId(),
                        collectionDate,
                        eggsCollected,
                        crackedEggs,
                        0  // Dead chickens (not used in eggs bay)
                );
                production.setCollectedBy(System.getProperty("user.name"));
                production.setNotes(notes);
                production.calculateGoodEggs();

                success = eggProductionDAO.addProduction(production);
                System.out.println("Created new production: " + eggsCollected + " eggs, " + crackedEggs + " cracked");
            }

            if (success) {
                System.out.println("Egg collection recorded successfully for house: " + selectedHouse.getName());
                saved = true;
                closeDialog();
            } else {
                showError("Erreur lors de l'enregistrement de la collecte");
            }

        } catch (NumberFormatException e) {
            showError("Veuillez entrer des nombres valides");
        } catch (Exception e) {
            System.err.println("Error saving egg collection: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }

    /**
     * Handle cancel button click
     */
    @FXML
    private void handleCancel() {
        System.out.println("Cancel button clicked");
        saved = false;
        closeDialog();
    }

    /**
     * Validate all input fields
     */
    private boolean validateInputs() {
        errorLabel.setVisible(false);

        // Check house selected
        if (houseSelector.getValue() == null) {
            showError("Veuillez sélectionner une maison");
            return false;
        }

        // Check eggs collected
        if (eggsCollectedField.getText().isEmpty()) {
            showError("Veuillez entrer le nombre d'œufs collectés");
            return false;
        }

        // Check cracked eggs
        if (crackedEggsField.getText().isEmpty()) {
            showError("Veuillez entrer le nombre d'œufs cracked");
            return false;
        }

        // Check date
        if (collectionDatePicker.getValue() == null) {
            showError("Veuillez sélectionner une date");
            return false;
        }

        try {
            int eggsCollected = Integer.parseInt(eggsCollectedField.getText());
            int crackedEggs = Integer.parseInt(crackedEggsField.getText());

            if (eggsCollected < 0 || crackedEggs < 0) {
                showError("Les nombres ne peuvent pas être négatifs");
                return false;
            }

            if (eggsCollected == 0 && crackedEggs == 0) {
                showError("Au moins un œuf doit être collecté");
                return false;
            }

        } catch (NumberFormatException e) {
            showError("Veuillez entrer des nombres valides");
            return false;
        }

        return true;
    }

    /**
     * Show error message to user
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        System.err.println("Validation error: " + message);
    }

    /**
     * Close the dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Check if save was clicked
     */
    public boolean isSaved() {
        return saved;
    }
}
