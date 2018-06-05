package ru.findcarwash.ru.helpers.json;

public class JsonChatMessage {
    public String isMessage, clientToAdmin, from, to, message;
    public JsonChatMessage(String isMessage, String clientToAdmin, String from, String to, String message) {
        this.isMessage = isMessage;
        this.clientToAdmin = clientToAdmin;
        this.from = from;
        this.to = to;
        this.message = message;
    }
}