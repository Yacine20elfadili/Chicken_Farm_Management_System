package ma.farm.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ma.farm.dao.PersonnelDAO;
import ma.farm.model.AdminPosition;
import ma.farm.model.Personnel;
import ma.farm.model.PersonnelType;
import ma.farm.util.IdentityCardGenerator;

/**
 * PersonnelController - Controls the Personnel view
 *
 * New Structure:
 * - 4 Stats Cards: Admin Staff, Veterinary (1/N), Inventory (1/N), Farmhand (1/N)
 * - Administration Section: Farm Owner, Cashier, Admin Staff cards
 * - Farm Section: Supervisors and their subordinates
 *
 * Card Actions:
 * - Admin cards: view/edit/drop (drop resets to missing)
 * - Farm cards: view/edit/delete (supervisor delete blocked if has subordinates)
 */
public class PersonnelController {

    // FXML Components - Stats Cards
    @FXML private Label adminStaffCountLabel;
    @FXML private Label veterinaryCountLabel;
    @FXML private Label inventoryCountLabel;
    @FXML private Label farmhandCountLabel;

    // FXML Components - Content Areas
    @FXML private VBox administrationSection;
    @FXML private FlowPane adminCardsPane;
    @FXML private VBox farmSection;
    @FXML private VBox veterinarySection;
    @FXML private FlowPane veterinaryCardsPane;
    @FXML private VBox inventorySection;
    @FXML private FlowPane inventoryCardsPane;
    @FXML private VBox farmhandSection;
    @FXML private FlowPane farmhandCardsPane;

    // Legacy support - GridPane if FlowPane not available
    @FXML private GridPane personnelGrid;

    // DAO
    private PersonnelDAO personnelDAO;

    // Track selected personnel
    private Personnel selectedPersonnel;

    @FXML
    public void initialize() {
        System.out.println("=== PersonnelController: Initializing (New Structure) ===");
        personnelDAO = new PersonnelDAO();

        try {
            // Load all data
            refreshData();
            System.out.println("=== PersonnelController: Initialization Complete ===");
        } catch (Exception e) {
            System.err.println("ERROR in personnel initialization: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Erreur de Base de Données", "Impossible de charger les données du personnel.", e.getMessage());
        }
    }

    /**
     * Refresh all data - stats and cards
     */
    @FXML
    public void refreshData() {
        updateStatistics();
        loadAdministrationCards();
        loadFarmCards();
        selectedPersonnel = null;
    }

    // ============================================================
    // STATISTICS UPDATE
    // ============================================================

    /**
     * Update all 4 statistics cards
     */
    private void updateStatistics() {
        // Admin Staff count: Owner + Cashier + Admin Staff
        int adminCount = personnelDAO.getAdminCount();
        if (adminStaffCountLabel != null) {
            adminStaffCountLabel.setText(String.valueOf(adminCount));
        }

        // Veterinary: 1/N format (1 supervisor, N total)
        updateFarmStatCard(veterinaryCountLabel, "veterinary_supervisor", "veterinary_subordinate");

        // Inventory: 1/N format
        updateFarmStatCard(inventoryCountLabel, "inventory_supervisor", "inventory_subordinate");

        // Farmhand: 1/N format
        updateFarmStatCard(farmhandCountLabel, "farmhand_supervisor", "farmhand_subordinate");
    }

    /**
     * Update farm stat card with 1/N format
     */
    private void updateFarmStatCard(Label label, String supervisorType, String subordinateType) {
        if (label == null) return;

        boolean hasSupervisor = personnelDAO.existsByJobTitle(supervisorType);
        int subordinateCount = personnelDAO.countByJobTitle(subordinateType);
        int total = (hasSupervisor ? 1 : 0) + subordinateCount;

        String supervisorIndicator = hasSupervisor ? "1" : "0";
        label.setText(supervisorIndicator + "/" + total);
    }

    // ============================================================
    // ADMINISTRATION SECTION
    // ============================================================

    /**
     * Load administration cards: Farm Owner, Cashier, Admin Staff
     */
    private void loadAdministrationCards() {
        // Use legacy grid if FlowPane not available
        if (adminCardsPane == null && personnelGrid != null) {
            loadAdministrationCardsToGrid();
            return;
        }

        if (adminCardsPane == null) {
            System.err.println("WARNING: adminCardsPane is null, cannot load admin cards");
            return;
        }

        adminCardsPane.getChildren().clear();

        // 1. Farm Owner Card
        Personnel farmOwner = personnelDAO.getFarmOwner();
        VBox ownerCard = createAdminCard(farmOwner, PersonnelType.FARM_OWNER, "Propriétaire");
        adminCardsPane.getChildren().add(ownerCard);

        // 2. Cashier Card
        Personnel cashier = personnelDAO.getCashier();
        VBox cashierCard = createAdminCard(cashier, PersonnelType.CASHIER, "Caissier");
        adminCardsPane.getChildren().add(cashierCard);

        // 3. Admin Staff Cards
        List<Personnel> adminStaff = personnelDAO.getAdminStaff();

        if (adminStaff.isEmpty()) {
            // Show default empty admin staff card
            VBox defaultStaffCard = createMissingCard("Personnel Admin", "Cliquez sur + Ajouter pour créer");
            adminCardsPane.getChildren().add(defaultStaffCard);
        } else {
            for (Personnel staff : adminStaff) {
                VBox staffCard = createAdminStaffCard(staff);
                adminCardsPane.getChildren().add(staffCard);
            }
        }


    }

    /**
     * Load administration cards to legacy GridPane
     */
    private void loadAdministrationCardsToGrid() {
        personnelGrid.getChildren().clear();
        int col = 0;
        int row = 0;
        int maxColumns = 3;

        // Farm Owner
        Personnel farmOwner = personnelDAO.getFarmOwner();
        VBox ownerCard = createAdminCard(farmOwner, PersonnelType.FARM_OWNER, "Propriétaire");
        personnelGrid.add(ownerCard, col++, row);

        // Cashier
        Personnel cashier = personnelDAO.getCashier();
        VBox cashierCard = createAdminCard(cashier, PersonnelType.CASHIER, "Caissier");
        personnelGrid.add(cashierCard, col++, row);

        // Admin Staff
        List<Personnel> adminStaff = personnelDAO.getAdminStaff();
        for (Personnel staff : adminStaff) {
            if (col >= maxColumns) {
                col = 0;
                row++;
            }
            VBox staffCard = createAdminStaffCard(staff);
            personnelGrid.add(staffCard, col++, row);
        }

        // Load farm personnel
        loadFarmCardsToGrid(col, row, maxColumns);
    }

    /**
     * Create admin card (for Owner, Cashier)
     */
    private VBox createAdminCard(Personnel personnel, PersonnelType type, String roleLabel) {
        if (personnel == null) {
            return createMissingCard(roleLabel, "Non assigné");
        }

        VBox card = createBaseCard();
        card.setStyle(card.getStyle() + "-fx-border-color: #007bff;");

        // Header with role badge
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label roleBadge = new Label(roleLabel);
        roleBadge.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 4 10; " +
                          "-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label nameLabel = new Label(personnel.getFullName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        nameLabel.setStyle("-fx-text-fill: #212529;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        header.getChildren().addAll(roleBadge, nameLabel);

        // Info
        VBox info = createInfoSection(personnel);

        // Actions: View, Edit, Drop
        HBox actions = createAdminActions(personnel);

        card.getChildren().addAll(header, info, actions);
        return card;
    }

    /**
     * Create admin staff card (shows positions)
     */
    private VBox createAdminStaffCard(Personnel personnel) {
        VBox card = createBaseCard();
        card.setStyle(card.getStyle() + "-fx-border-color: #28a745;");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label roleBadge = new Label("Admin Staff");
        roleBadge.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 4 10; " +
                          "-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label nameLabel = new Label(personnel.getFullName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        header.getChildren().addAll(roleBadge, nameLabel);

        // Positions
        VBox positionsBox = new VBox(3);
        positionsBox.setPadding(new Insets(5, 0, 5, 0));

        Label posLabel = new Label("Positions:");
        posLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d; -fx-font-weight: bold;");
        positionsBox.getChildren().add(posLabel);

        AdminPosition[] positions = personnel.getAdminPositions();
        if (positions.length > 0) {
            FlowPane posFlow = new FlowPane(5, 3);
            for (AdminPosition pos : positions) {
                Label posBadge = new Label(pos.getDisplayNameFr());
                posBadge.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #495057; -fx-padding: 2 6; " +
                                 "-fx-background-radius: 8; -fx-font-size: 10px;");
                posFlow.getChildren().add(posBadge);
            }
            positionsBox.getChildren().add(posFlow);
        } else {
            Label noPos = new Label("Aucune position");
            noPos.setStyle("-fx-font-size: 10px; -fx-text-fill: #adb5bd;");
            positionsBox.getChildren().add(noPos);
        }

        // Basic info
        VBox info = createInfoSection(personnel);

        // Actions
        HBox actions = createAdminActions(personnel);

        card.getChildren().addAll(header, positionsBox, info, actions);
        return card;
    }

    /**
     * Create missing card placeholder
     */
    private VBox createMissingCard(String title, String subtitle) {
        VBox card = createBaseCard();
        card.setStyle(card.getStyle() + "-fx-border-color: #dee2e6; -fx-border-style: dashed;");
        card.setAlignment(Pos.CENTER);

        Label icon = new Label("👤");
        icon.setStyle("-fx-font-size: 32px; -fx-text-fill: #adb5bd;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #6c757d;");

        Label subLabel = new Label(subtitle);
        subLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #adb5bd;");

        card.getChildren().addAll(icon, titleLabel, subLabel);
        return card;
    }

    /**
     * Create admin action buttons (View, Edit, Drop)
     */
    private HBox createAdminActions(Personnel personnel) {
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(8, 0, 0, 0));

        Button viewBtn = createActionButton("👁", "Voir", "#17a2b8");
        viewBtn.setOnAction(e -> openDetailDialog(personnel));

        Button editBtn = createActionButton("✏", "Modifier", "#ffc107");
        editBtn.setOnAction(e -> openEditDialog(personnel));

        Button dropBtn = createActionButton("↩", "Retirer", "#dc3545");
        dropBtn.setOnAction(e -> handleDropPersonnel(personnel));

        actions.getChildren().addAll(viewBtn, editBtn, dropBtn);
        return actions;
    }

    // ============================================================
    // FARM SECTION
    // ============================================================

    /**
     * Load farm cards: Supervisors and Subordinates
     */
    private void loadFarmCards() {
        if (veterinaryCardsPane == null && inventoryCardsPane == null && farmhandCardsPane == null) {
            // Using legacy grid, handled in loadAdministrationCardsToGrid
            return;
        }

        loadFarmCategoryCards(veterinaryCardsPane, "veterinary_supervisor", "veterinary_subordinate",
                             "Vétérinaire", "#17a2b8");
        loadFarmCategoryCards(inventoryCardsPane, "inventory_supervisor", "inventory_subordinate",
                             "Inventaire", "#6f42c1");
        loadFarmCategoryCards(farmhandCardsPane, "farmhand_supervisor", "farmhand_subordinate",
                             "Ouvrier Agricole", "#fd7e14");
    }

    /**
     * Load farm category cards (supervisor + subordinates)
     */
    private void loadFarmCategoryCards(FlowPane pane, String supervisorType, String subordinateType,
                                       String categoryName, String color) {
        if (pane == null) return;
        pane.getChildren().clear();

        // Get supervisor
        Personnel supervisor = personnelDAO.getSupervisorByType(supervisorType);

        if (supervisor == null) {
            // Show missing supervisor card
            VBox missingCard = createMissingCard("Superviseur " + categoryName, "Aucun superviseur");
            pane.getChildren().add(missingCard);
        } else {
            // Show supervisor card
            VBox supervisorCard = createFarmCard(supervisor, categoryName + " (Superviseur)", color, true);
            pane.getChildren().add(supervisorCard);

            // Show subordinates
            List<Personnel> subordinates = personnelDAO.getSubordinatesBySupervisorId(supervisor.getId());
            for (Personnel sub : subordinates) {
                VBox subCard = createFarmCard(sub, categoryName, color, false);
                pane.getChildren().add(subCard);
            }
        }


    }

    /**
     * Load farm cards to legacy grid
     */
    private void loadFarmCardsToGrid(int startCol, int startRow, int maxColumns) {
        int col = startCol;
        int row = startRow;

        // Add separator
        if (col > 0) {
            col = 0;
            row++;
        }
        Label separator = new Label("--- Personnel de Ferme ---");
        separator.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #343a40;");
        personnelGrid.add(separator, 0, row++, maxColumns, 1);

        // Veterinary
        Personnel vetSupervisor = personnelDAO.getSupervisorByType("veterinary_supervisor");
        if (vetSupervisor != null) {
            VBox vetCard = createFarmCard(vetSupervisor, "Vétérinaire (Superviseur)", "#17a2b8", true);
            personnelGrid.add(vetCard, col++, row);
            if (col >= maxColumns) { col = 0; row++; }

            List<Personnel> vetSubs = personnelDAO.getSubordinatesBySupervisorId(vetSupervisor.getId());
            for (Personnel sub : vetSubs) {
                VBox subCard = createFarmCard(sub, "Vétérinaire", "#17a2b8", false);
                personnelGrid.add(subCard, col++, row);
                if (col >= maxColumns) { col = 0; row++; }
            }
        }

        // Inventory
        Personnel invSupervisor = personnelDAO.getSupervisorByType("inventory_supervisor");
        if (invSupervisor != null) {
            VBox invCard = createFarmCard(invSupervisor, "Inventaire (Superviseur)", "#6f42c1", true);
            personnelGrid.add(invCard, col++, row);
            if (col >= maxColumns) { col = 0; row++; }

            List<Personnel> invSubs = personnelDAO.getSubordinatesBySupervisorId(invSupervisor.getId());
            for (Personnel sub : invSubs) {
                VBox subCard = createFarmCard(sub, "Inventaire", "#6f42c1", false);
                personnelGrid.add(subCard, col++, row);
                if (col >= maxColumns) { col = 0; row++; }
            }
        }

        // Farmhand
        Personnel farmSupervisor = personnelDAO.getSupervisorByType("farmhand_supervisor");
        if (farmSupervisor != null) {
            VBox farmCard = createFarmCard(farmSupervisor, "Ouvrier (Superviseur)", "#fd7e14", true);
            personnelGrid.add(farmCard, col++, row);
            if (col >= maxColumns) { col = 0; row++; }

            List<Personnel> farmSubs = personnelDAO.getSubordinatesBySupervisorId(farmSupervisor.getId());
            for (Personnel sub : farmSubs) {
                VBox subCard = createFarmCard(sub, "Ouvrier", "#fd7e14", false);
                personnelGrid.add(subCard, col++, row);
                if (col >= maxColumns) { col = 0; row++; }
            }
        }
    }

    /**
     * Create farm personnel card
     */
    private VBox createFarmCard(Personnel personnel, String roleLabel, String color, boolean isSupervisor) {
        VBox card = createBaseCard();
        card.setStyle(card.getStyle() + "-fx-border-color: " + color + ";");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label roleBadge = new Label(roleLabel);
        roleBadge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 4 10; " +
                          "-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        if (isSupervisor) {
            Label supervisorIcon = new Label("⭐");
            supervisorIcon.setStyle("-fx-font-size: 14px;");
            header.getChildren().add(supervisorIcon);
        }

        header.getChildren().add(roleBadge);

        // Name
        Label nameLabel = new Label(personnel.getFullName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        nameLabel.setStyle("-fx-text-fill: #212529;");

        // Info
        VBox info = createInfoSection(personnel);

        // Subordinate count for supervisors
        if (isSupervisor) {
            int subCount = personnelDAO.getSubordinateCount(personnel.getId());
            Label subLabel = new Label("👥 " + subCount + " subordonné(s)");
            subLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + color + "; -fx-font-weight: bold;");
            info.getChildren().add(subLabel);
        }

        // Actions: View, Edit, Delete
        HBox actions = createFarmActions(personnel, isSupervisor);

        card.getChildren().addAll(header, nameLabel, info, actions);
        return card;
    }

    /**
     * Create farm action buttons (View, Edit, Delete)
     */
    private HBox createFarmActions(Personnel personnel, boolean isSupervisor) {
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(8, 0, 0, 0));

        Button viewBtn = createActionButton("👁", "Voir", "#17a2b8");
        viewBtn.setOnAction(e -> openDetailDialog(personnel));

        Button editBtn = createActionButton("✏", "Modifier", "#ffc107");
        editBtn.setOnAction(e -> openEditDialog(personnel));

        Button deleteBtn = createActionButton("🗑", "Supprimer", "#dc3545");
        deleteBtn.setOnAction(e -> handleDeletePersonnel(personnel, isSupervisor));

        actions.getChildren().addAll(viewBtn, editBtn, deleteBtn);
        return actions;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Create base card VBox
     */
    private VBox createBaseCard() {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(12));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        card.setMinWidth(240);
        card.setMaxWidth(280);
        card.setPrefHeight(Region.USE_COMPUTED_SIZE);
        return card;
    }

    /**
     * Create info section for card
     */
    private VBox createInfoSection(Personnel personnel) {
        VBox info = new VBox(4);
        info.setPadding(new Insets(5, 0, 0, 0));

        Label ageLabel = new Label("👤 " + personnel.getAge() + " ans");
        ageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057;");

        Label phoneLabel = new Label("📞 " + (personnel.getPhone() != null ? personnel.getPhone() : "N/A"));
        phoneLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057;");

        Label emailLabel = new Label("✉️ " + (personnel.getEmail() != null ? personnel.getEmail() : "N/A"));
        emailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        emailLabel.setWrapText(true);

        info.getChildren().addAll(ageLabel, phoneLabel, emailLabel);
        return info;
    }

    /**
     * Create action button
     */
    private Button createActionButton(String icon, String tooltip, String color) {
        Button btn = new Button(icon);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + color + "22; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 5;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-cursor: hand;"));
        return btn;
    }

    // ============================================================
    // ACTION HANDLERS
    // ============================================================

    /**
     * Handle add personnel button (main button)
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
            } else if (adminCardsPane != null && adminCardsPane.getScene() != null) {
                dialogStage.initOwner(adminCardsPane.getScene().getWindow());
            }
            dialogStage.setScene(new Scene(root));
            controller.setDialogStage(dialogStage);
            controller.setPersonnel(null);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                refreshData();
                showSuccessAlert("Succès", "Personnel ajouté avec succès.");
            }
        } catch (IOException e) {
            System.err.println("Error opening Add Personnel dialog: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir le dialogue d'ajout.", e.getMessage());
        }
    }

    /**
     * Generate an identity card image for a selected personnel
     */
    @FXML
    public void generateCard() {
        List<Personnel> all = personnelDAO.getAllPersonnel();
        if (all.isEmpty()) {
            showErrorAlert("Erreur", "Aucun personnel", "La liste du personnel est vide.");
            return;
        }

        List<String> names = all.stream().map(Personnel::getFullName).collect(Collectors.toList());
        ChoiceDialog<String> dialog = new ChoiceDialog<>(names.get(0), names);
        dialog.setTitle("Générer carte d'identité");
        dialog.setHeaderText("Sélectionner le personnel");
        dialog.setContentText("Personnel:");

        Optional<String> selected = dialog.showAndWait();
        if (!selected.isPresent()) return;

        String chosen = selected.get();
        Personnel person = all.stream().filter(p -> chosen.equals(p.getFullName())).findFirst().orElse(null);
        if (person == null) {
            showErrorAlert("Erreur", "Personnel introuvable", "Impossible de trouver le personnel sélectionné.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(person.getFullName().replaceAll("\\s+","_") + "_id.png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));

        File dest;
        if (personnelGrid != null && personnelGrid.getScene() != null) {
            dest = fileChooser.showSaveDialog(personnelGrid.getScene().getWindow());
        } else if (adminCardsPane != null && adminCardsPane.getScene() != null) {
            dest = fileChooser.showSaveDialog(adminCardsPane.getScene().getWindow());
        } else {
            dest = fileChooser.showSaveDialog(null);
        }
        if (dest == null) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                IdentityCardGenerator gen = new IdentityCardGenerator();
                String barcode = "EMP-" + person.getId();
                gen.saveAsPng(person.getFullName(), person.getJobTitle() != null ? person.getJobTitle() : "", barcode, Paths.get(dest.toURI()));
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> showSuccessAlert("Succès", "Carte générée: " + dest.getAbsolutePath())));
        task.setOnFailed(e -> Platform.runLater(() -> showErrorAlertWithStack("Erreur", "Échec de génération", task.getException())));

        new Thread(task).start();
    }

    private void showErrorAlertWithStack(String title, String header, Throwable t) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);

        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        String exceptionText = sw.toString();

        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane content = new GridPane();
        content.setMaxWidth(Double.MAX_VALUE);
        content.add(new Label("Exception stacktrace:"), 0, 0);
        content.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(content);
        alert.showAndWait();
    }

    /**
     * Handle add admin staff
     */
    private void handleAddAdminStaff() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditPersonnelDialog.fxml"));
            Parent root = loader.load();
            ma.farm.controller.dialogs.AddEditPersonnelDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter Personnel Administratif");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            controller.setDialogStage(dialogStage);
            controller.setPersonnel(null);
            controller.setPreselectedDepartment("administration");
            controller.setPreselectedJobTitle("admin_staff");

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                refreshData();
                showSuccessAlert("Succès", "Personnel administratif ajouté avec succès.");
            }
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir le dialogue.", e.getMessage());
        }
    }

    /**
     * Handle add farm personnel
     */
    private void handleAddFarmPersonnel(String jobTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditPersonnelDialog.fxml"));
            Parent root = loader.load();
            ma.farm.controller.dialogs.AddEditPersonnelDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter Personnel de Ferme");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            controller.setDialogStage(dialogStage);
            controller.setPersonnel(null);
            controller.setPreselectedDepartment("farm");
            controller.setPreselectedJobTitle(jobTitle);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                refreshData();
                showSuccessAlert("Succès", "Personnel de ferme ajouté avec succès.");
            }
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir le dialogue.", e.getMessage());
        }
    }

    /**
     * Open detail dialog
     */
    private void openDetailDialog(Personnel personnel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/PersonnelDetailDialog.fxml"));
            Parent root = loader.load();
            ma.farm.controller.dialogs.PersonnelDetailDialogController controller = loader.getController();

            Stage detailStage = new Stage();
            detailStage.setTitle("Détails: " + personnel.getFullName());
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.setScene(new Scene(root));
            controller.setDialogStage(detailStage);
            controller.setPersonnel(personnel);

            detailStage.showAndWait();
            refreshData();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir les détails.", e.getMessage());
        }
    }

    /**
     * Open edit dialog
     */
    private void openEditDialog(Personnel personnel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditPersonnelDialog.fxml"));
            Parent root = loader.load();
            ma.farm.controller.dialogs.AddEditPersonnelDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Personnel");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            controller.setDialogStage(dialogStage);
            controller.setPersonnel(personnel);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                refreshData();
                showSuccessAlert("Succès", "Personnel modifié avec succès.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir le dialogue de modification.", e.getMessage());
        }
    }

    /**
     * Handle drop personnel (Admin - resets to missing)
     */
    private void handleDropPersonnel(Personnel personnel) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer le retrait");
        confirm.setHeaderText("Retirer " + personnel.getFullName() + " ?");
        confirm.setContentText("Cette action supprimera ce personnel. Le poste sera marqué comme vacant.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = personnelDAO.dropPersonnel(personnel.getId());
                if (success) {
                    refreshData();
                    showSuccessAlert("Succès", "Personnel retiré avec succès.");
                } else {
                    showErrorAlert("Erreur", "Impossible de retirer le personnel.", "Veuillez réessayer.");
                }
            }
        });
    }

    /**
     * Handle delete personnel (Farm)
     */
    private void handleDeletePersonnel(Personnel personnel, boolean isSupervisor) {
        // Check if supervisor has subordinates
        if (isSupervisor && personnelDAO.hasSubordinates(personnel.getId())) {
            showErrorAlert("Suppression impossible",
                          "Ce superviseur a des subordonnés.",
                          "Veuillez d'abord supprimer ou réassigner tous les subordonnés avant de supprimer ce superviseur.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer " + personnel.getFullName() + " ?");
        confirm.setContentText("Cette action est irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = personnelDAO.deletePersonnel(personnel.getId());
                if (success) {
                    refreshData();
                    showSuccessAlert("Succès", "Personnel supprimé avec succès.");
                } else {
                    showErrorAlert("Erreur", "Impossible de supprimer le personnel.", "Veuillez réessayer.");
                }
            }
        });
    }

    // ============================================================
    // ALERT HELPERS
    // ============================================================

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
