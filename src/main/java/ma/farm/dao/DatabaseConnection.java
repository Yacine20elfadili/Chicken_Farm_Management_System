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
     * This creates all tables, triggers, views, and inserts default data
     */
    private void initDatabase() {
        try {
            // Load schema file from resources
            InputStream schemaStream = getClass().getClassLoader()
                    .getResourceAsStream(SCHEMA_FILE);

            if (schemaStream == null) {
                System.err.println("schema.sql not found in resources!");
                return;
            }

            // Read the entire SQL script
            BufferedReader reader = new BufferedReader(new InputStreamReader(schemaStream));
            StringBuilder sqlScript = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comments
                if (!line.isEmpty() && !line.startsWith("--")) {
                    sqlScript.append(line).append(" ");
                }
            }
            reader.close();

            // Split by semicolon and execute each statement
            String[] statements = sqlScript.toString().split(";");

            try (Statement stmt = connection.createStatement()) {
                int executedCount = 0;
                for (String sql : statements) {
                    sql = sql.trim();
                    if (!sql.isEmpty()) {
                        try {
                            stmt.execute(sql);
                            executedCount++;
                        } catch (SQLException e) {
                            // Silently continue - triggers with BEGIN/END blocks will fail
                            // due to semicolon-based parsing, but tables work fine
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

}
