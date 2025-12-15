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
    @FXML private GridPane personnelGrid;
    @FXML private Label totalWorkersLabel;
    @FXML private Label totalTrackersLabel;
    @FXML private Label totalPersonnelLabel;
    @FXML private Label totalVeterinaryLabel;
    @FXML private Label totalInventoryLabel;
    @FXML private Label totalSupervisorsLabel;
    @FXML private Label totalFarmhandsLabel;

    // DAO
    private PersonnelDAO personnelDAO;

    // Track selected personnel for edit/delete operations
    private Personnel selectedPersonnel;

    // In the initialize() method of PersonnelController.java, add:
    @FXML
    public void initialize() {
        System.out.println("=== PersonnelController: Initializing ===");
        personnelDAO = new PersonnelDAO();

        if (personnelGrid != null) {
            personnelGrid.setHgap(25);
            personnelGrid.setVgap(25);
        }

        try {
            List<Personnel> testList = personnelDAO.getOperationsPersonnel();
            System.out.println("DEBUG: Initial operations personnel count: " + testList.size());

            if (testList.isEmpty()) {
                System.out.println("WARNING: No personnel data found. Database may need initialization.");
                System.out.println("DEBUG: Testing job title lookup...");
                int vetId = personnelDAO.getJobTitleId("veterinary");
                System.out.println("DEBUG: veterinary jobTitle ID: " + vetId);
            }

        } catch (Exception e) {
            System.err.println("ERROR in personnel initialization: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Cannot Load Personnel Data");
            alert.setContentText("There may be a database issue. Please contact administrator.\nError: " + e.getMessage());
            alert.showAndWait();
            return;
        }

        // Load all personnel (no filter)
        loadAllPersonnel();
        updateAllStatistics();

        System.out.println("=== PersonnelController: Initialization Complete ===");
    }

    /**
     * Load and display all personnel (no filter)
     */
    private void loadAllPersonnel() {
        try {
            // Get all operations personnel
            List<Personnel> personnelList = personnelDAO.getOperationsPersonnel();

            if (personnelGrid != null) {
                personnelGrid.getChildren().clear();
            }

            int row = 0;
            int col = 0;
            int maxColumns = 3;

            for (Personnel personnel : personnelList) {
                VBox card = createPersonnelCard(personnel);

                card.setOnMouseClicked(event -> {
                    selectedPersonnel = personnel;
                    highlightSelectedCard(card);
                });

                GridPane.setHgrow(card, Priority.ALWAYS);
                GridPane.setVgrow(card, Priority.NEVER);
                GridPane.setFillWidth(card, true);

                if (personnelGrid != null) {
                    personnelGrid.add(card, col, row);
                }

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
        if (personnelGrid != null) {
            personnelGrid.getChildren().forEach(node -> {
                if (node instanceof VBox) {
                    node.setStyle(node.getStyle().replace("-fx-border-color: #007bff;", "-fx-border-color: #dee2e6;"));
                }
            });
        }
        card.setStyle(card.getStyle().replace("-fx-border-color: #dee2e6;", "-fx-border-color: #007bff;"));
    }

    /**
     * Create a personnel card
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

        Button viewIcon = new Button("👁");
        viewIcon.setTooltip(new Tooltip("Voir détails"));
        viewIcon.setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");
        viewIcon.setOnAction(evt -> openDetailDialog(personnel));

        Button editIcon = new Button("✏");
        editIcon.setTooltip(new Tooltip("Modifier"));
        editIcon.setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");
        editIcon.setOnAction(evt -> openEditDialog(personnel));

        Button deleteIcon = new Button("🗑");
        deleteIcon.setTooltip(new Tooltip("Supprimer"));
        deleteIcon.setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");
        deleteIcon.setOnAction(evt -> openDeleteConfirmation(personnel));

        HBox actions = new HBox(6, viewIcon, editIcon, deleteIcon);
        actions.setAlignment(Pos.CENTER_RIGHT);

        topRow.getChildren().addAll(nameLabel, actions);

        Label jobTitleLabel = new Label(personnel.getJobTitle() == null ? "N/A" : personnel.getJobTitle());
        jobTitleLabel.setPadding(new Insets(4,8,4,8));
        applyJobTitleBadge(jobTitleLabel, personnel.getJobTitle());

        Label ageLabel = new Label("👤 " + personnel.getAge() + " ans");
        Label phoneLabel = new Label("📞 " + (personnel.getPhone() == null ? "N/A" : personnel.getPhone()));
        Label emailLabel = new Label("✉️ " + (personnel.getEmail() == null ? "N/A" : personnel.getEmail()));

        VBox info = new VBox(6, jobTitleLabel, ageLabel, phoneLabel, emailLabel);
        info.setPadding(new Insets(6,0,0,0));

        card.getChildren().addAll(topRow, info);
        card.setOnMouseClicked(evt -> highlightSelectedCard(card));

        return card;
    }

    /**
     * Open detail dialog
     */
    private void openDetailDialog(Personnel p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/PersonnelDetailDialog.fxml"));
            Parent root = loader.load();
            ma.farm.controller.dialogs.PersonnelDetailDialogController controller = loader.getController();

            Stage detailStage = new Stage();
            detailStage.setTitle("Détails: " + p.getFullName());
            detailStage.initModality(Modality.APPLICATION_MODAL);
            if (personnelGrid != null && personnelGrid.getScene() != null) {
                detailStage.initOwner(personnelGrid.getScene().getWindow());
            }
            detailStage.setScene(new Scene(root));
            controller.setDialogStage(detailStage);
            controller.setPersonnel(p);
            detailStage.showAndWait();

            refreshData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Open edit dialog
     */
    private void openEditDialog(Personnel p) {
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

    /**
     * Open delete confirmation
     */
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

    /**
     * Apply job title badge
     */
    private void applyJobTitleBadge(Label label, String jobTitle) {
        if (label == null || jobTitle == null) return;

        String key = jobTitle.toLowerCase();
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
            default:
                label.setStyle(label.getStyle() + "-fx-background-color: #adb5bd; -fx-text-fill: white;");
                label.setText(jobTitle);
                break;
        }
    }

    /**
     * Update all statistics
     */
    private void updateAllStatistics() {
        try {
            int totalPersonnel = personnelDAO.getOperationsPersonnel().size();
            if (totalPersonnelLabel != null) {
                totalPersonnelLabel.setText(String.valueOf(totalPersonnel));
            }
            if (totalWorkersLabel != null) {
                totalWorkersLabel.setText(String.valueOf(totalPersonnel));
            }

            int veterinaryCount = personnelDAO.countByJobTitle("veterinary");
            if (totalVeterinaryLabel != null) {
                totalVeterinaryLabel.setText(String.valueOf(veterinaryCount));
            }

            int inventoryCount = personnelDAO.countByJobTitle("inventory_tracker");
            if (totalInventoryLabel != null) {
                totalInventoryLabel.setText(String.valueOf(inventoryCount));
            }

            int supervisorsCount = personnelDAO.countByJobTitle("supervisor");
            if (totalSupervisorsLabel != null) {
                totalSupervisorsLabel.setText(String.valueOf(supervisorsCount));
            }

            int farmhandsCount = personnelDAO.countByJobTitle("farmhand");
            if (totalFarmhandsLabel != null) {
                totalFarmhandsLabel.setText(String.valueOf(farmhandsCount));
            }

            if (totalTrackersLabel != null) {
                totalTrackersLabel.setText(String.valueOf(supervisorsCount));
            }

        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle add personnel button
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
            if (personnelGrid != null && personnelGrid.getScene() != null) {
                dialogStage.initOwner(personnelGrid.getScene().getWindow());
            }
            dialogStage.setScene(new Scene(root));
            controller.setDialogStage(dialogStage);
            controller.setPersonnel(null);

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
     * Refresh data
     */
    @FXML
    public void refreshData() {
        loadAllPersonnel();
        updateAllStatistics();
        selectedPersonnel = null;
    }
}
