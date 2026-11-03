package com.emsi.subtracker.utils;

import com.emsi.subtracker.models.User;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.io.File;

public class UIUtils {

    public static void loadUserAvatar(ImageView imageView, double radius) {
        if (imageView == null) {
            System.out.println("DEBUG: loadUserAvatar - imageView is null");
            return;
        }

        User user = UserSession.getInstance().getUser();
        if (user != null) {
            System.out.println("DEBUG: User found: " + user.getUsername());
            System.out.println("DEBUG: Profile Pic Path: " + user.getProfilePicture());

            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                try {
                    File file = new File(user.getProfilePicture());
                    System.out
                            .println("DEBUG: Checking file: " + file.getAbsolutePath() + " | Exists: " + file.exists());

                    if (file.exists()) {
                        Image img = new Image(file.toURI().toString());
                        imageView.setImage(img);

                        // Apply circular clip
                        Circle clip = new Circle(radius, radius, radius);
                        imageView.setClip(clip);
                        System.out.println("DEBUG: Avatar loaded and clipped.");
                    } else {
                        System.out.println("DEBUG: File does not exist.");
                    }
                } catch (Exception e) {
                    System.err.println("Error loading avatar: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("DEBUG: No profile picture set.");
            }
        } else {
            System.out.println("DEBUG: User is null in session.");
        }
    }
}
