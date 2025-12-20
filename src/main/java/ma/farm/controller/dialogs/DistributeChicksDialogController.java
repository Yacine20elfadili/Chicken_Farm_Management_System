package ma.farm.controller.dialogs;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ma.farm.dao.HouseDAO;
import ma.farm.model.House;
import ma.farm.model.HouseType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Distribute Chicks Dialog
 * Handles distributing chicks from a DayOld house to FemaleEggLayer and MaleMeat houses
 */
public class DistributeChicksDialogController {

    @FXML
    private Label sourceHouseLabel;

    @FXML
    private Label totalChickensLabel;

    @FXML
    private TextField femaleCountField;

    @FXML
    private TextField maleCountField;

    @FXML
    private Label sumLabel;

    @FXML
    private Label genderErrorLabel;

    @FXML
    private Label femaleCapacityLabel;

    @FXML
    private VBox femaleHousesContainer;

    @FXML
    private Label maleCapacityLabel;

    @FXML
    private VBox maleHousesContainer;

    @FXML
    private Label errorLabel;

    @FXML
    private Button confirmButton;

    private HouseDAO houseDAO;
    private House sourceHouse;
    private boolean saved = false;

    // Maps to hold assignment text fields for each house
    private Map<House, TextField> femaleAssignmentFields = new HashMap<>();
    private Map<House, TextField> maleAssignmentFields = new HashMap<>();

    @FXML
    public void initialize() {
        houseDAO = new HouseDAO();

        // Set up number-only validation for gender count fields
        setupNumberField(femaleCountField);
        setupNumberField(maleCountField);

        // Set up listeners to update sum
        femaleCountField.textProperty().addListener((obs, oldVal, newVal) -> updateSum());
        maleCountField.textProperty().addListener((obs, oldVal, newVal) -> updateSum());
    }

    /**
     * Sets the source house from which chicks will be distributed
     * Must be called before showing the dialog
     */
    public void setSourceHouse(House house) {
        this.sourceHouse = house;

        // Update UI with source house info
        sourceHouseLabel.setText("Distributing from: " + house.getName());
        totalChickensLabel.setText(String.valueOf(house.getChickenCount()));

        // Load empty houses for each destination type
        loadFemaleHouses();
        loadMaleHouses();
    }

    private void setupNumberField(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                field.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void loadFemaleHouses() {
        femaleHousesContainer.getChildren().clear();
        femaleAssignmentFields.clear();

        List<House> emptyFemaleHouses = houseDAO.getEmptyHousesByType(HouseType.EGG_LAYER);

        int totalCapacity = 0;
        for (House house : emptyFemaleHouses) {
            totalCapacity += house.getCapacity();
            addHouseAssignmentRow(femaleHousesContainer, house, femaleAssignmentFields);
        }

        femaleCapacityLabel.setText("Total available capacity: " + totalCapacity + " chickens");

        if (emptyFemaleHouses.isEmpty()) {
            Label noHousesLabel = new Label("No empty Egg Layer houses available!");
            noHousesLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-style: italic; -fx-font-size: 12px;");
            femaleHousesContainer.getChildren().add(noHousesLabel);
        }
    }

    private void loadMaleHouses() {
        maleHousesContainer.getChildren().clear();
        maleAssignmentFields.clear();

        List<House> emptyMaleHouses = houseDAO.getEmptyHousesByType(HouseType.MEAT_MALE);

        int totalCapacity = 0;
        for (House house : emptyMaleHouses) {
            totalCapacity += house.getCapacity();
            addHouseAssignmentRow(maleHousesContainer, house, maleAssignmentFields);
        }

        maleCapacityLabel.setText("Total available capacity: " + totalCapacity + " chickens");

        if (emptyMaleHouses.isEmpty()) {
            Label noHousesLabel = new Label("No empty Male Meat houses available!");
            noHousesLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-style: italic; -fx-font-size: 12px;");
            maleHousesContainer.getChildren().add(noHousesLabel);
        }
    }

    private void addHouseAssignmentRow(VBox container, House house, Map<House, TextField> fieldMap) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #ffffff; -fx-padding: 10 12; -fx-background-radius: 5; " +
                     "-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-border-width: 1;");

        Label houseLabel = new Label(house.getName());
        houseLabel.setPrefWidth(160);
        houseLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333; -fx-font-size: 13px;");

        Label capacityLabel = new Label("Capacity: " + house.getCapacity());
        capacityLabel.setPrefWidth(100);
        capacityLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");

        Label assignLabel = new Label("Assign:");
        assignLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");

        TextField assignField = new TextField();
        assignField.setPromptText("0");
        assignField.setPrefWidth(80);
        assignField.setText("0");
        assignField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ced4da; " +
                            "-fx-border-radius: 4; -fx-background-radius: 4; -fx-text-fill: #333333;");

        // Number-only validation
        setupNumberField(assignField);

        fieldMap.put(house, assignField);
        row.getChildren().addAll(houseLabel, capacityLabel, assignLabel, assignField);
        container.getChildren().add(row);
    }

    private void updateSum() {
        int femaleCount = parseIntSafe(femaleCountField.getText());
        int maleCount = parseIntSafe(maleCountField.getText());
        int sum = femaleCount + maleCount;

        sumLabel.setText(String.valueOf(sum));

        // Check if sum equals total
        int total = sourceHouse != null ? sourceHouse.getChickenCount() : 0;
        if (sum == total) {
            sumLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #28a745; " +
                             "-fx-background-color: #e8f5e9; -fx-padding: 8 20; -fx-background-radius: 5;");
            genderErrorLabel.setVisible(false);
        } else if (sum > total) {
            sumLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #dc3545; " +
                             "-fx-background-color: #ffebee; -fx-padding: 8 20; -fx-background-radius: 5;");
            genderErrorLabel.setText("Sum exceeds total chickens (" + total + ")");
            genderErrorLabel.setVisible(true);
        } else {
            sumLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f57c00; " +
                             "-fx-background-color: #fff3e0; -fx-padding: 8 20; -fx-background-radius: 5;");
            genderErrorLabel.setText("Sum must equal total chickens (" + total + ")");
            genderErrorLabel.setVisible(true);
        }
    }

    private int parseIntSafe(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @FXML
    public void handleConfirm() {
        if (sourceHouse == null) {
            showError("No source house specified.");
            return;
        }

        // Validate gender counts
        int femaleCount = parseIntSafe(femaleCountField.getText());
        int maleCount = parseIntSafe(maleCountField.getText());
        int totalChickens = sourceHouse.getChickenCount();

        if (femaleCount + maleCount != totalChickens) {
            showError("Female + Male count must equal total chickens (" + totalChickens + ")");
            return;
        }

        // Validate female assignments
        int totalFemaleAssigned = 0;
        List<Map.Entry<House, Integer>> femaleAssignments = new ArrayList<>();
        for (Map.Entry<House, TextField> entry : femaleAssignmentFields.entrySet()) {
            int assigned = parseIntSafe(entry.getValue().getText());
            if (assigned > 0) {
                if (assigned > entry.getKey().getCapacity()) {
                    showError("Assignment for " + entry.getKey().getName() + " exceeds its capacity (" + entry.getKey().getCapacity() + ")");
                    return;
                }
                femaleAssignments.add(Map.entry(entry.getKey(), assigned));
                totalFemaleAssigned += assigned;
            }
        }

        if (totalFemaleAssigned != femaleCount) {
            showError("Female house assignments (" + totalFemaleAssigned + ") must equal female count (" + femaleCount + ")");
            return;
        }

        // Validate male assignments
        int totalMaleAssigned = 0;
        List<Map.Entry<House, Integer>> maleAssignments = new ArrayList<>();
        for (Map.Entry<House, TextField> entry : maleAssignmentFields.entrySet()) {
            int assigned = parseIntSafe(entry.getValue().getText());
            if (assigned > 0) {
                if (assigned > entry.getKey().getCapacity()) {
                    showError("Assignment for " + entry.getKey().getName() + " exceeds its capacity (" + entry.getKey().getCapacity() + ")");
                    return;
                }
                maleAssignments.add(Map.entry(entry.getKey(), assigned));
                totalMaleAssigned += assigned;
            }
        }

        if (totalMaleAssigned != maleCount) {
            showError("Male house assignments (" + totalMaleAssigned + ") must equal male count (" + maleCount + ")");
            return;
        }

        // Perform the distribution
        LocalDate today = LocalDate.now();
        boolean success = true;

        // Add chickens to female houses
        for (Map.Entry<House, Integer> assignment : femaleAssignments) {
            if (!houseDAO.addChickensToHouse(assignment.getKey().getId(), assignment.getValue(), today)) {
                success = false;
                break;
            }
        }

        // Add chickens to male houses
        if (success) {
            for (Map.Entry<House, Integer> assignment : maleAssignments) {
                if (!houseDAO.addChickensToHouse(assignment.getKey().getId(), assignment.getValue(), today)) {
                    success = false;
                    break;
                }
            }
        }

        // Reset source house
        if (success) {
            success = houseDAO.resetHouse(sourceHouse.getId());
        }

        if (success) {
            saved = true;
            closeDialog();
        } else {
            showError("Failed to distribute chicks. Please try again.");
        }
    }

    @FXML
    public void handleCancel() {
        saved = false;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
