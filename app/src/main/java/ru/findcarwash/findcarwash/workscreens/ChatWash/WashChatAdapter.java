package ru.findcarwash.findcarwash.workscreens.ChatWash;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ru.findcarwash.findcarwash.R;
import ru.findcarwash.findcarwash.workscreens.ChatClient.ClientChatItem;


public class WashChatAdapter extends BaseAdapter {

    private int sender;

    Context ctx;
    ArrayList<ClientChatItem> clientChatItems;
    LayoutInflater lInflater;


    public WashChatAdapter(Context context, ArrayList<ClientChatItem> clientChatItems) {
        ctx = context;
        this.clientChatItems = clientChatItems;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
      //  Log.d(MySettings.LOG_TAG, "countsChat=" +  clientChatItems.size());
        return clientChatItems.size();
    }

    @Override
    public Object getItem(int i) {
        return clientChatItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ClientChatItem clientChatItems = getId(i);
        sender = clientChatItems.sender; //get Sender OR receiver

        // sender
        if (sender == 1){
            if (view == null) {
                view = lInflater.inflate(R.layout.chat_item_right_layout, viewGroup, false);
            }
        }
        // receiver
        if (sender == 0){
            if (view == null) {
                view = lInflater.inflate(R.layout.chat_item_left_layout, viewGroup, false);
            }
        }
        // error
        if (sender == -1){
            if (view == null) {
                view = lInflater.inflate(R.layout.chat_item_center_layout, viewGroup, false);
            }
        }
      //  Log.d(MySettings.LOG_TAG, "clientChatItems=" + i);
        ((TextView) view.findViewById(R.id.clientChatMessageItem)).setText(clientChatItems.message);
        ((TextView) view.findViewById(R.id.ClientChatDataItem)).setText(clientChatItems.dataSend);

        return view;
    }

    ClientChatItem getId(int position) {
        return ((ClientChatItem) getItem(position));
    }
}
