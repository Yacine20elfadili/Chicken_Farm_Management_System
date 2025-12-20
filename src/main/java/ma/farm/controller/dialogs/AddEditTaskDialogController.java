package ma.farm.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ma.farm.dao.HouseDAO;
import ma.farm.dao.PersonnelDAO;
import ma.farm.dao.TaskDAO;
import ma.farm.model.House;
import ma.farm.model.HouseType;
import ma.farm.model.Personnel;
import ma.farm.model.Task;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AddEditTaskDialogController - Handles Add/Edit Task Dialog
 *
 * Features:
 * - Create new tasks with all required fields
 * - Edit existing tasks
 * - Smart filtering of personnel based on category
 * - Smart filtering of houses based on category
 * - Cracked eggs field for Collection category
 * - Validation of required fields
 */
public class AddEditTaskDialogController {

    // FXML Components - Header
    @FXML private Label dialogTitle;

    // FXML Components - Form Fields
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<String> houseComboBox;
    @FXML private ComboBox<String> assignedToComboBox;
    @FXML private Spinner<Integer> crackedEggsSpinner;
    @FXML private TextArea notesField;

    // FXML Components - Sections
    @FXML private VBox houseSection;
    @FXML private VBox crackedEggsSection;

    // FXML Components - Error Labels
    @FXML private Label descriptionErrorLabel;
    @FXML private Label categoryErrorLabel;
    @FXML private Label dueDateErrorLabel;

    // FXML Components - Buttons
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // DAOs
    private TaskDAO taskDAO;
    private HouseDAO houseDAO;
    private PersonnelDAO personnelDAO;

    // State
    private Stage dialogStage;
    private Task task; // null for Add, populated for Edit
    private boolean saveClicked = false;

    // Mappings for ComboBox values to IDs
    private Map<String, Integer> houseNameToIdMap = new HashMap<>();
    private Map<String, String> personnelDisplayToNameMap = new HashMap<>();

    // Category constants
    private static final String CAT_FEEDING = "Alimentation";
    private static final String CAT_CLEANING = "Nettoyage";
    private static final String CAT_COLLECTION = "Collecte";
    private static final String CAT_MEDICAL = "Vétérinaire";
    private static final String CAT_INVENTORY = "Inventaire";
    private static final String CAT_ADMINISTRATIVE = "Administratif";

    // Priority constants
    private static final String PRIORITY_HIGH = "Haute";
    private static final String PRIORITY_MEDIUM = "Moyenne";
    private static final String PRIORITY_LOW = "Basse";

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        // Initialize DAOs
        taskDAO = new TaskDAO();
        houseDAO = new HouseDAO();
        personnelDAO = new PersonnelDAO();

        // Setup category ComboBox
        setupCategoryComboBox();

        // Setup priority ComboBox
        setupPriorityComboBox();

        // Setup cracked eggs spinner
        setupCrackedEggsSpinner();

        // Setup date picker default
        if (dueDatePicker != null) {
            dueDatePicker.setValue(LocalDate.now());
        }

        // Hide error labels initially
        hideAllErrors();

        // Setup category change listener
        setupCategoryListener();

        // Load initial data
        loadHouses(null);
        loadPersonnel(null);
    }

    /**
     * Setup category ComboBox with all task categories
     */
    private void setupCategoryComboBox() {
        if (categoryComboBox == null) return;

        ObservableList<String> categories = FXCollections.observableArrayList(
            CAT_FEEDING,
            CAT_CLEANING,
            CAT_COLLECTION,
            CAT_MEDICAL,
            CAT_INVENTORY,
            CAT_ADMINISTRATIVE
        );
        categoryComboBox.setItems(categories);
    }

    /**
     * Setup priority ComboBox
     */
    private void setupPriorityComboBox() {
        if (priorityComboBox == null) return;

        ObservableList<String> priorities = FXCollections.observableArrayList(
            PRIORITY_HIGH,
            PRIORITY_MEDIUM,
            PRIORITY_LOW
        );
        priorityComboBox.setItems(priorities);
        priorityComboBox.setValue(PRIORITY_MEDIUM); // Default
    }

    /**
     * Setup cracked eggs spinner
     */
    private void setupCrackedEggsSpinner() {
        if (crackedEggsSpinner == null) return;

        SpinnerValueFactory<Integer> valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0);
        crackedEggsSpinner.setValueFactory(valueFactory);
        crackedEggsSpinner.setEditable(true);
    }

    /**
     * Setup listener for category changes to update house/personnel filters
     */
    private void setupCategoryListener() {
        if (categoryComboBox == null) return;

        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                onCategoryChanged(newVal);
            }
        });
    }

    /**
     * Handle category change - update UI based on selected category
     */
    private void onCategoryChanged(String category) {
        // Show/hide cracked eggs section for Collection category
        boolean isCollection = CAT_COLLECTION.equals(category);
        if (crackedEggsSection != null) {
            crackedEggsSection.setVisible(isCollection);
            crackedEggsSection.setManaged(isCollection);
        }

        // Show/hide house section for relevant categories
        boolean needsHouse = CAT_FEEDING.equals(category) ||
                            CAT_CLEANING.equals(category) ||
                            CAT_COLLECTION.equals(category) ||
                            CAT_MEDICAL.equals(category);
        if (houseSection != null) {
            houseSection.setVisible(needsHouse);
            houseSection.setManaged(needsHouse);
        }

        // Reload houses based on category
        loadHouses(category);

        // Reload personnel based on category
        loadPersonnel(category);
    }

    /**
     * Load houses based on category filter
     */
    private void loadHouses(String category) {
        if (houseComboBox == null) return;

        houseNameToIdMap.clear();
        ObservableList<String> houseNames = FXCollections.observableArrayList();

        // Add empty option
        houseNames.add("");

        try {
            List<House> houses;

            if (CAT_COLLECTION.equals(category)) {
                // Only egg-laying houses for Collection
                houses = houseDAO.getAllHouses();
                for (House house : houses) {
                    if (house.getType() == HouseType.EGG_LAYER ||
                        house.getType() == HouseType.MEAT_FEMALE) {
                        if (house.getChickenCount() > 0) {
                            houseNames.add(house.getName());
                            houseNameToIdMap.put(house.getName(), house.getId());
                        }
                    }
                }
            } else if (CAT_FEEDING.equals(category) || CAT_CLEANING.equals(category)) {
                // Only occupied houses for Feeding/Cleaning
                houses = houseDAO.getAllHouses();
                for (House house : houses) {
                    if (house.getChickenCount() > 0) {
                        houseNames.add(house.getName());
                        houseNameToIdMap.put(house.getName(), house.getId());
                    }
                }
            } else if (CAT_MEDICAL.equals(category)) {
                // All houses for Medical
                houses = houseDAO.getAllHouses();
                for (House house : houses) {
                    houseNames.add(house.getName());
                    houseNameToIdMap.put(house.getName(), house.getId());
                }
            } else {
                // No houses for Inventory/Administrative
                houses = houseDAO.getAllHouses();
                for (House house : houses) {
                    houseNames.add(house.getName());
                    houseNameToIdMap.put(house.getName(), house.getId());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading houses: " + e.getMessage());
        }

        houseComboBox.setItems(houseNames);
    }

    /**
     * Load personnel based on category filter
     */
    private void loadPersonnel(String category) {
        if (assignedToComboBox == null) return;

        personnelDisplayToNameMap.clear();
        ObservableList<String> personnelNames = FXCollections.observableArrayList();

        // Add empty option
        personnelNames.add("");

        try {
            List<Personnel> allPersonnel = personnelDAO.getAllPersonnel();

            for (Personnel person : allPersonnel) {
                if (!person.isActive()) continue;

                boolean shouldInclude = false;
                String jobTitle = person.getJobTitle();

                if (category == null) {
                    // Include all farm personnel
                    shouldInclude = person.isFarm();
                } else if (CAT_FEEDING.equals(category) || CAT_CLEANING.equals(category) || CAT_COLLECTION.equals(category)) {
                    // Farmhand workers
                    shouldInclude = "farmhand_supervisor".equals(jobTitle) ||
                                   "farmhand_subordinate".equals(jobTitle);
                } else if (CAT_MEDICAL.equals(category)) {
                    // Veterinary workers
                    shouldInclude = "veterinary_supervisor".equals(jobTitle) ||
                                   "veterinary_subordinate".equals(jobTitle);
                } else if (CAT_INVENTORY.equals(category)) {
                    // Inventory workers
                    shouldInclude = "inventory_supervisor".equals(jobTitle) ||
                                   "inventory_subordinate".equals(jobTitle);
                } else if (CAT_ADMINISTRATIVE.equals(category)) {
                    // Admin staff
                    shouldInclude = "admin_staff".equals(jobTitle) ||
                                   "cashier".equals(jobTitle);
                }

                if (shouldInclude) {
                    String displayName = person.getFullName() + " (" + getJobTitleDisplayName(jobTitle) + ")";
                    personnelNames.add(displayName);
                    personnelDisplayToNameMap.put(displayName, person.getFullName());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading personnel: " + e.getMessage());
        }

        assignedToComboBox.setItems(personnelNames);
    }

    /**
     * Get display name for job title
     */
    private String getJobTitleDisplayName(String jobTitle) {
        if (jobTitle == null) return "";
        switch (jobTitle) {
            case "farmhand_supervisor": return "Chef Ouvrier";
            case "farmhand_subordinate": return "Ouvrier";
            case "veterinary_supervisor": return "Chef Vétérinaire";
            case "veterinary_subordinate": return "Vétérinaire";
            case "inventory_supervisor": return "Chef Inventaire";
            case "inventory_subordinate": return "Inventaire";
            case "admin_staff": return "Admin";
            case "cashier": return "Caissier";
            default: return jobTitle;
        }
    }

    /**
     * Hide all error labels
     */
    private void hideAllErrors() {
        if (descriptionErrorLabel != null) {
            descriptionErrorLabel.setVisible(false);
            descriptionErrorLabel.setManaged(false);
        }
        if (categoryErrorLabel != null) {
            categoryErrorLabel.setVisible(false);
            categoryErrorLabel.setManaged(false);
        }
        if (dueDateErrorLabel != null) {
            dueDateErrorLabel.setVisible(false);
            dueDateErrorLabel.setManaged(false);
        }
    }

    /**
     * Set the dialog stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Set task for editing (null for new task)
     */
    public void setTask(Task task) {
        this.task = task;

        if (task != null) {
            // Edit mode - populate fields
            if (dialogTitle != null) {
                dialogTitle.setText("Modifier Tâche");
            }

            if (descriptionField != null) {
                descriptionField.setText(task.getDescription());
            }

            if (categoryComboBox != null && task.getCategory() != null) {
                categoryComboBox.setValue(mapCategoryToDisplay(task.getCategory()));
            }

            if (priorityComboBox != null && task.getPriority() != null) {
                priorityComboBox.setValue(mapPriorityToDisplay(task.getPriority()));
            }

            if (dueDatePicker != null && task.getDueDate() != null) {
                dueDatePicker.setValue(task.getDueDate());
            }

            if (houseComboBox != null && task.getHouseId() > 0) {
                // Find house name by ID
                try {
                    House house = houseDAO.getHouseById(task.getHouseId());
                    if (house != null) {
                        houseComboBox.setValue(house.getName());
                    }
                } catch (Exception e) {
                    System.err.println("Error loading house: " + e.getMessage());
                }
            }

            if (assignedToComboBox != null && task.getAssignedTo() != null) {
                // Find matching display name
                for (Map.Entry<String, String> entry : personnelDisplayToNameMap.entrySet()) {
                    if (entry.getValue().equals(task.getAssignedTo())) {
                        assignedToComboBox.setValue(entry.getKey());
                        break;
                    }
                }
                // If not found in map, just set the raw value
                if (assignedToComboBox.getValue() == null || assignedToComboBox.getValue().isEmpty()) {
                    assignedToComboBox.setValue(task.getAssignedTo());
                }
            }

            if (crackedEggsSpinner != null) {
                crackedEggsSpinner.getValueFactory().setValue(task.getCrackedEggs());
            }

            if (notesField != null) {
                notesField.setText(task.getNotes());
            }
        } else {
            // Add mode
            if (dialogTitle != null) {
                dialogTitle.setText("Nouvelle Tâche");
            }
        }
    }

    /**
     * Map database category to display category
     */
    private String mapCategoryToDisplay(String dbCategory) {
        if (dbCategory == null) return null;
        switch (dbCategory) {
            case "Feeding": return CAT_FEEDING;
            case "Cleaning": return CAT_CLEANING;
            case "Collection": return CAT_COLLECTION;
            case "Medical": return CAT_MEDICAL;
            case "Inventory": return CAT_INVENTORY;
            case "Administrative": return CAT_ADMINISTRATIVE;
            default: return dbCategory;
        }
    }

    /**
     * Map display category to database category
     */
    private String mapCategoryToDb(String displayCategory) {
        if (displayCategory == null) return null;
        switch (displayCategory) {
            case CAT_FEEDING: return "Feeding";
            case CAT_CLEANING: return "Cleaning";
            case CAT_COLLECTION: return "Collection";
            case CAT_MEDICAL: return "Medical";
            case CAT_INVENTORY: return "Inventory";
            case CAT_ADMINISTRATIVE: return "Administrative";
            default: return displayCategory;
        }
    }

    /**
     * Map database priority to display priority
     */
    private String mapPriorityToDisplay(String dbPriority) {
        if (dbPriority == null) return PRIORITY_MEDIUM;
        switch (dbPriority) {
            case "High": return PRIORITY_HIGH;
            case "Medium": return PRIORITY_MEDIUM;
            case "Low": return PRIORITY_LOW;
            default: return dbPriority;
        }
    }

    /**
     * Map display priority to database priority
     */
    private String mapPriorityToDb(String displayPriority) {
        if (displayPriority == null) return "Medium";
        switch (displayPriority) {
            case PRIORITY_HIGH: return "High";
            case PRIORITY_MEDIUM: return "Medium";
            case PRIORITY_LOW: return "Low";
            default: return displayPriority;
        }
    }

    /**
     * Check if save was clicked
     */
    public boolean isSaveClicked() {
        return saveClicked;
    }

    /**
     * Handle save button click
     */
    @FXML
    private void handleSave() {
        if (validateInput()) {
            // Create or update task
            if (task == null) {
                task = new Task();
                task.setStatus("Pending");
            }

            // Set values from form
            task.setDescription(descriptionField.getText().trim());
            task.setCategory(mapCategoryToDb(categoryComboBox.getValue()));
            task.setPriority(mapPriorityToDb(priorityComboBox.getValue()));
            task.setDueDate(dueDatePicker.getValue());

            // Set house ID
            String selectedHouse = houseComboBox.getValue();
            if (selectedHouse != null && !selectedHouse.isEmpty() && houseNameToIdMap.containsKey(selectedHouse)) {
                task.setHouseId(houseNameToIdMap.get(selectedHouse));
            } else {
                task.setHouseId(0);
            }

            // Set assigned to
            String selectedPersonnel = assignedToComboBox.getValue();
            if (selectedPersonnel != null && !selectedPersonnel.isEmpty()) {
                String actualName = personnelDisplayToNameMap.getOrDefault(selectedPersonnel, selectedPersonnel);
                task.setAssignedTo(actualName);
            } else {
                task.setAssignedTo(null);
            }

            // Set cracked eggs if Collection category
            if (CAT_COLLECTION.equals(categoryComboBox.getValue())) {
                task.setCrackedEggs(crackedEggsSpinner.getValue());
            } else {
                task.setCrackedEggs(0);
            }

            // Set notes
            task.setNotes(notesField.getText());

            // Save to database
            boolean success;
            if (task.getId() > 0) {
                // Update existing
                success = taskDAO.updateTask(task);
            } else {
                // Add new
                success = taskDAO.addTask(task);
            }

            if (success) {
                saveClicked = true;
                dialogStage.close();
            } else {
                showErrorAlert("Erreur", "Impossible de sauvegarder la tâche",
                    "Une erreur s'est produite lors de la sauvegarde.");
            }
        }
    }

    /**
     * Handle cancel button click
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Validate form input
     */
    private boolean validateInput() {
        boolean valid = true;
        hideAllErrors();

        // Validate description
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            showError(descriptionErrorLabel, "La description est requise");
            valid = false;
        }

        // Validate category
        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().isEmpty()) {
            showError(categoryErrorLabel, "La catégorie est requise");
            valid = false;
        }

        // Validate due date
        if (dueDatePicker.getValue() == null) {
            showError(dueDateErrorLabel, "La date d'échéance est requise");
            valid = false;
        }

        return valid;
    }

    /**
     * Show error label with message
     */
    private void showError(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    /**
     * Show error alert dialog
     */
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Get the task (after save)
     */
    public Task getTask() {
        return task;
    }
}
