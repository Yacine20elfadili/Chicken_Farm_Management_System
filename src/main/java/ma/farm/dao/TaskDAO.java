package ma.farm.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import ma.farm.model.Task;

/**
 * TaskDAO - Data Access Object for Task management
 *
 * Provides database operations for the Task model including:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Filtering by status, category, and assignee
 * - Task completion tracking
 *
 * All database operations use prepared statements to prevent SQL injection.
 *
 * @author Chicken Farm Management System
 * @version 1.0
 */
public class TaskDAO {

    private final DatabaseConnection dbConnection;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor - Initializes the DAO with a database connection instance
     */
    public TaskDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Maps a ResultSet row to a Task object
     *
     * @param rs the ResultSet positioned at the current row
     * @return a Task object populated with data from the ResultSet
     * @throws SQLException if a database access error occurs
     */
    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setDescription(rs.getString("description"));
        task.setStatus(rs.getString("status"));

        String dueDateStr = rs.getString("dueDate");
        if (dueDateStr != null && !dueDateStr.isEmpty()) {
            task.setDueDate(LocalDate.parse(dueDateStr));
        }

        String completedAtStr = rs.getString("completedAt");
        if (completedAtStr != null && !completedAtStr.isEmpty()) {
            try {
                task.setCompletedAt(
                    LocalDateTime.parse(completedAtStr, DATE_TIME_FORMATTER)
                );
            } catch (Exception e) {
                // Try parsing as just a date if datetime parsing fails
                try {
                    task.setCompletedAt(
                        LocalDate.parse(completedAtStr).atStartOfDay()
                    );
                } catch (Exception ex) {
                    // Leave completedAt as null if parsing fails
                }
            }
        }

        task.setAssignedTo(rs.getString("assignedTo"));
        task.setHouseId(rs.getInt("houseId"));
        task.setCategory(rs.getString("category"));
        task.setCrackedEggs(rs.getInt("crackedEggs"));
        task.setNotes(rs.getString("notes"));
        task.setPriority(rs.getString("priority"));

        return task;
    }

    /**
     * Retrieves all tasks from the database
     *
     * @return List of all Task objects, or empty list if none found
     */
    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        String query = "SELECT * FROM tasks ORDER BY dueDate DESC, priority";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                taskList.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all tasks: " + e.getMessage());
            e.printStackTrace();
        }

        return taskList;
    }

    /**
     * Retrieves a task by ID
     *
     * @param id the task ID
     * @return Task object if found, null otherwise
     */
    public Task getTaskById(int id) {
        String query = "SELECT * FROM tasks WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTask(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving task by ID: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves tasks filtered by status
     *
     * @param status the task status to filter by (e.g., "Done", "Pending", "Missed")
     * @return List of Task objects matching the status, or empty list if none found
     */
    public List<Task> getTasksByStatus(String status) {
        List<Task> taskList = new ArrayList<>();
        String query =
            "SELECT * FROM tasks WHERE status = ? ORDER BY dueDate DESC";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    taskList.add(mapResultSetToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving tasks by status: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return taskList;
    }

    /**
     * Retrieves tasks filtered by category
     *
     * @param category the task category to filter by (e.g., "Cleaning", "Feeding", "Collection", "Medical")
     * @return List of Task objects matching the category, or empty list if none found
     */
    public List<Task> getTasksByCategory(String category) {
        List<Task> taskList = new ArrayList<>();
        String query =
            "SELECT * FROM tasks WHERE category = ? ORDER BY dueDate DESC";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, category);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    taskList.add(mapResultSetToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving tasks by category: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return taskList;
    }

    /**
     * Retrieves tasks assigned to a specific worker
     *
     * @param assignedTo the name/ID of the worker
     * @return List of Task objects assigned to the worker, or empty list if none found
     */
    public List<Task> getTasksByAssignee(String assignedTo) {
        List<Task> taskList = new ArrayList<>();
        String query =
            "SELECT * FROM tasks WHERE assignedTo = ? ORDER BY dueDate DESC";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, assignedTo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    taskList.add(mapResultSetToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving tasks by assignee: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return taskList;
    }

    /**
     * Retrieves tasks for a specific house
     *
     * @param houseId the house ID
     * @return List of Task objects for the house, or empty list if none found
     */
    public List<Task> getTasksByHouse(int houseId) {
        List<Task> taskList = new ArrayList<>();
        String query =
            "SELECT * FROM tasks WHERE houseId = ? ORDER BY dueDate DESC";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setInt(1, houseId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    taskList.add(mapResultSetToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving tasks by house: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return taskList;
    }

    /**
     * Retrieves all overdue tasks (past due
 date and not completed)
     *
     * @return List of overdue Task objects, or empty list if none found
     */
    public List<Task> getOverdueTasks() {
        List<Task> taskList = new ArrayList<>();
        String query =
            "SELECT * FROM tasks WHERE status != 'Done' AND dueDate < date('now') ORDER BY dueDate";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                taskList.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving overdue tasks: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return taskList;
    }

    /**
     * Retrieves tasks due today
     *
     * @return List of Task objects due today, or empty list if none found
     */
    public List<Task> getTasksDueToday() {
        List<Task> taskList = new ArrayList<>();
        String query =
            "SELECT * FROM tasks WHERE dueDate = date('now') ORDER BY priority";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                taskList.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving tasks due today: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return taskList;
    }

    /**
     * Gets the count of pending tasks
     *
     * @return count of pending tasks
     */
    public int getPendingTaskCount() {
        String query =
            "SELECT COUNT(*) as count FROM tasks WHERE status = 'Pending'";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println(
                "Error counting pending tasks: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Gets the count of overdue tasks
     *
     * @return count of overdue tasks
     */
    public int getOverdueTaskCount() {
        String query =
            "SELECT COUNT(*) as count FROM tasks WHERE status != 'Done' AND dueDate < date('now')";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println(
                "Error counting overdue tasks: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Adds a new task to the database
     *
     * @param task the Task object to insert
     * @return true if insertion was successful, false otherwise
     */
    public boolean addTask(Task task) {
        String query =
            "INSERT INTO tasks (description, status, dueDate, completedAt, assignedTo, houseId, category, crackedEggs, notes, priority) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, task.getDescription());
            stmt.setString(
                2,
                task.getStatus() != null ? task.getStatus() : "Pending"
            );
            stmt.setString(
                3,
                task.getDueDate() != null ? task.getDueDate().toString() : null
            );
            stmt.setString(
                4,
                task.getCompletedAt() != null
                    ? task.getCompletedAt().format(DATE_TIME_FORMATTER)
                    : null
            );
            stmt.setString(5, task.getAssignedTo());
            // Set houseId to NULL if 0 to avoid foreign key constraint violation
            if (task.getHouseId() > 0) {
                stmt.setInt(6, task.getHouseId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            stmt.setString(7, task.getCategory());
            stmt.setInt(8, task.getCrackedEggs());
            stmt.setString(9, task.getNotes());
            stmt.setString(
                10,
                task.getPriority() != null ? task.getPriority() : "Medium"
            );

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // SQLite: use last_insert_rowid() to get the generated ID
                try (
                    Statement idStmt = dbConnection
                        .getConnection()
                        .createStatement();
                    ResultSet rs = idStmt.executeQuery(
                        "SELECT last_insert_rowid()"
                    )
                ) {
                    if (rs.next()) {
                        task.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding task: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates the status of a specific task
     *
     * @param id the task ID
     * @param status the new status (e.g., "Done", "Pending", "Missed")
     * @return true if update was successful, false otherwise
     */
    public boolean updateTaskStatus(int id, String status) {
        String query;
        if ("Done".equals(status)) {
            query =
                "UPDATE tasks SET status = ?, completedAt = datetime('now') WHERE id = ?";
        } else {
            query =
                "UPDATE tasks SET status = ?, completedAt = NULL WHERE id = ?";
        }

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, status);
            stmt.setInt(2, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating task status: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates an existing task
     *
     * @param task the Task object with updated values
     * @return true if update was successful, false otherwise
     */
    public boolean updateTask(Task task) {
        String query =
            "UPDATE tasks SET description = ?, status = ?, dueDate = ?, completedAt = ?, " +
            "assignedTo = ?, houseId = ?, category = ?, crackedEggs = ?, notes = ?, priority = ? WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, task.getDescription());
            stmt.setString(2, task.getStatus());
            stmt.setString(
                3,
                task.getDueDate() != null ? task.getDueDate().toString() : null
            );
            stmt.setString(
                4,
                task.getCompletedAt() != null
                    ? task.getCompletedAt().format(DATE_TIME_FORMATTER)
                    : null
            );
            stmt.setString(5, task.getAssignedTo());
            // Set houseId to NULL if 0 to avoid foreign key constraint violation
            if (task.getHouseId() > 0) {
                stmt.setInt(6, task.getHouseId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            stmt.setString(7, task.getCategory());
            stmt.setInt(8, task.getCrackedEggs());
            stmt.setString(9, task.getNotes());
            stmt.setString(10, task.getPriority());
            stmt.setInt(11, task.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating task: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Marks a task as complete
     *
     * @param id the task ID
     * @return true if update was successful, false otherwise
     */
    public boolean markTaskComplete(int id) {
        return updateTaskStatus(id, "Done");
    }

    /**
     * Deletes a task from the database
     *
     * @param id the task ID to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteTask(int id) {
        String query = "DELETE FROM tasks WHERE id = ?";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting task: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Gets the total count of tasks
     *
     * @return total number of tasks in the database
     */
    public int getTotalTaskCount() {
        String query = "SELECT COUNT(*) as count FROM tasks";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query);
            ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting tasks: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Gets tasks for a specific date range
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return List of Task objects within the date range
     */
    public List<Task> getTasksByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Task> taskList = new ArrayList<>();
        String query =
            "SELECT * FROM tasks WHERE dueDate BETWEEN ? AND ? ORDER BY dueDate";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, startDate.toString());
            stmt.setString(2, endDate.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    taskList.add(mapResultSetToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving tasks by date range: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return taskList;
    }

    /**
     * Searches tasks by description
     *
     * @param searchTerm the search term to match against task descriptions
     * @return List of Task objects matching the search term
     */
    public List<Task> searchByDescription(String searchTerm) {
        List<Task> taskList = new ArrayList<>();
        String query =
            "SELECT * FROM tasks WHERE description LIKE ? ORDER BY dueDate DESC";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    taskList.add(mapResultSetToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching tasks: " + e.getMessage());
            e.printStackTrace();
        }

        return taskList;
    }

    /**
     * Gets tasks by priority
     *
     * @param priority the priority level (e.g., "High", "Medium", "Low")
     * @return List of Task objects with the specified priority
     */
    public List<Task> getTasksByPriority(String priority) {
        List<Task> taskList = new ArrayList<>();
        String query =
            "SELECT * FROM tasks WHERE priority = ? ORDER BY dueDate DESC";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            stmt.setString(1, priority);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    taskList.add(mapResultSetToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error retrieving tasks by priority: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return taskList;
    }

    /**
     * Updates missed tasks (past due date and still pending)
     * Sets status to "Missed" for all such tasks
     *
     * @return number of tasks updated
     */
    public int updateMissedTasks() {
        String query =
            "UPDATE tasks SET status = 'Missed' WHERE status = 'Pending' AND dueDate < date('now')";

        try (
            PreparedStatement stmt = dbConnection
                .getConnection()
                .prepareStatement(query)
        ) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(
                "Error updating missed tasks: " + e.getMessage()
            );
            e.printStackTrace();
        }

        return 0;
    }

}
