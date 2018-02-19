package ru.nathalie.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties
public class AppProperties {
    private String filePath;
    @Value("${driver_class_name}")
    private String driverName;
    @Value("${url}")
    private String url;
    @Value("${db_username}")
    private String dbUsername;
    @Value("${db_password}")
    private String dbPassword;

    @Value("#{${extensions}}")
    private Map<String, String> extensions;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Map<String, List<String>> getExtensions() {
        Map<String, List<String>> extensionsList = new HashMap<>();
        for (String key : extensions.keySet()) {
            extensionsList.put(key, Arrays.asList(extensions.get(key).split(",")));
        }
        return extensionsList;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getUrl() {
        return url;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }
}
