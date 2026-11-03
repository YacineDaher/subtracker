package com.emsi.subtracker.controllers;

import com.emsi.subtracker.utils.CategoryManager;
import com.emsi.subtracker.utils.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;

import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class CategoriesController implements Initializable {

  @FXML
  private ListView<String> categoriesList;

  private ObservableList<String> listItems;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    loadCategories();
  }

  private void loadCategories() {
    listItems = FXCollections.observableArrayList(CategoryManager.getInstance().getAllCategories());
    categoriesList.setItems(listItems);
  }

  @FXML
  private void onAddCategory() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Nouvelle Catégorie");
    dialog.setHeaderText("Ajouter une catégorie personnalisée");
    dialog.setContentText("Nom de la catégorie :");

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(name -> {
      if (!name.trim().isEmpty()) {
        CategoryManager.getInstance().addCategory(name.trim());
        loadCategories(); // Refresh
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie ajoutée : " + name);
      }
    });
  }

  // --- NAVIGATION ---

  @FXML
  protected void goToDashboard() {
    try {
      Stage currentStage = (Stage) categoriesList.getScene().getWindow();
      SceneManager.switchScene(currentStage, "dashboard.fxml");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @FXML
  protected void goToAnalytics() {
    try {
      Stage currentStage = (Stage) categoriesList.getScene().getWindow();
      SceneManager.switchScene(currentStage, "analytics.fxml");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @FXML
  protected void goToSettings() {
    try {
      Stage currentStage = (Stage) categoriesList.getScene().getWindow();
      SceneManager.switchScene(currentStage, "settings.fxml");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void showAlert(Alert.AlertType type, String title, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
