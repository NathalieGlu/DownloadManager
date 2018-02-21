package ru.nathalie.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.nathalie.config.AppProperties;

import java.sql.Connection;
import java.sql.DriverManager;

@Component
public class SqlDaoFactory implements DaoFactory {
    private static final Logger log = LoggerFactory.getLogger(SqlDaoFactory.class.getName());
    private final AppProperties appProperties;

    public SqlDaoFactory(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public UserDao getDatabaseDao(Connection connection) {
        return new SqlUserDao(connection);
    }

    @Override
    public Connection getConnection() {
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
