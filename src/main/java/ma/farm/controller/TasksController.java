package ma.farm.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ma.farm.controller.dialogs.AddEditTaskDialogController;
import ma.farm.dao.HouseDAO;
import ma.farm.dao.TaskDAO;
import ma.farm.model.House;
import ma.farm.model.Task;
import ma.farm.util.DateUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TasksController - Controls the Tasks view
 *
 * Features:
 * - Display tasks in ListView with custom cells
 * - Statistics cards (Total, Done, Pending, Missed)
 * - Full CRUD operations via dialog
 * - Mark task as complete
 * - Action buttons on each task cell
 */
public class TasksController {

    // FXML Components - Statistics Labels
    @FXML
    private Label totalTasksLabel;

    @FXML
    private Label doneTasksLabel;

    @FXML
    private Label pendingTasksLabel;

    @FXML
    private Label missedTasksLabel;

    // FXML Components - Task List
    @FXML
    private ListView<Task> tasksListView;

    // FXML Components - Buttons
    @FXML
    private Button addTaskButton;

    @FXML
    private Button refreshButton;

    // DAOs
    private TaskDAO taskDAO;
    private HouseDAO houseDAO;

    // Observable list for UI
    private ObservableList<Task> tasksList;

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        // Initialize DAOs
        taskDAO = new TaskDAO();
        houseDAO = new HouseDAO();

        // Initialize observable list
        tasksList = FXCollections.observableArrayList();

        // Setup custom cell factory for task items
        setupTaskCellFactory();

        // Load tasks
        loadTasks();

        // Update task statistics
        updateTaskStatistics();
    }

    /**
     * Setup custom cell factory for task list items
     * Creates custom UI for each task with badge, description, action buttons
     */
    private void setupTaskCellFactory() {
        if (tasksListView == null) {
            return;
        }

        tasksListView.setCellFactory(param -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);

                if (empty || task == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // Create custom cell content
                    HBox taskCell = createTaskCell(task);
                    setGraphic(taskCell);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5 0;");
                }
            }
        });
    }

    /**
     * Create custom task cell with status badge, info, and action buttons
     * @param task The task to display
     * @return Custom cell content
     */
    private HBox createTaskCell(Task task) {
        // Main container
        HBox mainContainer = new HBox(12);
        mainContainer.setAlignment(Pos.CENTER_LEFT);
        mainContainer.setPadding(new Insets(12, 15, 12, 15));
        mainContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                              "-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-border-width: 1;");

        // Status badge
        Label statusBadge = createStatusBadge(task.getStatus());

        // Priority indicator
        Label priorityBadge = createPriorityBadge(task.getPriority());

        // Task details VBox
        VBox detailsBox = new VBox(4);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);

        // Description
        Label descriptionLabel = new Label(task.getDescription() != null ? task.getDescription() : "Sans description");
        descriptionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(400);

        // Info line (assigned to, house, due date)
        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        // Category
        if (task.getCategory() != null && !task.getCategory().isEmpty()) {
            Label categoryLabel = new Label("📁 " + mapCategoryToFrench(task.getCategory()));
            categoryLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
            infoBox.getChildren().add(categoryLabel);
        }

        // Assigned to
        if (task.getAssignedTo() != null && !task.getAssignedTo().isEmpty()) {
            Label assignedLabel = new Label("👤 " + task.getAssignedTo());
            assignedLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
            infoBox.getChildren().add(assignedLabel);
        }

        // House
        if (task.getHouseId() > 0) {
            try {
                House house = houseDAO.getHouseById(task.getHouseId());
                if (house != null) {
                    Label houseLabel = new Label("🏠 " + house.getName());
                    houseLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
                    infoBox.getChildren().add(houseLabel);
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        // Due date
        if (task.getDueDate() != null) {
            String dateStr = DateUtil.formatDate(task.getDueDate());
            Label dueDateLabel = new Label("📅 " + dateStr);

            // Color based on due date
            if (task.isOverdue()) {
                dueDateLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px; -fx-font-weight: bold;");
            } else if (task.getDueDate().equals(LocalDate.now())) {
                dueDateLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 12px; -fx-font-weight: bold;");
            } else {
                dueDateLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
            }
            infoBox.getChildren().add(dueDateLabel);
        }

        detailsBox.getChildren().addAll(descriptionLabel, infoBox);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.SOMETIMES);

        // Action buttons
        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        // Complete button (only for non-done tasks)
        if (!"Done".equalsIgnoreCase(task.getStatus())) {
            Button completeBtn = new Button("✓");
            completeBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                                "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 5 10; " +
                                "-fx-background-radius: 4;");
            completeBtn.setTooltip(new Tooltip("Marquer comme terminée"));
            completeBtn.setOnAction(e -> handleMarkTaskComplete(task));
            actionBox.getChildren().add(completeBtn);
        }

        // Edit button
        Button editBtn = new Button("✏");
        editBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; " +
                        "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 5 10; " +
                        "-fx-background-radius: 4;");
        editBtn.setTooltip(new Tooltip("Modifier"));
        editBtn.setOnAction(e -> handleEditTask(task));

        // Delete button
        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                          "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 5 10; " +
                          "-fx-background-radius: 4;");
        deleteBtn.setTooltip(new Tooltip("Supprimer"));
        deleteBtn.setOnAction(e -> handleDeleteTask(task));

        actionBox.getChildren().addAll(editBtn, deleteBtn);

        // Assemble main container
        mainContainer.getChildren().addAll(statusBadge, priorityBadge, detailsBox, spacer, actionBox);

        return mainContainer;
    }

    /**
     * Create status badge label
     */
    private Label createStatusBadge(String status) {
        Label badge = new Label(mapStatusToFrench(status));
        badge.setPadding(new Insets(4, 10, 4, 10));
        badge.setMinWidth(80);
        badge.setAlignment(Pos.CENTER);
        badge.setStyle("-fx-background-radius: 4; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");

        String bgColor;
        switch (status != null ? status.toLowerCase() : "") {
            case "done":
                bgColor = "#28a745";
                break;
            case "pending":
                bgColor = "#ffc107";
                badge.setStyle(badge.getStyle() + " -fx-text-fill: #212529;");
                break;
            case "missed":
                bgColor = "#dc3545";
                break;
            default:
                bgColor = "#6c757d";
        }
        badge.setStyle(badge.getStyle() + " -fx-background-color: " + bgColor + ";");

        return badge;
    }

    /**
     * Create priority badge label
     */
    private Label createPriorityBadge(String priority) {
        String displayText;
        String bgColor;

        switch (priority != null ? priority.toLowerCase() : "medium") {
            case "high":
                displayText = "▲";
                bgColor = "#dc3545";
                break;
            case "low":
                displayText = "▼";
                bgColor = "#17a2b8";
                break;
            default: // medium
                displayText = "●";
                bgColor = "#ffc107";
                break;
        }

        Label badge = new Label(displayText);
        badge.setPadding(new Insets(2, 6, 2, 6));
        badge.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 3; " +
                      "-fx-text-fill: white; -fx-font-size: 10px;");
        badge.setTooltip(new Tooltip("Priorité: " + mapPriorityToFrench(priority)));

        return badge;
    }

    /**
     * Map status to French
     */
    private String mapStatusToFrench(String status) {
        if (status == null) return "Inconnu";
        switch (status.toLowerCase()) {
            case "done": return "Terminée";
            case "pending": return "En Attente";
            case "missed": return "Manquée";
            default: return status;
        }
    }

    /**
     * Map priority to French
     */
    private String mapPriorityToFrench(String priority) {
        if (priority == null) return "Moyenne";
        switch (priority.toLowerCase()) {
            case "high": return "Haute";
            case "medium": return "Moyenne";
            case "low": return "Basse";
            default: return priority;
        }
    }

    /**
     * Map category to French
     */
    private String mapCategoryToFrench(String category) {
        if (category == null) return "";
        switch (category) {
            case "Feeding": return "Alimentation";
            case "Cleaning": return "Nettoyage";
            case "Collection": return "Collecte";
            case "Medical": return "Vétérinaire";
            case "Inventory": return "Inventaire";
            case "Administrative": return "Administratif";
            default: return category;
        }
    }

    /**
     * Load and display all tasks
     */
    private void loadTasks() {
        try {
            // Get all tasks from TaskDAO
            List<Task> tasks = taskDAO.getAllTasks();

            // Update overdue tasks status
            for (Task task : tasks) {
                if ("Pending".equalsIgnoreCase(task.getStatus()) && task.isOverdue()) {
                    task.setStatus("Missed");
                    taskDAO.updateTaskStatus(task.getId(), "Missed");
                }
            }

            // Sort by status (Missed first, then Pending, then Done), then by due date
            tasks.sort((t1, t2) -> {
                int status1Priority = getStatusPriority(t1.getStatus());
                int status2Priority = getStatusPriority(t2.getStatus());

                if (status1Priority != status2Priority) {
                    return Integer.compare(status1Priority, status2Priority);
                }

                // Then by priority
                int priority1 = getPriorityOrder(t1.getPriority());
                int priority2 = getPriorityOrder(t2.getPriority());
                if (priority1 != priority2) {
                    return Integer.compare(priority1, priority2);
                }

                // Then by due date
                if (t1.getDueDate() == null && t2.getDueDate() == null) return 0;
                if (t1.getDueDate() == null) return 1;
                if (t2.getDueDate() == null) return -1;
                return t1.getDueDate().compareTo(t2.getDueDate());
            });

            // Update observable list
            tasksList.clear();
            tasksList.addAll(tasks);

            // Update ListView
            if (tasksListView != null) {
                tasksListView.setItems(tasksList);
            }
        } catch (Exception e) {
            System.err.println("Error loading tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get priority order for status (lower = higher priority)
     */
    private int getStatusPriority(String status) {
        if (status == null) return 3;
        switch (status.toLowerCase()) {
            case "missed": return 0;
            case "pending": return 1;
            case "done": return 2;
            default: return 3;
        }
    }

    /**
     * Get priority order (lower = higher priority)
     */
    private int getPriorityOrder(String priority) {
        if (priority == null) return 2;
        switch (priority.toLowerCase()) {
            case "high": return 0;
            case "medium": return 1;
            case "low": return 2;
            default: return 2;
        }
    }

    /**
     * Update task statistics
     */
    private void updateTaskStatistics() {
        try {
            int totalTasks = tasksList.size();
            long doneTasks = tasksList.stream()
                    .filter(t -> "Done".equalsIgnoreCase(t.getStatus()))
                    .count();
            long pendingTasks = tasksList.stream()
                    .filter(t -> "Pending".equalsIgnoreCase(t.getStatus()))
                    .count();
            long missedTasks = tasksList.stream()
                    .filter(t -> "Missed".equalsIgnoreCase(t.getStatus()))
                    .count();

            if (totalTasksLabel != null) {
                totalTasksLabel.setText(String.valueOf(totalTasks));
            }
            if (doneTasksLabel != null) {
                doneTasksLabel.setText(String.valueOf(doneTasks));
            }
            if (pendingTasksLabel != null) {
                pendingTasksLabel.setText(String.valueOf(pendingTasks));
            }
            if (missedTasksLabel != null) {
                missedTasksLabel.setText(String.valueOf(missedTasks));
            }
        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
        }
    }

    /**
     * Handle add task button click - opens dialog
     */
    @FXML
    public void handleAddTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditTaskDialog.fxml"));
            Parent root = loader.load();

            AddEditTaskDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nouvelle Tâche");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tasksListView.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);
            controller.setTask(null); // New task

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                refreshData();
                showSuccessAlert("Tâche créée", "La tâche a été créée avec succès.");
            }
        } catch (IOException e) {
            System.err.println("Error opening add task dialog: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire de création.");
        }
    }

    /**
     * Handle edit task - opens dialog with task data
     */
    private void handleEditTask(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddEditTaskDialog.fxml"));
            Parent root = loader.load();

            AddEditTaskDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Tâche");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tasksListView.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);
            controller.setTask(task); // Edit existing task

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                refreshData();
                showSuccessAlert("Tâche modifiée", "La tâche a été modifiée avec succès.");
            }
        } catch (IOException e) {
            System.err.println("Error opening edit task dialog: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire de modification.");
        }
    }

    /**
     * Handle delete task
     */
    private void handleDeleteTask(Task task) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer la suppression");
        confirmAlert.setHeaderText("Supprimer cette tâche ?");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette tâche ?\n\n" +
                                   "\"" + task.getDescription() + "\"");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = taskDAO.deleteTask(task.getId());
            if (success) {
                refreshData();
                showSuccessAlert("Tâche supprimée", "La tâche a été supprimée avec succès.");
            } else {
                showErrorAlert("Erreur", "Impossible de supprimer la tâche.");
            }
        }
    }

    /**
     * Handle mark task as complete
     */
    private void handleMarkTaskComplete(Task task) {
        // For Collection tasks, we could ask for cracked eggs count
        // For now, just mark as complete

        boolean success = taskDAO.markTaskComplete(task.getId());
        if (success) {
            refreshData();
        } else {
            showErrorAlert("Erreur", "Impossible de marquer la tâche comme terminée.");
        }
    }

    /**
     * Handle mark as done button click (legacy method)
     */
    @FXML
    public void handleMarkAsDone() {
        Task selectedTask = tasksListView.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showWarningAlert("Aucune sélection", "Veuillez sélectionner une tâche.");
            return;
        }

        if ("Done".equalsIgnoreCase(selectedTask.getStatus())) {
            showInfoAlert("Déjà terminée", "Cette tâche est déjà marquée comme terminée.");
            return;
        }

        handleMarkTaskComplete(selectedTask);
    }

    /**
     * Handle edit task button click (legacy method)
     */
    @FXML
    public void handleEditTask() {
        Task selectedTask = tasksListView.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showWarningAlert("Aucune sélection", "Veuillez sélectionner une tâche à modifier.");
            return;
        }
        handleEditTask(selectedTask);
    }

    /**
     * Handle delete task button click (legacy method)
     */
    @FXML
    public void handleDeleteTask() {
        Task selectedTask = tasksListView.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showWarningAlert("Aucune sélection", "Veuillez sélectionner une tâche à supprimer.");
            return;
        }
        handleDeleteTask(selectedTask);
    }

    /**
     * Refresh all data
     */
    @FXML
    public void refreshData() {
        loadTasks();
        updateTaskStatistics();
    }

    // ============================================================
    // Alert Helper Methods
    // ============================================================

    private void showSuccessAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarningAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
