package ma.farm.dao;

import ma.farm.model.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOTest {

    private static UserDAO userDAO;
    private static User testUser;

    @BeforeAll
    static void setup() throws Exception {
        userDAO = new UserDAO();

        // Nettoyer uniquement l'utilisateur de test
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "DELETE FROM users WHERE email LIKE 'test_%@mail.com'"
            );
        }

        testUser = new User(
                0,
                "Test User",
                "test_user@mail.com",
                "1234",
                LocalDateTime.now()
        );
    }

    // ---------------- CREATE ----------------
    @Test
    @Order(1)
    void testCreateUser() {
        boolean created = userDAO.createUser(testUser);

        assertTrue(created);
        assertTrue(testUser.getId() > 0);
    }

    // ---------------- READ BY ID ----------------
    @Test
    @Order(2)
    void testGetUserById() {
        User user = userDAO.getUserById(testUser.getId());

        assertNotNull(user);
        assertEquals(testUser.getEmail(), user.getEmail());
    }

    // ---------------- READ BY EMAIL ----------------
    @Test
    @Order(3)
    void testGetUserByEmail() {
        User user = userDAO.getUserByEmail(testUser.getEmail());

        assertNotNull(user);
        assertEquals(testUser.getName(), user.getName());
    }

    // ---------------- GET ALL ----------------
    @Test
    @Order(4)
    void testGetAllUsers() {
        List<User> users = userDAO.getAllUsers();

        assertNotNull(users);
        assertTrue(users.size() > 0);
    }

    // ---------------- VALIDATE ----------------
    @Test
    @Order(5)
    void testValidate() {
        assertTrue(
                userDAO.validate("test_user@mail.com", "1234")
        );
        assertFalse(
                userDAO.validate("test_user@mail.com", "wrong")
        );
    }

    // ---------------- AUTHENTICATE ----------------
    @Test
    @Order(6)
    void testAuthenticate() {
        User user = userDAO.authenticate(
                "test_user@mail.com",
                "1234"
        );

        assertNotNull(user);
        assertEquals(testUser.getEmail(), user.getEmail());
    }

    @Test
    @Order(7)
    void testAuthenticateFail() {
        assertThrows(
                SecurityException.class,
                () -> userDAO.authenticate(
                        "test_user@mail.com",
                        "badpass"
                )
        );
    }

    // ---------------- UPDATE ----------------
    @Test
    @Order(8)
    void testUpdateUser() {
        testUser.setName("Updated Name");

        boolean updated = userDAO.updateUser(testUser);
        assertTrue(updated);

        User updatedUser = userDAO.getUserById(testUser.getId());
        assertEquals("Updated Name", updatedUser.getName());
    }

    // ---------------- COUNT ----------------
    @Test
    @Order(9)
    void testUserCount() {
        int count = userDAO.getUserCount();
        assertTrue(count > 0);
    }

    // ---------------- DELETE ----------------
    @Test
    @Order(10)
    void testDeleteUser() {
        boolean deleted = userDAO.deleteUser(testUser.getId());
        assertTrue(deleted);

        User user = userDAO.getUserById(testUser.getId());
        assertNull(user);
    }
}
