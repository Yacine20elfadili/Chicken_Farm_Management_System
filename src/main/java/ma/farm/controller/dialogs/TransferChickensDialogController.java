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
 * Controller for the Transfer Chickens Dialog
 * Handles transferring chickens from FemaleEggLayer houses to FemaleMeat houses
 */
public class TransferChickensDialogController {

    @FXML
    private Label sourceHouseLabel;

    @FXML
    private Label totalChickensLabel;

    @FXML
    private Label capacityInfoLabel;

    @FXML
    private VBox destinationHousesContainer;

    @FXML
    private Label selectedCapacityLabel;

    @FXML
    private Label neededCapacityLabel;

    @FXML
    private Label capacityErrorLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Button confirmButton;

    private HouseDAO houseDAO;
    private House sourceHouse;
    private boolean saved = false;

    // Map to hold checkboxes for destination houses
    private Map<House, CheckBox> destinationCheckboxes = new HashMap<>();

    @FXML
    public void initialize() {
        houseDAO = new HouseDAO();
    }

    /**
     * Sets the source house from which chickens will be transferred
     * Must be called before showing the dialog
     */
    public void setSourceHouse(House house) {
        this.sourceHouse = house;

        // Update UI with source house info
        sourceHouseLabel.setText("Transferring from: " + house.getName());
        totalChickensLabel.setText(String.valueOf(house.getChickenCount()));
        neededCapacityLabel.setText(String.valueOf(house.getChickenCount()));

        // Load empty FemaleMeat houses
        loadDestinationHouses();
    }

    private void loadDestinationHouses() {
        destinationHousesContainer.getChildren().clear();
        destinationCheckboxes.clear();

        List<House> emptyHouses = houseDAO.getEmptyHousesByType(HouseType.MEAT_FEMALE);

        int totalCapacity = 0;
        for (House house : emptyHouses) {
            totalCapacity += house.getCapacity();
            addHouseCheckbox(house);
        }

        capacityInfoLabel.setText("Total available capacity: " + totalCapacity + " chickens");

        if (emptyHouses.isEmpty()) {
            Label noHousesLabel = new Label("No empty FemaleMeat houses available!");
            noHousesLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-style: italic;");
            destinationHousesContainer.getChildren().add(noHousesLabel);
            confirmButton.setDisable(true);
        }

        updateSelectedCapacity();
    }

    private void addHouseCheckbox(House house) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        CheckBox checkBox = new CheckBox();
        checkBox.setOnAction(e -> updateSelectedCapacity());

        Label houseLabel = new Label(house.getName());
        houseLabel.setPrefWidth(180);
        houseLabel.setStyle("-fx-font-weight: bold;");

        Label capacityLabel = new Label("Capacity: " + house.getCapacity());
        capacityLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        destinationCheckboxes.put(house, checkBox);
        row.getChildren().addAll(checkBox, houseLabel, capacityLabel);
        destinationHousesContainer.getChildren().add(row);
    }

    private void updateSelectedCapacity() {
        int selectedCapacity = 0;
        for (Map.Entry<House, CheckBox> entry : destinationCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedCapacity += entry.getKey().getCapacity();
            }
        }

        selectedCapacityLabel.setText(String.valueOf(selectedCapacity));

        int needed = sourceHouse != null ? sourceHouse.getChickenCount() : 0;

        if (selectedCapacity >= needed) {
            selectedCapacityLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
            capacityErrorLabel.setVisible(false);
        } else {
            selectedCapacityLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
            capacityErrorLabel.setText("Selected capacity is insufficient. Need at least " + needed + " capacity.");
            capacityErrorLabel.setVisible(true);
        }
    }

    @FXML
    public void handleConfirm() {
        if (sourceHouse == null) {
            showError("No source house specified.");
            return;
        }

        // Get selected houses
        List<House> selectedHouses = new ArrayList<>();
        int selectedCapacity = 0;
        for (Map.Entry<House, CheckBox> entry : destinationCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedHouses.add(entry.getKey());
                selectedCapacity += entry.getKey().getCapacity();
            }
        }

        if (selectedHouses.isEmpty()) {
            showError("Please select at least one destination house.");
            return;
        }

        int totalChickens = sourceHouse.getChickenCount();
        if (selectedCapacity < totalChickens) {
            showError("Selected houses do not have enough capacity. Need " + totalChickens + ", but only have " + selectedCapacity);
            return;
        }

        // Perform the transfer - distribute chickens across selected houses
        LocalDate today = LocalDate.now();
        boolean success = true;
        int remaining = totalChickens;

        // Sort houses by name for consistent distribution
        selectedHouses.sort((h1, h2) -> h1.getName().compareTo(h2.getName()));

        for (House destHouse : selectedHouses) {
            if (remaining <= 0) break;

            int toTransfer = Math.min(remaining, destHouse.getCapacity());
            if (!houseDAO.addChickensToHouse(destHouse.getId(), toTransfer, today)) {
                success = false;
                break;
            }
            remaining -= toTransfer;
        }

        // Reset source house
        if (success) {
            success = houseDAO.resetHouse(sourceHouse.getId());
        }

        if (success) {
            saved = true;
            closeDialog();
        } else {
            showError("Failed to transfer chickens. Please try again.");
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
