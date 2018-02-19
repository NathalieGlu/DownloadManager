package ru.nathalie.api;

import org.postgresql.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.nathalie.config.AppProperties;

import java.sql.*;
import java.util.Date;

@Component
public class Database {
    private static final Logger log = LoggerFactory.getLogger(Database.class.getName());
    private Connection connection;
    private final AppProperties appProperties;

    private static final String CHECK_USER = "SELECT login FROM user_base_dm WHERE login = ?";
    private static final String CHECK_PASSWORD = "SELECT password FROM user_base_dm WHERE password = ?";
    private static final String GET_SOURCE = "SELECT downloaded FROM user_base_dm WHERE login = ?";
    private static final String UPDATE_SOURCE = "UPDATE user_base_dm SET downloaded = ? WHERE login = ?";
    private static final String SET_LAST_SOURCE = "UPDATE user_base_dm SET last_download = ? WHERE login = ?";
    private static final String SET_DOWNLOAD_DATE = "UPDATE user_base_dm SET last_download_date = ? WHERE login = ?";

    public Database(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.connection = getConnection();
    }

    public boolean userExists(String encrypted) {
        try {
            String user = new String(Base64.decode(encrypted));
            user = user.substring(0, user.indexOf(":"));

            PreparedStatement statement = connection.prepareStatement(CHECK_USER);
            statement.setString(1, user);
            ResultSet rs = statement.executeQuery();
            return rs.next();

        } catch (Exception e) {
            return false;
        }
    }

    public boolean checkPassword(String encrypted) {
        try {
            PreparedStatement statement = connection.prepareStatement(CHECK_PASSWORD);
            statement.setString(1, encrypted);
            ResultSet rs = statement.executeQuery();
            return rs.next();

        } catch (Exception e) {
            return false;
        }
    }

    public void update(String encrypted, String source) {
        String user = new String(Base64.decode(encrypted));
        user = user.substring(0, user.indexOf(":"));

        updateSource(user, source);
        setLastSource(user, source);
        setDownloadDate(user);
    }

    private void updateSource(String user, String source) {
        try {
            PreparedStatement statement = connection.prepareStatement(UPDATE_SOURCE);
            statement.setString(2, user);

            StringBuilder sources = getSource(user);
            if (sources != null) {
                sources.append(source);
            } else {
                sources = new StringBuilder().append(source);
            }

            statement.setString(1, sources.toString());
            statement.executeUpdate();
        } catch (Exception e) {
            log.info("Source update failed: ", e);
        }
    }

    private StringBuilder getSource(String login) {
        try {
            PreparedStatement statement = connection.prepareStatement(GET_SOURCE);
            statement.setString(1, login);

            ResultSet rs = statement.executeQuery();
            StringBuilder sources = new StringBuilder();

            while (rs.next()) {
                sources.append(rs.getString("downloaded")).append("\n");
            }
            return sources;
        } catch (Exception e) {
            log.info("Exception: ", e);
            return null;
        }
    }

    private void setLastSource(String user, String source) {
        try {
            PreparedStatement statement = connection.prepareStatement(SET_LAST_SOURCE);
            statement.setString(2, user);
            statement.setString(1, source);
            statement.executeUpdate();
        } catch (Exception e) {
            log.info("Setting of last source failed: ", e);
        }
    }

    private void setDownloadDate(String user) {
        try {
            PreparedStatement statement = connection.prepareStatement(SET_DOWNLOAD_DATE);
            statement.setString(2, user);
            statement.setTimestamp(1, new Timestamp(new Date().getTime()));
            statement.executeUpdate();
        } catch (Exception e) {
            log.info("Setting of download date failed: ", e);
        }
    }

    private Connection getConnection() {
        Connection connect = null;
        try {
            Class.forName(appProperties.getDriverName());
            connect = DriverManager.getConnection(appProperties.getUrl(),
                    appProperties.getDbUsername(), appProperties.getDbPassword());
            log.info("Connected to driver");
        } catch (Exception e) {
            log.info("DriverManagerDataSource error ", e);
        }
        return connect;
    }
}
