package com.emsi.subtracker.services;

import com.emsi.subtracker.config.EmailConfig;
import com.emsi.subtracker.models.Abonnement;
import com.emsi.subtracker.models.User;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.concurrent.Task;

import java.time.LocalDate;
import java.util.List;
import java.util.Properties;


public class EmailService {

    private static EmailService instance;

    private EmailService() {
        // Constructor privé pour Singleton
    }

    public static EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }


    public void sendEmail(String to, String subject, String body) {
        // Créer une tâche JavaFX pour l'envoi asynchrone
        Task<Void> emailTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Configuration des propriétés SMTP Mailtrap
                    Properties props = new Properties();
                    props.put("mail.smtp.host", EmailConfig.SMTP_HOST);
                    props.put("mail.smtp.port", EmailConfig.SMTP_PORT);
                    props.put("mail.smtp.auth", String.valueOf(EmailConfig.SMTP_AUTH));
                    props.put("mail.smtp.starttls.enable", String.valueOf(EmailConfig.SMTP_TLS_ENABLE));
                    props.put("mail.smtp.ssl.enable", String.valueOf(EmailConfig.SMTP_SSL_ENABLE));

                    // Création de la session avec authentification
                    Session session = Session.getInstance(props, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                    EmailConfig.EMAIL_USERNAME,
                                    EmailConfig.EMAIL_PASSWORD);
                        }
                    });

                    // Construction du message
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(EmailConfig.EMAIL_FROM, EmailConfig.EMAIL_FROM_NAME));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                    message.setSubject(subject);
                    message.setText(body);

                    // Envoi
                    Transport.send(message);

                    System.out.println("✅ Email envoyé avec succès à: " + to);
                    System.out.println("   📬 Vérifiez votre inbox Mailtrap: https://mailtrap.io/inboxes");

                } catch (Exception e) {
                    System.err.println("❌ Erreur lors de l'envoi de l'email à " + to);
                    System.err.println("   Sujet: " + subject);
                    System.err.println("   Erreur: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
        };

        // Lancer la tâche dans un nouveau thread (non-blocking)
        Thread emailThread = new Thread(emailTask);
        emailThread.setDaemon(true);
        emailThread.start();
    }

    /**Envoie un email de bienvenue à un nouvel utilisateur.*/
    public void sendWelcomeEmail(User user) {
        String subject = "Bienvenue sur SubTracker ! 🎉";

        String body = String.format(
                "Bonjour %s,\n\n" +
                        "Bienvenue sur SubTracker !\n\n" +
                        "Votre compte a été créé avec succès.\n\n" +
                        "Vous pouvez maintenant:\n" +
                        "- Ajouter vos abonnements\n" +
                        "- Suivre vos dépenses mensuelles\n" +
                        "- Recevoir des alertes de renouvellement\n\n" +
                        "Merci d'utiliser SubTracker pour gérer vos abonnements.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe SubTracker",
                user.getUsername());

        sendEmail(user.getEmail(), subject, body);
    }

    /**Vérifie les abonnements et envoie des alertes pour ceux qui vont être
      renouvelés dans 3 jours.*/
    public void checkAndSendAlerts(User user, List<Abonnement> subscriptions) {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(3); // J+3

        for (Abonnement sub : subscriptions) {
            LocalDate nextRenewal = calculateNextRenewal(sub);

            // Vérifier si le renouvellement est exactement dans 3 jours
            if (nextRenewal != null && nextRenewal.isEqual(targetDate)) {
                sendRenewalAlert(user, sub, nextRenewal);
            }
        }
    }

    /**Calcule la prochaine date de renouvellement d'un abonnement.*/
    private LocalDate calculateNextRenewal(Abonnement sub) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = sub.getDateDebut();

        if (startDate.isAfter(today)) {
            return startDate;
        }

        if ("Mensuel".equalsIgnoreCase(sub.getFrequence())) {
            LocalDate nextDate = startDate;
            while (nextDate.isBefore(today) || nextDate.isEqual(today)) {
                nextDate = nextDate.plusMonths(1);
            }
            return nextDate;

        } else if ("Annuel".equalsIgnoreCase(sub.getFrequence())) {
            LocalDate nextDate = startDate;
            while (nextDate.isBefore(today) || nextDate.isEqual(today)) {
                nextDate = nextDate.plusYears(1);
            }
            return nextDate;
        }

        return null;
    }

    /**Envoie une alerte de renouvellement pour un abonnement.*/
    private void sendRenewalAlert(User user, Abonnement sub, LocalDate renewalDate) {
        String subject = "⚠️ Alerte: Renouvellement d'abonnement dans 3 jours";

        String body = String.format(
                "Bonjour %s,\n\n" +
                        "Ceci est un rappel automatique:\n\n" +
                        "Votre abonnement \"%s\" va être renouvelé le %s pour %.2f DH.\n\n" +
                        "Fréquence: %s\n" +
                        "Catégorie: %s\n\n" +
                        "Si vous souhaitez annuler cet abonnement, pensez à le faire avant la date de renouvellement.\n\n"
                        +
                        "Cordialement,\n" +
                        "SubTracker - Votre assistant d'abonnements",
                user.getUsername(),
                sub.getNom(),
                renewalDate,
                sub.getPrix(),
                sub.getFrequence(),
                sub.getCategorie());

        sendEmail(user.getEmail(), subject, body);
    }
}
