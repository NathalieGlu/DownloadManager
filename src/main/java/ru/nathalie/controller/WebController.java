package ru.nathalie.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nathalie.dao.SqlDaoFactory;
import ru.nathalie.dao.UserDao;
import ru.nathalie.service.Mail.EmailService;
import ru.nathalie.service.downloader.FileDownloaderThread;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
final public class WebController {
    private FileDownloaderThread fileDownloaderThread;
    private EmailService emailService;
    private SqlDaoFactory sqlDaoFactory;

    private final Logger log = LoggerFactory.getLogger(WebController.class.getName());

    public WebController(FileDownloaderThread fileDownloaderThread, EmailService emailService, SqlDaoFactory sqlDaoFactory) {
        this.fileDownloaderThread = fileDownloaderThread;
        this.emailService = emailService;
        this.sqlDaoFactory = sqlDaoFactory;
    }

    @ExceptionHandler(FileAlreadyExistsException.class)
    private HttpStatus handleConflict() {
        log.info(HttpStatus.CONFLICT.toString() + " " + HttpStatus.CONFLICT.getReasonPhrase());
        return HttpStatus.CONFLICT;
    }

    @ExceptionHandler(BadCredentialsException.class)
    private HttpStatus handleUnauthorized() {
        log.info(HttpStatus.UNAUTHORIZED.toString() + " " + HttpStatus.UNAUTHORIZED.getReasonPhrase());
        return HttpStatus.UNAUTHORIZED;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    private HttpStatus handleNotFound() {
        log.info(HttpStatus.NOT_FOUND.toString() + " " + HttpStatus.NOT_FOUND.getReasonPhrase());
        return HttpStatus.NOT_FOUND;
    }

    @ExceptionHandler(MalformedURLException.class)
    private HttpStatus handleMalformed() {
        log.info(HttpStatus.BAD_REQUEST.toString() + " " + HttpStatus.BAD_REQUEST.getReasonPhrase());
        return HttpStatus.BAD_REQUEST;
    }

    @PostMapping("/download")
    private String downloadFile(Authentication authentication,
                                @RequestParam(value = "source") String source,
                                @RequestParam(value = "email") String email,
                                @RequestParam(value = "md5") String hash) throws FileAlreadyExistsException,
            ResourceNotFoundException, MalformedURLException {

        try {
            fileDownloaderThread.checkUrl(source);
        } catch (IOException e) {
            throw new MalformedURLException();
        }

        try (Connection connection = sqlDaoFactory.getConnection()) {
            UserDao dao = sqlDaoFactory.getDatabaseDao(connection);
            String status = fileDownloaderThread.parseExtensions(source, hash);
            dao.update(authentication.getName(), source);
            emailService.sendMessage(email, source);
            return status;
        } catch (SQLException e) {
            log.info("Database exception: ", e);
            return "Database exception";
        }
    }
}
