package ru.nathalie.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nathalie.api.Database;
import ru.nathalie.service.Mail.EmailService;
import ru.nathalie.service.downloader.FileDownloaderThread;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;

@RestController
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

    @ExceptionHandler(FileAlreadyExistsException.class)
    public HttpStatus handleConflict() {
        log.info(HttpStatus.CONFLICT.toString() + " " + HttpStatus.CONFLICT.getReasonPhrase());
        return HttpStatus.CONFLICT;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public HttpStatus handleUnauthorized() {
        log.info(HttpStatus.UNAUTHORIZED.toString() + " " + HttpStatus.UNAUTHORIZED.getReasonPhrase());
        return HttpStatus.UNAUTHORIZED;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public HttpStatus handleNotFound() {
        log.info(HttpStatus.NOT_FOUND.toString() + " " + HttpStatus.NOT_FOUND.getReasonPhrase());
        return HttpStatus.NOT_FOUND;
    }

    @ExceptionHandler(MalformedURLException.class)
    public HttpStatus handleMalformed() {
        log.info(HttpStatus.BAD_REQUEST.toString() + " " + HttpStatus.BAD_REQUEST.getReasonPhrase());
        return HttpStatus.BAD_REQUEST;
    }

    @PostMapping("/download")
    public String downloadFile(@RequestParam(value = "source") String source,
                               @RequestParam(value = "encrypted") String encrypted,
                               @RequestParam(value = "email") String email,
                               @RequestParam(value = "md5") String hash) throws FileAlreadyExistsException,
            ResourceNotFoundException, MalformedURLException {

        try {
            fileDownloaderThread.checkUrl(source);
        } catch (IOException e) {
            throw new MalformedURLException();
        }

        if (database.checkPassword(encrypted)) {
            String status = fileDownloaderThread.parseExtensions(source, hash);
            database.update(encrypted, source);
            emailService.sendMessage(email, source);
            return status;
        } else {
            log.info("Bad credentials");
            throw new BadCredentialsException("Bad credentials");
        }
    }
}
