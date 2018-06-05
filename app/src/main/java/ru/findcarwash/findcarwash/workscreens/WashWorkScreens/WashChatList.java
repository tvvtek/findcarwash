package ru.findcarwash.findcarwash.workscreens.WashWorkScreens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;

import io.realm.Realm;
import io.realm.RealmResults;
import ru.findcarwash.findcarwash.AppContact;
import ru.findcarwash.findcarwash.AppLaunch;
import ru.findcarwash.findcarwash.R;
import ru.findcarwash.findcarwash.registerorauth.WashRegistrationUploadPhoto;
import ru.findcarwash.findcarwash.workscreens.ChatHelpers.ChatListItem;
import ru.findcarwash.findcarwash.workscreens.ChatHelpers.ChatListItemAdapter;
import ru.findcarwash.findcarwash.workscreens.ChatHelpers.ChatNoteItem;
import ru.findcarwash.findcarwash.workscreens.ChatWash.WashChat;
import ru.findcarwash.findcarwash.workscreens.ClientFragmentTwoHelpers.EventSelectItem;
import ru.findcarwash.ru.helpers.factory.DependenciesFactory;
import ru.findcarwash.ru.helpers.settings.MySettings;

public class WashChatList extends AppCompatActivity{

    private String myLogin;

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    SharedPreferences preferences;
    // Layout
    RelativeLayout layout;
    // Data base reader
    Realm realm;
    // View
    TextView emptyList;
    MenuItem menuListClearChatItemActionBarButton;
    ListView chatList;
    ArrayList<ChatListItem> chatListItem = new ArrayList<>();
    ChatListItemAdapter chatListItemAdapter;
    private boolean isLongClickState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wash_chat_list);
        preferences = DependenciesFactory.getPreferencesGet();
        layout = findViewById(R.id.fragmentTwoChatListMainLayout);
        emptyList = findViewById(R.id.washChatListEmpty);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startClientWorkScreenActionBar)));
        myLogin = preferences.getString(MySettings.CLIENT_LOGIN_PREFERENCE, "");
        chatList = findViewById(R.id.chatWashList);

        Realm.init(this);
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
                    Intent intent = new Intent(getApplicationContext(), WashChat.class);
                    intent.putExtra("receiverLogin", chatListItem.get(position).myLogin);
                    intent.putExtra("senderLogin", chatListItem.get(position).senderLogin);
                    startActivity(intent);
                    finish();
                }
                checkSelectItemAndSwitchState();
            }
        });

        chatList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int position, long arg3) {
                if (!isLongClickState){
                    menuListClearChatItemActionBarButton.setEnabled(true);
                    menuListClearChatItemActionBarButton.setVisible(true);
                    isLongClickState = true; // switch view to state long click
                    chatListItem.get(position).imageSelectItem = 1;
                    chatListItemAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menuWashScreen) {
        getMenuInflater().inflate(R.menu.wash_work_screen_base_menu, menuWashScreen);
        menuListClearChatItemActionBarButton = menuWashScreen.findItem(R.id.washWorkScreenMenuClearChatList);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.washWorkScreenMenuClearChatList:
                pressButtonDelete();
                return true;
            case R.id.washWorkScreenMenuMyProfile:
                finish();
                Intent myPhoto = new Intent(this, WashRegistrationUploadPhoto.class);
                myPhoto.putExtra("whoRun", "WashChatList");
                myPhoto.putExtra("myLogin", myLogin);
                startActivity(myPhoto);
                return true;
            case R.id.washWorkScreenMenuContact:
                Intent aboutApp = new Intent(this, AppContact.class);
                startActivity(aboutApp);
                return true;
            case R.id.washWorkScreenMenuExit:
                if (exitApp()) {
                    finish();
                    Intent launchApp = new Intent(this, AppLaunch.class);
                    startActivity(launchApp);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void readDb(){
        chatListItem.clear();
        RealmResults<ChatNoteItem> realmResult = realm.where(ChatNoteItem.class).distinctValues("senderLogin").findAll();
        for (ChatNoteItem temp : realmResult){
            RealmResults<ChatNoteItem> realmRow = realm.where(ChatNoteItem.class).equalTo("senderLogin", temp.getSenderName()).findAll();
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
        if (!chatListItem.isEmpty())emptyList.setVisibility(View.INVISIBLE);
        else emptyList.setVisibility(View.VISIBLE);
        Collections.sort(chatListItem);
        chatListItemAdapter = new ChatListItemAdapter(this, chatListItem);
        chatList.setAdapter(chatListItemAdapter);
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
                Log.d(MySettings.LOG_TAG, "senderlogin=" + senderLogin);
                final RealmResults results = selectRowsForDelete(senderLogin);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        results.deleteAllFromRealm();}
                });
            }
        }
        readDb();
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

    private boolean exitApp(){
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(MySettings.IS_WASH, false);
            editor.putString(MySettings.CLIENT_LOGIN_PREFERENCE, "");
            editor.putString(MySettings.CLIENT_EMAIL_PREFERENCE, "");
            editor.putString(MySettings.CLIENT_KEY, "");
            editor.putString(MySettings.CLIENT_DEVICE_PREFERENCE, "");
            editor.apply();
            return true;
        }
        catch (Exception writeToPreferenceException){
            writeToPreferenceException.printStackTrace();
            return false;
        }
    }
}