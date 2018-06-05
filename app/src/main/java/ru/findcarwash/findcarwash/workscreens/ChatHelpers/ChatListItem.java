package ru.findcarwash.findcarwash.workscreens.ChatHelpers;

import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatListItem implements Comparable<ChatListItem>{
    public boolean isRead = false;
    public String myLogin, senderLogin; // helpers for item click
    public int image, imageSelectItem;
    public String senderName, itemSubText, itemDate, itemInfo;
    public ChatListItem(int image, String senderName, String itemSubText, String itemDate, String itemInfo, int imageSelectItem, String myLogin, String senderLogin, boolean isRead) {
        this.image = image; // 1 - 3 value
        this.senderName = senderName;
        this.itemSubText = itemSubText;
        this.itemDate = itemDate;
        this.itemInfo = itemInfo; // other
        this.imageSelectItem = imageSelectItem; // 0 is not enable OR 1 is enable

        this.myLogin = myLogin;
        this.senderLogin = senderLogin;

        this.isRead = isRead;
    }

    public Date getDate(){
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(itemDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    @Override
    public int compareTo(@NonNull ChatListItem chatListItem) {
        return chatListItem.getDate().compareTo(getDate());
    }
}