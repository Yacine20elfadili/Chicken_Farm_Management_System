package ma.farm.dao;

import ma.farm.model.User;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOTest {

    private static UserDAO userDAO;
    private static int testUserId;

    @BeforeAll
    static void setup() {
        userDAO = new UserDAO();
    }

    private User createTestUser(String email) {
        return new User(
                0,
                email,
                "password123",
                "Test Company",
                "SARL",
                100000,
                "ICE" + System.currentTimeMillis(),
                "RC12345",
                "FISC123",
                123456,
                "CNSS123",
                "ONSSA123",
                "Rue Test",
                "Casablanca",
                "20000",
                "RIB123456789",
                "CIH",
                "0612345678",
                "www.test.ma",
                null, // logo
                (java.time.LocalDateTime) null, // creationDate
                (java.time.LocalDateTime) null // updatedAt
        );
    }

    @Test
    @Order(1)
    void testCreateUser() {
        User user = createTestUser("user@test.ma");
        boolean created = userDAO.createUser(user);

        assertTrue(created);
        assertTrue(user.getId() > 0);

        testUserId = user.getId();
    }

    @Test
    @Order(2)
    void testGetUserById() {
        User user = userDAO.getUserById(testUserId);

        assertNotNull(user);
        assertEquals(testUserId, user.getId());
        assertEquals("user@test.ma", user.getEmail());
    }

    @Test
    @Order(3)
    void testGetUserByEmail() {
        User user = userDAO.getUserByEmail("user@test.ma");

        assertNotNull(user);
        assertEquals(testUserId, user.getId());
    }

    @Test
    @Order(4)
    void testValidateLogin() {
        assertTrue(userDAO.validate("user@test.ma", "password123"));
        assertFalse(userDAO.validate("user@test.ma", "wrongpassword"));
    }

    @Test
    @Order(5)
    void testAuthenticate() {
        User user = userDAO.authenticate("user@test.ma", "password123");

        assertNotNull(user);
        assertEquals("user@test.ma", user.getEmail());
    }

    @Test
    @Order(6)
    void testUpdateUser() {
        User user = userDAO.getUserById(testUserId);
        user.setCompanyName("Updated Company");

        boolean updated = userDAO.updateUser(user);

        assertTrue(updated);

        User updatedUser = userDAO.getUserById(testUserId);
        assertEquals("Updated Company", updatedUser.getCompanyName());
    }

    @Test
    @Order(7)
    void testEmailExists() {
        assertTrue(userDAO.isEmailExists("user@test.ma"));
        assertFalse(userDAO.isEmailExists("notexist@test.ma"));
    }

    @Test
    @Order(8)
    void testICEExists() {
        User user = userDAO.getUserById(testUserId);
        assertTrue(userDAO.isICEExists(user.getIce()));
    }

    @Test
    @Order(9)
    void testRIBExists() {
        User user = userDAO.getUserById(testUserId);
        assertTrue(userDAO.isRIBExists(user.getBankRIB()));
    }

    @Test
    @Order(10)
    void testUserCount() {
        int count = userDAO.getUserCount();
        assertTrue(count > 0);
    }

    @Test
    @Order(11)
    void testDeleteUser() {
        boolean deleted = userDAO.deleteUser(testUserId);
        assertTrue(deleted);

        User user = userDAO.getUserById(testUserId);
        assertNull(user);
    }
}
