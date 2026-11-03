package com.emsi.subtracker.controllers;

import com.emsi.subtracker.models.User;
import com.emsi.subtracker.services.UserService;
import com.emsi.subtracker.utils.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorLabel;

    private final UserService userService;

    public LoginController() {
        this.userService = new UserService();
    }

    @FXML
    private void handleLogin() {
        System.out.println("Login button clicked.");
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        // Authentification universelle (User OU FamilyMember)
        Object authResult = userService.authenticateAny(username, password);

        if (authResult instanceof User) {
            // Connexion en tant qu'utilisateur principal
            User user = (User) authResult;
            System.out.println("Authentication successful for user: " + user.getUsername());

            try {
                com.emsi.subtracker.utils.UserSession.getInstance().setUser(user);
                System.out.println("Switching to dashboard.fxml...");
                SceneManager.switchScene((Stage) loginButton.getScene().getWindow(), "dashboard.fxml");
            } catch (IOException e) {
                System.err.println("FAILED to load dashboard: " + e.getMessage());
                e.printStackTrace();
                errorLabel.setText("Erreur lors du chargement du dashboard.");
            }

        } else if (authResult instanceof com.emsi.subtracker.models.FamilyMember) {
            // Connexion en tant que membre de famille
            com.emsi.subtracker.models.FamilyMember member = (com.emsi.subtracker.models.FamilyMember) authResult;
            System.out.println("Authentication successful for family member: " + member.getName());

            try {
                com.emsi.subtracker.utils.UserSession.getInstance().setFamilyMember(member);
                System.out.println("Switching to dashboard.fxml...");
                SceneManager.switchScene((Stage) loginButton.getScene().getWindow(), "dashboard.fxml");
            } catch (IOException e) {
                System.err.println("FAILED to load dashboard: " + e.getMessage());
                e.printStackTrace();
                errorLabel.setText("Erreur lors du chargement du dashboard.");
            }

        } else {
            System.out.println("Authentication failed for: " + username);
            errorLabel.setText("Identifiants incorrects.");
        }
    }

    @FXML
    private TextField passwordTextField;
    @FXML
    private javafx.scene.control.CheckBox showPasswordCheckBox;

    @FXML
    public void initialize() {
        // Sync text fields
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void onShowPassword() {
        if (showPasswordCheckBox.isSelected()) {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
        }
    }

    @FXML
    private void goToRegister() {
        try {
            SceneManager.switchScene((Stage) loginButton.getScene().getWindow(), "register.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur de navigation.");
        }
    }

    @FXML
    private void handleResetDB() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Réinitialiser la Base de Données");
        alert.setHeaderText("Supprimer toutes les données ?");
        alert.setContentText("Cela effacera tous les comptes et abonnements. Cette action est irréversible.");

        if (alert.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            try {
                com.emsi.subtracker.utils.DatabaseCleaner.reset();
                javafx.scene.control.Alert success = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                success.setTitle("Succès");
                success.setHeaderText(null);
                success.setContentText("Base de données réinitialisée avec succès !");
                success.show();
            } catch (Exception e) {
                errorLabel.setText("Erreur lors du reset: " + e.getMessage());
            }
        }
    }
}
