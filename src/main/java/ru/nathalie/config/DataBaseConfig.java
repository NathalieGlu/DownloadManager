package ru.nathalie.dao;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DataBaseConfig {
    private static DataBaseConfig dataBaseConfig;
    private ComboPooledDataSource cpds;

    private DataBaseConfig() throws IOException, SQLException, PropertyVetoException {
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass("com.mysql.jdbc.Driver"); //loads the jdbc driver
        cpds.setJdbcUrl("jdbc:postgresql://localhost:5432/");
        cpds.setUser("nathalie");
        cpds.setPassword("123");

        // the settings below are optional -- c3p0 can work with defaults
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
        cpds.setMaxStatements(180);

    }

    public static DataBaseConfig getInstance() throws IOException, SQLException, PropertyVetoException {
        if (dataBaseConfig == null) {
            dataBaseConfig = new DataBaseConfig();
            return dataBaseConfig;
        } else {
            return dataBaseConfig;
        }
    }

    public Connection getConnection() throws SQLException {
        return this.cpds.getConnection();
    }
}
