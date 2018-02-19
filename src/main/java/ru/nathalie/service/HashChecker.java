package ru.nathalie.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.nathalie.config.AppProperties;
import ru.nathalie.model.extensions.ArchiveDL;

import java.io.FileInputStream;

@Component
public class HashChecker {
    private final AppProperties appProperties;
    private final Logger log = LoggerFactory.getLogger(ArchiveDL.class.getName());

    public HashChecker(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public boolean checkHash(String source, String hash) {
        String file = appProperties.getFilePath().concat(source.substring(source.lastIndexOf("/")));
        return hash.equals(getHash(file));
    }

    private String getHash(String file) {

        try (FileInputStream fin = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fin);
        } catch (Exception e) {
            log.info("Failed: ", e);
            return null;
        }
    }
}
