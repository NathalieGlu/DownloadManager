package ru.nathalie.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

public class SqlUserDao implements UserDao {
    private static final Logger log = LoggerFactory.getLogger(SqlDaoFactory.class.getName());

    private static final String CHECK_USER = "SELECT login FROM user_base_dm WHERE login = ?";
    private static final String CHECK_PASSWORD = "SELECT password FROM user_base_dm WHERE password = ?";
    private static final String GET_SOURCE = "SELECT downloaded FROM user_base_dm WHERE login = ?";
    private static final String UPDATE_SOURCE = "UPDATE user_base_dm SET downloaded = ? WHERE login = ?";
    private static final String SET_LAST_SOURCE = "UPDATE user_base_dm SET last_download = ? WHERE login = ?";
    private static final String SET_DOWNLOAD_DATE = "UPDATE user_base_dm SET last_download_date = ? WHERE login = ?";

    private final Connection connection;

    public SqlUserDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void update(String login, String source) {
        updateSource(login, source);
        setLastSource(login, source);
        setDownloadDate(login);
    }

    @Override
    public void updateSource(String login, String source) {
        try {
            PreparedStatement statement = connection.prepareStatement(UPDATE_SOURCE);
            statement.setString(2, login);

            StringBuilder sources = getSource(login);
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

    @Override
    public StringBuilder getSource(String login) {
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

    @Override
    public void setLastSource(String login, String source) {
        try {
            PreparedStatement statement = connection.prepareStatement(SET_LAST_SOURCE);
            statement.setString(2, login);
            statement.setString(1, source);
            statement.executeUpdate();
        } catch (Exception e) {
            log.info("Setting of last source failed: ", e);
        }
    }

    @Override
    public void setDownloadDate(String login) {
        try {
            PreparedStatement statement = connection.prepareStatement(SET_DOWNLOAD_DATE);
            statement.setString(2, login);
            statement.setTimestamp(1, new Timestamp(new Date().getTime()));
            statement.executeUpdate();
        } catch (Exception e) {
            log.info("Setting of download date failed: ", e);
        }
    }
}
