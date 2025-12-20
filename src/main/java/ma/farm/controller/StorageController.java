package ma.farm.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ma.farm.controller.dialogs.AddEquipmentCategoryDialogController;
import ma.farm.controller.dialogs.ManageEquipmentItemsDialogController;
import ma.farm.controller.dialogs.UseFeedDialogController;
import ma.farm.controller.dialogs.UseMedicationDialogController;
import ma.farm.dao.EquipmentCategoryDAO;
import ma.farm.dao.FeedDAO;
import ma.farm.dao.MedicationDAO;
import ma.farm.model.EquipmentCategory;
import ma.farm.model.Feed;
import ma.farm.model.Medication;

import java.io.IOException;
import java.util.List;

/**
 * StorageController - Controls the Storage view
 * Shows: Feed inventory, medications, equipment categories
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

    // FXML Components - Equipment Categories Section
    @FXML
    private VBox equipmentCategoriesContainer;

    @FXML
    private Button addEquipmentCategoryButton;

    // DAOs
    private FeedDAO feedDAO;
    private MedicationDAO medicationDAO;
    private EquipmentCategoryDAO equipmentCategoryDAO;

    // Observable lists for UI
    private ObservableList<String> feedList;
    private ObservableList<String> medicationList;

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        // Initialize DAOs
        feedDAO = new FeedDAO();
        medicationDAO = new MedicationDAO();
        equipmentCategoryDAO = new EquipmentCategoryDAO();

        // Initialize observable lists
        feedList = FXCollections.observableArrayList();
        medicationList = FXCollections.observableArrayList();

        // Load feed data
        loadFeedData();

        // Load medications data
        loadMedicationsData();

        // Load equipment categories
        loadEquipmentCategories();
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
     * Load and display equipment categories as cards
     */
    private void loadEquipmentCategories() {
        if (equipmentCategoriesContainer == null) return;

        try {
            // Clear existing cards
            equipmentCategoriesContainer.getChildren().clear();

            // Get all categories with item counts
            List<EquipmentCategory> categories = equipmentCategoryDAO.getAllCategoriesWithCounts();

            // Create a card for each category
            for (EquipmentCategory category : categories) {
                VBox categoryCard = createCategoryCard(category);
                equipmentCategoriesContainer.getChildren().add(categoryCard);
            }

            // If no categories, show message
            if (categories.isEmpty()) {
                Label emptyLabel = new Label("Aucune catégorie d'équipement. Cliquez sur 'Ajouter Équipement' pour commencer.");
                emptyLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-style: italic; -fx-padding: 20;");
                equipmentCategoriesContainer.getChildren().add(emptyLabel);
            }

        } catch (Exception e) {
            System.err.println("Error loading equipment categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a card UI for an equipment category
     */
    private VBox createCategoryCard(EquipmentCategory category) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        card.setPrefWidth(300);
        VBox.setMargin(card, new Insets(10, 10, 10, 10));

        // Header with category name and type badge
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(category.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label typeBadge = new Label(category.getCategory());
        typeBadge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; " +
                "-fx-padding: 4 8; -fx-background-radius: 4; -fx-font-size: 11px;");

        header.getChildren().addAll(nameLabel, typeBadge);

        // Item count
        Label countLabel = new Label("Quantité: " + category.getItemCount());
        countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");

        // Location
        Label locationLabel = new Label("📍 " + (category.getLocation() != null ? category.getLocation() : "Non spécifié"));
        locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        // Notes (if exists)
        Label notesLabel = null;
        if (category.getNotes() != null && !category.getNotes().isEmpty()) {
            notesLabel = new Label("Note: " + category.getNotes());
            notesLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af; -fx-wrap-text: true;");
            notesLabel.setMaxWidth(270);
        }

        // Manage button
        Button manageButton = new Button("Modifier Catégorie");
        manageButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        manageButton.setOnAction(e -> handleManageCategory(category));

        // Add all elements to card
        card.getChildren().addAll(header, countLabel, locationLabel);
        if (notesLabel != null) {
            card.getChildren().add(notesLabel);
        }
        card.getChildren().add(manageButton);

        return card;
    }

    /**
     * Handle manage category button click
     * Opens dialog to manage items in this category
     */
    private void handleManageCategory(EquipmentCategory category) {
        try {
            // Load Manage Items Dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/ManageEquipmentItemsDialog.fxml"));
            Parent root = loader.load();

            // Get controller and set category
            ManageEquipmentItemsDialogController controller = loader.getController();
            controller.setCategory(category);

            // Create dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Gérer: " + category.getName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addEquipmentCategoryButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);

            // Show dialog and wait
            dialogStage.showAndWait();

            // Refresh categories after dialog closes
            loadEquipmentCategories();

        } catch (IOException e) {
            System.err.println("Error opening Manage Items dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du dialogue");
        }
    }

    /**
     * Handle add equipment category button click
     */
    @FXML
    public void handleAddEquipmentCategory() {
        try {
            // Load Add Category Dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEquipmentCategoryDialog.fxml"));
            Parent root = loader.load();

            // Get controller
            AddEquipmentCategoryDialogController controller = loader.getController();

            // Create dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter Catégorie d'Équipement");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addEquipmentCategoryButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);

            // Show dialog and wait
            dialogStage.showAndWait();

            // Refresh categories if save was clicked
            if (controller.isSaveClicked()) {
                loadEquipmentCategories();
            }

        } catch (IOException e) {
            System.err.println("Error opening Add Category dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du dialogue");
        }
    }

    /**
     * Handle add feed button click
     * UNCHANGED - Same as before
     */
    @FXML
    public void handleAddFeed() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditFeedDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter Aliment");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addFeedButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            dialogStage.showAndWait();

            loadFeedData();

        } catch (IOException e) {
            System.err.println("Error opening Add Feed dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du dialogue");
        }
    }

    /**
     * Handle use feed button click
     * NOW opens dialog that asks for quantity AND worker
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

        try {
            // Load Use Feed Dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/UseFeedDialog.fxml"));
            Parent root = loader.load();

            // Get controller and set feed
            UseFeedDialogController controller = loader.getController();
            controller.setFeed(selectedFeed);

            // Create dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Utiliser Aliment");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(useFeedButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);

            // Show dialog and wait
            dialogStage.showAndWait();

            // Refresh if usage was recorded
            if (controller.isUsageRecorded()) {
                loadFeedData();
            }

        } catch (IOException e) {
            System.err.println("Error opening Use Feed dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du dialogue");
        }
    }

    /**
     * Handle add medication button click
     * UNCHANGED - Same as before
     */
    @FXML
    public void handleAddMedication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditMedicationDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter Médicament");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addMedicationButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

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
     * NOW opens dialog that asks for quantity AND worker
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

        try {
            // Load Use Medication Dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/UseMedicationDialog.fxml"));
            Parent root = loader.load();

            // Get controller and set medication
            UseMedicationDialogController controller = loader.getController();
            controller.setMedication(selectedMed);

            // Create dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Utiliser Médicament");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(useMedicationButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);

            // Show dialog and wait
            dialogStage.showAndWait();

            // Refresh if usage was recorded
            if (controller.isUsageRecorded()) {
                loadMedicationsData();
            }

        } catch (IOException e) {
            System.err.println("Error opening Use Medication dialog: " + e.getMessage());
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
        loadEquipmentCategories();
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