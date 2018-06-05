package ru.findcarwash.findcarwash.workscreens.ChatHelpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ru.findcarwash.findcarwash.R;


public class ChatListItemAdapter extends BaseAdapter {

    Context ctx;
    ArrayList<ChatListItem> chatListItem;
    LayoutInflater lInflater;
    ImageView clientFragmentTwoChatItemImg, itemSelect, newMessage;
    LinearLayout chatListItemLayout;

    public ChatListItemAdapter(Context context, ArrayList<ChatListItem> chatListItem) {
        ctx = context;
        this.chatListItem = chatListItem;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return chatListItem.size();
    }

    @Override
    public Object getItem(int position) {
        return chatListItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = lInflater.inflate(R.layout.client_fragment_two_chat_list_item, viewGroup, false);
        }

      //  Log.d(MySettings.LOG_TAG, "chatListItemPosition=" + position);
        ChatListItem chatListItems = getProduct(position);

        // check box list
        itemSelect = view.findViewById(R.id.clientFragmentTwoChatIemSelect);
        chatListItemLayout = view.findViewById(R.id.chatListItemLayout);
        newMessage = view.findViewById(R.id.clientFragmentTwoChatItemNewMess);
        // show  OR not show new messImage
        if (!chatListItems.isRead) newMessage.setVisibility(View.VISIBLE);
        if (chatListItems.isRead) newMessage.setVisibility(View.INVISIBLE);

        if (chatListItems.imageSelectItem == 1) {
            chatListItemLayout.setBackgroundColor(ctx.getResources().getColor(R.color.fragmentTwoBackgroundSelectItem));
            itemSelect.setVisibility(View.VISIBLE);
        }
        else if (chatListItems.imageSelectItem == 0) {
            chatListItemLayout.setBackgroundColor(ctx.getResources().getColor(R.color.fragmentTwoBackground));
            itemSelect.setVisibility(View.INVISIBLE);
        }

        clientFragmentTwoChatItemImg = view.findViewById(R.id.clientFragmentTwoChatItemImg);
        if (chatListItems.image == 1) clientFragmentTwoChatItemImg.setImageResource(R.drawable.chat_list_item_1);
        if (chatListItems.image == 2) clientFragmentTwoChatItemImg.setImageResource(R.drawable.chat_list_item_2);
        if (chatListItems.image == 3) clientFragmentTwoChatItemImg.setImageResource(R.drawable.chat_list_item_3);

        ((TextView) view.findViewById(R.id.clientFragmentTwoChatItemText)).setText(chatListItems.senderName);
        ((TextView) view.findViewById(R.id.clientFragmentTwoChatItemSubText)).setText(chatListItems.itemSubText);
        ((TextView) view.findViewById(R.id.clientFragmentTwoChatItemData)).setText(chatListItems.itemDate);

        return view;
    }

    ChatListItem getProduct(int position) {
        return ((ChatListItem) getItem(position));
    }
}
