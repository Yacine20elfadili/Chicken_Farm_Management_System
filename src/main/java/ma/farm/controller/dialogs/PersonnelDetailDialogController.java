package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ma.farm.dao.PersonnelDAO;
import ma.farm.model.Personnel;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * PersonnelDetailDialogController - Shows full personnel details
 *
 * Features:
 * - Display complete personnel information
 * - Show supervisor relationship (for workers)
 * - Show subordinates list (for supervisors)
 * - Edit button to open edit dialog
 * - Clickable supervisor/subordinate names
 */
public class PersonnelDetailDialogController {

    // FXML Components - Personal Info
    @FXML private Label nameLabel;
    @FXML private Label ageLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label jobTitleLabel;
    @FXML private Label hireDateLabel;
    @FXML private Label yearsOfServiceLabel;
    @FXML private Label salaryLabel;
    @FXML private Label shiftLabel;
    @FXML private Label addressLabel;
    @FXML private Label emergencyContactLabel;
    @FXML private Label statusLabel;

    // Relationship Section
    @FXML private VBox supervisorSection;
    @FXML private Label supervisorNameLabel;
    @FXML private Button viewSupervisorButton;

    @FXML private VBox subordinatesSection;
    @FXML private Label subordinateCountLabel;
    @FXML private ListView<String> subordinatesListView;

    // Buttons
    @FXML private Button editButton;
    @FXML private Button closeButton;

    // DAOs
    private PersonnelDAO personnelDAO;

    // State
    private Stage dialogStage;
    private Personnel personnel;
    private Personnel supervisor; // If worker has supervisor
    private List<Personnel> subordinates; // If supervisor has subordinates

    /**
     * Initialize method
     */
    @FXML
    public void initialize() {
        personnelDAO = new PersonnelDAO();
    }

    /**
     * Sets the dialog stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the personnel to display
     */
    public void setPersonnel(Personnel personnel) {
        this.personnel = personnel;
        displayPersonnelInfo();
        loadRelationships();
    }

    /**
     * Display personnel information
     */
    private void displayPersonnelInfo() {
        if (personnel == null) return;

        // Basic Info
        nameLabel.setText(personnel.getFullName());
        ageLabel.setText(personnel.getAge() + " ans");
        phoneLabel.setText(personnel.getPhone());
        emailLabel.setText(personnel.getEmail());
        jobTitleLabel.setText(formatJobTitle(personnel.getJobTitle()));

        // Employment Info
        if (personnel.getHireDate() != null) {
            hireDateLabel.setText(personnel.getHireDate().toString());
            yearsOfServiceLabel.setText(calculateYearsOfService(personnel.getHireDate()) + " ans");
        } else {
            hireDateLabel.setText("Non spécifié");
            yearsOfServiceLabel.setText("N/A");
        }

        salaryLabel.setText(String.format("%.2f MAD", personnel.getSalary()));
        shiftLabel.setText(formatShift(personnel.getShift()));

        // Additional Info
        addressLabel.setText(personnel.getAddress() != null ? personnel.getAddress() : "Non spécifié");
        emergencyContactLabel.setText(personnel.getEmergencyContact() != null ? personnel.getEmergencyContact() : "Non spécifié");

        // Status
        if (personnel.isActive()) {
            statusLabel.setText("Actif");
            statusLabel.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 5 10; -fx-background-radius: 5;");
        } else {
            statusLabel.setText("Inactif");
            statusLabel.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-padding: 5 10; -fx-background-radius: 5;");
        }
    }

    /**
     * Load and display relationships (supervisor or subordinates)
     */
    private void loadRelationships() {
        // Hide both sections initially
        supervisorSection.setVisible(false);
        supervisorSection.setManaged(false);
        subordinatesSection.setVisible(false);
        subordinatesSection.setManaged(false);

        // Check if personnel has a supervisor (is a worker)
        if (personnel.hasSupervisor()) {
            supervisor = personnelDAO.getPersonnelById(personnel.getSupervisorId());
            if (supervisor != null) {
                supervisorSection.setVisible(true);
                supervisorSection.setManaged(true);
                supervisorNameLabel.setText(supervisor.getFullName());

                // Setup click handler for supervisor button
                viewSupervisorButton.setOnAction(e -> handleViewSupervisor());
            }
        }

        // Check if personnel is a supervisor (has subordinates)
        if (personnel.isSupervisor()) {
            subordinates = personnelDAO.getPersonnelBySupervisorId(personnel.getId());
            if (!subordinates.isEmpty()) {
                subordinatesSection.setVisible(true);
                subordinatesSection.setManaged(true);
                subordinateCountLabel.setText(subordinates.size() + " subordonné(s)");

                // Populate subordinates list
                subordinatesListView.getItems().clear();
                for (Personnel subordinate : subordinates) {
                    subordinatesListView.getItems().add(subordinate.getFullName() + " - " + formatJobTitle(subordinate.getJobTitle()));
                }

                // Setup click handler for subordinates list
                subordinatesListView.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) { // Double-click
                        String selected = subordinatesListView.getSelectionModel().getSelectedItem();
                        if (selected != null) {
                            handleViewSubordinate(selected);
                        }
                    }
                });
            }
        }
    }

    /**
     * Handle view supervisor button click
     */
    private void handleViewSupervisor() {
        if (supervisor != null) {
            openPersonnelDetailDialog(supervisor);
        }
    }

    /**
     * Handle subordinate selection
     */
    private void handleViewSubordinate(String selectedItem) {
        // Extract name from "Name - Job Title"
        String name = selectedItem.split(" - ")[0];

        // Find subordinate by name
        Personnel subordinate = subordinates.stream()
                .filter(p -> p.getFullName().equals(name))
                .findFirst()
                .orElse(null);

        if (subordinate != null) {
            openPersonnelDetailDialog(subordinate);
        }
    }

    /**
     * Opens PersonnelDetailDialog for another personnel
     */
    private void openPersonnelDetailDialog(Personnel personnel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/PersonnelDetailDialog.fxml"));
            Parent root = loader.load();

            PersonnelDetailDialogController controller = loader.getController();

            Stage detailStage = new Stage();
            detailStage.setTitle("Détails: " + personnel.getFullName());
            detailStage.initModality(Modality.WINDOW_MODAL);
            detailStage.initOwner(dialogStage);
            detailStage.setScene(new Scene(root));

            controller.setDialogStage(detailStage);
            controller.setPersonnel(personnel);

            detailStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error opening detail dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle edit button click
     */
    @FXML
    public void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditPersonnelDialog.fxml"));
            Parent root = loader.load();

            AddEditPersonnelDialogController controller = loader.getController();

            Stage editStage = new Stage();
            editStage.setTitle("Modifier Personnel");
            editStage.initModality(Modality.WINDOW_MODAL);
            editStage.initOwner(dialogStage);
            editStage.setScene(new Scene(root));

            controller.setDialogStage(editStage);
            controller.setPersonnel(personnel);

            editStage.showAndWait();

            // Refresh if save was clicked
            if (controller.isSaveClicked()) {
                personnel = personnelDAO.getPersonnelById(personnel.getId());
                displayPersonnelInfo();
                loadRelationships();
            }

        } catch (IOException e) {
            System.err.println("Error opening edit dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle close button click
     */
    @FXML
    public void handleClose() {
        dialogStage.close();
    }

    /**
     * Format job title for display
     */
    private String formatJobTitle(String jobTitle) {
        if (jobTitle == null) return "Non spécifié";

        switch (jobTitle.toLowerCase()) {
            case "veterinary": return "Vétérinaire";
            case "inventory_tracker": return "Gestionnaire d'Inventaire";
            case "supervisor": return "Superviseur";
            case "farmhand": return "Ouvrier Agricole";
            default: return jobTitle;
        }
    }

    /**
     * Format shift for display
     */
    private String formatShift(String shift) {
        if (shift == null) return "Non spécifié";

        switch (shift.toLowerCase()) {
            case "morning": return "Matin";
            case "evening": return "Soir";
            default: return shift;
        }
    }

    /**
     * Calculate years of service from hire date
     */
    private int calculateYearsOfService(LocalDate hireDate) {
        if (hireDate == null) return 0;
        return Period.between(hireDate, LocalDate.now()).getYears();
    }
}