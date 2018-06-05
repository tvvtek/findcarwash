package ru.findcarwash.ru.helpers.json;

public class JsonNotificationFirebase {
    public String receiverLogin, senderLogin, messageInfo, senderName;
    public JsonNotificationFirebase(String receiverLogin, String senderLogin, String messageInfo, String senderName) {
        this.receiverLogin = receiverLogin;
        this.senderLogin = senderLogin;
        this.messageInfo = messageInfo;
        this.senderName = senderName;
    }
}