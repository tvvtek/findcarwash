package ru.findcarwash.findcarwash.workscreens.ChatWash;

public class WashChatItem {
    public int sender; // if 1 to sender OR 0 to receiver
    public String message, dataSend;
    public WashChatItem(int sender, String message, String dataSend){
        this.sender = sender;
        this.message = message;
        this.dataSend = dataSend;
    }
}
