package ru.nathalie.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailService {

    private final JavaMailSender emailSender;
    private final Logger log = LoggerFactory.getLogger(Downloader.class.getName());

    @Autowired
    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendMessage(String to, String source) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("File has been downloaded");
        message.setText("You downloaded file from " + source);
        emailSender.send(message);
        log.info("Sent e-mail to: {}", to);
    }
}