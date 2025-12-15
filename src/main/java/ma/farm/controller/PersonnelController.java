package ma.farm.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ma.farm.dao.PersonnelDAO;
import ma.farm.model.Personnel;

import java.io.IOException;
import java.util.List;

/**
 * PersonnelController - Controls the Personnel view
 * Shows: Grid of personnel cards with basic info
 */
public class PersonnelController {

    // FXML Components
    @FXML
    private GridPane personnelGrid;

    @FXML
    private Label totalWorkersLabel;

    @FXML
    private Label totalTrackersLabel;

    // Statistics labels for new departments
    @FXML
    private Label totalPersonnelLabel;

    @FXML
    private Label totalVeterinaryLabel;

    @FXML
    private Label totalInventoryLabel;

    @FXML
    private Label totalSupervisorsLabel;

    @FXML
    private Label totalFarmhandsLabel;

    // Filter state
    private String currentFilter = "All"; // Default filter

    // DAO
    private PersonnelDAO personnelDAO;

    // Track selected personnel for edit/delete operations
    private Personnel selectedPersonnel;

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        // Initialize DAO
        personnelDAO = new PersonnelDAO();

        // Configure grid to prevent overlapping
        if (personnelGrid != null) {
            personnelGrid.setHgap(25);
            personnelGrid.setVgap(25);
        }

        // Load all operations personnel by default
        loadPersonnelData();

        // Update all statistics
        updateAllStatistics();
    }

    /**
     * Load and display personnel based on current filter
     */
    private void loadPersonnelData() {
        try {
            // Get personnel based on current filter
            List<Personnel> personnelList;

            switch (currentFilter) {
                case "Veterinary":
                    personnelList = personnelDAO.getAllVeterinary();
                    break;
                case "Inventory":
                    personnelList = personnelDAO.getAllInventoryTrackers();
                    break;
                case "Supervisors":
                    personnelList = personnelDAO.getAllSupervisors();
                    break;
                case "Farmhands":
                    personnelList = personnelDAO.getAllFarmhands();
                    break;
                case "All":
                default:
                    personnelList = personnelDAO.getOperationsPersonnel();
                    break;
            }

            // Clear grid
            if (personnelGrid != null) {
                personnelGrid.getChildren().clear();
            }

            // Create card for each personnel
            int row = 0;
            int col = 0;
            int maxColumns = 3; // 3 columns grid layout

            for (Personnel personnel : personnelList) {
                // Create card
                VBox card = createPersonnelCard(personnel);

                // Add click handler for selection
                card.setOnMouseClicked(event -> {
                    selectedPersonnel = personnel;
                    highlightSelectedCard(card);
                });

                // Set constraints to prevent overlap
                GridPane.setHgrow(card, Priority.ALWAYS);
                GridPane.setVgrow(card, Priority.NEVER);
                GridPane.setFillWidth(card, true);

                // Add to grid
                if (personnelGrid != null) {
                    personnelGrid.add(card, col, row);
                }

                // Move to next position
                col++;
                if (col >= maxColumns) {
                    col = 0;
                    row++;
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading personnel data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Highlight the selected card
     */
    private void highlightSelectedCard(VBox card) {
        // Remove highlight from all cards
        if (personnelGrid != null) {
            personnelGrid.getChildren().forEach(node -> {
                if (node instanceof VBox) {
                    node.setStyle(node.getStyle().replace("-fx-border-color: #007bff;", "-fx-border-color: #dee2e6;"));
                }
            });
        }

        // Highlight selected card
        card.setStyle(card.getStyle().replace("-fx-border-color: #dee2e6;", "-fx-border-color: #007bff;"));
    }

    /**
     * Create a personnel card
     * @param personnel Personnel to display
     * @return VBox card containing personnel info
     */
    private VBox createPersonnelCard(Personnel personnel) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #dee2e6; " +
                        "-fx-border-width: 1.5px; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px;"
        );
        card.setMinWidth(240);
        card.setPrefHeight(180);

        // Top row: name + action icons
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(personnel.getFullName() == null ? "Unknown" : personnel.getFullName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setStyle("-fx-text-fill: #212529;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // action icons (no text)
        Button editIcon = new Button("✏");
        editIcon.setTooltip(new Tooltip("Modifier"));
        editIcon.setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");
        editIcon.setOnAction(evt -> {
            // open edit dialog for this personnel
            openEditDialog(personnel);
        });

        Button deleteIcon = new Button("🗑");
        deleteIcon.setTooltip(new Tooltip("Supprimer"));
        deleteIcon.setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");
        deleteIcon.setOnAction(evt -> {
            openDeleteConfirmation(personnel);
        });

        Button viewIcon = new Button("👁");
        viewIcon.setTooltip(new Tooltip("Voir détails"));
        viewIcon.setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");
        viewIcon.setOnAction(evt -> openDetailDialog(personnel));

        HBox actions = new HBox(6, viewIcon, editIcon, deleteIcon);
        actions.setAlignment(Pos.CENTER_RIGHT);

        topRow.getChildren().addAll(nameLabel, actions);

        // job title badge
        Label jobTitleLabel = new Label(personnel.getJobTitle() == null ? "N/A" : personnel.getJobTitle());
        jobTitleLabel.setPadding(new Insets(4,8,4,8));
        applyJobTitleBadge(jobTitleLabel, personnel.getJobTitle());

        Label ageLabel = new Label("👤 " + personnel.getAge() + " ans");
        Label phoneLabel = new Label("📞 " + (personnel.getPhone() == null ? "N/A" : personnel.getPhone()));
        Label emailLabel = new Label("✉️ " + (personnel.getEmail() == null ? "N/A" : personnel.getEmail()));

        VBox info = new VBox(6, jobTitleLabel, ageLabel, phoneLabel, emailLabel);
        info.setPadding(new Insets(6,0,0,0));

        card.getChildren().addAll(topRow, info);

        // click selects card (visual)
        card.setOnMouseClicked(evt -> highlightSelectedCard(card));

        return card;
    }

    // new helpers for card actions:
    private void openEditDialog(Personnel p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditPersonnelDialog.fxml"));
            Parent root = loader.load();
            ma.farm.controller.dialogs.AddEditPersonnelDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Personnel");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            if (personnelGrid != null && personnelGrid.getScene() != null) dialogStage.initOwner(personnelGrid.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            controller.setDialogStage(dialogStage);
            controller.setPersonnel(p);
            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                refreshData();
                updateAllStatistics();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDeleteConfirmation(Personnel p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer personnel");
        confirm.setContentText("Supprimer " + p.getFullName() + " ?");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                boolean ok = personnelDAO.deletePersonnel(p.getId());
                if (ok) {
                    refreshData();
                    updateAllStatistics();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR, "Suppression échouée.");
                    err.showAndWait();
                }
            }
        });
    }

    private void openDetailDialog(Personnel p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/PersonnelDetailDialog.fxml"));
            Parent root = loader.load();
            ma.farm.controller.dialogs.PersonnelDetailDialogController controller = loader.getController();

            Stage detailStage = new Stage();
            detailStage.setTitle("Détails: " + p.getFullName());
            detailStage.initModality(Modality.APPLICATION_MODAL);
            if (personnelGrid != null && personnelGrid.getScene() != null) detailStage.initOwner(personnelGrid.getScene().getWindow());
            detailStage.setScene(new Scene(root));
            controller.setDialogStage(detailStage);
            controller.setPersonnel(p);
            detailStage.showAndWait();

            // refresh after detail (in case of edit there)
            refreshData();
            updateAllStatistics();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update personnel statistics
     */
    private void updateStatistics() {
        try {
            // Get all personnel
            List<Personnel> allPersonnel = personnelDAO.getAllPersonnel();

            // Count total workers (job title = "Worker")
            long workersCount = allPersonnel.stream()
                    .filter(p -> "Worker".equalsIgnoreCase(p.getJobTitle()) || "farmhand".equalsIgnoreCase(p.getJobTitle()))
                    .count();

            // Count total trackers (job title = "Tracker")
            long trackersCount = allPersonnel.stream()
                    .filter(p -> "Tracker".equalsIgnoreCase(p.getJobTitle()) || "supervisor".equalsIgnoreCase(p.getJobTitle()))
                    .count();

            // Update totalWorkersLabel
            if (totalWorkersLabel != null) {
                totalWorkersLabel.setText(String.valueOf(workersCount));
            }

            // Update totalTrackersLabel
            if (totalTrackersLabel != null) {
                totalTrackersLabel.setText(String.valueOf(trackersCount));
            }
        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle add personnel button click
     * Opens dialog to add new worker/tracker
     */
    @FXML
    public void handleAddPersonnel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditPersonnelDialog.fxml"));
            Parent root = loader.load();
            ma.farm.controller.dialogs.AddEditPersonnelDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter Personnel");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            // owner
            if (personnelGrid != null && personnelGrid.getScene() != null) {
                dialogStage.initOwner(personnelGrid.getScene().getWindow());
            }
            dialogStage.setScene(new Scene(root));
            controller.setDialogStage(dialogStage);
            controller.setPersonnel(null); // add mode

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                refreshData();
                updateAllStatistics();
            }
        } catch (IOException e) {
            System.err.println("Error opening Add Personnel dialog: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le dialogue d'ajout.");
            alert.showAndWait();
        }
    }

    /**
     * Handle edit personnel button click
     * Opens dialog to edit selected personnel
     */
    @FXML
    public void handleEditPersonnel() {
        // Get selected personnel
        if (selectedPersonnel == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Personnel Selected");
            alert.setContentText("Please select a personnel card to edit.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditPersonnelDialog.fxml"));
            Parent root = loader.load();
            ma.farm.controller.dialogs.AddEditPersonnelDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Personnel");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            if (personnelGrid != null && personnelGrid.getScene() != null) {
                dialogStage.initOwner(personnelGrid.getScene().getWindow());
            }
            dialogStage.setScene(new Scene(root));
            controller.setDialogStage(dialogStage);
            controller.setPersonnel(selectedPersonnel);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                refreshData();
                updateAllStatistics();
            }

        } catch (IOException e) {
            System.err.println("Error opening Edit Personnel dialog: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le dialogue de modification.");
            alert.showAndWait();
        }
    }

    /**
     * Handle delete personnel button click
     * Deletes selected personnel
     */
    @FXML
    public void handleDeletePersonnel() {
        // Get selected personnel
        if (selectedPersonnel == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Personnel Selected");
            alert.setContentText("Please select a personnel card to delete.");
            alert.showAndWait();
            return;
        }

        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Personnel");
        confirmAlert.setContentText("Are you sure you want to delete this personnel?\n\n" +
                "Name: " + selectedPersonnel.getFullName() + "\n" +
                "Job Title: " + selectedPersonnel.getJobTitle());

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Delete from database
                    boolean success = personnelDAO.deletePersonnel(selectedPersonnel.getId());

                    if (success) {
                        // Clear selection
                        selectedPersonnel = null;

                        // Refresh personnel grid
                        refreshData();
                        updateAllStatistics();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText("Personnel Deleted");
                        alert.setContentText("The personnel has been successfully deleted.");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Failed to Delete Personnel");
                        alert.setContentText("Could not delete the personnel. Please try again.");
                        alert.showAndWait();
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting personnel: " + e.getMessage());
                    e.printStackTrace();

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Database Error");
                    alert.setContentText("An error occurred while deleting the personnel: " + e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }

    /**
     * Handle view personnel details button click
     * Shows detailed information about selected personnel
     */
    @FXML
    public void handleViewDetails() {
        // Get selected personnel
        if (selectedPersonnel == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Personnel Selected");
            alert.setContentText("Please select a personnel card to view details.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/PersonnelDetailDialog.fxml"));
            Parent root = loader.load();
            ma.farm.controller.dialogs.PersonnelDetailDialogController controller = loader.getController();

            Stage detailStage = new Stage();
            detailStage.setTitle("Personnel Details: " + selectedPersonnel.getFullName());
            detailStage.initModality(Modality.APPLICATION_MODAL);
            if (personnelGrid != null && personnelGrid.getScene() != null) {
                detailStage.initOwner(personnelGrid.getScene().getWindow());
            }
            detailStage.setScene(new Scene(root));

            controller.setDialogStage(detailStage);
            controller.setPersonnel(selectedPersonnel);

            detailStage.showAndWait();

            // After closing detail dialog refresh to pick up any edits
            refreshData();
            updateAllStatistics();

        } catch (IOException e) {
            System.err.println("Error opening details dialog: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le dialogue de détails.");
            alert.showAndWait();
        }
    }

    /**
     * Handle filter by job title
     * @param jobTitle Job title to filter by (All, Worker, Tracker)
     */
    @FXML
    public void handleFilterByJobTitle(String jobTitle) {
        try {
            // Map friendly UI names to DB jobTitle keys
            String key = null;
            if (jobTitle == null) jobTitle = "All";

            switch (jobTitle.toLowerCase()) {
                case "all":
                    currentFilter = "All";
                    break;
                case "veterinary":
                case "vétérinaires":
                    currentFilter = "Veterinary";
                    break;
                case "inventory":
                case "inventaire":
                    currentFilter = "Inventory";
                    break;
                case "supervisors":
                case "superviseurs":
                    currentFilter = "Supervisors";
                    break;
                case "farmhands":
                case "ouvriers":
                    currentFilter = "Farmhands";
                    break;
                default:
                    // If caller passes DB key already, try to normalize
                    if ("veterinary".equalsIgnoreCase(jobTitle) || "inventory_tracker".equalsIgnoreCase(jobTitle)
                            || "supervisor".equalsIgnoreCase(jobTitle) || "farmhand".equalsIgnoreCase(jobTitle)) {
                        // Set friendly filter to trigger correct DAO call in loadPersonnelData
                        if ("veterinary".equalsIgnoreCase(jobTitle)) currentFilter = "Veterinary";
                        else if ("inventory_tracker".equalsIgnoreCase(jobTitle)) currentFilter = "Inventory";
                        else if ("supervisor".equalsIgnoreCase(jobTitle)) currentFilter = "Supervisors";
                        else if ("farmhand".equalsIgnoreCase(jobTitle)) currentFilter = "Farmhands";
                    } else {
                        currentFilter = "All";
                    }
            }

            // Reload view using currentFilter
            loadPersonnelData();

        } catch (Exception e) {
            System.err.println("Error filtering personnel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Apply job title badge styling
     * @param label Label to style
     * @param jobTitle Job title (DB key or friendly)
     */
    private void applyJobTitleBadge(Label label, String jobTitle) {
        if (label == null || jobTitle == null) {
            return;
        }

        String key = jobTitle.toLowerCase();

        // Default styling reset
        label.setStyle("-fx-background-radius: 5px; -fx-padding: 5 10; -fx-font-size: 13px; -fx-font-weight: bold;");

        switch (key) {
            case "veterinary":
                label.setStyle(label.getStyle() + "-fx-background-color: #17a2b8; -fx-text-fill: white;");
                label.setText("Vétérinaire");
                break;
            case "inventory_tracker":
                label.setStyle(label.getStyle() + "-fx-background-color: #6f42c1; -fx-text-fill: white;");
                label.setText("Inventaire");
                break;
            case "supervisor":
                label.setStyle(label.getStyle() + "-fx-background-color: #007bff; -fx-text-fill: white;");
                label.setText("Superviseur");
                break;
            case "farmhand":
            case "worker":
                label.setStyle(label.getStyle() + "-fx-background-color: #6c757d; -fx-text-fill: white;");
                label.setText("Ouvrier");
                break;
            case "tracker":
                label.setStyle(label.getStyle() + "-fx-background-color: #007bff; -fx-text-fill: white;");
                label.setText("Tracker");
                break;
            default:
                label.setStyle(label.getStyle() + "-fx-background-color: #adb5bd; -fx-text-fill: white;");
                label.setText(jobTitle);
                break;
        }
    }

    /**
     * Update all statistics labels
     */
    private void updateAllStatistics() {
        try {
            // Total operations personnel
            int totalPersonnel = personnelDAO.getOperationsPersonnel().size();
            if (totalPersonnelLabel != null) {
                totalPersonnelLabel.setText(String.valueOf(totalPersonnel));
            }

            // Still update old labels for backward compatibility
            if (totalWorkersLabel != null) {
                totalWorkersLabel.setText(String.valueOf(totalPersonnel));
            }

            // Veterinary staff
            int veterinaryCount = personnelDAO.countByJobTitle("veterinary");
            if (totalVeterinaryLabel != null) {
                totalVeterinaryLabel.setText(String.valueOf(veterinaryCount));
            }

            // Inventory trackers
            int inventoryCount = personnelDAO.countByJobTitle("inventory_tracker");
            if (totalInventoryLabel != null) {
                totalInventoryLabel.setText(String.valueOf(inventoryCount));
            }

            // Supervisors
            int supervisorsCount = personnelDAO.countByJobTitle("supervisor");
            if (totalSupervisorsLabel != null) {
                totalSupervisorsLabel.setText(String.valueOf(supervisorsCount));
            }

            // Farmhands
            int farmhandsCount = personnelDAO.countByJobTitle("farmhand");
            if (totalFarmhandsLabel != null) {
                totalFarmhandsLabel.setText(String.valueOf(farmhandsCount));
            }

            // Still update old tracker label
            if (totalTrackersLabel != null) {
                totalTrackersLabel.setText(String.valueOf(supervisorsCount));
            }

        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle filter button clicks (called from FXML buttons with onAction="#handleFilterChange")
     * Reads the button's userData to determine which filter to apply.
     */
    @FXML
    public void handleFilterChange(ActionEvent event) {
        String filterType = "All";
        if (event != null && event.getSource() instanceof Button) {
            Object ud = ((Button) event.getSource()).getUserData();
            if (ud != null) filterType = ud.toString();
        }
        handleFilterByJobTitle(filterType);
    }

    /**
     * Refresh all data and statistics
     */
    @FXML
    public void refreshData() {
        loadPersonnelData();
        updateAllStatistics();
        selectedPersonnel = null;
    }
}
