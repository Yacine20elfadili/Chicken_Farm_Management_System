package ma.farm.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static volatile DatabaseConnection instance;
    private Connection connection;

    private static final String URL = "jdbc:sqlite:database/farm.db";
    private static final String DRIVER = "org.sqlite.JDBC";
    private static final String SCHEMA_FILE = "database/schema.sql";

    private DatabaseConnection() {
        try {
            // **FIX 1**: Ensure the database directory exists
            ensureDatabaseDirectoryExists();

            // Load SQLite JDBC driver
            Class.forName(DRIVER);

            // **FIX 2**: Connect to database (will create file if doesn't exist)
            getConnection();

            // **FIX 3**: Enable foreign keys (important for SQLite)
            enableForeignKeys();

            System.out.println("Database connected successfully!");
            System.out.println("Database path: " + getDatabasePath());

            // Initialize database schema
            initDatabase();

        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    /**
     * FIX 1: Ensures the database directory exists
     * Creates the directory if it doesn't exist
     */
    private void ensureDatabaseDirectoryExists() {
        String dbPath = getDatabasePath();
        File dbFile = new File(dbPath);
        File dbDir = dbFile.getParentFile();

        if (dbDir != null && !dbDir.exists()) {
            boolean created = dbDir.mkdirs();
            if (created) {
                System.out.println("Created database directory: " + dbDir.getAbsolutePath());
            } else {
                System.err.println("Failed to create database directory: " + dbDir.getAbsolutePath());
            }
        }
    }

    /**
     * FIX 2: Enables foreign key constraints for SQLite
     * Must be called for each connection
     */
    private void enableForeignKeys() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            System.out.println("Foreign keys enabled.");
        } catch (SQLException e) {
            System.err.println("Failed to enable foreign keys: " + e.getMessage());
        }
    }

    public static String getDatabasePath() {
        // Remove "jdbc:sqlite:" prefix to get the actual file path
        return URL.substring(12);
    }

    /**
     * Initializes the database by executing the schema.sql file
     * Uses a robust parser to handle Triggers correctly (which contain semicolons).
     */
    private void initDatabase() {
        try {
            // Load schema file from resources
            InputStream schemaStream = getClass().getClassLoader()
                    .getResourceAsStream(SCHEMA_FILE);

            if (schemaStream == null) {
                System.err.println("schema.sql not found in resources!");
                System.err.println("Expected path: " + SCHEMA_FILE);
                return;
            }

            System.out.println("Loading schema from: " + SCHEMA_FILE);

            // Read the entire SQL script
            BufferedReader reader = new BufferedReader(new InputStreamReader(schemaStream));
            StringBuilder scriptBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                scriptBuilder.append(line).append("\n");
            }
            reader.close();

            String[] statements = splitSqlScript(scriptBuilder.toString());

            try (Statement stmt = connection.createStatement()) {
                int executedCount = 0;
                for (String sql : statements) {
                    sql = sql.trim();
                    if (!sql.isEmpty() && !sql.startsWith("--")) {
                        try {
                            stmt.execute(sql);
                            executedCount++;
                        } catch (SQLException e) {
                            // Suppress "table already exists" or "index already exists" errors quietly
                            if (!e.getMessage().contains("already exists")) {
                                System.err.println("Error executing SQL: " + e.getMessage());
                                System.err.println("Statement start: " + sql.substring(0, Math.min(50, sql.length())));
                            }
                        }
                    }
                }
                System.out.println("Database initialized successfully!");
                System.out.println("Executed " + executedCount + " SQL statements.");
            }

        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Splits SQL script into statements, preserving semicolons inside BEGIN...END
     * blocks (Triggers) and handling comments correctly.
     */
    private String[] splitSqlScript(String script) {
        java.util.List<String> statements = new java.util.ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();

        // We track the depth of blocks (BEGIN/CASE/IF) to prevent splitting triggers
        // prematurely.
        int blockDepth = 0;

        // Normalize line endings
        String[] lines = script.split("\n");

        for (String line : lines) {
            String trimmedLine = line.trim();

            // Skip purely empty lines or full-line comments
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                continue;
            }

            // Append line with a newline to preserve comment structure (important!)
            currentStatement.append(line).append("\n");

            // Analyze the line for semantic blocks (only if it's impactful)
            // We do a simple token scan using normalized uppercase version
            String upper = trimmedLine.toUpperCase();

            // Count occurrences of keywords that start a block
            if (containsKeyword(upper, "BEGIN"))
                blockDepth++;
            if (containsKeyword(upper, "CASE"))
                blockDepth++;

            // Count occurrences of END
            if (containsKeyword(upper, "END"))
                blockDepth--;

            // Safety: clamp depth to 0
            if (blockDepth < 0)
                blockDepth = 0;

            // Check for statement terminator
            if (trimmedLine.endsWith(";") && blockDepth == 0) {
                statements.add(currentStatement.toString());
                currentStatement.setLength(0);
            }
        }

        return statements.toArray(new String[0]);
    }

    private boolean containsKeyword(String line, String keyword) {
        // Simple regex check for whole word
        return java.util.regex.Pattern.compile("\\b" + keyword + "\\b").matcher(line).find();
    }

    /**
     * Returns the singleton instance of DatabaseConnection
     * Uses double-checked locking for thread safety
     */
    public static DatabaseConnection getInstance() {
        DatabaseConnection result = DatabaseConnection.instance;
        if (result == null) {
            synchronized (DatabaseConnection.class) {
                result = DatabaseConnection.instance;
                if (result == null) {
                    DatabaseConnection.instance = result = new DatabaseConnection();
                }
            }
        }
        return result;
    }

    /**
     * Returns the database connection
     * Reconnects if the connection is closed
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
                enableForeignKeys(); // Re-enable foreign keys for new connection
            }
        } catch (SQLException e) {
            System.err.println("Error getting connection: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Closes the database connection
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Resets the database by dropping all tables.
     * Effectively Wipes the database similar to deleting the file.
     * 
     * @return true if successful, false otherwise.
     */
    public boolean resetDatabase() {
        // Get all table names
        java.util.List<String> tables = new java.util.ArrayList<>();
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';";

        try (Statement stmt = getConnection().createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                tables.add(rs.getString("name"));
            }

            // Disable FKs temporarily to avoid constraint issues during drop
            stmt.execute("PRAGMA foreign_keys = OFF;");

            for (String table : tables) {
                stmt.execute("DROP TABLE IF EXISTS " + table);
            }

            stmt.execute("PRAGMA foreign_keys = ON;");

            System.out.println("All tables dropped. Database reset.");

            // Re-initialize schema immediately so the app can restart cleanly if needed,
            // or leave it empty if the intention is a clean slate that needs
            // initialization.
            // The user said "same effect when I delete the database file".
            // If we delete the file, the next 'new DatabaseConnection()' re-inits it.
            // So we should probably call initDatabase() again to recreate tables?
            // "Drop all the tables... same effect as deleting file".
            // If I delete file, the app usually recreates tables on next launch.
            // If I redirect to Login page, the app expects tables to exist (e.g. Users
            // table to check login).
            // But if I want to "delete account", I assume the user means "WIPE EVERYTHING".
            // So I should Re-Init the blank schema so it's fresh.

            initDatabase();

            return true;
        } catch (SQLException e) {
            System.err.println("Error resetting database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
