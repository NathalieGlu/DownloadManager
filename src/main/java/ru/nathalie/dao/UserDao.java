package ru.nathalie.dao;

public interface UserDao {

    void update(String login, String source);

    void updateSource(String user, String source);

    StringBuilder getSource(String login);

    void setLastSource(String user, String source);

    void setDownloadDate(String user);
}