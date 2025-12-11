package ma.farm.dao;

import ma.farm.model.Equipment;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EquipmentDAOTest {

    private static EquipmentDAO equipmentDAO;
    private static int createdId;

    @BeforeAll
    static void setup() {
        equipmentDAO = new EquipmentDAO();
    }


    @Test
    @Order(1)
    void testAddEquipment() {
        Equipment eq = new Equipment(
                0,
                "TEST-EQ-JUNIT",
                "TestCategory",
                7,
                "Good",
                LocalDate.now(),
                500.0,
                LocalDate.now(),
                LocalDate.now().plusMonths(3),
                "TestLocation",
                "JUnit test equipment"
        );

        boolean result = equipmentDAO.addEquipment(eq);

        assertTrue(result, "L'insertion doit réussir sur la base principale");
        assertTrue(eq.getId() > 0, "SQLite doit générer un ID");

        createdId = eq.getId();
    }

    @Test
    @Order(2)
    void testGetById() {
        Equipment eq = equipmentDAO.getEquipmentById(createdId);

        assertNotNull(eq, "L'équipement doit exister dans farm.db");
        assertEquals("TEST-EQ-JUNIT", eq.getName());
    }

    @Test
    @Order(3)
    void testUpdateStatus() {
        boolean ok = equipmentDAO.updateStatus(createdId, "Broken");

        assertTrue(ok, "La mise à jour doit réussir dans la base principale");

        Equipment eq = equipmentDAO.getEquipmentById(createdId);
        assertEquals("Broken", eq.getStatus());
    }

    @Test
    @Order(4)
    void testRecordMaintenance() {
        LocalDate nextDate = LocalDate.now().plusMonths(6);

        boolean ok = equipmentDAO.recordMaintenance(createdId, nextDate);

        assertTrue(ok);

        Equipment eq = equipmentDAO.getEquipmentById(createdId);

        assertEquals("Good", eq.getStatus());
        assertEquals(nextDate, eq.getNextMaintenanceDate());
        assertNotNull(eq.getLastMaintenanceDate());
    }

    @Test
    @Order(5)
    void testGetAll() {
        List<Equipment> list = equipmentDAO.getAllEquipment();

        assertNotNull(list);
        assertTrue(list.size() > 0, "farm.db doit contenir au moins un équipement");
    }

    @Test
    @Order(6)
    void testUpdateEquipment() {
        Equipment eq = equipmentDAO.getEquipmentById(createdId);

        eq.setName("UPDATED-JUNIT-EQ");
        eq.setQuantity(99);

        boolean ok = equipmentDAO.updateEquipment(eq);

        assertTrue(ok);

        Equipment updated = equipmentDAO.getEquipmentById(createdId);

        assertEquals("UPDATED-JUNIT-EQ", updated.getName());
        assertEquals(99, updated.getQuantity());
    }

    @Test
    @Order(7)
    void testDeleteEquipment() {
        boolean ok = equipmentDAO.deleteEquipment(createdId);

        assertTrue(ok, "La suppression doit réussir");

        Equipment eq = equipmentDAO.getEquipmentById(createdId);

        assertNull(eq, "L'équipement ne doit plus exister dans la base");
    }
}
