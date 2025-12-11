package ma.farm.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ma.farm.dao.FeedDAO;
import ma.farm.dao.MedicationDAO;
import ma.farm.dao.EquipmentDAO;
import ma.farm.model.Feed;
import ma.farm.model.Medication;
import ma.farm.model.Equipment;

import java.util.List;

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

    // FXML Components - Medications Card
    @FXML
    private Label totalMedicationsLabel;

    @FXML
    private Label lowStockMedicationsLabel;

    // FXML Components - Equipment Table
    @FXML
    private TableView<Equipment> equipmentTable;

    @FXML
    private TableColumn<Equipment, String> equipmentNameColumn;

    @FXML
    private TableColumn<Equipment, Integer> equipmentQuantityColumn;

    @FXML
    private TableColumn<Equipment, String> equipmentStatusColumn;

    // DAOs
    private FeedDAO feedDAO;
    private MedicationDAO medicationDAO;
    private EquipmentDAO equipmentDAO;

    // Observable lists for UI
    private ObservableList<String> feedList;
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
            // Bind equipmentNameColumn to name property
            equipmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        }

        if (equipmentQuantityColumn != null) {
            // Bind equipmentQuantityColumn to quantity property
            equipmentQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        }

        if (equipmentStatusColumn != null) {
            // Bind equipmentStatusColumn to status property
            equipmentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Add status badge cell factory (color coding)
            equipmentStatusColumn.setCellFactory(column -> new TableCell<Equipment, String>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);

                    if (empty || status == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(status);

                        // Apply color based on status
                        switch (status.toLowerCase()) {
                            case "good":
                                setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                                break;
                            case "fair":
                                setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                                break;
                            case "broken":
                                setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                                break;
                            default:
                                setStyle("");
                        }
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
            // Get all feed from FeedDAO
            List<Feed> feeds = feedDAO.getAllFeed();

            // Clear list
            feedList.clear();

            // Format feed info (name, quantity, type)
            for (Feed feed : feeds) {
                String feedInfo = String.format("%s - %.1f kg (%s)",
                        feed.getName(),
                        feed.getQuantityKg(),
                        feed.getType());

                // Highlight low stock items
                if (feed.isLowStock()) {
                    feedInfo += " ⚠️ LOW STOCK";
                }

                feedList.add(feedInfo);
            }

            // Update feedListView
            if (feedListView != null) {
                feedListView.setItems(feedList);

                // Apply custom cell factory for low stock highlighting
                feedListView.setCellFactory(param -> new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);

                            // Highlight low stock in red
                            if (item.contains("LOW STOCK")) {
                                setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                            } else {
                                setStyle("");
                            }
                        }
                    }
                });
            }

            // Update totalFeedTypesLabel
            if (totalFeedTypesLabel != null) {
                totalFeedTypesLabel.setText(String.valueOf(feeds.size()));
            }
        } catch (Exception e) {
            System.err.println("Error loading feed data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load and display medications data
     */
    private void loadMedicationsData() {
        try {
            // Get all medications from MedicationDAO
            List<Medication> medications = medicationDAO.getAllMedications();

            // Count total medications
            int totalMedications = medications.size();

            // Count low stock medications
            long lowStockCount = medications.stream()
                    .filter(Medication::isLowStock)
                    .count();

            // Update totalMedicationsLabel
            if (totalMedicationsLabel != null) {
                totalMedicationsLabel.setText(String.valueOf(totalMedications));
            }

            // Update lowStockMedicationsLabel
            if (lowStockMedicationsLabel != null) {
                lowStockMedicationsLabel.setText(String.valueOf(lowStockCount));

                // Apply warning badge if low stock > 0
                if (lowStockCount > 0) {
                    lowStockMedicationsLabel.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
                } else {
                    lowStockMedicationsLabel.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
                }
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
            // Get all equipment from EquipmentDAO
            List<Equipment> equipments = equipmentDAO.getAllEquipment();

            // Sort by status (Broken first)
            equipments.sort((e1, e2) -> {
                if (e1.isBroken() && !e2.isBroken()) return -1;
                if (!e1.isBroken() && e2.isBroken()) return 1;
                return e1.getName().compareTo(e2.getName());
            });

            // Add to equipmentList
            equipmentList.clear();
            equipmentList.addAll(equipments);

            // Update equipmentTable
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
        // TODO: Open add/restock feed dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Feed");
        alert.setHeaderText("Add/Restock Feed Feature");
        alert.setContentText("This feature will open a dialog to add new feed or restock existing.\n\nDialog implementation is pending.");
        alert.showAndWait();

        // After dialog implementation:
        // - Get feed details (name, type, quantity, price)
        // - Check if feed exists
        // - If exists, update quantity (restock)
        // - If new, create Feed record using feedDAO.addFeed()
        // - Refresh feed data
    }

    /**
     * Handle use feed button click
     * Opens dialog to deduct feed from inventory
     */
    @FXML
    public void handleUseFeed() {
        // TODO: Open use feed dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Use Feed");
        alert.setHeaderText("Use Feed Feature");
        alert.setContentText("This feature will open a dialog to deduct feed from inventory.\n\nDialog implementation is pending.");
        alert.showAndWait();

        // After dialog implementation:
        // - Get feed selection
        // - Get quantity to use
        // - Validate available quantity
        // - Deduct from inventory using feedDAO.updateQuantity()
        // - Refresh feed data
    }

    /**
     * Handle add medication button click
     * Opens dialog to add new medication or restock
     */
    @FXML
    public void handleAddMedication() {
        // TODO: Open add/restock medication dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Medication");
        alert.setHeaderText("Add/Restock Medication Feature");
        alert.setContentText("This feature will open a dialog to add new medication or restock.\n\nDialog implementation is pending.");
        alert.showAndWait();

        // After dialog implementation:
        // - Get medication details
        // - Check if medication exists
        // - Update or create record using medicationDAO.addMedication()
        // - Refresh medications data
    }

    /**
     * Handle use medication button click
     * Opens dialog to deduct medication from inventory
     */
    @FXML
    public void handleUseMedication() {
        // TODO: Open use medication dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Use Medication");
        alert.setHeaderText("Use Medication Feature");
        alert.setContentText("This feature will open a dialog to deduct medication from inventory.\n\nDialog implementation is pending.");
        alert.showAndWait();

        // After dialog implementation:
        // - Get medication selection
        // - Get quantity to use
        // - Validate available quantity
        // - Deduct from inventory using medicationDAO.updateQuantity()
        // - Refresh medications data
    }

    /**
     * Handle add equipment button click
     * Opens dialog to add new equipment
     */
    @FXML
    public void handleAddEquipment() {
        // TODO: Open add equipment dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Equipment");
        alert.setHeaderText("Add Equipment Feature");
        alert.setContentText("This feature will open a dialog to add new equipment.\n\nDialog implementation is pending.");
        alert.showAndWait();

        // After dialog implementation:
        // - Get equipment details
        // - Create Equipment record using equipmentDAO.addEquipment()
        // - Refresh equipment table
    }

    /**
     * Handle update equipment status button click
     * Opens dialog to change equipment status
     */
    @FXML
    public void handleUpdateEquipmentStatus() {
        // Get selected equipment from table
        Equipment selectedEquipment = equipmentTable.getSelectionModel().getSelectedItem();

        // If nothing selected, show error
        if (selectedEquipment == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Equipment Selected");
            alert.setContentText("Please select equipment to update its status.");
            alert.showAndWait();
            return;
        }

        // TODO: Open update status dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Update Equipment Status");
        alert.setHeaderText("Update Status Feature");
        alert.setContentText("This feature will open a dialog to change equipment status.\n\nCurrent status: " + selectedEquipment.getStatus() + "\n\nDialog implementation is pending.");
        alert.showAndWait();

        // After dialog implementation:
        // - Get new status (Good/Fair/Broken)
        // - Update equipment record using equipmentDAO.updateStatus()
        // - Refresh equipment table
    }

    /**
     * Refresh all storage data
     */
    @FXML
    public void refreshData() {
        // Reload feed data
        loadFeedData();

        // Reload medications data
        loadMedicationsData();

        // Reload equipment data
        loadEquipmentData();
    }
}
