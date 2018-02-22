package ru.nathalie.service.downloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.nathalie.config.AppProperties;
import ru.nathalie.model.Downloader;
import ru.nathalie.service.Hash.HashChecker;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FileDownloaderThread {
    private final Map<String, List<String>> extensions;
    private Map<String, Downloader> downloaderMap;
    private List<String> types;

    private final Logger log = LoggerFactory.getLogger(FileDownloaderThread.class.getName());

    public FileDownloaderThread(AppProperties appProperties, HashChecker hashChecker) {
        this.extensions = appProperties.getExtensions();
        this.types = appProperties.getTypes();
        this.downloaderMap = new HashMap<>();
        for (String type : types) {
            this.downloaderMap.put(type, new Downloader(appProperties, hashChecker, type));
        }
    }

    public String parseExtensions(String source, String hash) throws FileAlreadyExistsException {
        log.info("Parsing extensions...");

        for (String type : types.subList(0, types.size() - 1)) {
            if (isExtension(source, type)) {
                return downloaderMap.get(type).downloadInThread(source, hash);
            }
        }
        return downloaderMap.get(types.get(types.size())).downloadInThread(source, hash);
    }

    public void checkUrl(String url) throws ResourceNotFoundException, IOException {
        if (((HttpURLConnection) new URL(url).openConnection()).getResponseCode() == HttpStatus.NOT_FOUND.value()) {
            throw new ResourceNotFoundException();
        }
    }

    private boolean isExtension(String url, String key) {
        String extension = url.substring(url.lastIndexOf("."), url.length());
        return extensions.get(key).stream().parallel().anyMatch(s -> extension.toLowerCase().contains(s));
    }
}
