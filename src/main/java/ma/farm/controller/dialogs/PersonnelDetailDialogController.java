package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ma.farm.dao.PersonnelDAO;
import ma.farm.model.AdminPosition;
import ma.farm.model.Personnel;
import ma.farm.model.PersonnelType;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * PersonnelDetailDialogController - Shows full personnel details
 *
 * Updated for new structure:
 * - Display admin positions for admin_staff
 * - Show department (administration/farm)
 * - Handle supervisor/subordinate relationships
 * - Updated job title formatting
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
    @FXML private Label departmentLabel;

    // Positions Section (Admin Staff only)
    @FXML private VBox positionsSection;
    @FXML private FlowPane positionsPane;

    // Relationship Section - Supervisor
    @FXML private VBox supervisorSection;
    @FXML private Label supervisorNameLabel;
    @FXML private Button viewSupervisorButton;

    // Relationship Section - Subordinates
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
    private Personnel supervisor;
    private List<Personnel> subordinates;

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
        loadPositions();
        loadRelationships();
    }

    /**
     * Display personnel information
     */
    private void displayPersonnelInfo() {
        if (personnel == null) return;

        // Basic Info
        if (nameLabel != null) {
            nameLabel.setText(personnel.getFullName());
        }
        if (ageLabel != null) {
            ageLabel.setText(personnel.getAge() + " ans");
        }
        if (phoneLabel != null) {
            phoneLabel.setText(personnel.getPhone() != null ? personnel.getPhone() : "Non spécifié");
        }
        if (emailLabel != null) {
            emailLabel.setText(personnel.getEmail() != null ? personnel.getEmail() : "Non spécifié");
        }
        if (jobTitleLabel != null) {
            jobTitleLabel.setText(formatJobTitle(personnel.getJobTitle()));
        }

        // Department
        if (departmentLabel != null) {
            String dept = personnel.getDepartment();
            if ("administration".equals(dept)) {
                departmentLabel.setText("Administration");
                departmentLabel.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;");
            } else if ("farm".equals(dept)) {
                departmentLabel.setText("Ferme");
                departmentLabel.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;");
            } else {
                departmentLabel.setText("Non spécifié");
            }
        }

        // Employment Info
        if (hireDateLabel != null) {
            if (personnel.getHireDate() != null) {
                hireDateLabel.setText(personnel.getHireDate().toString());
            } else {
                hireDateLabel.setText("Non spécifié");
            }
        }

        if (yearsOfServiceLabel != null) {
            yearsOfServiceLabel.setText(personnel.getYearsOfService() + " ans");
        }

        if (salaryLabel != null) {
            salaryLabel.setText(String.format("%.2f MAD", personnel.getSalary()));
        }

        // Hide shift label (removed from new structure)
        if (shiftLabel != null) {
            shiftLabel.setVisible(false);
            shiftLabel.setManaged(false);
        }

        // Additional Info
        if (addressLabel != null) {
            addressLabel.setText(personnel.getAddress() != null ? personnel.getAddress() : "Non spécifié");
        }
        if (emergencyContactLabel != null) {
            emergencyContactLabel.setText(personnel.getEmergencyContact() != null ? personnel.getEmergencyContact() : "Non spécifié");
        }

        // Status
        if (statusLabel != null) {
            if (personnel.isActive()) {
                statusLabel.setText("Actif");
                statusLabel.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 5 10; -fx-background-radius: 5;");
            } else {
                statusLabel.setText("Inactif");
                statusLabel.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-padding: 5 10; -fx-background-radius: 5;");
            }
        }
    }

    /**
     * Load and display positions for admin staff
     */
    private void loadPositions() {
        // Hide positions section by default
        if (positionsSection != null) {
            positionsSection.setVisible(false);
            positionsSection.setManaged(false);
        }

        // Only show for admin_staff
        if (personnel == null || !personnel.isAdminStaff()) {
            return;
        }

        if (positionsSection != null && positionsPane != null) {
            positionsSection.setVisible(true);
            positionsSection.setManaged(true);

            positionsPane.getChildren().clear();

            AdminPosition[] positions = personnel.getAdminPositions();
            if (positions != null && positions.length > 0) {
                for (AdminPosition pos : positions) {
                    Label posLabel = new Label(pos.getDisplayNameFr());
                    posLabel.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #495057; " +
                                     "-fx-padding: 5 12; -fx-background-radius: 15; -fx-font-size: 12px;");
                    positionsPane.getChildren().add(posLabel);
                }
            } else {
                Label noPos = new Label("Aucune position assignée");
                noPos.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");
                positionsPane.getChildren().add(noPos);
            }
        }
    }

    /**
     * Load and display relationships (supervisor or subordinates)
     */
    private void loadRelationships() {
        // Hide both sections initially
        if (supervisorSection != null) {
            supervisorSection.setVisible(false);
            supervisorSection.setManaged(false);
        }
        if (subordinatesSection != null) {
            subordinatesSection.setVisible(false);
            subordinatesSection.setManaged(false);
        }

        if (personnel == null) return;

        // Check if personnel is a subordinate (has a supervisor)
        if (personnel.isSubordinate() && personnel.hasSupervisor()) {
            supervisor = personnelDAO.getPersonnelById(personnel.getSupervisorId());
            if (supervisor != null && supervisorSection != null) {
                supervisorSection.setVisible(true);
                supervisorSection.setManaged(true);

                if (supervisorNameLabel != null) {
                    supervisorNameLabel.setText(supervisor.getFullName());
                }

                // Setup click handler for supervisor button
                if (viewSupervisorButton != null) {
                    viewSupervisorButton.setOnAction(e -> handleViewSupervisor());
                }
            }
        }

        // Check if personnel is a supervisor (can have subordinates)
        if (personnel.isSupervisor()) {
            subordinates = personnelDAO.getSubordinatesBySupervisorId(personnel.getId());

            if (subordinatesSection != null) {
                subordinatesSection.setVisible(true);
                subordinatesSection.setManaged(true);

                if (subordinateCountLabel != null) {
                    subordinateCountLabel.setText(subordinates.size() + " subordonné(s)");
                }

                // Populate subordinates list
                if (subordinatesListView != null) {
                    subordinatesListView.getItems().clear();
                    for (Personnel subordinate : subordinates) {
                        subordinatesListView.getItems().add(
                            subordinate.getFullName() + " - " + formatJobTitle(subordinate.getJobTitle())
                        );
                    }

                    // Setup click handler for subordinates list
                    subordinatesListView.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) {
                            String selected = subordinatesListView.getSelectionModel().getSelectedItem();
                            if (selected != null) {
                                handleViewSubordinate(selected);
                            }
                        }
                    });
                }
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
        if (subordinates == null || selectedItem == null) return;

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
                loadPositions();
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
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    /**
     * Format job title for display (updated for new structure)
     */
    private String formatJobTitle(String jobTitle) {
        if (jobTitle == null) return "Non spécifié";

        // Try to get from PersonnelType enum first
        PersonnelType type = PersonnelType.fromCode(jobTitle);
        if (type != null) {
            return type.getDisplayNameFr();
        }

        // Fallback for legacy job titles
        switch (jobTitle.toLowerCase()) {
            case "farm_owner":
                return "Propriétaire / Directeur Général";
            case "cashier":
                return "Caissier";
            case "admin_staff":
                return "Personnel Administratif";
            case "veterinary_supervisor":
                return "Superviseur Vétérinaire";
            case "veterinary_subordinate":
                return "Subordonné Vétérinaire";
            case "inventory_supervisor":
                return "Superviseur Inventaire";
            case "inventory_subordinate":
                return "Subordonné Inventaire";
            case "farmhand_supervisor":
                return "Superviseur Ouvriers";
            case "farmhand_subordinate":
                return "Subordonné Ouvrier";
            case "veterinary":
                return "Vétérinaire";
            case "inventory_tracker":
                return "Gestionnaire d'Inventaire";
            case "supervisor":
                return "Superviseur";
            case "farmhand":
                return "Ouvrier Agricole";
            default:
                return jobTitle;
        }
    }
}
