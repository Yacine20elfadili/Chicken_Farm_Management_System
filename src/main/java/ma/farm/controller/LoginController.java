package ma.farm.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ma.farm.dao.UserDAO;
import ma.farm.model.User;
import ma.farm.util.ValidationUtil;

import java.io.IOException;

public class LoginController {

    // FXML Components - must match fx:id in LoginView.fxml
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signUpLinkButton;

    @FXML
    private Label errorLabel;

    // DAO for database operations
    private UserDAO userDAO;

    /**
     * Initialize method - called automatically after FXML is loaded
     */
    @FXML
    public void initialize() {
        // Initialize DAO
        userDAO = new UserDAO();

        // Hide error label initially
        errorLabel.setVisible(false);

        // Add enter key listener to password field
        passwordField.setOnAction(this::handleLogin);
        passwordField.setText("admin123");

        // Add enter key listener to email field
        emailField.setOnAction(event -> passwordField.requestFocus());
        emailField.setText("admin@farm.ma");
    }

    /**
     * Handles login button click
     * 
     * @param event The action event
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        // Hide previous error messages
        errorLabel.setVisible(false);

        // Get input values
        String email = ValidationUtil.sanitize(emailField.getText());
        String password = passwordField.getText(); // Don't trim password

        // Validate inputs
        if (!validateInputs(email, password)) {
            return; // Validation failed, error already shown
        }

        // Attempt authentication
        try {
            User user = userDAO.authenticate(email, password);

            if (user != null) {
                // Login successful
                System.out.println("Login successful for: " + user.getCompanyName());
                navigateToDashboard();
            } else {
                // This shouldn't happen as authenticate throws exception
                showError("Authentication failed. Please try again.");
            }

        } catch (SecurityException e) {
            // Invalid credentials
            showError("Email ou mot de passe incorrect.");

        } catch (Exception e) {
            // Database or other errors
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur de connexion. Veuillez réessayer.");
        }
    }

    /**
     * Validates user inputs
     * 
     * @param email    The email to validate
     * @param password The password to validate
     * @return true if inputs are valid, false otherwise
     */
    private boolean validateInputs(String email, String password) {
        // Check for empty email
        if (ValidationUtil.isEmpty(email)) {
            showError("Veuillez entrer votre email.");
            emailField.requestFocus();
            return false;
        }

        // Check for valid email format
        if (!ValidationUtil.isValidEmail(email)) {
            showError("Format d'email invalide.");
            emailField.requestFocus();
            return false;
        }

        // Check for empty password
        if (ValidationUtil.isEmpty(password)) {
            showError("Veuillez entrer votre mot de passe.");
            passwordField.requestFocus();
            return false;
        }

        // Check password length
        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            passwordField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Shows error message to user
     * 
     * @param message The error message to display
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Navigates to MainWindow after successful login
     */
    private void navigateToDashboard() {
        try {
            // Load MainWindow FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
            Parent mainWindowRoot = loader.load();

            // Get current stage (login window)
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Create new scene with MainWindow
            Scene mainScene = new Scene(mainWindowRoot);

            // Set the new scene
            stage.setScene(mainScene);
            stage.setTitle("Chicken Farm Management - Dashboard");
            stage.setMaximized(true); // Open maximized for better view

            System.out.println("Successfully navigated to MainWindow!");

        } catch (IOException e) {
            System.err.println("Error loading MainWindow: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du tableau de bord.");
        }
    }

    /**
     * Clears all input fields and error messages
     */
    private void clearForm() {
        emailField.clear();
        passwordField.clear();
        errorLabel.setVisible(false);
        emailField.requestFocus();
    }

    /**
     * Handles sign up link click - navigates to sign up page
     * 
     * @param event The action event
     */
    @FXML
    private void handleSignUpLink(ActionEvent event) {
        try {
            // Load sign up page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SignUpView.fxml"));
            Parent signUpRoot = loader.load();

            // Get current stage
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Set sign up scene
            Scene signUpScene = new Scene(signUpRoot);
            stage.setScene(signUpScene);
            stage.setTitle("Chicken Farm Management - Sign Up");

            System.out.println("Navigated to sign up page successfully!");

        } catch (IOException e) {
            System.err.println("Error loading sign up page: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors du chargement de la page d'inscription.");
        }
    }
}