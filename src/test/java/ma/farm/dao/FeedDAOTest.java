package ma.farm.dao;

import ma.farm.model.Feed;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FeedDAOTest {

    private static FeedDAO feedDAO;
    private static int feedId;

    @BeforeAll
    static void setup() {
        feedDAO = new FeedDAO();
    }

    @Test
    @Order(1)
    void testAddFeed() {
        Feed feed = new Feed();
        feed.setName("Maïs Test");
        feed.setType("Grain");
        feed.setQuantityKg(500);
        feed.setPricePerKg(3.5);
        feed.setSupplier("Supplier-Test");
        feed.setLastRestockDate(LocalDate.now());
        feed.setExpiryDate(LocalDate.now().plusDays(40));
        feed.setMinStockLevel(100);

        boolean created = feedDAO.addFeed(feed);
        assertTrue(created);
    }

    @Test
    @Order(2)
    void testGetAllFeed() {
        List<Feed> feeds = feedDAO.getAllFeed();
        assertNotNull(feeds);
        assertFalse(feeds.isEmpty());

        feedId = feeds.get(feeds.size() - 1).getId();
        assertTrue(feedId > 0);
    }

    @Test
    @Order(3)
    void testGetFeedById() {
        Feed feed = feedDAO.getFeedById(feedId);
        assertNotNull(feed);
        assertEquals("Maïs Test", feed.getName());
    }

    @Test
    @Order(4)
    void testGetFeedByType() {
        List<Feed> feeds = feedDAO.getFeedByType("Grain");
        assertFalse(feeds.isEmpty());
    }

    @Test
    @Order(5)
    void testGetLowStockFeed() {
        List<Feed> feeds = feedDAO.getLowStockFeed();
        assertNotNull(feeds); // peut être vide
    }

    @Test
    @Order(6)
    void testGetExpiringFeed() {
        List<Feed> feeds = feedDAO.getExpiringFeed();
        assertNotNull(feeds);
    }

    @Test
    @Order(7)
    void testGetExpiredFeed() {
        List<Feed> feeds = feedDAO.getExpiredFeed();
        assertNotNull(feeds);
    }

    @Test
    @Order(8)
    void testUpdateFeed() {
        Feed feed = feedDAO.getFeedById(feedId);
        assertNotNull(feed);

        feed.setQuantityKg(300);
        feed.setPricePerKg(4.0);
        feed.setSupplier("Supplier-Updated");

        boolean updated = feedDAO.updateFeed(feed);
        assertTrue(updated);

        Feed updatedFeed = feedDAO.getFeedById(feedId);
        assertEquals(300, updatedFeed.getQuantityKg());
    }

    @Test
    @Order(9)
    void testUpdateQuantity() {
        boolean updated = feedDAO.updateQuantity(feedId, 200);
        assertTrue(updated);

        Feed feed = feedDAO.getFeedById(feedId);
        assertEquals(200, feed.getQuantityKg());
    }

    @Test
    @Order(10)
    void testGetTotalFeedQuantity() {
        double total = feedDAO.getTotalFeedQuantity();
        assertTrue(total >= 0);
    }

    @Test
    @Order(11)
    void testGetTotalFeedValue() {
        double value = feedDAO.getTotalFeedValue();
        assertTrue(value >= 0);
    }

    @Test
    @Order(12)
    void testDeleteFeed() {
        boolean deleted = feedDAO.deleteFeed(feedId);
        assertTrue(deleted);

        Feed feed = feedDAO.getFeedById(feedId);
        assertNull(feed);
    }
}
