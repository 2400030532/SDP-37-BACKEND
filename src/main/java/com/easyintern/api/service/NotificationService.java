package com.easyintern.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public NotificationService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public void sendRoundUpdateMail(String to, String internshipTitle, String roundName, String status) {
        String subject = "EasyIntern Update: " + roundName + " round is " + status;
        String text = "Your application for " + internshipTitle + " has an update.\n\n"
                + roundName + " round status: " + status + "\n"
                + "Please log in to EasyIntern for full details.";
        sendMailSafely(to, subject, text);
    }

    public void sendTaskAssignedMail(String to, String internshipTitle, String taskTitle) {
        String subject = "EasyIntern Task Assigned";
        String text = "A new task has been assigned for your " + internshipTitle + " application.\n\n"
                + "Task: " + taskTitle + "\n"
                + "Please complete it before the due date.";
        sendMailSafely(to, subject, text);
    }

    private void sendMailSafely(String to, String subject, String text) {
        try {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender == null) {
                log.debug("Mail sender not configured; skipping email to {}", to);
                return;
            }

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
        } catch (Exception ex) {
            log.warn("Mail send failed for {}: {}", to, ex.getMessage());
        }
    }
}
