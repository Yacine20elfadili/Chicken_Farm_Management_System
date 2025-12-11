package ma.farm.dao;

import ma.farm.model.Chicken;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChickenDAOTest {

    private static ChickenDAO chickenDAO;
    private static Chicken testChicken;

    @BeforeAll
    public static void setup() {
        chickenDAO = new ChickenDAO();
        System.out.println("Starting ChickenDAO tests...");
    }

    @Test
    @Order(1)
    public void testCreateChickenBatch() {
        // Assure-toi que houseId existe dans la table houses
        int houseId = 1; // H1
        testChicken = new Chicken();
        testChicken.setHouseId(houseId);
        testChicken.setBatchNumber("BATCH-001");
        testChicken.setQuantity(200);
        testChicken.setArrivalDate(LocalDate.now());
        testChicken.setAgeInDays(1);
        testChicken.setGender("Female");
        testChicken.setHealthStatus("Good");
        testChicken.setAverageWeight(0.05);
        testChicken.setNextTransferDate(LocalDate.now().plusDays(10));

        boolean created = chickenDAO.createChickenBatch(testChicken);
        assertTrue(created, "Le batch de poulets devrait être créé avec succès");
    }

    @Test
    @Order(2)
    public void testGetChickenById() {
        // Récupère le dernier chicken inséré
        List<Chicken> chickens = chickenDAO.getAllChickens();
        assertFalse(chickens.isEmpty(), "Il doit y avoir au moins un chicken dans la base");
        testChicken = chickens.get(0);
        Chicken c = chickenDAO.getChickenById(testChicken.getId());
        assertNotNull(c, "Le chicken récupéré par ID ne doit pas être null");
        assertEquals(testChicken.getBatchNumber(), c.getBatchNumber());
    }

    @Test
    @Order(3)
    public void testUpdateChickenBatch() {
        testChicken.setQuantity(250);
        testChicken.setHealthStatus("Excellent");
        boolean updated = chickenDAO.updateChickenBatch(testChicken);
        assertTrue(updated, "Le chicken batch devrait être mis à jour");

        Chicken updatedChicken = chickenDAO.getChickenById(testChicken.getId());
        assertEquals(250, updatedChicken.getQuantity());
        assertEquals("Excellent", updatedChicken.getHealthStatus());
    }

    @Test
    @Order(4)
    public void testGetByBatch() {
        List<Chicken> chickens = chickenDAO.getChickensByBatch("BATCH-001");
        assertFalse(chickens.isEmpty(), "Il devrait y avoir des chickens pour ce batch");
    }

    @Test
    @Order(5)
    public void testGetByHouse() {
        List<Chicken> chickens = chickenDAO.getChickensByHouse(1);
        assertFalse(chickens.isEmpty(), "Il devrait y avoir des chickens pour cette maison");
    }

    @Test
    @Order(6)
    public void testDeleteChickenBatch() {
        boolean deleted = chickenDAO.deleteChickenBatch(testChicken.getId());
        assertTrue(deleted, "Le chicken batch devrait être supprimé avec succès");

        Chicken deletedChicken = chickenDAO.getChickenById(testChicken.getId());
        assertNull(deletedChicken, "Le chicken supprimé ne doit plus exister");
    }
}
