package ma.farm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the Login FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            // Create the scene
            Scene scene = new Scene(root);

            // Set the window title
            primaryStage.setTitle("Chicken Farm Management - Login");

            // Set the logo icon
            try {
                Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
                primaryStage.getIcons().add(logo);
            } catch (Exception e) {
                System.out.println("Logo not found: " + e.getMessage());
            }

            // Set the scene and show
            primaryStage.setScene(scene);
            primaryStage.setResizable(false); // Optional: prevent window resizing
            primaryStage.show();

            System.out.println("Application started successfully!");
            System.out.println("Login page loaded.");

        } catch (Exception e) {
            System.err.println("Error loading application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}