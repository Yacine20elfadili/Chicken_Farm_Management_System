package ma.farm.dao;
import java.io.BufferedReader;
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
            Class.forName(DRIVER);
            this.connection = DriverManager.getConnection(URL);
            System.out.println("Database connected successfully!");
            initDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection failed!");
            e.printStackTrace();
        }
    }

    public static String getDatabasePath() {
        return URL.substring(12);
    }

    private void initDatabase() {
        try {
            InputStream schemaStream = getClass().getClassLoader()
                    .getResourceAsStream(SCHEMA_FILE);

            if (schemaStream == null) {
                System.err.println("schema.sql not found!");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(schemaStream));
            StringBuilder sqlScript = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("--")) {
                    sqlScript.append(line).append(" ");
                }
            }
            reader.close();

            String[] statements = sqlScript.toString().split(";");

            try (Statement stmt = connection.createStatement()) {
                for (String sql : statements) {
                    sql = sql.trim();
                    if (!sql.isEmpty()) {
                        stmt.execute(sql);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}