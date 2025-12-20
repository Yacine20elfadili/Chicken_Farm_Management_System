package ma.farm.dao;

import ma.farm.model.House;
import ma.farm.model.HouseType;
import ma.farm.model.HealthStatus;

import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HouseDAOTest {

    private static HouseDAO houseDAO;
    private static int houseId;

    @BeforeAll
    static void setup() {
        houseDAO = new HouseDAO();
    }

    @Test
    @Order(1)
    void testAddHouse() {
        House house = new House();
        house.setName("Test-House-1");
        house.setType(HouseType.DAY_OLD);
        house.setChickenCount(0);
        house.setCapacity(1000);
        house.setHealthStatus(HealthStatus.GOOD);
        house.setCreationDate(LocalDate.now());

        boolean created = houseDAO.addHouse(house);
        assertTrue(created);
        assertTrue(house.getId() > 0);

        houseId = house.getId();
    }

    @Test
    @Order(2)
    void testGetHouseById() {
        House house = houseDAO.getHouseById(houseId);
        assertNotNull(house);
        assertEquals("Test-House-1", house.getName());
    }

    @Test
    @Order(3)
    void testGetHouseByName() {
        House house = houseDAO.getHouseByName("Test-House-1");
        assertNotNull(house);
        assertEquals(houseId, house.getId());
    }

    @Test
    @Order(4)
    void testGetAllHouses() {
        List<House> houses = houseDAO.getAllHouses();
        assertFalse(houses.isEmpty());
    }

    @Test
    @Order(5)
    void testGetHousesByType() {
        List<House> houses = houseDAO.getHousesByType(HouseType.DAY_OLD);
        assertFalse(houses.isEmpty());
    }

    @Test
    @Order(6)
    void testUpdateHouse() {
        House house = houseDAO.getHouseById(houseId);
        assertNotNull(house);

        house.setCapacity(1200);
        house.setHealthStatus(HealthStatus.FAIR);

        boolean updated = houseDAO.updateHouse(house);
        assertTrue(updated);

        House updatedHouse = houseDAO.getHouseById(houseId);
        assertEquals(1200, updatedHouse.getCapacity());
        assertEquals(HealthStatus.FAIR, updatedHouse.getHealthStatus());
    }

    @Test
    @Order(7)
    void testUpdateChickenCount() {
        boolean updated = houseDAO.updateChickenCount(houseId, 500);
        assertTrue(updated);

        House house = houseDAO.getHouseById(houseId);
        assertEquals(500, house.getChickenCount());
    }

    @Test
    @Order(8)
    void testAddChickensToHouse() {
        boolean added = houseDAO.addChickensToHouse(
                houseId,
                200,
                LocalDate.now()
        );
        assertTrue(added);

        House house = houseDAO.getHouseById(houseId);
        assertEquals(700, house.getChickenCount());
    }

    @Test
    @Order(9)
    void testRemoveChickensFromHouse() {
        boolean removed = houseDAO.removeChickensFromHouse(houseId, 200);
        assertTrue(removed);

        House house = houseDAO.getHouseById(houseId);
        assertEquals(500, house.getChickenCount());
    }

    @Test
    @Order(10)
    void testUpdateArrivalDate() {
        LocalDate date = LocalDate.now().minusDays(3);
        boolean updated = houseDAO.updateArrivalDate(houseId, date);
        assertTrue(updated);

        House house = houseDAO.getHouseById(houseId);
        assertEquals(date, house.getArrivalDate());
    }

    @Test
    @Order(11)
    void testUpdateHealthStatus() {
        boolean updated = houseDAO.updateHealthStatus(houseId, HealthStatus.GOOD);
        assertTrue(updated);

        House house = houseDAO.getHouseById(houseId);
        assertEquals(HealthStatus.GOOD, house.getHealthStatus());
    }

    @Test
    @Order(12)
    void testUpdateLastCleaningDate() {
        LocalDate cleaningDate = LocalDate.now().minusDays(10);
        boolean updated = houseDAO.updateLastCleaningDate(houseId, cleaningDate);
        assertTrue(updated);

        House house = houseDAO.getHouseById(houseId);
        assertEquals(cleaningDate, house.getLastCleaningDate());
    }

    @Test
    @Order(13)
    void testHasAnyChickens() {
        boolean hasChickens = houseDAO.hasAnyChickens();
        assertTrue(hasChickens);
    }

    @Test
    @Order(14)
    void testGetTotalChickenCount() {
        int total = houseDAO.getTotalChickenCount();
        assertTrue(total >= 500);
    }

    @Test
    @Order(15)
    void testGetTotalChickenCountByType() {
        int total = houseDAO.getTotalChickenCountByType(HouseType.DAY_OLD);
        assertTrue(total >= 500);
    }

    @Test
    @Order(16)
    void testGetHousesNeedingCleaning() {
        List<House> houses = houseDAO.getHousesNeedingCleaning(7);
        assertNotNull(houses);
    }

    @Test
    @Order(17)
    void testResetHouse() {
        boolean reset = houseDAO.resetHouse(houseId);
        assertTrue(reset);

        House house = houseDAO.getHouseById(houseId);
        assertEquals(0, house.getChickenCount());
        assertNull(house.getArrivalDate());
    }

    @Test
    @Order(18)
    void testDeleteHouse() {
        boolean deleted = houseDAO.deleteHouse(houseId);
        assertTrue(deleted);

        House house = houseDAO.getHouseById(houseId);
        assertNull(house);
    }
}
