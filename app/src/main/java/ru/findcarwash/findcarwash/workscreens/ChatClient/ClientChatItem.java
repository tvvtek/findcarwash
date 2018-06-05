package ru.findcarwash.findcarwash.workscreens.ChatClient;

public class ClientChatItem{
    public int sender; // if 1 to sender OR 0 to receiver
    public String message, dataSend;
    public ClientChatItem(int sender, String message, String dataSend){
        this.sender = sender;
        this.message = message;
        this.dataSend = dataSend;
    }
}
