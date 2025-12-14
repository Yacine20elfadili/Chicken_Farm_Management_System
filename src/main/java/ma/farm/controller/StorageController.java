package ma.farm.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ma.farm.controller.dialogs.AddEditEquipmentDialogController;
import ma.farm.controller.dialogs.AddEditFeedDialogController;
import ma.farm.controller.dialogs.AddEditMedicationDialogController;
import ma.farm.dao.FeedDAO;
import ma.farm.dao.MedicationDAO;
import ma.farm.dao.EquipmentDAO;
import ma.farm.model.Feed;
import ma.farm.model.Medication;
import ma.farm.model.Equipment;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * StorageController - Controls the Storage view
 * Shows: Feed inventory, medications, equipment
 */
public class StorageController {

    // FXML Components - Feed Card
    @FXML
    private ListView<String> feedListView;

    @FXML
    private Label totalFeedTypesLabel;

    @FXML
    private Button addFeedButton;

    @FXML
    private Button useFeedButton;

    // FXML Components - Medications Card
    @FXML
    private ListView<String> medicationListView;

    @FXML
    private Label totalMedicationsLabel;

    @FXML
    private Button addMedicationButton;

    @FXML
    private Button useMedicationButton;

    // FXML Components - Equipment Table
    @FXML
    private TableView<Equipment> equipmentTable;

    @FXML
    private TableColumn<Equipment, String> equipmentNameColumn;

    @FXML
    private TableColumn<Equipment, Integer> equipmentQuantityColumn;

    @FXML
    private TableColumn<Equipment, String> equipmentStatusColumn;

    @FXML
    private Button addEquipmentButton;

    @FXML
    private Button updateEquipmentStatusButton;

    // DAOs
    private FeedDAO feedDAO;
    private MedicationDAO medicationDAO;
    private EquipmentDAO equipmentDAO;

    // Observable lists for UI
    private ObservableList<String> feedList;
    private ObservableList<String> medicationList;
    private ObservableList<Equipment> equipmentList;

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        // Initialize DAOs
        feedDAO = new FeedDAO();
        medicationDAO = new MedicationDAO();
        equipmentDAO = new EquipmentDAO();

        // Setup table columns
        setupTableColumns();

        // Initialize observable lists
        feedList = FXCollections.observableArrayList();
        medicationList = FXCollections.observableArrayList();
        equipmentList = FXCollections.observableArrayList();

        // Load feed data
        loadFeedData();

        // Load medications data
        loadMedicationsData();

        // Load equipment data
        loadEquipmentData();
    }

    /**
     * Setup equipment table columns
     * Bind columns to Equipment model properties
     */
    private void setupTableColumns() {
        if (equipmentNameColumn != null) {
            equipmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        }

        if (equipmentQuantityColumn != null) {
            equipmentQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        }

        if (equipmentStatusColumn != null) {
            equipmentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Add status badge cell factory (color coding)
            equipmentStatusColumn.setCellFactory(column -> new TableCell<Equipment, String>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);

                    if (empty || status == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        setText(null);
                        
                        Label statusLabel = new Label(status);
                        statusLabel.setStyle("-fx-padding: 6px 12px; -fx-background-radius: 6px; -fx-font-weight: bold; -fx-font-size: 12px;");
                        
                        switch (status.toLowerCase()) {
                            case "good":
                                statusLabel.setStyle(statusLabel.getStyle() + " -fx-background-color: #d1fae5; -fx-text-fill: #065f46;");
                                break;
                            case "fair":
                                statusLabel.setStyle(statusLabel.getStyle() + " -fx-background-color: #fef3c7; -fx-text-fill: #92400e;");
                                break;
                            case "broken":
                                statusLabel.setStyle(statusLabel.getStyle() + " -fx-background-color: #fee2e2; -fx-text-fill: #991b1b;");
                                break;
                            default:
                                statusLabel.setStyle(statusLabel.getStyle() + " -fx-background-color: #e5e7eb; -fx-text-fill: #374151;");
                        }
                        
                        setGraphic(statusLabel);
                        setStyle("-fx-alignment: CENTER_LEFT;");
                    }
                }
            });
        }
    }

    /**
     * Load and display feed inventory
     */
    private void loadFeedData() {
        try {
            List<Feed> feeds = feedDAO.getAllFeed();

            feedList.clear();

            for (Feed feed : feeds) {
                String feedInfo = String.format("%s - %.1f kg (%s)",
                        feed.getName(),
                        feed.getQuantityKg(),
                        feed.getType());

                if (feed.isLowStock()) {
                    feedInfo += " ⚠️ LOW STOCK";
                }

                feedList.add(feedInfo);
            }

            if (feedListView != null) {
                feedListView.setItems(feedList);

                feedListView.setCellFactory(param -> new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);

                            if (item.contains("LOW STOCK")) {
                                setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                            } else {
                                setStyle("");
                            }
                        }
                    }
                });
            }

            if (totalFeedTypesLabel != null) {
                totalFeedTypesLabel.setText(String.valueOf(feeds.size()));
            }
        } catch (Exception e) {
            System.err.println("Error loading feed data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load and display medications inventory
     */
    private void loadMedicationsData() {
        try {
            List<Medication> medications = medicationDAO.getAllMedications();

            medicationList.clear();

            for (Medication medication : medications) {
                String medicationInfo;
                
                if (medication.getExpiryDate() != null && medication.getSupplier() != null) {
                    medicationInfo = String.format("%s (%s) - %d %s\nExpire: %s | Fournisseur: %s",
                            medication.getName(),
                            medication.getType(),
                            medication.getQuantity(),
                            medication.getUnit(),
                            medication.getExpiryDate().toString(),
                            medication.getSupplier());
                } else if (medication.getExpiryDate() != null) {
                    medicationInfo = String.format("%s (%s) - %d %s\nExpire: %s",
                            medication.getName(),
                            medication.getType(),
                            medication.getQuantity(),
                            medication.getUnit(),
                            medication.getExpiryDate().toString());
                } else {
                    medicationInfo = String.format("%s (%s) - %d %s",
                            medication.getName(),
                            medication.getType(),
                            medication.getQuantity(),
                            medication.getUnit());
                }

                if (medication.isExpired()) {
                    medicationInfo = "🚫 EXPIRÉ - " + medicationInfo;
                } else if (medication.isLowStock()) {
                    medicationInfo = "⚠️ STOCK BAS - " + medicationInfo;
                }

                medicationList.add(medicationInfo);
            }

            if (medicationListView != null) {
                medicationListView.setItems(medicationList);

                medicationListView.setCellFactory(param -> new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);

                            if (item.contains("EXPIRÉ")) {
                                setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-background-color: #fee2e2;");
                            } else if (item.contains("STOCK BAS")) {
                                setStyle("-fx-text-fill: #ea580c; -fx-font-weight: bold; -fx-background-color: #ffedd5;");
                            } else {
                                setStyle("");
                            }
                        }
                    }
                });
            }

            if (totalMedicationsLabel != null) {
                totalMedicationsLabel.setText(String.valueOf(medications.size()));
            }
        } catch (Exception e) {
            System.err.println("Error loading medications data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load and display equipment table
     */
    private void loadEquipmentData() {
        try {
            List<Equipment> equipments = equipmentDAO.getAllEquipment();

            equipments.sort((e1, e2) -> {
                if (e1.isBroken() && !e2.isBroken()) return -1;
                if (!e1.isBroken() && e2.isBroken()) return 1;
                return e1.getName().compareTo(e2.getName());
            });

            equipmentList.clear();
            equipmentList.addAll(equipments);

            if (equipmentTable != null) {
                equipmentTable.setItems(equipmentList);
            }
        } catch (Exception e) {
            System.err.println("Error loading equipment data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle add feed button click
     * Opens dialog to add new feed or restock existing
     */
    @FXML
    public void handleAddFeed() {
        try {
            // Load FXML for Add/Edit Feed Dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditFeedDialog.fxml"));
            Parent root = loader.load();

            // Get controller
            AddEditFeedDialogController controller = loader.getController();

            // Create dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter Aliment");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addFeedButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            // Set controller reference to stage
            controller.setDialogStage(dialogStage);

            // Show dialog and wait
            dialogStage.showAndWait();

            // Refresh feed data after dialog closes
            loadFeedData();

        } catch (IOException e) {
            System.err.println("Error opening Add Feed dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du dialogue");
        }
    }

    /**
     * Handle use feed button click
     * Opens dialog to deduct feed from inventory
     */
    @FXML
    public void handleUseFeed() {
        // Get selected feed from list
        String selectedFeedStr = feedListView.getSelectionModel().getSelectedItem();
        
        if (selectedFeedStr == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un aliment à utiliser");
            return;
        }

        // Extract feed name from the string
        String feedName = selectedFeedStr.split(" - ")[0].trim();
        
        // Find the feed object
        List<Feed> feeds = feedDAO.getAllFeed();
        Feed selectedFeed = feeds.stream()
                .filter(f -> f.getName().equals(feedName))
                .findFirst()
                .orElse(null);

        if (selectedFeed == null) {
            showError("Aliment introuvable");
            return;
        }

        // Create input dialog for quantity
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Utiliser Aliment");
        dialog.setHeaderText("Utiliser: " + selectedFeed.getName());
        dialog.setContentText("Quantité à utiliser (kg):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(quantityStr -> {
            try {
                double quantity = Double.parseDouble(quantityStr);
                
                if (quantity <= 0) {
                    showError("La quantité doit être supérieure à 0");
                    return;
                }
                
                if (quantity > selectedFeed.getQuantityKg()) {
                    showError("Quantité insuffisante en stock (" + selectedFeed.getQuantityKg() + " kg disponible)");
                    return;
                }

                // Update quantity
                double newQuantity = selectedFeed.getQuantityKg() - quantity;
                boolean success = feedDAO.updateQuantity(selectedFeed.getId(), newQuantity);

                if (success) {
                    showSuccess("Aliment utilisé avec succès");
                    loadFeedData();
                } else {
                    showError("Erreur lors de l'utilisation de l'aliment");
                }

            } catch (NumberFormatException e) {
                showError("Veuillez entrer un nombre valide");
            }
        });
    }

    /**
     * Handle add medication button click
     * Opens dialog to add new medication or restock
     */
    @FXML
    public void handleAddMedication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditMedicationDialog.fxml"));
            Parent root = loader.load();

            AddEditMedicationDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter Médicament");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addMedicationButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            loadMedicationsData();

        } catch (IOException e) {
            System.err.println("Error opening Add Medication dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du dialogue");
        }
    }

    /**
     * Handle use medication button click
     * Opens dialog to deduct medication from inventory
     */
    @FXML
    public void handleUseMedication() {
        String selectedMedStr = medicationListView.getSelectionModel().getSelectedItem();
        
        if (selectedMedStr == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un médicament à utiliser");
            return;
        }

        // Extract medication name
        String medName = selectedMedStr;
        if (medName.contains("EXPIRÉ")) {
            medName = medName.replace("🚫 EXPIRÉ - ", "");
        } else if (medName.contains("STOCK BAS")) {
            medName = medName.replace("⚠️ STOCK BAS - ", "");
        }
        medName = medName.split(" \\(")[0].trim();
        final String MedName = medName;
        List<Medication> medications = medicationDAO.getAllMedications();
        Medication selectedMed = medications.stream()
                .filter(m -> m.getName().equals(MedName))
                .findFirst()
                .orElse(null);

        if (selectedMed == null) {
            showError("Médicament introuvable");
            return;
        }

        if (selectedMed.isExpired()) {
            showWarning("Médicament expiré", "Ce médicament est expiré et ne devrait pas être utilisé");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Utiliser Médicament");
        dialog.setHeaderText("Utiliser: " + selectedMed.getName());
        dialog.setContentText("Quantité à utiliser (" + selectedMed.getUnit() + "):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(quantityStr -> {
            try {
                int quantity = Integer.parseInt(quantityStr);
                
                if (quantity <= 0) {
                    showError("La quantité doit être supérieure à 0");
                    return;
                }
                
                if (quantity > selectedMed.getQuantity()) {
                    showError("Quantité insuffisante en stock (" + selectedMed.getQuantity() + " " + selectedMed.getUnit() + " disponible)");
                    return;
                }

                double newQuantity = selectedMed.getQuantity() - quantity;
                boolean success = medicationDAO.updateQuantity(selectedMed.getId(), newQuantity);

                if (success) {
                    showSuccess("Médicament utilisé avec succès");
                    loadMedicationsData();
                } else {
                    showError("Erreur lors de l'utilisation du médicament");
                }

            } catch (NumberFormatException e) {
                showError("Veuillez entrer un nombre entier valide");
            }
        });
    }

    /**
     * Handle add equipment button click
     * Opens dialog to add new equipment
     */
    @FXML
    public void handleAddEquipment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditEquipmentDialog.fxml"));
            Parent root = loader.load();

            AddEditEquipmentDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter Équipement");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addEquipmentButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            loadEquipmentData();

        } catch (IOException e) {
            System.err.println("Error opening Add Equipment dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du dialogue");
        }
    }

    /**
     * Handle update equipment status button click
     * Opens dialog to change equipment status
     */
    @FXML
    public void handleUpdateEquipmentStatus() {
        Equipment selectedEquipment = equipmentTable.getSelectionModel().getSelectedItem();

        if (selectedEquipment == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un équipement à modifier");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditEquipmentDialog.fxml"));
            Parent root = loader.load();

            AddEditEquipmentDialogController controller = loader.getController();

            // Set equipment for editing
            controller.setEquipment(selectedEquipment);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Équipement");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(updateEquipmentStatusButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            loadEquipmentData();

        } catch (IOException e) {
            System.err.println("Error opening Edit Equipment dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du dialogue");
        }
    }

    /**
     * Refresh all storage data
     */
    @FXML
    public void refreshData() {
        loadFeedData();
        loadMedicationsData();
        loadEquipmentData();
    }

    // Helper methods for showing alerts

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Une erreur s'est produite");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText("Opération réussie");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}