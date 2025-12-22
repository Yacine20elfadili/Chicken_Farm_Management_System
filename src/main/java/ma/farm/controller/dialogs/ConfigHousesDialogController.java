package ma.farm.controller.dialogs;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ma.farm.dao.HouseDAO;
import ma.farm.model.House;
import ma.farm.model.HouseType;

import java.util.*;

/**
 * Controller for the Config Houses Dialog (Automatic)
 */
public class ConfigHousesDialogController {

    @FXML
    private HBox warningBox;
    @FXML
    private Label warningLabel;

    // Top Section
    @FXML
    private Spinner<Integer> totalHousesSpinner;
    @FXML
    private FlowPane inputsContainer;

    // Bottom Section
    @FXML
    private VBox dayOldResultContainer;
    @FXML
    private VBox eggLayerResultContainer;
    @FXML
    private VBox femaleMeatResultContainer;
    @FXML
    private VBox maleMeatResultContainer;

    @FXML
    private Label dayOldTotalLabel;
    @FXML
    private Label eggLayerTotalLabel;
    @FXML
    private Label femaleMeatTotalLabel;
    @FXML
    private Label maleMeatTotalLabel;

    // Stats
    @FXML
    private Label maxImportLabel;
    @FXML
    private Label bottleneckLabel;
    @FXML
    private HBox loadingBox;

    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private HouseDAO houseDAO;
    private boolean saved = false;

    // Data Model
    private Map<Integer, TextField> inputFields = new HashMap<>();
    private List<HouseConfig> houseConfigs = new ArrayList<>();
    private Map<HouseConfig, HouseType> currentAssignment = new HashMap<>();
    private int maxImportLimit = 0;

    // Constants
    private static final int DEFAULT_CAPACITY = 10000;
    private static final int MIN_HOUSES = 4;
    private static final int MAX_HOUSES = 20;

    private static class HouseConfig {
        int id;
        int capacity;

        HouseConfig(int id, int capacity) {
            this.id = id;
            this.capacity = capacity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            HouseConfig that = (HouseConfig) o;
            return id == that.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    @FXML
    public void initialize() {
        houseDAO = new HouseDAO();

        if (houseDAO.hasAnyChickens()) {
            warningBox.setVisible(true);
            warningBox.setManaged(true);
            saveButton.setDisable(true);
            inputsContainer.setDisable(true);
        }

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_HOUSES,
                MAX_HOUSES, 4);
        totalHousesSpinner.setValueFactory(valueFactory);

        totalHousesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateInputFields(newVal);
            recalculate();
        });

        updateInputFields(4);
        Platform.runLater(this::recalculate);
    }

    private void updateInputFields(int total) {
        while (inputFields.size() < total) {
            addInputField(inputFields.size() + 1);
        }
        while (inputFields.size() > total) {
            removeInputField(inputFields.size());
        }
    }

    private void addInputField(int id) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("House " + id);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        TextField field = new TextField(String.valueOf(DEFAULT_CAPACITY));
        field.setPrefWidth(90);

        field.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*")) {
                field.setText(val.replaceAll("[^\\d]", ""));
            }
            recalculate();
        });

        box.getChildren().addAll(label, field);
        inputsContainer.getChildren().add(box);
        inputFields.put(id, field);
    }

    private void removeInputField(int id) {
        inputFields.remove(id);
        if (inputsContainer.getChildren().size() > 0) {
            inputsContainer.getChildren().remove(inputsContainer.getChildren().size() - 1);
        }
    }

    private void recalculate() {
        houseConfigs.clear();
        for (Map.Entry<Integer, TextField> entry : inputFields.entrySet()) {
            int cap = 0;
            try {
                String text = entry.getValue().getText();
                if (text != null && !text.isEmpty()) {
                    cap = Integer.parseInt(text.replaceAll("[^\\d]", ""));
                }
            } catch (Exception e) {
            }
            if (cap < 0)
                cap = 0;
            houseConfigs.add(new HouseConfig(entry.getKey(), cap));
        }

        if (houseConfigs.size() < MIN_HOUSES) {
            clearResults();
            saveButton.setDisable(true);
            return;
        }

        optimizeAllocation();
    }

    private void optimizeAllocation() {
        currentAssignment.clear();

        // Step 1: Sort Inputs
        List<HouseConfig> sorted = new ArrayList<>(houseConfigs);
        sorted.sort((h1, h2) -> Integer.compare(h2.capacity, h1.capacity));

        List<HouseConfig> fHouses = new ArrayList<>();
        List<HouseConfig> eHouses = new ArrayList<>();
        List<HouseConfig> dHouses = new ArrayList<>();
        List<HouseConfig> mHouses = new ArrayList<>();

        // Allocation Helper List
        LinkedList<HouseConfig> pool = new LinkedList<>(sorted);

        // A. Minimum Requirement: MaleMeat (Smallest)
        if (!pool.isEmpty())
            mHouses.add(pool.removeLast());

        // B. Maximize EggLayer (Target ~65% of remaining capacity)
        // We take the largest available houses for E initially
        int targetE = Math.max(1, (int) Math.ceil(pool.size() * 0.65));
        for (int i = 0; i < targetE && !pool.isEmpty(); i++) {
            eHouses.add(pool.removeFirst());
        }

        // C. Balance FemaleMeat: Satisfy F >= E constraint
        // Take from remaining pool first
        long eTotal = sum(eHouses);
        long fTotal = 0;

        while (!pool.isEmpty() && fTotal < eTotal) {
            HouseConfig h = pool.removeFirst(); // Take largest remaining
            fHouses.add(h);
            fTotal += h.capacity;
        }

        // If pool is empty but F < E, must take from E
        // Take smallest E houses to minimize E loss while boosting F
        while (fTotal < eTotal && eHouses.size() > 1) {
            HouseConfig move = eHouses.remove(eHouses.size() - 1); // Remove smallest E
            fHouses.add(move);
            fTotal += move.capacity;
            eTotal -= move.capacity;
        }

        // D. DayOld: Gets remaining
        dHouses.addAll(pool);

        // E. Minimum Constraint Check (Ensure 1 per category)
        // If D is empty, steal from E or F (whichever has surplus or is valid)
        if (dHouses.isEmpty()) {
            // Try taking from E if E has >1
            if (eHouses.size() > 1) {
                dHouses.add(eHouses.remove(eHouses.size() - 1));
            } else if (fHouses.size() > 1) {
                dHouses.add(fHouses.remove(fHouses.size() - 1));
            }
        }
        // If F is empty (rare), steal from E
        if (fHouses.isEmpty() && eHouses.size() > 1) {
            fHouses.add(eHouses.remove(eHouses.size() - 1));
        }

        // Step 4: Iterative Optimization (Hill Climbing)
        runInteractiveOptimization(dHouses, eHouses, mHouses, fHouses);

        // Calculate Final Stats
        CalculationResult res = calculateX(dHouses, eHouses, mHouses, fHouses);

        // Save Results
        saveAssignment(dHouses, HouseType.DAY_OLD);
        saveAssignment(eHouses, HouseType.EGG_LAYER);
        saveAssignment(mHouses, HouseType.MEAT_MALE);
        saveAssignment(fHouses, HouseType.MEAT_FEMALE);

        this.maxImportLimit = (int) res.X;

        // Display
        maxImportLabel.setText(String.format("%,d", (int) res.X));
        bottleneckLabel.setText(res.bottleneckType.getDisplayName());
        updateResultsUI();
        saveButton.setDisable(false);
    }

    // This helper runs the "Attempts" to find the optimal result
    private void runInteractiveOptimization(List<HouseConfig> d, List<HouseConfig> e, List<HouseConfig> m,
            List<HouseConfig> f) {
        boolean improved = true;
        while (improved) {
            improved = false;
            CalculationResult current = calculateX(d, e, m, f);

            double bestX = current.X;
            HouseConfig bestMoveHouse = null;
            List<HouseConfig> bestSourceList = null;
            List<HouseConfig> bestTargetList = getListByType(current.bottleneckType, d, e, m, f);

            if (bestTargetList == null)
                break;

            // Try moving every possible house into the Bottleneck category to see if it
            // improves X
            for (List<HouseConfig> src : Arrays.asList(d, e, m, f)) {
                if (src == bestTargetList || src.size() <= 1)
                    continue;

                for (HouseConfig h : new ArrayList<>(src)) {
                    src.remove(h);
                    bestTargetList.add(h);

                    if (isValidDistribution(d, e, m, f)) {
                        CalculationResult res = calculateX(d, e, m, f);
                        if (res.X > bestX) {
                            bestX = res.X;
                            bestMoveHouse = h;
                            bestSourceList = src;
                        }
                    }

                    bestTargetList.remove(h);
                    src.add(h);
                }
            }

            if (bestMoveHouse != null && bestSourceList != null) {
                bestSourceList.remove(bestMoveHouse);
                bestTargetList.add(bestMoveHouse);
                improved = true;
            }
        }
    }

    // Checks if the distribution is valid (e.g. E <= F)
    private boolean isValidDistribution(List<HouseConfig> d, List<HouseConfig> e, List<HouseConfig> m,
            List<HouseConfig> f) {
        if (d.isEmpty() || e.isEmpty() || m.isEmpty() || f.isEmpty())
            return false;
        long D = sum(d);
        long E = sum(e);
        long M = sum(m);
        long F = sum(f);
        if (E > F)
            return false;
        if (D > E + M)
            return false;
        return true;
    }

    private List<HouseConfig> getListByType(HouseType type, List<HouseConfig> d, List<HouseConfig> e,
            List<HouseConfig> m, List<HouseConfig> f) {
        switch (type) {
            case DAY_OLD:
                return d;
            case EGG_LAYER:
                return e;
            case MEAT_MALE:
                return m;
            case MEAT_FEMALE:
                return f;
            default:
                return null;
        }
    }

    private void saveAssignment(List<HouseConfig> list, HouseType type) {
        for (HouseConfig h : list)
            currentAssignment.put(h, type);
    }

    private long sum(List<HouseConfig> list) {
        return list.stream().mapToLong(h -> h.capacity).sum();
    }

    private static class CalculationResult {
        double X;
        HouseType bottleneckType;

        CalculationResult(double X, HouseType bottleneckType) {
            this.X = X;
            this.bottleneckType = bottleneckType;
        }
    }

    private CalculationResult calculateX(List<HouseConfig> d, List<HouseConfig> e, List<HouseConfig> m,
            List<HouseConfig> f) {
        long D = sum(d);
        long E = sum(e);
        long M = sum(m);
        long F = sum(f);

        double lD = D * 0.9;
        double lE = E * (0.125 / 0.7);
        double lM = M * (0.8 / 0.3);
        double lF = F * (1.33 / (0.7 * (8.0 / 64.0)));

        double X = Math.min(Math.min(lD, lE), Math.min(lM, lF));

        HouseType bn = HouseType.DAY_OLD;
        if (X == lE)
            bn = HouseType.EGG_LAYER;
        else if (X == lM)
            bn = HouseType.MEAT_MALE;
        else if (X == lF)
            bn = HouseType.MEAT_FEMALE;

        return new CalculationResult(X, bn);
    }

    private void updateResultsUI() {
        clearResults(false);

        Map<HouseType, List<HouseConfig>> groups = new HashMap<>();
        currentAssignment.forEach((h, type) -> groups.computeIfAbsent(type, k -> new ArrayList<>()).add(h));

        populateResultColumn(dayOldResultContainer, groups.get(HouseType.DAY_OLD), dayOldTotalLabel);
        populateResultColumn(eggLayerResultContainer, groups.get(HouseType.EGG_LAYER), eggLayerTotalLabel);
        populateResultColumn(femaleMeatResultContainer, groups.get(HouseType.MEAT_FEMALE), femaleMeatTotalLabel);
        populateResultColumn(maleMeatResultContainer, groups.get(HouseType.MEAT_MALE), maleMeatTotalLabel);
    }

    private void populateResultColumn(VBox container, List<HouseConfig> list, Label totalLabel) {
        container.getChildren().clear();
        if (list == null || list.isEmpty()) {
            totalLabel.setText("Total: 0");
            return;
        }

        long total = 0;
        list.sort(Comparator.comparingInt(h -> h.id));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            HouseConfig h = list.get(i);
            sb.append("h").append(h.id).append("(").append(String.format("%,d", h.capacity)).append(")");
            if (i < list.size() - 1) {
                sb.append(" , ");
            }
            total += h.capacity;
        }

        Label content = new Label(sb.toString());
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 11px; -fx-text-fill: #333;");
        container.getChildren().add(content);

        totalLabel.setText("Total: " + String.format("%,d", total));
    }

    private void clearResults() {
        clearResults(true);
    }

    private void clearResults(boolean resetStats) {
        dayOldResultContainer.getChildren().clear();
        eggLayerResultContainer.getChildren().clear();
        femaleMeatResultContainer.getChildren().clear();
        maleMeatResultContainer.getChildren().clear();

        dayOldTotalLabel.setText("Total: 0");
        eggLayerTotalLabel.setText("Total: 0");
        femaleMeatTotalLabel.setText("Total: 0");
        maleMeatTotalLabel.setText("Total: 0");

        if (resetStats) {
            maxImportLabel.setText("-");
            bottleneckLabel.setText("-");
        }
    }

    @FXML
    public void handleSave() {
        if (currentAssignment.isEmpty())
            return;

        loadingBox.setVisible(true);
        saveButton.setDisable(true);

        new Thread(() -> {
            try {
                houseDAO.deleteAllHouses();
                Map<HouseType, Integer> typeCounters = new HashMap<>();
                houseConfigs.sort(Comparator.comparingInt(h -> h.id));
                boolean success = true;
                for (HouseConfig cfg : houseConfigs) {
                    HouseType type = currentAssignment.get(cfg);
                    int typeIndex = typeCounters.getOrDefault(type, 0) + 1;
                    typeCounters.put(type, typeIndex);

                    House house = new House(type, typeIndex, cfg.capacity);
                    house.setMaxImportLimit(type == HouseType.DAY_OLD ? maxImportLimit : 0);
                    int weeks = switch (type) {
                        case DAY_OLD -> 8;
                        case EGG_LAYER -> 64;
                        case MEAT_FEMALE -> 6;
                        case MEAT_MALE -> 10;
                    };
                    house.setEstimatedStayWeeks(weeks);
                    if (!houseDAO.addHouse(house)) {
                        success = false;
                        break;
                    }
                }

                if (success)
                    houseDAO.updateMaxImportLimitForDayOld(maxImportLimit);

                final boolean finalSuccess = success;
                Platform.runLater(() -> {
                    loadingBox.setVisible(false);
                    saveButton.setDisable(false);
                    if (finalSuccess) {
                        saved = true;
                        closeDialog();
                    } else
                        showError("Failed to save.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingBox.setVisible(false);
                    saveButton.setDisable(false);
                    showError("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    public void handleCancel() {
        saved = false;
        closeDialog();
    }

    private void closeDialog() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
    }

    public boolean isSaved() {
        return saved;
    }
}
