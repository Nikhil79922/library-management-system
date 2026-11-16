package com.nikkssy.library_management.services.helper.email;

import com.nikkssy.library_management.services.impl.email.EmailServiceImpl;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailServiceImpl {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.mail.from-name:Library Management}")
    private String fromName;

    @Override
    @Async("emailExecutor")
    public void sendPasswordResetEmail(String recipient, String resetLink) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("resetLink", resetLink);
        vars.put("expiryMinutes", 15);
        send(recipient, "Reset Your Library Management Password", "password-reset", vars);
    }

    @Override
    @Async("emailExecutor")
    public void sendWelcomeEmail(String recipient, String name, String loginLink) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", name);
        vars.put("loginLink", loginLink);
        send(recipient, "Welcome to Library Management", "welcome", vars);
    }

    @Override
    @Async("emailExecutor")
    public void sendDueReminderEmail(String recipient, String name, String bookTitle,
                                     String author, String dueDate, String bookLink) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", name);
        vars.put("bookTitle", bookTitle);
        vars.put("author", author);
        vars.put("dueDate", dueDate);
        vars.put("bookLink", bookLink);
        send(recipient, "Reminder: \"" + bookTitle + "\" is due on " + dueDate, "due-reminder", vars);
    }

    @Override
    @Async("emailExecutor")
    public void sendNotificationEmail(String recipient, String subject, String name,
                                      String message, String actionLabel, String actionLink) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("subject", subject);
        vars.put("name", name);
        vars.put("message", message);
        vars.put("actionLabel", actionLabel);
        vars.put("actionLink", actionLink);
        send(recipient, subject, "notification", vars);
    }

    private void send(String recipient, String subject, String template, Map<String, Object> vars) {
        try {
            String html = templateService.render(template, vars);
            String plain = html
                    .replaceAll("(?s)<style.*?</style>", "")
                    .replaceAll("<[^>]+>", " ")
                    .replaceAll("\\s+", " ")
                    .trim();

            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(senderEmail, fromName);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(plain, html);
            mailSender.send(mime);
        } catch (Exception e) {
            // No caller is waiting anymore, so log here; exceptions can't propagate
            log.error("Failed to send '{}' email to {}", subject, recipient, e);
        }
    }
}