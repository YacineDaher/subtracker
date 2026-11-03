package com.emsi.subtracker;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        // Init Database
        com.emsi.subtracker.utils.DBConnection.getInstance();

        primaryStage = stage;
        primaryStage.setTitle("SubTracker - Gestion d'Abonnements");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        // Use SceneManager to ensure consistent styling and sizing logic
        com.emsi.subtracker.utils.SceneManager.switchScene(stage, "login.fxml");
    }

    public static void main(String[] args) {
        launch();
    }
}
