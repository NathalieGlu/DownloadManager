package ru.nathalie.service;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.nathalie.config.AppProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Downloader {
    private final AppProperties appProperties;
    private final ExecutorService executor;
    private final HashChecker hashChecker;
    private final Logger log = LoggerFactory.getLogger(Downloader.class.getName());

    public Downloader(AppProperties appProperties, HashChecker hashChecker) {
        this.appProperties = appProperties;
        this.executor = Executors.newCachedThreadPool();
        this.hashChecker = hashChecker;
    }

    public String downloadInThread(String source, String hash) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            executor.submit(() -> {
                try {
                    String fileName = source.substring(source.lastIndexOf("/") + 1);
                    download(new URL(source), new File(appProperties.getFilePath() + fileName));
                    latch.countDown();
                } catch (Exception e) {
                    log.info("Error during thread execution: {}", e);
                }
            });
            latch.await();
            log.info("Checking hash...");

            if (hashChecker.checkHash(source, hash)) {
                log.info("Hash OK");
                return "Hash OK";
            } else {
                log.info("Hash BAD");
                return "Hash BAD";
            }
        } catch (Exception e) {
            log.info("Error during threading: ", e);
            return "Error during threading";
        }
    }

    public void download(URL url, File dstFile) {

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build()) {

            log.info("Downloading file, link: {}", url.getPath());
            HttpGet get = new HttpGet(url.toURI());
            httpclient.execute(get, new FileDownloadResponseHandler(dstFile));
            log.info("File successfully downloaded: {}", dstFile.getName());
        } catch (Exception e) {
            log.info("Downloading failed due to {}", e);
            throw new IllegalStateException(e);
        }
    }

    static class FileDownloadResponseHandler implements ResponseHandler<File> {
        private final File target;

        private FileDownloadResponseHandler(File target) {
            this.target = target;
        }

        @Override
        public File handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            InputStream source = response.getEntity().getContent();
            FileUtils.copyInputStreamToFile(source, this.target);
            return this.target;
        }
    }
}
