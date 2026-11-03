package com.emsi.subtracker.controllers;

import com.emsi.subtracker.models.FamilyMember;
import com.emsi.subtracker.models.User;
import com.emsi.subtracker.services.EmailService;
import com.emsi.subtracker.services.FamilyService;
import com.emsi.subtracker.services.UserService;
import com.emsi.subtracker.utils.PasswordUtil;
import com.emsi.subtracker.utils.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegisterController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private RadioButton individualRadio;
    @FXML
    private RadioButton familyRadio;
    @FXML
    private VBox familyMembersContainer;
    @FXML
    private VBox membersVBox;
    @FXML
    private Button addMemberButton;
    @FXML
    private Button registerButton;
    @FXML
    private Label errorLabel;
    @FXML
    private Label successLabel;

    private final UserService userService;
    private final EmailService emailService;
    private final FamilyService familyService;
    private final List<FamilyMemberInput> familyMemberInputs = new ArrayList<>();

    public RegisterController() {
        this.userService = new UserService();
        this.emailService = EmailService.getInstance();
        this.familyService = new FamilyService();
    }

    @FXML
    public void initialize() {
        // Initialiser avec compte individuel sélectionné
        if (individualRadio != null) {
            individualRadio.setSelected(true);
            familyMembersContainer.setVisible(false);
            familyMembersContainer.setManaged(false);
        }
    }

    @FXML
    private void onAccountTypeChanged() {
        boolean isFamilyAccount = familyRadio.isSelected();
        familyMembersContainer.setVisible(isFamilyAccount);
        familyMembersContainer.setManaged(isFamilyAccount);

        if (isFamilyAccount && familyMemberInputs.isEmpty()) {
            addFamilyMemberInput(); // Ajouter 1 membre par défaut
        }
    }

    @FXML
    private void onAddMemberClick() {
        if (familyMemberInputs.size() < 5) {
            addFamilyMemberInput();
        } else {
            errorLabel.setText("Maximum 5 membres autorisés.");
        }
    }

    private void addFamilyMemberInput() {
        HBox memberBox = new HBox(10);
        memberBox.setAlignment(Pos.CENTER_LEFT);

        TextField nameField = new TextField();
        nameField.setPromptText("Nom complet");
        nameField.setPrefWidth(150);
        nameField.setStyle("-fx-background-color: #2D2D2D; -fx-text-fill: white; -fx-background-radius: 5;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(120);
        usernameField.setStyle("-fx-background-color: #2D2D2D; -fx-text-fill: white; -fx-background-radius: 5;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setPrefWidth(120);
        passwordField.setStyle("-fx-background-color: #2D2D2D; -fx-text-fill: white; -fx-background-radius: 5;");

        Button removeBtn = new Button("×");
        removeBtn.setStyle(
                "-fx-background-color: #ff4c4c; -fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");
        removeBtn.setOnAction(e -> {
            membersVBox.getChildren().remove(memberBox);
            familyMemberInputs.removeIf(input -> input.container == memberBox);
        });

        memberBox.getChildren().addAll(nameField, usernameField, passwordField, removeBtn);
        membersVBox.getChildren().add(memberBox);

        familyMemberInputs.add(new FamilyMemberInput(memberBox, nameField, usernameField, passwordField));
    }

    private List<FamilyMember> getFamilyMembersFromInputs() {
        List<FamilyMember> members = new ArrayList<>();

        for (FamilyMemberInput input : familyMemberInputs) {
            String name = input.nameField.getText().trim();
            String username = input.usernameField.getText().trim();
            String password = input.passwordField.getText().trim();

            if (!name.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                FamilyMember member = new FamilyMember();
                member.setName(name);
                member.setUsername(username);
                member.setPassword(PasswordUtil.hash(password));
                members.add(member);
            }
        }

        return members;
    }

    @FXML
    private void handleRegister() {
        errorLabel.setText("");
        successLabel.setText("");

        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        if (!isValidEmail(email)) {
            errorLabel.setText("Format d'email invalide.");
            return;
        }

        try {
            // Déterminer le type de compte
            String accountType = familyRadio.isSelected() ? "family" : "individual";

            // Créer l'utilisateur
            User newUser = userService.register(username, email, password, accountType);

            // Si compte familial, créer les membres
            if ("family".equals(accountType)) {
                List<FamilyMember> members = getFamilyMembersFromInputs();
                if (!members.isEmpty()) {
                    familyService.createMultipleMembers(newUser.getId(), members);
                    successLabel.setText("Compte familial créé avec " + members.size() + " membre(s) !");
                } else {
                    successLabel.setText("Compte familial créé (aucun membre ajouté).");
                }
            } else {
                successLabel.setText("Compte créé avec succès !");
            }

            // Envoyer email de bienvenue (asynchrone)
            emailService.sendWelcomeEmail(newUser);
            System.out.println("📧 Email de bienvenue envoyé à: " + newUser.getEmail());

            // Rediriger vers login après délai
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::goToLogin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            errorLabel.setText("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    @FXML
    private void goToLogin() {
        try {
            SceneManager.switchScene((Stage) registerButton.getScene().getWindow(), "login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur de navigation.");
        }
    }

    private static class FamilyMemberInput {
        HBox container;
        TextField nameField;
        TextField usernameField;
        PasswordField passwordField;

        FamilyMemberInput(HBox container, TextField name, TextField username, PasswordField password) {
            this.container = container;
            this.nameField = name;
            this.usernameField = username;
            this.passwordField = password;
        }
    }
}
