package ru.nathalie.service.Mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import ru.nathalie.config.AppProperties;

@Component
public class EmailService {

    private AppProperties appProperties;
    private final JavaMailSender emailSender;
    private final Logger log = LoggerFactory.getLogger(EmailService.class.getName());

    @Autowired
    public EmailService(AppProperties appProperties, JavaMailSender emailSender) {
        this.appProperties = appProperties;
        this.emailSender = emailSender;
    }

    public void sendMessage(String to, String source) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(appProperties.getSenderEmail());
        message.setTo(to);
        message.setSubject("File has been downloaded");
        message.setText("You downloaded file from " + source);
        emailSender.send(message);
        log.info("Sent e-mail to: {}", to);
    }
}