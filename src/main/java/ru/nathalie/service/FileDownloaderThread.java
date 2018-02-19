package ru.nathalie.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.nathalie.config.AppProperties;
import ru.nathalie.model.extensions.*;

import java.util.List;
import java.util.Map;

@Component
public class FileDownloaderThread {
    private final Map<String, List<String>> extensions;
    private final ArchiveDL archiveDL;
    private final MediaDL mediaDL;
    private final ImageDL imageDL;
    private final DocumentDL documentDL;
    private final OtherTypeDL otherTypeDL;

    private final Logger log = LoggerFactory.getLogger(FileDownloaderThread.class.getName());

    public FileDownloaderThread(AppProperties appProperties, ArchiveDL archiveDL,
                                MediaDL mediaDL, ImageDL imageDL, DocumentDL documentDL,
                                OtherTypeDL otherTypeDL) {
        this.extensions = appProperties.getExtensions();
        this.archiveDL = archiveDL;
        this.mediaDL = mediaDL;
        this.imageDL = imageDL;
        this.documentDL = documentDL;
        this.otherTypeDL = otherTypeDL;
    }

    public String parseExtensions(String source, String hash) {

        log.info("Parsing extensions...");

        if (isExtension(source, "archive")) {
            return archiveDL.downloadInThread(source, hash);
        } else if (isExtension(source, "media")) {
            return mediaDL.downloadInThread(source, hash);
        } else if (isExtension(source, "image")) {
            return imageDL.downloadInThread(source, hash);
        } else if (isExtension(source, "document")) {
            return documentDL.downloadInThread(source, hash);
        } else {
            return otherTypeDL.downloadInThread(source, hash);
        }
    }

    private boolean isExtension(String url, String key) {
        String extension = url.substring(url.lastIndexOf("."), url.length());
        return extensions.get(key).stream().parallel().anyMatch(s -> extension.toLowerCase().contains(s));
    }
}
