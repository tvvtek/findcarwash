package ru.findcarwash.findcarwash.workscreens.ClientWorkScreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import ru.findcarwash.findcarwash.R;
import ru.findcarwash.findcarwash.workscreens.ChatHelpers.ChatNoteItem;
import ru.findcarwash.findcarwash.workscreens.ChatClient.ClientChat;
import ru.findcarwash.findcarwash.workscreens.ChatHelpers.ChatListItem;
import ru.findcarwash.findcarwash.workscreens.ChatHelpers.ChatListItemAdapter;
import ru.findcarwash.findcarwash.workscreens.ClientFragmentTwoHelpers.EventSelectItem;
import ru.findcarwash.ru.helpers.factory.DependenciesFactory;

import io.realm.Realm;
import io.realm.RealmResults;

public class FragmentTwoChatsList extends Fragment{
    // Layout
    RelativeLayout layout;
    // Context
    Context ctx;
    // Data base reader
    Realm realm;
    // View
    TextView fragmentTwoChatListMainLayoutEmptyList;
    ListView chatList;
    ArrayList<ChatListItem> chatListItem = new ArrayList<>();
    ChatListItemAdapter chatListItemAdapter;
    private boolean isLongClickState = false;

    @Override
    public void onStop() {
        super.onStop();
    //    Log.d(MySettings.LOG_TAG, "STOP");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.client_fragment_two_chat_list, container, false);
        layout = view.findViewById(R.id.fragmentTwoChatListMainLayout);
        fragmentTwoChatListMainLayoutEmptyList = view.findViewById(R.id.fragmentTwoChatListMainLayoutEmptyList);

        ctx = getContext();
        chatList = view.findViewById(R.id.chatList);

        Realm.init(getContext());
        realm = DependenciesFactory.getRealm();
        readDb();

        // Click and Go Chat
        chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if (isLongClickState){
                    if (chatListItem.get(position).imageSelectItem == 1) {
                        chatListItem.get(position).imageSelectItem = 0;
                    }
                    else if (chatListItem.get(position).imageSelectItem == 0) {
                        chatListItem.get(position).imageSelectItem = 1;
                    }
                    chatListItemAdapter.notifyDataSetChanged();
                }
                else{
                Intent intent = new Intent(getContext(), ClientChat.class);
                intent.putExtra("receiverLogin", chatListItem.get(position).myLogin);
                intent.putExtra("senderName", chatListItem.get(position).senderName);
                intent.putExtra("senderLogin", chatListItem.get(position).senderLogin);
                startActivity(intent);
                getActivity().finish();
                }
                checkSelectItemAndSwitchState();
            }
        });

        chatList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int position, long arg3) {
                if (!isLongClickState){
                    EventBus.getDefault().post(new EventSelectItem(true));
                    isLongClickState = true; // switch view to state long click
                    chatListItem.get(position).imageSelectItem = 1;
                    chatListItemAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clientWorkScreenMenuClearChatList:
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void readDb(){
        chatListItem.clear();
        RealmResults<ChatNoteItem> realmResult = realm.where(ChatNoteItem.class).distinctValues("senderLogin").findAll();
        for (ChatNoteItem temp : realmResult){
            RealmResults<ChatNoteItem> realmRow = realm.where(ChatNoteItem.class).equalTo("senderLogin", temp.getSenderLogin()).findAll();

            String itemText = realmRow.last().getMessage();
            if (itemText.length() > 20) itemText = itemText.substring(0, 20) + "...";
            chatListItem.add(new ChatListItem(
                    (int) (Math.random() * 3) + 1,
                    temp.getSenderName(),
                    itemText,
                    realmRow.last().getMessDate(),
                    "",
                    0, // check box false and invisible by default
                    realmRow.last().getReceiver(),
                    realmRow.last().getSenderLogin(),
                    realmRow.last().getIsRead()));
        }
        if (chatListItem.size() > 0) layout.removeView(fragmentTwoChatListMainLayoutEmptyList);
        chatListItemAdapter = new ChatListItemAdapter(getContext(), chatListItem);
        chatList.setAdapter(chatListItemAdapter);
    }

    /**
     * Select rows via receivers for delete selected items
     * @param senderLogin senderLogin
     * @return return RealmResults via List interfaces
     */
    private RealmResults selectRowsForDelete(String senderLogin){
        RealmResults<ChatNoteItem> resultQuery = realm.where(ChatNoteItem.class)
                .equalTo("senderLogin", senderLogin)
                .findAll();
        return resultQuery;
    }

    private void checkSelectItemAndSwitchState(){
        isLongClickState = false;
        for (ChatListItem temp : chatListItem){
            if (temp.imageSelectItem == 1) isLongClickState = true;
        }
        if (!isLongClickState) EventBus.getDefault().post(new EventSelectItem(false));
    }

    public void pressButtonDelete() {
        String senderLogin = "empty";
        for (ChatListItem temp : chatListItem){
            if (temp.imageSelectItem == 1) {
                senderLogin = temp.senderLogin;
                final RealmResults results = selectRowsForDelete(senderLogin);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        results.deleteAllFromRealm();}
                });
            }
        }
        EventBus.getDefault().post(new EventSelectItem(false));
        readDb();
    }
}