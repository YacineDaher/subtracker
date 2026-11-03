package com.emsi.subtracker.config;

public class EmailConfig {

    public static final String SMTP_HOST = "smtp.gmail.com";
    public static final String SMTP_PORT = "587";
    public static final boolean SMTP_AUTH = true;
    public static final boolean SMTP_TLS_ENABLE = true;
    public static final boolean SMTP_SSL_ENABLE = false;

    public static final String EMAIL_USERNAME;
    public static final String EMAIL_PASSWORD;

    public static final String EMAIL_FROM;
    public static final String EMAIL_FROM_NAME = "SubTracker App";

    static {
        String username = null;
        String password = null;
        try (java.io.InputStream input = EmailConfig.class.getClassLoader().getResourceAsStream("mail.properties")) {
            java.util.Properties prop = new java.util.Properties();
            if (input != null) {
                prop.load(input);
                username = prop.getProperty("mail.username");
                password = prop.getProperty("mail.password");
            } else {
                System.err.println("Sorry, unable to find mail.properties");
            }
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
        EMAIL_USERNAME = username;
        EMAIL_PASSWORD = password;
        EMAIL_FROM = username;
    }

    public static boolean isConfigured() {
        return EMAIL_USERNAME != null && !EMAIL_USERNAME.isEmpty() && EMAIL_PASSWORD != null
                && !EMAIL_PASSWORD.isEmpty();
    }
}
