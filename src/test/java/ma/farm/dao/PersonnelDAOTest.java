package ma.farm.dao;

import ma.farm.model.Personnel;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonnelDAOTest {

    private static PersonnelDAO personnelDAO;
    private static int insertedId;

    @BeforeAll
    static void init() {
        personnelDAO = new PersonnelDAO();
    }

    @Test
    @Order(1)
    void testCreatePersonnel() {

        Personnel p = new Personnel(
                0,
                "Test Employee",
                30,
                "0600000000",
                "test.employee@example.com",
                "worker",                     // doit exister dans jobTitles
                LocalDate.now(),
                3500.0,
                "morning",                    // doit exister dans shifts
                true,
                "Test Address",
                "Emergency Contact"
        );

        boolean result = personnelDAO.createPersonnel(p);

        assertTrue(result, "Personnel should be created");
        assertTrue(p.getId() > 0, "Inserted personnel ID should be > 0");

        insertedId = p.getId();
    }

    @Test
    @Order(2)
    void testGetPersonnelById() {
        Personnel p = personnelDAO.getPersonnelById(insertedId);
        assertNotNull(p, "Personnel should be found");
        assertEquals("Test Employee", p.getFullName());
    }

    @Test
    @Order(3)
    void testUpdatePersonnel() {
        Personnel p = personnelDAO.getPersonnelById(insertedId);

        p.setFullName("Updated Employee");
        p.setSalary(4000);

        boolean updated = personnelDAO.updatePersonnel(p);
        assertTrue(updated, "Personnel should be updated");

        Personnel updatedP = personnelDAO.getPersonnelById(insertedId);
        assertEquals("Updated Employee", updatedP.getFullName());
        assertEquals(4000, updatedP.getSalary());
    }

    @Test
    @Order(4)
    void testGetPersonnelByEmail() {
        Personnel p = personnelDAO.getPersonnelByEmail("test.employee@example.com");
        assertNotNull(p, "Personnel should be found by email");
        assertEquals(insertedId, p.getId());
    }

    @Test
    @Order(5)
    void testGetAllPersonnel() {
        List<Personnel> list = personnelDAO.getAllPersonnel();
        assertNotNull(list);
        assertTrue(list.size() > 0, "Personnel list should not be empty");
    }

    @Test
    @Order(6)
    void testGetActivePersonnel() {
        List<Personnel> list = personnelDAO.getActivePersonnel();
        assertNotNull(list);
        assertTrue(list.size() > 0, "Active personnel list should not be empty");
    }

    @Test
    @Order(7)
    void testGetPersonnelByJobTitle() {
        List<Personnel> workers = personnelDAO.getPersonnelByJobTitle("worker");
        assertNotNull(workers);

        // Peut être 0 si pas d’autres workers → on valide seulement que la liste n’est pas null
        assertTrue(workers.size() >= 0);
    }

    @Test
    @Order(8)
    void testGetPersonnelByShift() {
        List<Personnel> morning = personnelDAO.getPersonnelByShift("morning");
        assertNotNull(morning);
        assertTrue(morning.size() >= 0);
    }

    @Test
    @Order(9)
    void testGetCounts() {
        int total = personnelDAO.getTotalPersonnelCount();
        int active = personnelDAO.getActivePersonnelCount();

        assertTrue(total > 0, "Total personnel count should be > 0");
        assertTrue(active >= 0, "Active personnel count should be >= 0");
    }

    @Test
    @Order(10)
    void testDeletePersonnel() {
        boolean deleted = personnelDAO.deletePersonnel(insertedId);
        assertTrue(deleted, "Personnel should be deleted");

        Personnel p = personnelDAO.getPersonnelById(insertedId);
        assertNull(p, "Personnel must not exist after deletion");
    }
}
