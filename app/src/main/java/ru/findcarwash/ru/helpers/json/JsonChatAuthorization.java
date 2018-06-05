package ru.findcarwash.ru.helpers.json;

public class JsonChatAuthorization {
    private String error, level, login, key;
    public JsonChatAuthorization(String error, String level, String login, String key) {
        this.error = error;
        this.level = level;
        this.login = login;
        this.key = key;
    }
}