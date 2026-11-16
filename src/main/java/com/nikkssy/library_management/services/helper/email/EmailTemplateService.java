package com.nikkssy.library_management.services.helper.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Year;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    @Value("${app.mail.app-name:Library Management System}")
    private String appName;

    @Value("${app.mail.app-url:http://localhost:8080}")
    private String appUrl;

    @Value("${app.mail.support-email:support@example.com}")
    private String supportEmail;

    @Value("${app.mail.address:}")
    private String address;

    public String render(String template, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        context.setVariable("appName", appName);
        context.setVariable("appUrl", appUrl);
        context.setVariable("supportEmail", supportEmail);
        context.setVariable("address", address);
        context.setVariable("year", Year.now().getValue());
        return templateEngine.process("email/" + template, context);
    }
}