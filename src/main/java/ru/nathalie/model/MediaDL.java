package ru.nathalie.model.extensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.nathalie.service.Downloader;
import ru.nathalie.config.AppProperties;
import ru.nathalie.service.HashChecker;

import java.io.File;
import java.net.URL;

@Component
public class MediaDL extends Downloader {
    private final Logger log = LoggerFactory.getLogger(MediaDL.class.getName());

    public MediaDL(AppProperties appProperties, HashChecker hashChecker) {
        super(appProperties, hashChecker);
    }


    @Override
    public void download(URL url, File dstFile) {
        log.info("Using media downloader");
        super.download(url, dstFile);
    }
}
