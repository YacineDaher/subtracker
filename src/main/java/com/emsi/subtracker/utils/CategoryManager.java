package com.emsi.subtracker.utils;

import java.io.*;
import java.util.*;

public class CategoryManager {
  private static CategoryManager instance;
  private final List<String> categories;
  private static final String FILE_NAME = "categories.txt";

  private CategoryManager() {
    categories = new ArrayList<>();
    loadCategories();
  }

  public static synchronized CategoryManager getInstance() {
    if (instance == null) {
      instance = new CategoryManager();
    }
    return instance;
  }

  public List<String> getAllCategories() {
    return new ArrayList<>(categories);
  }

  public void addCategory(String category) {
    if (category != null && !category.trim().isEmpty()) {
      String cat = category.trim();
      // Check formatted case insensitive
      boolean exists = categories.stream().anyMatch(c -> c.equalsIgnoreCase(cat));
      if (!exists) {
        categories.add(cat);
        saveCategories();
      }
    }
  }

  public void removeCategory(String category) {
    if (category != null) {
      categories.removeIf(c -> c.equalsIgnoreCase(category.trim()));
      saveCategories();
    }
  }

  private void loadCategories() {
    File file = new File(FILE_NAME);
    if (file.exists()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (!line.trim().isEmpty()) {
            categories.add(line.trim());
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // If empty (first run), add defaults
    if (categories.isEmpty()) {
      categories.addAll(Arrays.asList("Divertissement", "Travail", "Sport", "Musique", "Autre"));
      saveCategories();
    }
  }

  private void saveCategories() {
    try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
      for (String cat : categories) {
        writer.println(cat);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
