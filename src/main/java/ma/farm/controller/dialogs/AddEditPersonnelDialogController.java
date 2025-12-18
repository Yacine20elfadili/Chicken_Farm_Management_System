package ma.farm.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ma.farm.dao.PersonnelDAO;
import ma.farm.model.AdminPosition;
import ma.farm.model.Personnel;
import ma.farm.model.PersonnelType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * AddEditPersonnelDialogController - Handles Add/Edit Personnel Dialog
 *
 * New Structure:
 * - Toggle between Administration and Farm departments
 * - Administration: Farm Owner, Cashier, Admin Staff with position checkboxes
 * - Farm: Supervisors and Subordinates with auto-linking
 *
 * Features:
 * - Department toggle (Admin/Farm)
 * - Job title dropdown based on department
 * - Position checkboxes for Admin Staff
 * - Supervisor selection for subordinates
 * - Singleton validation
 * - Email uniqueness validation
 */
public class AddEditPersonnelDialogController {

    // FXML Components - Header
    @FXML private Label dialogTitle;

    // FXML Components - Basic Info
    @FXML private TextField fullNameField;
    @FXML private TextField ageField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea addressTextArea;
    @FXML private TextField emergencyContactField;

    // FXML Components - Department Toggle
    @FXML private ToggleGroup departmentToggle;
    @FXML private RadioButton adminRadio;
    @FXML private RadioButton farmRadio;
    @FXML private HBox departmentToggleBox;

    // FXML Components - Job Title
    @FXML private ComboBox<String> jobTitleComboBox;
    @FXML private VBox jobTitleSection;

    // FXML Components - Positions (Admin Staff only)
    @FXML private VBox positionsSection;
    @FXML private FlowPane positionsPane;
    @FXML private CheckBox accountingCheckBox;
    @FXML private CheckBox hrCheckBox;
    @FXML private CheckBox legalCheckBox;
    @FXML private CheckBox salesCheckBox;

    // FXML Components - Supervisor Selection (Subordinates only)
    @FXML private VBox supervisorSection;
    @FXML private ComboBox<String> supervisorComboBox; // Keep for backward compatibility, but hidden
    @FXML private Label supervisorInfoLabel;

    // Store the selected supervisor ID internally (auto-selected since only one per type)
    private Integer selectedSupervisorId = null;

    // FXML Components - Employment Info
    @FXML private DatePicker hireDatePicker;
    @FXML private TextField salaryField;

    // FXML Components - Error Labels
    @FXML private Label fullNameErrorLabel;
    @FXML private Label ageErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label jobTitleErrorLabel;
    @FXML private Label positionsErrorLabel;
    @FXML private Label supervisorErrorLabel;
    @FXML private Label salaryErrorLabel;

    // FXML Components - Warning Banner
    @FXML private HBox noSupervisorWarningLabel;

    // FXML Components - Buttons
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // Legacy components (for backward compatibility)
    @FXML private ComboBox<String> shiftComboBox;
    @FXML private Label shiftErrorLabel;

    // DAOs
    private PersonnelDAO personnelDAO;

    // State
    private Stage dialogStage;
    private Personnel personnel; // null for Add, populated for Edit
    private boolean saveClicked = false;
    private String preselectedDepartment = null;
    private String preselectedJobTitle = null;

    // Supervisor cache
    private List<Personnel> veterinarySupervisors = new ArrayList<>();
    private List<Personnel> inventorySupervisors = new ArrayList<>();
    private List<Personnel> farmhandSupervisors = new ArrayList<>();

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        personnelDAO = new PersonnelDAO();

        // Setup department toggle
        setupDepartmentToggle();

        // Load supervisors for subordinate selection
        loadSupervisors();

        // Setup job title listener
        setupJobTitleListener();

        // Setup position checkboxes
        setupPositionCheckboxes();

        // Hide all error labels initially
        hideAllErrors();

        // Hide warning banner initially
        if (noSupervisorWarningLabel != null) {
            noSupervisorWarningLabel.setVisible(false);
            noSupervisorWarningLabel.setManaged(false);
        }

        // Default to admin department
        if (adminRadio != null) {
            adminRadio.setSelected(true);
            updateJobTitlesForDepartment("administration");
        } else {
            // Fallback - populate with all job titles
            populateAllJobTitles();
        }

        // Set default hire date
        if (hireDatePicker != null) {
            hireDatePicker.setValue(LocalDate.now());
        }

        // Hide shift section if exists (removed in new structure)
        if (shiftComboBox != null) {
            shiftComboBox.setVisible(false);
            shiftComboBox.setManaged(false);
        }
        if (shiftErrorLabel != null) {
            shiftErrorLabel.setVisible(false);
            shiftErrorLabel.setManaged(false);
        }
    }

    /**
     * Setup department toggle radio buttons
     */
    private void setupDepartmentToggle() {
        if (departmentToggle == null) {
            // Create toggle group programmatically if not defined in FXML
            departmentToggle = new ToggleGroup();
            if (adminRadio != null) adminRadio.setToggleGroup(departmentToggle);
            if (farmRadio != null) farmRadio.setToggleGroup(departmentToggle);
        }

        if (departmentToggle != null) {
            departmentToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    RadioButton selected = (RadioButton) newVal;
                    String department = selected == adminRadio ? "administration" : "farm";
                    updateJobTitlesForDepartment(department);
                    hidePositionsSection();
                    hideSupervisorSection();
                }
            });
        }
    }

    /**
     * Update job titles based on selected department
     */
    private void updateJobTitlesForDepartment(String department) {
        if (jobTitleComboBox == null) return;

        ObservableList<String> jobTitles = FXCollections.observableArrayList();

        if ("administration".equals(department)) {
            // Check which admin roles are available
            if (!personnelDAO.hasFarmOwner()) {
                jobTitles.add("farm_owner");
            }
            if (!personnelDAO.hasCashier()) {
                jobTitles.add("cashier");
            }
            if (personnelDAO.canAddMoreAdminStaff()) {
                jobTitles.add("admin_staff");
            }

            if (jobTitles.isEmpty()) {
                jobTitles.add("(Tous les postes admin sont pourvus)");
            }
        } else {
            // Farm department
            // Add supervisors if not exists
            if (!personnelDAO.hasSupervisor("veterinary_supervisor")) {
                jobTitles.add("veterinary_supervisor");
            }
            if (!personnelDAO.hasSupervisor("inventory_supervisor")) {
                jobTitles.add("inventory_supervisor");
            }
            if (!personnelDAO.hasSupervisor("farmhand_supervisor")) {
                jobTitles.add("farmhand_supervisor");
            }

            // Add subordinates if their supervisors exist
            if (personnelDAO.hasSupervisor("veterinary_supervisor")) {
                jobTitles.add("veterinary_subordinate");
            }
            if (personnelDAO.hasSupervisor("inventory_supervisor")) {
                jobTitles.add("inventory_subordinate");
            }
            if (personnelDAO.hasSupervisor("farmhand_supervisor")) {
                jobTitles.add("farmhand_subordinate");
            }

            if (jobTitles.isEmpty()) {
                jobTitles.add("(Aucun poste disponible)");
            }
        }

        jobTitleComboBox.setItems(jobTitles);

        // Auto-select if only one option or if preselected
        if (preselectedJobTitle != null && jobTitles.contains(preselectedJobTitle)) {
            jobTitleComboBox.setValue(preselectedJobTitle);
        } else if (jobTitles.size() == 1 && !jobTitles.get(0).startsWith("(")) {
            jobTitleComboBox.setValue(jobTitles.get(0));
        }
    }

    /**
     * Populate all job titles (fallback)
     */
    private void populateAllJobTitles() {
        if (jobTitleComboBox == null) return;

        ObservableList<String> jobTitles = FXCollections.observableArrayList(
                "farm_owner",
                "cashier",
                "admin_staff",
                "veterinary_supervisor",
                "inventory_supervisor",
                "farmhand_supervisor",
                "veterinary_subordinate",
                "inventory_subordinate",
                "farmhand_subordinate"
        );
        jobTitleComboBox.setItems(jobTitles);
    }

    /**
     * Load supervisors for subordinate selection
     */
    private void loadSupervisors() {
        Personnel vetSup = personnelDAO.getSupervisorByType("veterinary_supervisor");
        if (vetSup != null) veterinarySupervisors.add(vetSup);

        Personnel invSup = personnelDAO.getSupervisorByType("inventory_supervisor");
        if (invSup != null) inventorySupervisors.add(invSup);

        Personnel farmSup = personnelDAO.getSupervisorByType("farmhand_supervisor");
        if (farmSup != null) farmhandSupervisors.add(farmSup);
    }

    /**
     * Setup job title change listener
     */
    private void setupJobTitleListener() {
        if (jobTitleComboBox == null) return;

        jobTitleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.startsWith("(")) return;

            // Show/hide positions section for admin_staff
            if ("admin_staff".equals(newVal)) {
                // Check if this is the first admin staff
                if (personnelDAO.isAddingFirstAdminStaff()) {
                    // First admin staff - hide positions section, auto-assign all 4
                    hidePositionsSection();
                    System.out.println("DEBUG: First admin staff - will auto-assign all 4 positions");
                } else {
                    // Subsequent admin staff - show position selection
                    showPositionsSection();
                    updateAvailablePositions();
                }
            } else {
                hidePositionsSection();
            }

            // Show/hide supervisor section for subordinates
            if (newVal.endsWith("_subordinate")) {
                showSupervisorSection(newVal);
            } else {
                hideSupervisorSection();
            }

            // Enable/disable save button
            if (saveButton != null) {
                saveButton.setDisable(newVal.startsWith("("));
            }
        });
    }

    /**
     * Setup position checkboxes
     */
    private void setupPositionCheckboxes() {
        // Checkboxes are defined in FXML or created programmatically
        if (positionsPane != null && accountingCheckBox == null) {
            // Create checkboxes programmatically if not in FXML
            accountingCheckBox = new CheckBox("Comptabilité");
            hrCheckBox = new CheckBox("Ressources Humaines");
            legalCheckBox = new CheckBox("Juridique");
            salesCheckBox = new CheckBox("Ventes");

            positionsPane.getChildren().addAll(accountingCheckBox, hrCheckBox, legalCheckBox, salesCheckBox);
        }
    }

    /**
     * Show positions section for admin staff
     */
    private void showPositionsSection() {
        if (positionsSection != null) {
            positionsSection.setVisible(true);
            positionsSection.setManaged(true);
        }
    }

    /**
     * Hide positions section
     */
    private void hidePositionsSection() {
        if (positionsSection != null) {
            positionsSection.setVisible(false);
            positionsSection.setManaged(false);
        }
    }

    /**
     * Update available positions based on what's already assigned
     * For subsequent admin staff: shows positions held by first admin staff (available for delegation)
     * Uses single-selection mode (only one position can be chosen)
     */
    private void updateAvailablePositions() {
        System.out.println("DEBUG: updateAvailablePositions() called");

        // Get positions available for delegation (from first admin staff)
        AdminPosition[] availablePositions = personnelDAO.getAvailablePositions();
        System.out.println("DEBUG: Available positions for delegation: " + (availablePositions != null ? availablePositions.length : "null"));

        List<String> available = new ArrayList<>();
        for (AdminPosition pos : availablePositions) {
            available.add(pos.getCode());
            System.out.println("DEBUG: Available position: " + pos.getCode());
        }

        // Also include positions currently assigned to this personnel (if editing)
        if (personnel != null && personnel.getPositions() != null) {
            AdminPosition[] currentPositions = personnel.getAdminPositions();
            for (AdminPosition pos : currentPositions) {
                if (!available.contains(pos.getCode())) {
                    available.add(pos.getCode());
                }
            }
        }

        System.out.println("DEBUG: Final available list: " + available);

        // Reset all checkboxes first
        if (accountingCheckBox != null) accountingCheckBox.setSelected(false);
        if (hrCheckBox != null) hrCheckBox.setSelected(false);
        if (legalCheckBox != null) legalCheckBox.setSelected(false);
        if (salesCheckBox != null) salesCheckBox.setSelected(false);

        // Enable/disable checkboxes based on availability
        if (accountingCheckBox != null) {
            boolean isAvailable = available.contains("accounting");
            accountingCheckBox.setDisable(!isAvailable);
            System.out.println("DEBUG: accountingCheckBox - available=" + isAvailable + ", disabled=" + accountingCheckBox.isDisabled());
            if (personnel != null && personnel.hasPosition(AdminPosition.ACCOUNTING)) {
                accountingCheckBox.setSelected(true);
            }
        }
        if (hrCheckBox != null) {
            boolean isAvailable = available.contains("hr");
            hrCheckBox.setDisable(!isAvailable);
            System.out.println("DEBUG: hrCheckBox - available=" + isAvailable + ", disabled=" + hrCheckBox.isDisabled());
            if (personnel != null && personnel.hasPosition(AdminPosition.HR)) {
                hrCheckBox.setSelected(true);
            }
        }
        if (legalCheckBox != null) {
            boolean isAvailable = available.contains("legal");
            legalCheckBox.setDisable(!isAvailable);
            System.out.println("DEBUG: legalCheckBox - available=" + isAvailable + ", disabled=" + legalCheckBox.isDisabled());
            if (personnel != null && personnel.hasPosition(AdminPosition.LEGAL)) {
                legalCheckBox.setSelected(true);
            }
        }
        if (salesCheckBox != null) {
            boolean isAvailable = available.contains("sales");
            salesCheckBox.setDisable(!isAvailable);
            System.out.println("DEBUG: salesCheckBox - available=" + isAvailable + ", disabled=" + salesCheckBox.isDisabled());
            if (personnel != null && personnel.hasPosition(AdminPosition.SALES)) {
                salesCheckBox.setSelected(true);
            }
        }

        // Setup single-selection behavior for subsequent admin staff
        setupSinglePositionSelection();
    }

    /**
     * Setup single-selection behavior for position checkboxes
     * When adding a new admin staff (not the first), only ONE position can be selected
     */
    private void setupSinglePositionSelection() {
        // Clear previous listeners first by removing and re-adding
        CheckBox[] checkboxes = {accountingCheckBox, hrCheckBox, legalCheckBox, salesCheckBox};

        for (CheckBox cb : checkboxes) {
            if (cb != null) {
                cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        // Deselect all other checkboxes
                        for (CheckBox other : checkboxes) {
                            if (other != null && other != cb && other.isSelected()) {
                                other.setSelected(false);
                            }
                        }
                    }
                });
            }
        }
        System.out.println("DEBUG: Single-selection mode enabled for positions");
    }

    /**
     * Show supervisor section for subordinates
     * Since there's only ONE supervisor per type, we auto-assign and display info (no dropdown needed)
     */
    private void showSupervisorSection(String subordinateType) {
        if (supervisorSection != null) {
            supervisorSection.setVisible(true);
            supervisorSection.setManaged(true);
        }

        // Hide the old ComboBox if it exists
        if (supervisorComboBox != null) {
            supervisorComboBox.setVisible(false);
            supervisorComboBox.setManaged(false);
        }

        // Find the supervisor for this subordinate type
        Personnel supervisor = null;
        String supervisorTitle = "";

        switch (subordinateType) {
            case "veterinary_subordinate":
                supervisor = !veterinarySupervisors.isEmpty() ? veterinarySupervisors.get(0) : null;
                supervisorTitle = "Superviseur Vétérinaire";
                break;
            case "inventory_subordinate":
                supervisor = !inventorySupervisors.isEmpty() ? inventorySupervisors.get(0) : null;
                supervisorTitle = "Superviseur Inventaire";
                break;
            case "farmhand_subordinate":
                supervisor = !farmhandSupervisors.isEmpty() ? farmhandSupervisors.get(0) : null;
                supervisorTitle = "Superviseur Ouvriers";
                break;
        }

        if (supervisor != null) {
            // Store the supervisor ID for saving
            selectedSupervisorId = supervisor.getId();

            // Update the info label
            if (supervisorInfoLabel != null) {
                supervisorInfoLabel.setText(supervisor.getFullName() + " (" + supervisorTitle + ")");
            }

            if (noSupervisorWarningLabel != null) {
                noSupervisorWarningLabel.setVisible(false);
                noSupervisorWarningLabel.setManaged(false);
            }
            if (saveButton != null) {
                saveButton.setDisable(false);
            }

            System.out.println("DEBUG: Auto-assigned supervisor: " + supervisor.getFullName() + " (ID: " + supervisor.getId() + ")");
        } else {
            // No supervisor available
            selectedSupervisorId = null;

            if (supervisorInfoLabel != null) {
                supervisorInfoLabel.setText("Aucun superviseur disponible");
                supervisorInfoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
            }

            if (noSupervisorWarningLabel != null) {
                noSupervisorWarningLabel.setVisible(true);
                noSupervisorWarningLabel.setManaged(true);
            }
            if (saveButton != null) {
                saveButton.setDisable(true);
            }

            System.out.println("DEBUG: No supervisor available for " + subordinateType);
        }
    }

    /**
     * Hide supervisor section
     */
    private void hideSupervisorSection() {
        if (supervisorSection != null) {
            supervisorSection.setVisible(false);
            supervisorSection.setManaged(false);
        }
        if (noSupervisorWarningLabel != null) {
            noSupervisorWarningLabel.setVisible(false);
            noSupervisorWarningLabel.setManaged(false);
        }
        if (saveButton != null) {
            saveButton.setDisable(false);
        }
        // Clear the stored supervisor ID
        selectedSupervisorId = null;
    }

    /**
     * Hide all error labels
     */
    private void hideAllErrors() {
        hideError(fullNameErrorLabel);
        hideError(ageErrorLabel);
        hideError(phoneErrorLabel);
        hideError(emailErrorLabel);
        hideError(jobTitleErrorLabel);
        hideError(positionsErrorLabel);
        hideError(supervisorErrorLabel);
        hideError(salaryErrorLabel);
        hideError(shiftErrorLabel);
    }

    private void hideError(Label label) {
        if (label != null) {
            label.setVisible(false);
            label.setManaged(false);
        }
    }

    private void showError(Label label, String message) {
        if (label != null) {
            label.setText(message);
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    /**
     * Sets the dialog stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets personnel for editing (null for new personnel)
     */
    public void setPersonnel(Personnel personnel) {
        this.personnel = personnel;

        if (personnel != null) {
            // EDIT MODE
            if (dialogTitle != null) {
                dialogTitle.setText("Modifier Personnel");
            }
            populateFields();

            // Hide department toggle in edit mode
            if (departmentToggleBox != null) {
                departmentToggleBox.setVisible(false);
                departmentToggleBox.setManaged(false);
            }

            // Disable job title change in edit mode
            if (jobTitleComboBox != null) {
                jobTitleComboBox.setDisable(true);
            }
        } else {
            // ADD MODE
            if (dialogTitle != null) {
                dialogTitle.setText("Ajouter Personnel");
            }

            // Apply preselected values
            if (preselectedDepartment != null) {
                if ("administration".equals(preselectedDepartment) && adminRadio != null) {
                    adminRadio.setSelected(true);
                } else if ("farm".equals(preselectedDepartment) && farmRadio != null) {
                    farmRadio.setSelected(true);
                }
                updateJobTitlesForDepartment(preselectedDepartment);
            }
        }
    }

    /**
     * Preselect department before setting personnel
     */
    public void setPreselectedDepartment(String department) {
        this.preselectedDepartment = department;
    }

    /**
     * Preselect job title before setting personnel
     */
    public void setPreselectedJobTitle(String jobTitle) {
        this.preselectedJobTitle = jobTitle;
    }

    /**
     * Populate form fields with existing personnel data
     */
    private void populateFields() {
        if (personnel == null) return;

        if (fullNameField != null) fullNameField.setText(personnel.getFullName());
        if (ageField != null) ageField.setText(String.valueOf(personnel.getAge()));
        if (phoneField != null) phoneField.setText(personnel.getPhone());
        if (emailField != null) emailField.setText(personnel.getEmail());
        if (addressTextArea != null) addressTextArea.setText(personnel.getAddress());
        if (emergencyContactField != null) emergencyContactField.setText(personnel.getEmergencyContact());

        if (hireDatePicker != null) hireDatePicker.setValue(personnel.getHireDate());
        if (salaryField != null) salaryField.setText(String.valueOf(personnel.getSalary()));

        // Set department radio
        if (personnel.isAdministration() && adminRadio != null) {
            adminRadio.setSelected(true);
            updateJobTitlesForDepartment("administration");
        } else if (personnel.isFarm() && farmRadio != null) {
            farmRadio.setSelected(true);
            updateJobTitlesForDepartment("farm");
        }

        // Set job title
        if (jobTitleComboBox != null && personnel.getJobTitle() != null) {
            // Add the current job title if not in list
            if (!jobTitleComboBox.getItems().contains(personnel.getJobTitle())) {
                jobTitleComboBox.getItems().add(personnel.getJobTitle());
            }
            jobTitleComboBox.setValue(personnel.getJobTitle());
        }

        // Set positions if admin_staff
        if (personnel.isAdminStaff()) {
            showPositionsSection();
            updateAvailablePositions();
        }

        // Set supervisor if subordinate
        if (personnel.isSubordinate() && personnel.getSupervisorId() != null) {
            showSupervisorSection(personnel.getJobTitle());
            Personnel supervisor = personnelDAO.getPersonnelById(personnel.getSupervisorId());
            if (supervisor != null && supervisorComboBox != null) {
                supervisorComboBox.setValue(supervisor.getFullName() + " (ID: " + supervisor.getId() + ")");
            }
        }
    }

    /**
     * Handle save button click
     */
    @FXML
    public void handleSave() {
        System.out.println("DEBUG: Save button clicked");

        if (validateInput()) {
            try {
                // Create or update personnel object
                if (personnel == null) {
                    personnel = new Personnel();
                    System.out.println("DEBUG: Creating new personnel");
                } else {
                    System.out.println("DEBUG: Updating existing personnel ID: " + personnel.getId());
                }

                // Set all fields
                personnel.setFullName(fullNameField.getText().trim());
                personnel.setAge(Integer.parseInt(ageField.getText().trim()));
                personnel.setPhone(phoneField.getText().trim());
                personnel.setEmail(emailField.getText().trim());

                String selectedJobTitle = jobTitleComboBox.getValue();
                personnel.setJobTitle(selectedJobTitle);

                // Set department based on job title
                PersonnelType personnelType = PersonnelType.fromCode(selectedJobTitle);
                if (personnelType != null) {
                    personnel.setDepartment(personnelType.getDepartment());
                } else {
                    // Fallback - determine from toggle
                    String dept = (adminRadio != null && adminRadio.isSelected()) ? "administration" : "farm";
                    personnel.setDepartment(dept);
                }

                // Set positions for admin_staff
                if ("admin_staff".equals(selectedJobTitle)) {
                    if (personnelDAO.isAddingFirstAdminStaff() && (personnel == null || personnel.getId() == 0)) {
                        // First admin staff - auto-assign all 4 positions
                        personnel.setPositions("accounting,hr,legal,sales");
                        System.out.println("DEBUG: First admin staff - auto-assigned all 4 positions");
                    } else if (personnel == null || personnel.getId() == 0) {
                        // Subsequent admin staff - get selected position and delegate from first
                        List<String> selectedPositions = new ArrayList<>();
                        if (accountingCheckBox != null && accountingCheckBox.isSelected()) selectedPositions.add("accounting");
                        if (hrCheckBox != null && hrCheckBox.isSelected()) selectedPositions.add("hr");
                        if (legalCheckBox != null && legalCheckBox.isSelected()) selectedPositions.add("legal");
                        if (salesCheckBox != null && salesCheckBox.isSelected()) selectedPositions.add("sales");
                        personnel.setPositions(String.join(",", selectedPositions));

                        // Remove selected position from first admin staff
                        for (String pos : selectedPositions) {
                            AdminPosition adminPos = AdminPosition.fromCode(pos);
                            if (adminPos != null) {
                                personnelDAO.removePositionFromFirstAdminStaff(adminPos);
                            }
                        }
                        System.out.println("DEBUG: Subsequent admin staff - delegated position: " + selectedPositions);
                    } else {
                        // Editing existing admin staff - just update positions
                        List<String> selectedPositions = new ArrayList<>();
                        if (accountingCheckBox != null && accountingCheckBox.isSelected()) selectedPositions.add("accounting");
                        if (hrCheckBox != null && hrCheckBox.isSelected()) selectedPositions.add("hr");
                        if (legalCheckBox != null && legalCheckBox.isSelected()) selectedPositions.add("legal");
                        if (salesCheckBox != null && salesCheckBox.isSelected()) selectedPositions.add("sales");
                        personnel.setPositions(String.join(",", selectedPositions));
                    }
                }

                // Set supervisor for subordinates
                if (selectedJobTitle != null && selectedJobTitle.endsWith("_subordinate")) {
                    Integer supervisorId = extractSupervisorId();
                    personnel.setSupervisorId(supervisorId);
                }

                // Set employment details
                if (hireDatePicker != null) {
                    personnel.setHireDate(hireDatePicker.getValue());
                }
                if (salaryField != null && !salaryField.getText().trim().isEmpty()) {
                    personnel.setSalary(Double.parseDouble(salaryField.getText().trim()));
                }

                // Set address and emergency contact
                if (addressTextArea != null) {
                    personnel.setAddress(addressTextArea.getText().trim());
                }
                if (emergencyContactField != null) {
                    personnel.setEmergencyContact(emergencyContactField.getText().trim());
                }

                personnel.setActive(true);

                // Save to database
                boolean success;
                if (personnel.getId() > 0) {
                    success = personnelDAO.updatePersonnel(personnel);
                } else {
                    success = personnelDAO.createPersonnel(personnel);
                }

                if (success) {
                    saveClicked = true;
                    dialogStage.close();
                } else {
                    showError(emailErrorLabel, "Erreur lors de l'enregistrement. Veuillez réessayer.");
                }

            } catch (Exception e) {
                System.err.println("ERROR saving personnel: " + e.getMessage());
                e.printStackTrace();
                showError(emailErrorLabel, "Erreur: " + e.getMessage());
            }
        }
    }

    /**
     * Extract supervisor ID - now uses the stored selectedSupervisorId
     */
    private Integer extractSupervisorId() {
        // Use the internally stored supervisor ID (auto-assigned)
        if (selectedSupervisorId != null) {
            return selectedSupervisorId;
        }

        // Fallback: try to extract from combo box if it exists (backward compatibility)
        if (supervisorComboBox == null || supervisorComboBox.getValue() == null) {
            return null;
        }

        String selected = supervisorComboBox.getValue();
        if (selected.startsWith("(")) return null;

        // Extract ID from "Name (ID: 123)"
        try {
            int startIdx = selected.lastIndexOf("ID: ") + 4;
            int endIdx = selected.lastIndexOf(")");
            if (startIdx > 3 && endIdx > startIdx) {
                return Integer.parseInt(selected.substring(startIdx, endIdx));
            }
        } catch (Exception e) {
            System.err.println("ERROR extracting supervisor ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Validate all input fields
     */
    private boolean validateInput() {
        hideAllErrors();
        boolean valid = true;
        System.out.println("DEBUG: Starting validation...");

        // Full Name
        if (fullNameField == null || fullNameField.getText().trim().isEmpty()) {
            showError(fullNameErrorLabel, "Le nom complet est requis");
            System.out.println("DEBUG: FAILED - Full name is empty");
            valid = false;
        } else {
            System.out.println("DEBUG: OK - Full name: " + fullNameField.getText().trim());
        }

        // Age
        if (ageField == null || ageField.getText().trim().isEmpty()) {
            showError(ageErrorLabel, "L'âge est requis");
            System.out.println("DEBUG: FAILED - Age is empty");
            valid = false;
        } else {
            try {
                int age = Integer.parseInt(ageField.getText().trim());
                if (age < 18 || age > 100) {
                    showError(ageErrorLabel, "L'âge doit être entre 18 et 100");
                    System.out.println("DEBUG: FAILED - Age out of range: " + age);
                    valid = false;
                } else {
                    System.out.println("DEBUG: OK - Age: " + age);
                }
            } catch (NumberFormatException e) {
                showError(ageErrorLabel, "L'âge doit être un nombre valide");
                System.out.println("DEBUG: FAILED - Age not a number");
                valid = false;
            }
        }

        // Phone
        if (phoneField == null || phoneField.getText().trim().isEmpty()) {
            showError(phoneErrorLabel, "Le téléphone est requis");
            System.out.println("DEBUG: FAILED - Phone is empty");
            valid = false;
        } else {
            System.out.println("DEBUG: OK - Phone: " + phoneField.getText().trim());
        }

        // Email
        if (emailField == null || emailField.getText().trim().isEmpty()) {
            showError(emailErrorLabel, "L'email est requis");
            System.out.println("DEBUG: FAILED - Email is empty");
            valid = false;
        } else {
            String email = emailField.getText().trim();
            if (!email.contains("@") || !email.contains(".")) {
                showError(emailErrorLabel, "Format d'email invalide");
                System.out.println("DEBUG: FAILED - Email format invalid: " + email);
                valid = false;
            } else {
                // Check uniqueness
                if (personnel == null || personnel.getId() == 0) {
                    if (personnelDAO.emailExists(email)) {
                        showError(emailErrorLabel, "Cet email existe déjà");
                        System.out.println("DEBUG: FAILED - Email already exists: " + email);
                        valid = false;
                    } else {
                        System.out.println("DEBUG: OK - Email: " + email);
                    }
                } else {
                    if (personnelDAO.emailExistsForOther(email, personnel.getId())) {
                        showError(emailErrorLabel, "Cet email existe déjà pour un autre personnel");
                        System.out.println("DEBUG: FAILED - Email exists for other: " + email);
                        valid = false;
                    } else {
                        System.out.println("DEBUG: OK - Email: " + email);
                    }
                }
            }
        }

        // Job Title
        String jobTitleValue = (jobTitleComboBox != null) ? jobTitleComboBox.getValue() : null;
        System.out.println("DEBUG: Job title value = '" + jobTitleValue + "'");
        if (jobTitleComboBox == null || jobTitleValue == null || jobTitleValue.startsWith("(")) {
            showError(jobTitleErrorLabel, "Le titre du poste est requis");
            System.out.println("DEBUG: FAILED - Job title is null or invalid");
            valid = false;
        } else {
            System.out.println("DEBUG: OK - Job title: " + jobTitleValue);
        }

        // Positions for admin_staff
        if (jobTitleComboBox != null && "admin_staff".equals(jobTitleComboBox.getValue())) {
            System.out.println("DEBUG: Checking positions for admin_staff...");

            // First admin staff doesn't need position selection (auto-assigned all 4)
            if (personnelDAO.isAddingFirstAdminStaff() && (personnel == null || personnel.getId() == 0)) {
                System.out.println("DEBUG: OK - First admin staff, positions auto-assigned");
            } else {
                System.out.println("DEBUG: accountingCheckBox=" + (accountingCheckBox != null ? accountingCheckBox.isSelected() : "null"));
                System.out.println("DEBUG: hrCheckBox=" + (hrCheckBox != null ? hrCheckBox.isSelected() : "null"));
                System.out.println("DEBUG: legalCheckBox=" + (legalCheckBox != null ? legalCheckBox.isSelected() : "null"));
                System.out.println("DEBUG: salesCheckBox=" + (salesCheckBox != null ? salesCheckBox.isSelected() : "null"));

                boolean hasPosition = (accountingCheckBox != null && accountingCheckBox.isSelected()) ||
                                     (hrCheckBox != null && hrCheckBox.isSelected()) ||
                                     (legalCheckBox != null && legalCheckBox.isSelected()) ||
                                     (salesCheckBox != null && salesCheckBox.isSelected());
                if (!hasPosition) {
                    showError(positionsErrorLabel, "Au moins une position est requise");
                    System.out.println("DEBUG: FAILED - No position selected for admin_staff");
                    valid = false;
                } else {
                    System.out.println("DEBUG: OK - At least one position selected");
                }
            }
        }

        // Supervisor for subordinates
        if (jobTitleComboBox != null && jobTitleComboBox.getValue() != null &&
            jobTitleComboBox.getValue().endsWith("_subordinate")) {
            System.out.println("DEBUG: Checking supervisor for subordinate...");
            if (selectedSupervisorId == null) {
                showError(supervisorErrorLabel, "Aucun superviseur disponible");
                System.out.println("DEBUG: FAILED - No supervisor available for subordinate");
                valid = false;
            } else {
                System.out.println("DEBUG: OK - Supervisor ID: " + selectedSupervisorId);
            }
        }

        // Salary (optional but must be valid if provided)
        if (salaryField != null && !salaryField.getText().trim().isEmpty()) {
            try {
                double salary = Double.parseDouble(salaryField.getText().trim());
                if (salary < 0) {
                    showError(salaryErrorLabel, "Le salaire ne peut pas être négatif");
                    System.out.println("DEBUG: FAILED - Salary is negative: " + salary);
                    valid = false;
                } else {
                    System.out.println("DEBUG: OK - Salary: " + salary);
                }
            } catch (NumberFormatException e) {
                showError(salaryErrorLabel, "Le salaire doit être un nombre valide");
                System.out.println("DEBUG: FAILED - Salary not a valid number: " + salaryField.getText());
                valid = false;
            }
        }

        System.out.println("DEBUG: Validation result = " + valid);
        return valid;
    }

    /**
     * Handle cancel button click
     */
    @FXML
    public void handleCancel() {
        dialogStage.close();
    }

    /**
     * Returns whether save was clicked
     */
    public boolean isSaveClicked() {
        return saveClicked;
    }

    /**
     * Returns the personnel (for retrieving after dialog closes)
     */
    public Personnel getPersonnel() {
        return personnel;
    }
}
