package ma.farm.dao;

import ma.farm.model.EquipmentCategory;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EquipmentCategoryDAOTest {

    private static EquipmentCategoryDAO categoryDAO;
    private static EquipmentCategory testCategory;

    @BeforeAll
    static void setup() {
        categoryDAO = new EquipmentCategoryDAO();

        // ⚠️ category DOIT respecter la contrainte CHECK
        testCategory = new EquipmentCategory(
                0,
                "JUnit Category",
                "Other",              // ✅ valeur AUTORISÉE
                "Warehouse",
                "Category for JUnit testing",
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // ---------- CREATE ----------
    @Test
    @Order(1)
    void testAddCategory() {
        boolean created = categoryDAO.addCategory(testCategory);
        assertTrue(created);

        assertTrue(
                categoryDAO.categoryNameExists("JUnit Category")
        );
    }

    // ---------- READ ALL ----------
    @Test
    @Order(2)
    void testGetAllCategories() {
        List<EquipmentCategory> categories =
                categoryDAO.getAllCategories();

        assertNotNull(categories);
        assertTrue(categories.size() > 0);
    }

    // ---------- READ BY ID ----------
    @Test
    @Order(3)
    void testGetCategoryById() {
        EquipmentCategory found = categoryDAO
                .getAllCategories()
                .stream()
                .filter(c -> "JUnit Category".equals(c.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull(found);

        testCategory.setId(found.getId());

        EquipmentCategory byId =
                categoryDAO.getCategoryById(found.getId());

        assertNotNull(byId);
        assertEquals("JUnit Category", byId.getName());
    }

    // ---------- UPDATE ----------
    @Test
    @Order(4)
    void testUpdateCategory() {
        testCategory.setName("JUnit Category Updated");

        boolean updated =
                categoryDAO.updateCategory(testCategory);

        assertTrue(updated);

        EquipmentCategory updatedCategory =
                categoryDAO.getCategoryById(testCategory.getId());

        assertEquals(
                "JUnit Category Updated",
                updatedCategory.getName()
        );
    }

    // ---------- DELETE ----------
    @Test
    @Order(5)
    void testDeleteCategory() {
        boolean deleted =
                categoryDAO.deleteCategory(testCategory.getId());

        assertTrue(deleted);

        EquipmentCategory deletedCategory =
                categoryDAO.getCategoryById(testCategory.getId());

        assertNull(deletedCategory);
    }
}
