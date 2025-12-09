package ma.farm.dao;

import ma.farm.model.EggProduction;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EggProductionDAOTest {

    private static EggProductionDAO dao;
    private static EggProduction testProduction;

    @BeforeAll
    public static void setup() {
        dao = new EggProductionDAO();
    }

    @Test
    @Order(1)
    public void testAddProduction() {
        testProduction = new EggProduction(
                2, // houseId
                LocalDate.now(),
                500,
                10,
                5
        );
        testProduction.setCollectedBy("TestUser");
        testProduction.setNotes("JUnit test record");

        boolean added = dao.addProduction(testProduction);
        assertTrue(added, "Production should be added successfully");
        assertTrue(testProduction.getId() > 0, "Generated ID should be set");
    }

    @Test
    @Order(2)
    public void testGetProductionById() {
        EggProduction retrieved = dao.getProductionById(testProduction.getId());
        assertNotNull(retrieved, "Retrieved production should not be null");
        assertEquals(testProduction.getHouseId(), retrieved.getHouseId());
        assertEquals(testProduction.getEggsCollected(), retrieved.getEggsCollected());
    }

    @Test
    @Order(3)
    public void testUpdateProduction() {
        testProduction.setEggsCollected(550);
        testProduction.calculateGoodEggs(); // assuming this updates goodEggs
        boolean updated = dao.updateProduction(testProduction);
        assertTrue(updated, "Production should be updated successfully");

        EggProduction retrieved = dao.getProductionById(testProduction.getId());
        assertEquals(550, retrieved.getEggsCollected(), "Eggs collected should be updated");
    }

    @Test
    @Order(4)
    public void testGetProductionByDate() {
        List<EggProduction> productions = dao.getProductionByDate(LocalDate.now());
        assertFalse(productions.isEmpty(), "There should be productions for today");
    }

    @Test
    @Order(5)
    public void testDeleteProduction() {
        boolean deleted = dao.deleteProduction(testProduction.getId());
        assertTrue(deleted, "Production should be deleted successfully");

        EggProduction retrieved = dao.getProductionById(testProduction.getId());
        assertNull(retrieved, "Deleted production should no longer exist");
    }
}
