package ma.farm.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ma.farm.dao.EquipmentItemDAO;
import ma.farm.model.EquipmentCategory;
import ma.farm.model.EquipmentItem;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * ManageEquipmentItemsDialogController - Manages items within a category
 * Shows all items in the category with options to:
 * - Add new item (+)
 * - Edit existing item (✏️)
 * - Delete item (×)
 */
public class ManageEquipmentItemsDialogController {

    @FXML
    private Label categoryNameLabel;

    @FXML
    private Label itemCountLabel;

    @FXML
    private ListView<EquipmentItem> itemsListView;

    @FXML
    private Button addItemButton;

    @FXML
    private Button editItemButton;

    @FXML
    private Button deleteItemButton;

    @FXML
    private Label goodCountLabel;

    @FXML
    private Label fairCountLabel;

    @FXML
    private Label brokenCountLabel;

    private EquipmentItemDAO itemDAO;
    private EquipmentCategory currentCategory;
    private Stage dialogStage;
    private ObservableList<EquipmentItem> itemsList;

    /**
     * Initialize method called after FXML loads
     */
    @FXML
    public void initialize() {
        itemDAO = new EquipmentItemDAO();
        itemsList = FXCollections.observableArrayList();

        // Set up ListView cell factory for custom display
        itemsListView.setCellFactory(param -> new ListCell<EquipmentItem>() {
            @Override
            protected void updateItem(EquipmentItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    // Format: "Item #1 - Good | Acheté: 2024-01-15 | Prix: 25.00 MAD"
                    String displayText = String.format("Item #%d - %s | Acheté: %s | Prix: %.2f MAD",
                            item.getId(),
                            item.getStatus(),
                            item.getPurchaseDate() != null ? item.getPurchaseDate().toString() : "N/A",
                            item.getPurchasePrice());

                    // Add maintenance info if exists
                    if (item.getNextMaintenanceDate() != null) {
                        displayText += String.format(" | Maintenance: %s", item.getNextMaintenanceDate().toString());

                        if (item.isMaintenanceOverdue()) {
                            displayText += " ⚠️ EN RETARD";
                        } else if (item.isMaintenanceDueSoon()) {
                            displayText += " 🔔 Bientôt";
                        }
                    }

                    setText(displayText);

                    // Style based on status
                    if (item.isBroken()) {
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    } else if (item.isFair()) {
                        setStyle("-fx-text-fill: #fd7e14;");
                    } else {
                        setStyle("-fx-text-fill: #28a745;");
                    }
                }
            }
        });

        // Set ListView items
        itemsListView.setItems(itemsList);
    }

    /**
     * Set dialog stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Set the category to manage
     */
    public void setCategory(EquipmentCategory category) {
        this.currentCategory = category;

        if (categoryNameLabel != null) {
            categoryNameLabel.setText(category.getName());
        }

        loadItems();
    }

    /**
     * Load items for the current category
     */
    private void loadItems() {
        if (currentCategory == null) return;

        try {
            List<EquipmentItem> items = itemDAO.getItemsByCategory(currentCategory.getId());

            itemsList.clear();
            itemsList.addAll(items);

            // Update count labels
            updateCountLabels(items);

        } catch (Exception e) {
            System.err.println("Error loading items: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors du chargement des équipements");
        }
    }

    /**
     * Update count labels (Good, Fair, Broken)
     */
    private void updateCountLabels(List<EquipmentItem> items) {
        int totalCount = items.size();
        int goodCount = (int) items.stream().filter(EquipmentItem::isGood).count();
        int fairCount = (int) items.stream().filter(EquipmentItem::isFair).count();
        int brokenCount = (int) items.stream().filter(EquipmentItem::isBroken).count();

        if (itemCountLabel != null) {
            itemCountLabel.setText(String.valueOf(totalCount));
        }

        if (goodCountLabel != null) {
            goodCountLabel.setText(String.valueOf(goodCount));
        }

        if (fairCountLabel != null) {
            fairCountLabel.setText(String.valueOf(fairCount));
        }

        if (brokenCountLabel != null) {
            brokenCountLabel.setText(String.valueOf(brokenCount));
        }
    }

    /**
     * Handle add item button click
     */
    @FXML
    public void handleAddItem() {
        try {
            // Load Add/Edit Item Dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditEquipmentItemDialog.fxml"));
            Parent root = loader.load();

            // Get controller and set category
            AddEditEquipmentItemDialogController controller = loader.getController();
            controller.setCategoryId(currentCategory.getId());

            // Create dialog stage
            Stage itemDialogStage = new Stage();
            itemDialogStage.setTitle("Ajouter Équipement");
            itemDialogStage.initModality(Modality.WINDOW_MODAL);
            itemDialogStage.initOwner(dialogStage);
            itemDialogStage.setScene(new Scene(root));

            controller.setDialogStage(itemDialogStage);

            // Show dialog and wait
            itemDialogStage.showAndWait();

            // Refresh list if item was saved
            if (controller.isSaveClicked()) {
                loadItems();
            }

        } catch (IOException e) {
            System.err.println("Error opening Add Item dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du dialogue");
        }
    }

    /**
     * Handle edit item button click
     */
    @FXML
    public void handleEditItem() {
        EquipmentItem selectedItem = itemsListView.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un équipement à modifier");
            return;
        }

        try {
            // Load Add/Edit Item Dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditEquipmentItemDialog.fxml"));
            Parent root = loader.load();

            // Get controller and set item for editing
            AddEditEquipmentItemDialogController controller = loader.getController();
            controller.setItem(selectedItem);

            // Create dialog stage
            Stage itemDialogStage = new Stage();
            itemDialogStage.setTitle("Modifier Équipement");
            itemDialogStage.initModality(Modality.WINDOW_MODAL);
            itemDialogStage.initOwner(dialogStage);
            itemDialogStage.setScene(new Scene(root));

            controller.setDialogStage(itemDialogStage);

            // Show dialog and wait
            itemDialogStage.showAndWait();

            // Refresh list if item was saved
            if (controller.isSaveClicked()) {
                loadItems();
            }

        } catch (IOException e) {
            System.err.println("Error opening Edit Item dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du dialogue");
        }
    }

    /**
     * Handle delete item button click
     */
    @FXML
    public void handleDeleteItem() {
        EquipmentItem selectedItem = itemsListView.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un équipement à supprimer");
            return;
        }

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer l'équipement");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cet équipement?\n\n" +
                "Item #" + selectedItem.getId() + " - " + selectedItem.getStatus());

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = itemDAO.deleteItem(selectedItem.getId());

                if (success) {
                    showSuccess("Équipement supprimé avec succès");
                    loadItems();
                } else {
                    showError("Erreur lors de la suppression de l'équipement");
                }

            } catch (Exception e) {
                System.err.println("Error deleting item: " + e.getMessage());
                e.printStackTrace();
                showError("Erreur: " + e.getMessage());
            }
        }
    }

    /**
     * Handle close button click
     */
    @FXML
    public void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    // Helper methods for alerts

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