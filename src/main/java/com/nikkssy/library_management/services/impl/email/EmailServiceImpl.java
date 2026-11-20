package com.nikkssy.library_management.services.impl.email;

public interface EmailServiceImpl {
    void sendPasswordResetEmail(String recipient, String resetLink);

    void sendWelcomeEmail(String recipient, String name, String loginLink);

    void sendDueReminderEmail(String recipient, String name, String bookTitle,
                              String author, String dueDate, String bookLink);

    void sendNotificationEmail(String recipient, String subject, String name,
                               String message, String actionLabel, String actionLink);

    void sendEmailVerificationOtp(String recipient, String name, String otp);

    void sendLoginOtp(String recipient, String name, String otp);

}
