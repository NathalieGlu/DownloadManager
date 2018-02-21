package ru.nathalie.dao;

import java.sql.Connection;

public interface DaoFactory {

    UserDao getDatabaseDao(Connection connection);

    Connection getConnection();
}
