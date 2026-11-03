package com.emsi.subtracker.utils;

import com.emsi.subtracker.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Utilitaire pour gérer la navigation entre les scènes.
 */
public class SceneManager {

    /**
     * Change la scène actuelle du Stage donné.
     * 
     * @param stage        La fenêtre principale.
     * @param fxmlFileName Le nom du fichier FXML (ex: "dashboard.fxml").
     */
    public static void switchScene(Stage stage, String fxmlFileName) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/views/" + fxmlFileName));
            javafx.scene.Parent root = fxmlLoader.load();

            // Try to reuse existing scene to prevent resizing/flickering
            Scene scene = stage.getScene();

            if (scene == null) {
                // First launch: Create new Scene
                scene = new Scene(root);
                scene.getStylesheets()
                        .add(Objects.requireNonNull(Main.class.getResource("/styles_v2.css")).toExternalForm());
                ThemeManager.applyTheme(scene);
                stage.setScene(scene);

                // Initial Maximize
                stage.setMaximized(true);
            } else {
                // Navigation: Replace Root
                scene.setRoot(root);

                // Ensure Stylesheet is present (crucial for Logout/Login flow)
                String cssPath = Objects.requireNonNull(Main.class.getResource("/styles_v2.css")).toExternalForm();
                if (!scene.getStylesheets().contains(cssPath)) {
                    scene.getStylesheets().add(cssPath);
                }

                ThemeManager.applyTheme(scene); // Re-apply theme to new root

                // Prevent Window Resizing: Ensure stage respects maximized state
                if (!stage.isMaximized()) {
                    stage.setMaximized(true);
                }

                // Smooth Fade In Transition
                root.setOpacity(0);
                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                        javafx.util.Duration.millis(300), root);
                ft.setFromValue(0.0);
                ft.setToValue(1.0);
                ft.play();
            }

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue : " + fxmlFileName);
            e.printStackTrace();
        }
    }
}
