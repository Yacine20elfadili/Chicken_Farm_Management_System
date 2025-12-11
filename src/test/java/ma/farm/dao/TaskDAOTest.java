package ma.farm.dao;

import ma.farm.model.Task;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskDAOTest {

    private static TaskDAO taskDAO;
    private static int generatedTaskId;

    @BeforeAll
    static void setup() {
        System.out.println("=== Using MAIN DATABASE for tests ===");
        taskDAO = new TaskDAO();
    }

    @Test
    @Order(1)
    void testAddTask() {
        Task task = new Task();
        task.setDescription("JUnit test task");
        task.setStatus("Pending");
        task.setDueDate(LocalDate.now().plusDays(1));
        task.setAssignedTo("Worker A");
        task.setHouseId(1);
        task.setCategory("Testing");
        task.setCrackedEggs(0);
        task.setNotes("This is a test task");
        task.setPriority("High");

        boolean result = taskDAO.addTask(task);

        assertTrue(result, "Task must be added successfully");
        assertTrue(task.getId() > 0, "Generated ID must be > 0");

        generatedTaskId = task.getId();
        System.out.println("Added Task ID = " + generatedTaskId);
    }

    @Test
    @Order(2)
    void testGetTaskById() {
        Task task = taskDAO.getTaskById(generatedTaskId);

        assertNotNull(task, "Task must exist");
        assertEquals("JUnit test task", task.getDescription());
        assertEquals("Pending", task.getStatus());
    }

    @Test
    @Order(3)
    void testUpdateTask() {
        Task task = taskDAO.getTaskById(generatedTaskId);
        task.setDescription("Updated JUnit task");
        task.setPriority("Medium");

        boolean result = taskDAO.updateTask(task);

        assertTrue(result, "Task must be updated");

        Task updated = taskDAO.getTaskById(generatedTaskId);
        assertEquals("Updated JUnit task", updated.getDescription());
        assertEquals("Medium", updated.getPriority());
    }

    @Test
    @Order(4)
    void testUpdateTaskStatus() {
        boolean result = taskDAO.updateTaskStatus(generatedTaskId, "Done");

        assertTrue(result, "Status update must work");

        Task task = taskDAO.getTaskById(generatedTaskId);
        assertEquals("Done", task.getStatus());
        assertNotNull(task.getCompletedAt(), "completedAt must be set when Done");
    }

    @Test
    @Order(5)
    void testDeleteTask() {
        boolean result = taskDAO.deleteTask(generatedTaskId);

        assertTrue(result, "Task must be deleted");

        Task task = taskDAO.getTaskById(generatedTaskId);
        assertNull(task, "Task must no longer exist");
    }
}
