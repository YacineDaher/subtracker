package com.emsi.subtracker.utils;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ThemeManager {
  private static boolean isDarkTheme = true; // Default to dark

  public static boolean isDarkTheme() {
    return isDarkTheme;
  }

  public static void setDarkTheme(boolean dark) {
    isDarkTheme = dark;
    updateActiveWindows();
  }

  public static void applyTheme(Scene scene) {
    if (scene == null)
      return;

    ObservableList<String> styleClasses = scene.getRoot().getStyleClass();
    if (!isDarkTheme) {
      if (!styleClasses.contains("light-theme")) {
        styleClasses.add("light-theme");
      }
    } else {
      styleClasses.remove("light-theme");
    }
  }

  private static void updateActiveWindows() {
    for (Window window : Stage.getWindows()) {
      if (window instanceof Stage && window.isShowing()) {
        Scene scene = window.getScene();
        if (scene != null) {
          applyTheme(scene);
        }
      }
    }
  }
}
