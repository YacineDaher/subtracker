package com.emsi.subtracker.services;

import com.emsi.subtracker.models.SubscriptionTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CsvService {

    public List<SubscriptionTemplate> loadTemplates() {
        List<SubscriptionTemplate> templates = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/data/subscriptions.csv");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            if (is == null) {
                System.err.println("CSV Not Found!");
                return templates;
            }

            // Skip header
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String name = parts[0].trim();
                    String category = parts[1].trim();
                    double price = 0.0;
                    try {
                        price = Double.parseDouble(parts[2].trim());
                    } catch (NumberFormatException e) {
                        /* default 0 */ }
                    String color = parts[3].trim();

                    templates.add(new SubscriptionTemplate(name, category, price, color));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return templates;
    }

    public List<String> getUniqueCategories(List<SubscriptionTemplate> templates) {
        return templates.stream()
                .map(SubscriptionTemplate::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
