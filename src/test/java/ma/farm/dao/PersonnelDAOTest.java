package ma.farm.dao;

import ma.farm.model.Personnel;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PersonnelDAOTest {

    private PersonnelDAO personnelDAO;

    @BeforeEach
    void setUp() {
        personnelDAO = new PersonnelDAO();
    }

    private Personnel createTestPersonnel(String email) {
        // Add timestamp to ensure uniqueness with mock data
        String uniqueEmail = System.currentTimeMillis() + "_" + email;
        Personnel p = new Personnel(
                0,
                "JUnit User",
                30,
                "0600000000",
                uniqueEmail,
                0,
                "admin_staff",
                "administration",
                "accounting",
                LocalDate.now(),
                4000,
                true,
                "Agadir",
                "0611111111",
                null,
                null,
                null);
        assertTrue(personnelDAO.createPersonnel(p));
        return p;
    }

    @Test
    void testCreatePersonnel() {
        Personnel p = createTestPersonnel("create@test.com");
        assertNotNull(p);
        assertTrue(p.getId() > 0);
    }

    @Test
    void testGetPersonnelById() {
        Personnel created = createTestPersonnel("byid@test.com");

        Personnel found = personnelDAO.getPersonnelById(created.getId());
        assertNotNull(found);
        assertEquals(created.getEmail(), found.getEmail());
    }

    @Test
    void testGetPersonnelByEmail() {
        Personnel created = createTestPersonnel("email@test.com");

        Personnel found = personnelDAO.getPersonnelByEmail(created.getEmail());
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void testUpdatePersonnel() {
        Personnel p = createTestPersonnel("update@test.com");

        p.setFullName("Updated Name");
        assertTrue(personnelDAO.updatePersonnel(p));

        Personnel updated = personnelDAO.getPersonnelById(p.getId());
        assertNotNull(updated);
        assertEquals("Updated Name", updated.getFullName());
    }

    @Test
    void testDeletePersonnel() {
        Personnel p = createTestPersonnel("delete@test.com");

        assertTrue(personnelDAO.deletePersonnel(p.getId()));

        Personnel deleted = personnelDAO.getPersonnelById(p.getId());
        assertNull(deleted);
    }
}
