package ru.nathalie.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.nathalie.api.Database;
import ru.nathalie.service.EmailService;
import ru.nathalie.service.FileDownloaderThread;

@Controller
public class WebController {
    private FileDownloaderThread fileDownloaderThread;
    private Database database;
    private EmailService emailService;

    private final Logger log = LoggerFactory.getLogger(WebController.class.getName());

    public WebController(FileDownloaderThread fileDownloaderThread, Database database, EmailService emailService) {
        this.fileDownloaderThread = fileDownloaderThread;
        this.database = database;
        this.emailService = emailService;
    }

    @PostMapping("/download")
    public String downloadFile(@RequestParam(value = "source") String source,
                               @RequestParam(value = "encrypted") String encrypted,
                               @RequestParam(value = "email") String email,
                               @RequestParam(value = "md5") String hash) {
        try {
            if (database.userExists(encrypted)) {
                if (database.checkPassword(encrypted)) {
                    database.update(encrypted, source);
                    emailService.sendMessage(email, source);
                    return "File successfully downloaded" + fileDownloaderThread.parseExtensions(source, hash);
                } else {
                    log.info("Wrong password!");
                    return "Wrong password!";
                }
            } else {
                log.info("User doesn't exist");
                return "User doesn't exist";
            }
        } catch (Exception e) {
            log.info("Exception while downloading file: ", e);
            return String.format("Error while downloading: %s", e.getMessage());
        }
    }
}
