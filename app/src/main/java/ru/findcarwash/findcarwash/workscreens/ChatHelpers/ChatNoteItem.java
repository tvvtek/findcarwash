package ru.findcarwash.findcarwash.workscreens.ChatHelpers;


import io.realm.RealmObject;

public class ChatNoteItem extends RealmObject {

    long id;
    private boolean isRead; // прочитано сообщение или нет
    private int sender;
    private String message; // само сообщение
   // private String messDate; // дата сообщения
    private String messDate;
    private String receiver; // мой логин
    private String senderName; // имя отправителя
    private String senderLogin; // логин отправителя

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) { this.message = message;}

    public String getSenderLogin() {
        return senderLogin;
    }

    public void setSenderLogin(String senderLogin) {
        this.senderLogin = senderLogin;
    }

    public boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(boolean Read) {
        isRead = Read;
    }


    public String getMessDate() {
        return messDate;
    }

    public void setMessDate(String messDate) {
        this.messDate = messDate;
    }
}