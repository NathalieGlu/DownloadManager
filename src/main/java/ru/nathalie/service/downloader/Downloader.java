package ru.nathalie.service;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import ru.nathalie.config.AppProperties;
import ru.nathalie.service.Hash.HashChecker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Downloader {
    private final AppProperties appProperties;
    private final ExecutorService executor;
    private final HashChecker hashChecker;
    private final Logger log = LoggerFactory.getLogger(Downloader.class.getName());
    private String type;

    public Downloader(AppProperties appProperties, HashChecker hashChecker, String type) {
        this.appProperties = appProperties;
        this.executor = Executors.newCachedThreadPool();
        this.hashChecker = hashChecker;
        this.type = type;
    }

    public String downloadInThread(String source, String hash) throws FileAlreadyExistsException {

        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder status = new StringBuilder();

        String fileName = source.substring(source.lastIndexOf("/") + 1);
        File file = new File(appProperties.getFilePath() + fileName);

        if (file.exists()) {
            log.info("File already exists: " + fileName);
            throw new FileAlreadyExistsException("File already exists");
        }

        executor.submit(() -> {
            try {
                download(new URL(source), file);
                latch.countDown();
                status.append("File has been successfully downloaded");
            } catch (Exception e) {
                log.info("Error during downloading: ", e);
                status.append("Error during downloading: ").append(e.getMessage());
            }
        });

        try {
            latch.await();
            log.info("Checking hash...");

            if (hashChecker.checkHash(source, hash)) {
                log.info("Hash OK");
                return status.append("\nHash OK").toString();
            } else {
                log.info("Hash BAD");
                return status.append("\nHash BAD").toString();
            }

        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException();
        } catch (Exception e) {
            log.info("Error during threading: ", e);
            return "Error during threading";
        }
    }

    public void download(URL url, File dstFile) throws ResourceNotFoundException {

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build()) {
            log.info("Using {} type downloader", type);
            log.info("Downloading file, link: {}", url.getPath());
            httpclient.execute(new HttpGet(url.toURI()), new FileDownloadResponseHandler(dstFile));
            log.info("File successfully downloaded: {}", dstFile.getName());
        } catch (Exception e) {
            log.info("Downloading failed due to ", e);
            throw new IllegalStateException(e);
        }
    }

    private static class FileDownloadResponseHandler implements ResponseHandler<File> {
        private final File target;

        private FileDownloadResponseHandler(File target) {
            this.target = target;
        }

        @Override
        public File handleResponse(HttpResponse response) throws IOException, ResourceNotFoundException {
            InputStream source = response.getEntity().getContent();
            FileUtils.copyInputStreamToFile(source, this.target);
            return this.target;
        }
    }
}
