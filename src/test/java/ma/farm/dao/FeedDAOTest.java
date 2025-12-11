package ma.farm.dao;

import ma.farm.model.Feed;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FeedDAOTest {

    private static FeedDAO feedDAO;
    private static int createdFeedId;

    @BeforeAll
    static void setup() {
        feedDAO = new FeedDAO();
    }

    @Test
    @Order(1)
    void testAddFeed() {
        Feed feed = new Feed();
        feed.setName("JUnit Test Feed");
        feed.setType("Layer");
        feed.setQuantityKg(120.0);
        feed.setPricePerKg(10.0);
        feed.setSupplier("JUnit Supplier");
        feed.setLastRestockDate(LocalDate.now());
        feed.setExpiryDate(LocalDate.now().plusDays(30));
        feed.setMinStockLevel(50.0);

        boolean created = feedDAO.addFeed(feed);

        assertTrue(created, "L'ajout doit réussir");
        assertTrue(feed.getId() > 0, "L'ID doit être généré");

        createdFeedId = feed.getId();
    }

    @Test
    @Order(2)
    void testGetFeedById() {
        Feed feed = feedDAO.getFeedById(createdFeedId);

        assertNotNull(feed, "Feed ne doit pas être null");
        assertEquals("JUnit Test Feed", feed.getName());
    }

    @Test
    @Order(3)
    void testGetAllFeed() {
        List<Feed> list = feedDAO.getAllFeed();
        assertNotNull(list);
        assertTrue(list.size() > 0, "La liste ne doit pas être vide");
    }

    @Test
    @Order(4)
    void testUpdateQuantity() {
        boolean updated = feedDAO.updateQuantity(createdFeedId, 200.0);
        assertTrue(updated);

        Feed updatedFeed = feedDAO.getFeedById(createdFeedId);
        assertEquals(200.0, updatedFeed.getQuantityKg());
    }

    @Test
    @Order(5)
    void testUpdateFeed() {
        Feed feed = feedDAO.getFeedById(createdFeedId);

        feed.setName("Updated JUnit Feed");
        feed.setPricePerKg(12.5);

        boolean updated = feedDAO.updateFeed(feed);
        assertTrue(updated);

        Feed updatedFeed = feedDAO.getFeedById(createdFeedId);
        assertEquals("Updated JUnit Feed", updatedFeed.getName());
        assertEquals(12.5, updatedFeed.getPricePerKg());
    }

    @Test
    @Order(6)
    void testGetLowStockCount() {
        int count = feedDAO.getLowStockCount();
        assertTrue(count >= 0, "Le count doit être >= 0");
    }

    @Test
    @Order(7)
    void testGetTotalFeedValue() {
        double value = feedDAO.getTotalFeedValue();
        assertTrue(value >= 0);
    }

    @Test
    @Order(8)
    void testDeleteFeed() {
        boolean deleted = feedDAO.deleteFeed(createdFeedId);
        assertTrue(deleted, "La suppression doit réussir");

        Feed deletedFeed = feedDAO.getFeedById(createdFeedId);
        assertNull(deletedFeed, "Le feed supprimé ne doit plus exister");
    }
}
