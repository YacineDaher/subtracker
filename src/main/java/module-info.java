module com.emsi.subtracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires com.microsoft.sqlserver.jdbc;
    requires jbcrypt;
    // Jakarta Mail for SMTP email notifications
    requires jakarta.mail;
    requires jakarta.activation;

    opens com.emsi.subtracker to javafx.fxml;
    opens com.emsi.subtracker.controllers to javafx.fxml;
    opens com.emsi.subtracker.models to javafx.base;

    exports com.emsi.subtracker;
    exports com.emsi.subtracker.models;
    exports com.emsi.subtracker.utils;
    exports com.emsi.subtracker.services;
    exports com.emsi.subtracker.controllers;
}
