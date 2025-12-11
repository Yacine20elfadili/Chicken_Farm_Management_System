package ma.farm.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ma.farm.dao.TaskDAO;
import ma.farm.model.Task;
import ma.farm.util.DateUtil;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * TasksController - Controls the Tasks view
 * Shows: List of tasks with status badges
 */
public class TasksController {

    // FXML Components
    @FXML
    private ListView<Task> tasksListView;

    @FXML
    private Label totalTasksLabel;

    @FXML
    private Label doneTasksLabel;

    @FXML
    private Label pendingTasksLabel;

    @FXML
    private Label missedTasksLabel;

    // DAO
    private TaskDAO taskDAO;

    // Observable list for UI
    private ObservableList<Task> tasksList;

    /**
     * Initialize method - called automatically after FXML loads
     */
    @FXML
    public void initialize() {
        // Initialize DAO
        taskDAO = new TaskDAO();

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
     * Creates custom UI for each task with badge, description, etc.
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
                } else {
                    // Create custom cell content
                    HBox taskCell = createTaskCell(task);
                    setGraphic(taskCell);
                }
            }
        });
    }

    /**
     * Load and display all tasks
     */
    private void loadTasks() {
        try {
            // Get all tasks from TaskDAO
            List<Task> tasks = taskDAO.getAllTasks();

            // Sort by status (Missed first, then Pending, then Done)
            tasks.sort((t1, t2) -> {
                // Priority order: Missed > Pending > Done
                int status1Priority = getStatusPriority(t1.getStatus());
                int status2Priority = getStatusPriority(t2.getStatus());

                if (status1Priority != status2Priority) {
                    return Integer.compare(status1Priority, status2Priority);
                }

                // If same status, sort by due date
                if (t1.getDueDate() == null && t2.getDueDate() == null) return 0;
                if (t1.getDueDate() == null) return 1;
                if (t2.getDueDate() == null) return -1;
                return t1.getDueDate().compareTo(t2.getDueDate());
            });

            // Add to tasksList
            tasksList.clear();
            tasksList.addAll(tasks);

            // Update tasksListView
            if (tasksListView != null) {
                tasksListView.setItems(tasksList);
            }
        } catch (Exception e) {
            System.err.println("Error loading tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get priority order for status (lower number = higher priority)
     */
    private int getStatusPriority(String status) {
        if (status == null) return 3;
        switch (status.toLowerCase()) {
            case "missed":
                return 0;
            case "pending":
                return 1;
            case "done":
                return 2;
            default:
                return 3;
        }
    }

    /**
     * Update task statistics summary
     */
    private void updateTaskStatistics() {
        try {
            // Count total tasks
            int totalTasks = tasksList.size();

            // Count done tasks
            long doneTasks = tasksList.stream()
                    .filter(task -> "Done".equalsIgnoreCase(task.getStatus()))
                    .count();

            // Count pending tasks
            long pendingTasks = tasksList.stream()
                    .filter(task -> "Pending".equalsIgnoreCase(task.getStatus()))
                    .count();

            // Count missed tasks
            long missedTasks = tasksList.stream()
                    .filter(task -> "Missed".equalsIgnoreCase(task.getStatus()) || task.isOverdue())
                    .count();

            // Update labels
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
            System.err.println("Error updating task statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle add task button click
     * Opens dialog to create new task
     */
    @FXML
    public void handleAddTask() {
        // TODO: Open add task dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Task");
        alert.setHeaderText("Add Task Feature");
        alert.setContentText("This feature will open a dialog to create a new task.\n\nDialog implementation is pending.");
        alert.showAndWait();

        // After dialog implementation:
        // - Get task details (description, due date, assigned worker, category, house)
        // - Create Task record with status "Pending"
        // - Save to database using taskDAO.addTask()
        // - Refresh tasks list
    }

    /**
     * Handle mark as done button click
     * Marks selected task as completed
     */
    @FXML
    public void handleMarkAsDone() {
        // Get selected task from list
        Task selectedTask = tasksListView.getSelectionModel().getSelectedItem();

        // If nothing selected, show error
        if (selectedTask == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Task Selected");
            alert.setContentText("Please select a task to mark as done.");
            alert.showAndWait();
            return;
        }

        // If already done, show message
        if ("Done".equalsIgnoreCase(selectedTask.getStatus())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Already Done");
            alert.setHeaderText("Task Already Completed");
            alert.setContentText("This task is already marked as done.");
            alert.showAndWait();
            return;
        }

        try {
            // Update task status to "Done"
            boolean success = taskDAO.markTaskComplete(selectedTask.getId());

            if (success) {
                // Refresh tasks list
                refreshData();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Task Marked as Done");
                alert.setContentText("The task has been successfully marked as completed.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to Update Task");
                alert.setContentText("Could not mark the task as done. Please try again.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("Error marking task as done: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle edit task button click
     * Opens dialog to edit existing task
     */
    @FXML
    public void handleEditTask() {
        // Get selected task from list
        Task selectedTask = tasksListView.getSelectionModel().getSelectedItem();

        // If nothing selected, show error
        if (selectedTask == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Task Selected");
            alert.setContentText("Please select a task to edit.");
            alert.showAndWait();
            return;
        }

        // TODO: Open edit task dialog with current data
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Task");
        alert.setHeaderText("Edit Task Feature");
        alert.setContentText("This feature will open a dialog to edit the selected task.\n\nDialog implementation is pending.");
        alert.showAndWait();

        // After dialog implementation:
        // - Get updated task details
        // - Update Task record
        // - Save to database using taskDAO.updateTask()
        // - Refresh tasks list
    }

    /**
     * Handle delete task button click
     * Deletes selected task
     */
    @FXML
    public void handleDeleteTask() {
        // Get selected task from list
        Task selectedTask = tasksListView.getSelectionModel().getSelectedItem();

        // If nothing selected, show error
        if (selectedTask == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Task Selected");
            alert.setContentText("Please select a task to delete.");
            alert.showAndWait();
            return;
        }

        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Task");
        confirmAlert.setContentText("Are you sure you want to delete this task?\n\n" + selectedTask.getDescription());

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Delete from database
                    boolean success = taskDAO.deleteTask(selectedTask.getId());

                    if (success) {
                        // Refresh tasks list
                        refreshData();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText("Task Deleted");
                        alert.setContentText("The task has been successfully deleted.");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Failed to Delete Task");
                        alert.setContentText("Could not delete the task. Please try again.");
                        alert.showAndWait();
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting task: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Handle filter tasks by status
     * @param status Status to filter by (All, Done, Pending, Missed)
     */
    @FXML
    public void handleFilterByStatus(String status) {
        try {
            List<Task> filteredTasks;

            if ("All".equalsIgnoreCase(status)) {
                // Get all tasks
                filteredTasks = taskDAO.getAllTasks();
            } else {
                // Get tasks by status
                filteredTasks = taskDAO.getTasksByStatus(status);
            }

            // Update tasksListView
            tasksList.clear();
            tasksList.addAll(filteredTasks);
        } catch (Exception e) {
            System.err.println("Error filtering tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create custom task cell with status badge
     * @param task The task to display
     * @return Custom cell content
     */
    private HBox createTaskCell(Task task) {
        // Create HBox container
        HBox hbox = new HBox(15);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(10));

        // Create status badge label
        Label statusBadge = new Label(task.getStatus() != null ? task.getStatus() : "Unknown");
        statusBadge.setPadding(new Insets(5, 10, 5, 10));
        statusBadge.setStyle("-fx-background-radius: 5px; -fx-text-fill: white;");

        // Apply badge color based on status
        String status = task.getStatus() != null ? task.getStatus().toLowerCase() : "";
        switch (status) {
            case "done":
                statusBadge.setStyle(statusBadge.getStyle() + " -fx-background-color: #28a745;");
                break;
            case "pending":
                statusBadge.setStyle(statusBadge.getStyle() + " -fx-background-color: #ffc107;");
                break;
            case "missed":
                statusBadge.setStyle(statusBadge.getStyle() + " -fx-background-color: #dc3545;");
                break;
            default:
                statusBadge.setStyle(statusBadge.getStyle() + " -fx-background-color: #6c757d;");
        }

        // Create VBox for task details
        VBox detailsBox = new VBox(5);

        // Create task description label
        Label descriptionLabel = new Label(task.getDescription() != null ? task.getDescription() : "No description");
        descriptionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Create info line with worker name and due date
        HBox infoBox = new HBox(15);

        // Create worker name label
        if (task.getAssignedTo() != null && !task.getAssignedTo().isEmpty()) {
            Label workerLabel = new Label("👤 " + task.getAssignedTo());
            workerLabel.setStyle("-fx-text-fill: #6c757d;");
            infoBox.getChildren().add(workerLabel);
        }

        // Create due date label
        if (task.getDueDate() != null) {
            Label dueDateLabel = new Label("📅 " + DateUtil.formatDate(task.getDueDate()));
            dueDateLabel.setStyle("-fx-text-fill: #6c757d;");
            infoBox.getChildren().add(dueDateLabel);
        }

        // If egg collection task, show cracked eggs
        if (task.getCrackedEggs() > 0) {
            Label crackedEggsLabel = new Label("🥚 Cracked: " + task.getCrackedEggs());
            crackedEggsLabel.setStyle("-fx-text-fill: #6c757d;");
            infoBox.getChildren().add(crackedEggsLabel);
        }

        detailsBox.getChildren().addAll(descriptionLabel, infoBox);

        // Create spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Create category label
        Label categoryLabel = new Label(task.getCategory() != null ? task.getCategory() : "");
        categoryLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");

        // Arrange in HBox
        hbox.getChildren().addAll(statusBadge, detailsBox, spacer, categoryLabel);

        return hbox;
    }

    /**
     * Refresh tasks list
     */
    @FXML
    public void refreshData() {
        // Reload tasks
        loadTasks();

        // Update statistics
        updateTaskStatistics();
    }
}
