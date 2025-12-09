package ma.farm.dao;

import ma.farm.model.House;
import ma.farm.model.HouseType;
import ma.farm.model.HealthStatus;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HouseDAOTest {

    private static HouseDAO houseDAO;
    private static int house1Id, house2Id;

    @BeforeAll
    static void setup() {
        houseDAO = new HouseDAO();
    }

    @Test
    @Order(1)
    @DisplayName("Ajouter des maisons")
    void testAddHouse() {
        House h1 = new House("TestH1", HouseType.DAY_OLD, 1000);
        h1.setCapacity(1200);
        h1.setHealthStatus(HealthStatus.GOOD);
        h1.setLastCleaningDate(LocalDate.now().minusDays(3));
        h1.setCreationDate(LocalDate.now());

        House h2 = new House("TestH2", HouseType.EGG_LAYER, 500);
        h2.setCapacity(800);
        h2.setHealthStatus(HealthStatus.FAIR);
        h2.setLastCleaningDate(LocalDate.now().minusDays(5));
        h2.setCreationDate(LocalDate.now());

        assertTrue(houseDAO.addHouse(h1), "H1 doit être ajoutée");
        assertTrue(houseDAO.addHouse(h2), "H2 doit être ajoutée");

        house1Id = h1.getId();
        house2Id = h2.getId();

        assertTrue(house1Id > 0);
        assertTrue(house2Id > 0);
    }

    @Test
    @Order(2)
    @DisplayName("Lire les maisons")
    void testReadHouses() {
        House h1 = houseDAO.getHouseById(house1Id);
        assertNotNull(h1);
        assertEquals("TestH1", h1.getName());

        House h2 = houseDAO.getHouseByName("TestH2");
        assertNotNull(h2);
        assertEquals(house2Id, h2.getId());

        List<House> allHouses = houseDAO.getAllHouses();
        assertTrue(allHouses.size() >= 2);

        List<House> dayOlds = houseDAO.getHousesByType(HouseType.DAY_OLD);
        assertTrue(dayOlds.stream().anyMatch(h -> h.getId() == house1Id));
    }

    @Test
    @Order(3)
    @DisplayName("Mettre à jour les maisons")
    void testUpdateHouse() {
        // Mise à jour complète
        House h2 = houseDAO.getHouseById(house2Id);
        h2.setName("TestH2-Updated");
        h2.setCapacity(900);
        assertTrue(houseDAO.updateHouse(h2));

        House updated = houseDAO.getHouseById(house2Id);
        assertEquals("TestH2-Updated", updated.getName());
        assertEquals(900, updated.getCapacity());

        // Mise à jour spécifique
        assertTrue(houseDAO.updateChickenCount(house1Id, 1100));
        assertEquals(1100, houseDAO.getHouseById(house1Id).getChickenCount());

        assertTrue(houseDAO.updateHealthStatus(house1Id, HealthStatus.POOR));
        assertEquals(HealthStatus.POOR, houseDAO.getHouseById(house1Id).getHealthStatus());

        LocalDate newDate = LocalDate.now();
        assertTrue(houseDAO.updateLastCleaningDate(house1Id, newDate));
        assertEquals(newDate, houseDAO.getHouseById(house1Id).getLastCleaningDate());
    }

    @Test
    @Order(4)
    @DisplayName("Tester les statistiques et requêtes avancées")
    void testStatsAndQueries() {
        int totalChickens = houseDAO.getTotalChickenCount();
        assertTrue(totalChickens > 0, "Total de poulets doit être supérieur à 0");

        List<House> needCleaning = houseDAO.getHousesNeedingCleaning(2);
        assertNotNull(needCleaning);
    }

    @Test
    @Order(5)
    @DisplayName("Supprimer les maisons")
    void testDeleteHouse() {
        assertTrue(houseDAO.deleteHouse(house1Id), "H1 doit être supprimée");
        assertTrue(houseDAO.deleteHouse(house2Id), "H2 doit être supprimée");

        assertNull(houseDAO.getHouseById(house1Id));
        assertNull(houseDAO.getHouseById(house2Id));
    }
}
