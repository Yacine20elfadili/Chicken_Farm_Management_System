package ma.farm.dao;

import ma.farm.model.EquipmentCategory;
import ma.farm.model.EquipmentItem;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EquipmentItemDAOTest {

    private static EquipmentItemDAO itemDAO;
    private static EquipmentCategoryDAO categoryDAO;

    private static int categoryId;
    private static EquipmentItem testItem;

    @BeforeAll
    static void setup() {
        itemDAO = new EquipmentItemDAO();
        categoryDAO = new EquipmentCategoryDAO();

        // ---------- CREATE CATEGORY ----------
        EquipmentCategory category = new EquipmentCategory(
                0,
                "JUnit Item Category",
                "Other",
                "Storage",
                "Category for EquipmentItem tests",
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        assertTrue(categoryDAO.addCategory(category));

        categoryId = categoryDAO.getAllCategories()
                .stream()
                .filter(c -> "JUnit Item Category".equals(c.getName()))
                .findFirst()
                .orElseThrow()
                .getId();

        // ---------- CREATE ITEM ----------
        testItem = new EquipmentItem(
                0,
                categoryId,
                "Good",                        // ✅ VALIDE
                LocalDate.now().minusDays(10),
                1500.0,
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(10),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @Order(1)
    void testAddItem() {
        assertTrue(itemDAO.addItem(testItem));
    }

    @Test
    @Order(2)
    void testGetAllItems() {
        List<EquipmentItem> items = itemDAO.getAllItems();
        assertNotNull(items);
        assertFalse(items.isEmpty());
    }

    @Test
    @Order(3)
    void testGetItemById() {
        EquipmentItem saved = itemDAO.getAllItems()
                .stream()
                .filter(i -> i.getCategoryId() == categoryId)
                .findFirst()
                .orElse(null);

        assertNotNull(saved);
        testItem.setId(saved.getId());

        EquipmentItem found = itemDAO.getItemById(saved.getId());
        assertNotNull(found);
        assertEquals("Good", found.getStatus());
    }

    @Test
    @Order(4)
    void testGetItemsByCategory() {
        List<EquipmentItem> items =
                itemDAO.getItemsByCategory(categoryId);

        assertFalse(items.isEmpty());
    }

    @Test
    @Order(5)
    void testGetItemsByStatus() {
        List<EquipmentItem> items =
                itemDAO.getItemsByStatus("Good");

        assertFalse(items.isEmpty());
    }

    @Test
    @Order(6)
    void testUpdateItem() {
        testItem.setStatus("Broken");   // ✅ VALIDE

        assertTrue(itemDAO.updateItem(testItem));

        EquipmentItem updated =
                itemDAO.getItemById(testItem.getId());

        assertEquals("Broken", updated.getStatus());
    }

    @Test
    @Order(7)
    void testGetItemCountByStatus() {
        int count =
                itemDAO.getItemCountByStatus(categoryId, "Broken");

        assertTrue(count >= 1);
    }

    @Test
    @Order(8)
    void testDeleteItem() {
        assertTrue(itemDAO.deleteItem(testItem.getId()));
        assertNull(itemDAO.getItemById(testItem.getId()));
    }
}
