package com.project.task_management_app.services;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${app.domain}")
    private String appDomain;

    // For plain text emails
    public void sendMessage(String to, String subject, String text) {
        log.info("{} {} {}", to, subject, text);
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@taskbuddy.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText("This is the testing purpose email only: " + text);
            javaMailSender.send(message);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Load the email template from resources
    private String loadEmailTemplate(String templateName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:/templates/" + templateName);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load email template: {}", templateName, e);
            return "";
        }
    }

    // Send HTML email with template
    public void sendWorkspaceWelcomeEmail(String to, String userName, String workspaceName, String workspaceLink) {
        try {
            // Load template
            String htmlTemplate = loadEmailTemplate("workspace-welcome.html");

            // Replace placeholders
            htmlTemplate = htmlTemplate
                    .replace("{{userName}}", userName)
                    .replace("{{workspaceName}}", workspaceName)
                    .replace("{{workspaceLink}}", workspaceLink)
                    .replace("{{unsubscribeLink}}", appDomain + "/unsubscribe?email=" + to);

            // Send email
            sendHtmlMessage(to, "Welcome to " + workspaceName + "!", htmlTemplate);
        } catch (Exception e) {
            log.error("Failed to send workspace welcome email", e);
        }
    }

    // For HTML emails
    public void sendHtmlMessage(String to, String subject, String htmlContent) {
        log.info("Sending HTML email to: {}, subject: {}", to, subject);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("noreply@"+appDomain);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates this is HTML

            javaMailSender.send(message);
            log.info("HTML email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send HTML email", e);
        }
    }
}