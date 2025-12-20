package ma.farm.dao;

import ma.farm.model.Task;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskDAOTest {

    private static TaskDAO taskDAO;
    private static int testTaskId;

    @BeforeAll
    static void setup() {
        taskDAO = new TaskDAO();
    }

    @Test
    @Order(1)
    void testAddTask() {
        Task task = new Task();
        task.setDescription("Test task JUnit");
        task.setStatus("Pending");
        task.setDueDate(LocalDate.now().plusDays(1));
        task.setAssignedTo("Worker-1");
        task.setHouseId(0); // IMPORTANT : 0 → NULL (pas de FK)
        task.setCategory("Cleaning");
        task.setCrackedEggs(0);
        task.setNotes("JUnit test");
        task.setPriority("High");

        boolean created = taskDAO.addTask(task);
        assertTrue(created, "Task should be created");

        assertTrue(task.getId() > 0, "Task ID should be generated");
        testTaskId = task.getId();
    }

    @Test
    @Order(2)
    void testGetTaskById() {
        Task task = taskDAO.getTaskById(testTaskId);
        assertNotNull(task);
        assertEquals("Test task JUnit", task.getDescription());
    }

    @Test
    @Order(3)
    void testGetAllTasks() {
        List<Task> tasks = taskDAO.getAllTasks();
        assertFalse(tasks.isEmpty());
    }

    @Test
    @Order(4)
    void testGetTasksByStatus() {
        List<Task> tasks = taskDAO.getTasksByStatus("Pending");
        assertFalse(tasks.isEmpty());
    }

    @Test
    @Order(5)
    void testUpdateTaskStatus() {
        boolean updated = taskDAO.updateTaskStatus(testTaskId, "Done");
        assertTrue(updated);

        Task task = taskDAO.getTaskById(testTaskId);
        assertEquals("Done", task.getStatus());
        assertNotNull(task.getCompletedAt());
    }

    @Test
    @Order(6)
    void testUpdateTask() {
        Task task = taskDAO.getTaskById(testTaskId);
        assertNotNull(task);

        task.setDescription("Updated JUnit Task");
        task.setPriority("Low");

        boolean updated = taskDAO.updateTask(task);
        assertTrue(updated);

        Task updatedTask = taskDAO.getTaskById(testTaskId);
        assertEquals("Updated JUnit Task", updatedTask.getDescription());
        assertEquals("Low", updatedTask.getPriority());
    }

    @Test
    @Order(7)
    void testGetPendingTaskCount() {
        int count = taskDAO.getPendingTaskCount();
        assertTrue(count >= 0);
    }

    @Test
    @Order(8)
    void testGetOverdueTaskCount() {
        int count = taskDAO.getOverdueTaskCount();
        assertTrue(count >= 0);
    }

    @Test
    @Order(9)
    void testSearchByDescription() {
        List<Task> tasks = taskDAO.searchByDescription("JUnit");
        assertFalse(tasks.isEmpty());
    }

    @Test
    @Order(10)
    void testDeleteTask() {
        boolean deleted = taskDAO.deleteTask(testTaskId);
        assertTrue(deleted);

        Task task = taskDAO.getTaskById(testTaskId);
        assertNull(task);
    }
}
