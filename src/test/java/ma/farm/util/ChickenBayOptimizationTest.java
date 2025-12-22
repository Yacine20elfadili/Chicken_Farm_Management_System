package ma.farm.util;

import ma.farm.model.House;
import ma.farm.model.HouseType;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChickenBayOptimizationTest {

    private static ChickenBayOptimization optimizer;

    @BeforeAll
    public static void setup() {
        optimizer = new ChickenBayOptimization();
    }

    @Test
    @Order(1)
    public void testCalculateX() {
        List<House> houses = new ArrayList<>();
        houses.add(new House(HouseType.DAY_OLD, 1, 100));
        houses.add(new House(HouseType.EGG_LAYER, 2, 150));

        int result = optimizer.calculateX(houses);
        assertEquals(250, result, "La somme des capacités doit être correcte");
    }

    @Test
    @Order(2)
    public void testValidateConstraints() {
        assertTrue(optimizer.validateConstraints(5, 10, 8, 3));
        assertFalse(optimizer.validateConstraints(12, 10, 8, 3));
        assertFalse(optimizer.validateConstraints(5, 10, 9, 3));
    }

    @Test
    @Order(3)
    public void testOptimizeHillClimbingIncreasesScore() {
        List<House> houses = new ArrayList<>();
        houses.add(new House(HouseType.DAY_OLD, 1, 1));
        houses.add(new House(HouseType.EGG_LAYER, 2, 2));
        houses.add(new House(HouseType.MEAT_FEMALE, 3, 3));
        houses.add(new House(HouseType.MEAT_MALE, 4, 4));

        int before = optimizer.calculateX(houses);
        optimizer.optimizeHillClimbing(houses);
        int after = optimizer.calculateX(houses);

        assertTrue(after >= before, "Le score après optimisation doit être supérieur ou égal au score initial");
    }

    @Test
    @Order(4)
    public void testOptimizeHillClimbingThrowsForSmallList() {
        List<House> houses = new ArrayList<>();
        houses.add(new House(HouseType.DAY_OLD, 1, 1));
        houses.add(new House(HouseType.EGG_LAYER, 2, 2));
        houses.add(new House(HouseType.MEAT_FEMALE, 3, 3));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            optimizer.optimizeHillClimbing(houses);
        });

        assertEquals("Minimum 4 houses required", exception.getMessage());
    }
}
