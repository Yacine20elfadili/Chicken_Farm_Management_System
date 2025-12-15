package ma.farm.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ma.farm.dao.PersonnelDAO;
import ma.farm.model.Personnel;
import ma.farm.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * AddEditPersonnelDialogController - Handles Add/Edit Personnel Dialog
 *
 * Features:
 * - Add new personnel or edit existing
 * - Job title-specific validation (farmhand requires supervisor)
 * - Prevents adding farmhands if no supervisors exist
 * - Email uniqueness validation
 * - Age and phone validation
 */
public class AddEditPersonnelDialogController {

    // FXML Components - Basic Info
    @FXML private Label dialogTitle;
    @FXML private TextField fullNameField;
    @FXML private TextField ageField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> jobTitleComboBox;
    @FXML private ComboBox<String> supervisorComboBox;
    @FXML private DatePicker hireDatePicker;
    @FXML private TextField salaryField;
    @FXML private ComboBox<String> shiftComboBox;
    @FXML private TextArea addressTextArea;
    @FXML private TextField emergencyContactField;

    // Error Labels
    @FXML private Label fullNameErrorLabel;
    @FXML private Label ageErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label jobTitleErrorLabel;
    @FXML private Label supervisorErrorLabel;
    @FXML private Label salaryErrorLabel;
    @FXML private Label shiftErrorLabel;

    // Warning Banner
    @FXML private HBox noSupervisorWarningLabel;

    // DAOs
    private PersonnelDAO personnelDAO;

    // State
    private Stage dialogStage;
    private Personnel personnel; // null for Add, populated for Edit
    private boolean saveClicked = false;
    private List<Personnel> allSupervisors;

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        personnelDAO = new PersonnelDAO();

        // Load job titles
        populateJobTitles();

        // Load shifts
        populateShifts();

        // Load supervisors
        loadSupervisors();

        // Setup job title listener to show/hide supervisor field
        setupJobTitleListener();

        // Hide all error labels initially
        hideAllErrors();

        // Hide warning banner initially
        if (noSupervisorWarningLabel != null) {
            noSupervisorWarningLabel.setVisible(false);
            noSupervisorWarningLabel.setManaged(false);
        }
    }

    /**
     * Populates job title ComboBox with operations personnel roles
     */
    private void populateJobTitles() {
        ObservableList<String> jobTitles = FXCollections.observableArrayList(
                "veterinary",
                "inventory_tracker",
                "supervisor",
                "farmhand"
        );
        jobTitleComboBox.setItems(jobTitles);
    }

    /**
     * Populates shift ComboBox
     */
    private void populateShifts() {
        ObservableList<String> shifts = FXCollections.observableArrayList(
                "morning",
                "evening"
        );
        shiftComboBox.setItems(shifts);
    }

    /**
     * Loads all supervisors from database
     */
    private void loadSupervisors() {
        allSupervisors = personnelDAO.getAllSupervisors();

        ObservableList<String> supervisorNames = FXCollections.observableArrayList();
        for (Personnel supervisor : allSupervisors) {
            supervisorNames.add(supervisor.getFullName() + " (ID: " + supervisor.getId() + ")");
        }

        if (supervisorComboBox != null) {
            supervisorComboBox.setItems(supervisorNames);
        }
    }

    /**
     * Setup job title listener to handle supervisor field visibility
     */
    private void setupJobTitleListener() {
        jobTitleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                boolean isFarmhand = "farmhand".equalsIgnoreCase(newVal);

                // Show/hide supervisor field
                if (supervisorComboBox != null) {
                    supervisorComboBox.setVisible(isFarmhand);
                    supervisorComboBox.setManaged(isFarmhand);
                }

                if (supervisorErrorLabel != null) {
                    supervisorErrorLabel.setVisible(isFarmhand);
                    supervisorErrorLabel.setManaged(isFarmhand);
                }

                // Show warning if farmhand selected but no supervisors exist
                if (isFarmhand && allSupervisors.isEmpty()) {
                    if (noSupervisorWarningLabel != null) {
                        noSupervisorWarningLabel.setVisible(true);
                        noSupervisorWarningLabel.setManaged(true);
                    }
                    // Disable farmhand option
                    showError("Aucun superviseur disponible. Ajoutez un superviseur d'abord.");
                } else {
                    if (noSupervisorWarningLabel != null) {
                        noSupervisorWarningLabel.setVisible(false);
                        noSupervisorWarningLabel.setManaged(false);
                    }
                }
            }
        });
    }

    /**
     * Sets the dialog stage
     * @param dialogStage the stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets personnel for editing (null for new personnel)
     * @param personnel the personnel to edit, or null
     */
    public void setPersonnel(Personnel personnel) {
        this.personnel = personnel;

        if (personnel != null) {
            // EDIT MODE
            dialogTitle.setText("Modifier Personnel");
            populateFields();
        } else {
            // ADD MODE
            dialogTitle.setText("Ajouter Personnel");
            hireDatePicker.setValue(LocalDate.now());
        }
    }

    /**
     * Populates form fields with existing personnel data
     */
    private void populateFields() {
        fullNameField.setText(personnel.getFullName());
        ageField.setText(String.valueOf(personnel.getAge()));
        phoneField.setText(personnel.getPhone());
        emailField.setText(personnel.getEmail());
        jobTitleComboBox.setValue(personnel.getJobTitle());
        hireDatePicker.setValue(personnel.getHireDate());
        salaryField.setText(String.valueOf(personnel.getSalary()));
        shiftComboBox.setValue(personnel.getShift());
        addressTextArea.setText(personnel.getAddress());
        emergencyContactField.setText(personnel.getEmergencyContact());

        // Set supervisor if farmhand
        if (personnel.hasSupervisor()) {
            Personnel supervisor = personnelDAO.getPersonnelById(personnel.getSupervisorId());
            if (supervisor != null) {
                supervisorComboBox.setValue(supervisor.getFullName() + " (ID: " + supervisor.getId() + ")");
            }
        }
    }

    /**
     * Handle save button click
     */
    @FXML
    public void handleSave() {
        if (validateInput()) {
            try {
                // Create or update personnel object
                if (personnel == null) {
                    personnel = new Personnel();
                }

                // Set all fields
                personnel.setFullName(fullNameField.getText().trim());
                personnel.setAge(Integer.parseInt(ageField.getText().trim()));
                personnel.setPhone(phoneField.getText().trim());
                personnel.setEmail(emailField.getText().trim());
                personnel.setJobTitle(jobTitleComboBox.getValue());
                personnel.setHireDate(hireDatePicker.getValue());
                personnel.setSalary(Double.parseDouble(salaryField.getText().trim()));
                personnel.setShift(shiftComboBox.getValue());
                personnel.setAddress(addressTextArea.getText().trim());
                personnel.setEmergencyContact(emergencyContactField.getText().trim());
                personnel.setActive(true); // Always active when creating/editing

                // Set supervisor if farmhand
                if ("farmhand".equalsIgnoreCase(jobTitleComboBox.getValue())) {
                    Integer supervisorId = extractSupervisorId(supervisorComboBox.getValue());
                    personnel.setSupervisorId(supervisorId);
                } else {
                    personnel.setSupervisorId(null);
                }

                // Save to database
                boolean success;
                if (personnel.getId() == 0) {
                    // CREATE
                    success = personnelDAO.createPersonnel(personnel);
                } else {
                    // UPDATE
                    success = personnelDAO.updatePersonnel(personnel);
                }

                if (success) {
                    saveClicked = true;
                    dialogStage.close();
                } else {
                    showError("Échec de l'enregistrement du personnel");
                }

            } catch (Exception e) {
                showError("Erreur lors de l'enregistrement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Extracts supervisor ID from ComboBox selection
     * Format: "Name (ID: 123)"
     */
    private Integer extractSupervisorId(String selection) {
        if (selection == null || selection.isEmpty()) {
            return null;
        }

        try {
            int startIndex = selection.indexOf("ID: ") + 4;
            int endIndex = selection.indexOf(")", startIndex);
            String idStr = selection.substring(startIndex, endIndex);
            return Integer.parseInt(idStr);
        } catch (Exception e) {
            System.err.println("Error extracting supervisor ID: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validates all input fields
     * @return true if all validations pass
     */
    private boolean validateInput() {
        hideAllErrors();
        boolean isValid = true;

        // Full Name validation
        if (ValidationUtil.isEmpty(fullNameField.getText())) {
            fullNameErrorLabel.setText("Le nom complet est requis");
            fullNameErrorLabel.setVisible(true);
            isValid = false;
        }

        // Age validation
        try {
            int age = Integer.parseInt(ageField.getText().trim());
            if (age < 18 || age > 100) {
                ageErrorLabel.setText("L'âge doit être entre 18 et 100");
                ageErrorLabel.setVisible(true);
                isValid = false;
            }
        } catch (NumberFormatException e) {
            ageErrorLabel.setText("L'âge doit être un nombre valide");
            ageErrorLabel.setVisible(true);
            isValid = false;
        }

        // Phone validation (basic)
        if (ValidationUtil.isEmpty(phoneField.getText())) {
            phoneErrorLabel.setText("Le téléphone est requis");
            phoneErrorLabel.setVisible(true);
            isValid = false;
        }

        // Email validation
        String email = emailField.getText().trim();
        if (!ValidationUtil.isValidEmail(email)) {
            emailErrorLabel.setText("Email invalide");
            emailErrorLabel.setVisible(true);
            isValid = false;
        } else {
            // Check email uniqueness (skip if editing same email)
            Personnel existingPersonnel = personnelDAO.getPersonnelByEmail(email);
            if (existingPersonnel != null) {
                if (personnel == null || existingPersonnel.getId() != personnel.getId()) {
                    emailErrorLabel.setText("Cet email existe déjà");
                    emailErrorLabel.setVisible(true);
                    isValid = false;
                }
            }
        }

        // Job Title validation
        if (jobTitleComboBox.getValue() == null) {
            jobTitleErrorLabel.setText("Le titre du poste est requis");
            jobTitleErrorLabel.setVisible(true);
            isValid = false;
        }

        // Supervisor validation (only for farmhands)
        if ("farmhand".equalsIgnoreCase(jobTitleComboBox.getValue())) {
            if (allSupervisors.isEmpty()) {
                supervisorErrorLabel.setText("Aucun superviseur disponible");
                supervisorErrorLabel.setVisible(true);
                isValid = false;
            } else if (supervisorComboBox.getValue() == null) {
                supervisorErrorLabel.setText("Le superviseur est requis pour les ouvriers");
                supervisorErrorLabel.setVisible(true);
                isValid = false;
            }
        }

        // Salary validation
        try {
            double salary = Double.parseDouble(salaryField.getText().trim());
            if (salary < 0) {
                salaryErrorLabel.setText("Le salaire doit être positif");
                salaryErrorLabel.setVisible(true);
                isValid = false;
            }
        } catch (NumberFormatException e) {
            salaryErrorLabel.setText("Le salaire doit être un nombre valide");
            salaryErrorLabel.setVisible(true);
            isValid = false;
        }

        // Shift validation
        if (shiftComboBox.getValue() == null) {
            shiftErrorLabel.setText("L'équipe est requise");
            shiftErrorLabel.setVisible(true);
            isValid = false;
        }

        return isValid;
    }

    /**
     * Hide all error labels
     */
    private void hideAllErrors() {
        fullNameErrorLabel.setVisible(false);
        ageErrorLabel.setVisible(false);
        phoneErrorLabel.setVisible(false);
        emailErrorLabel.setVisible(false);
        jobTitleErrorLabel.setVisible(false);
        supervisorErrorLabel.setVisible(false);
        salaryErrorLabel.setVisible(false);
        shiftErrorLabel.setVisible(false);
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
     * @return true if save was clicked
     */
    public boolean isSaveClicked() {
        return saveClicked;
    }

    /**
     * Show error alert
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Erreur de Validation");
        alert.setContentText(message);
        alert.showAndWait();
    }
}