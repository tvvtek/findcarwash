package ru.findcarwash.findcarwash.workscreens.ChatWash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import ru.findcarwash.findcarwash.R;
import ru.findcarwash.findcarwash.workscreens.ChatClient.ClientChatAdapter;
import ru.findcarwash.findcarwash.workscreens.ChatClient.ClientChatItem;
import ru.findcarwash.findcarwash.workscreens.ChatHelpers.ChatNoteItem;
import ru.findcarwash.findcarwash.workscreens.WashWorkScreens.WashChatList;
import ru.findcarwash.network.WebSocket;
import ru.findcarwash.network.WebSocketConnect;
import ru.findcarwash.network.WebSocketDisconnect;
import ru.findcarwash.network.WebSocketError;
import ru.findcarwash.network.WebSocketReceived;
import ru.findcarwash.ru.helpers.factory.DependenciesFactory;
import ru.findcarwash.ru.helpers.json.JsonChatAuthorization;
import ru.findcarwash.ru.helpers.json.JsonChatMessage;
import ru.findcarwash.ru.helpers.settings.MySettings;

/**
 * API
 *
 * send JsonChatAuthorization
 * received "auth:ok" THEN success, switch to enabled send button and switch to green status in actionbar
 *
 * EXAMPLE:
 * receiverLogin = "Intercabel" мой логин
 * senderLogin = "tvvtek" логин отпрвивтеля
 * senderName = "tvvtek" имя отправителя
 */


public class WashChat extends AppCompatActivity {
    Gson gson;
    SharedPreferences sharedPreferences;
    // Actionbar button
    MenuItem menuItemStatus;
    // data base
    Realm realm;
    // view
    Button sendBtn;
    ListView chat;
    EditText messageForSend;
    ClientChatAdapter clientChatAdapter;
    // network
    private WebSocket webSocket;
    // other
    private String authorization;
    // auth chat and send message
    private String myLogin, senderLogin, key;
    // for json mess decode and write DB
    private String isMessage, to, from, message;
    // Adapter for List
    private ArrayList<ClientChatItem> chatItemArrayList = new ArrayList<>();

    JsonChatMessage jsonChatMessageReceive;

    private final String CHAT_AUTH_OK = "auth:ok";

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        //    Log.d(MySettings.LOG_TAG,"START");
        super.onStart();
    }

    @Override
    protected void onStop() {
     //   Log.d(MySettings.LOG_TAG,"STOP");
        EventBus.getDefault().unregister(this);
        webSocket.closeConnection();
        super.onStop();
    }

    @Override
    protected void onResume() {
      //  Log.d(MySettings.LOG_TAG,"onResume");

        Realm.init(this);
        realm = DependenciesFactory.getRealm();
        setReadChat(senderLogin); // меняем последнее сообщение в чате на статус прочитанного, чтобы в списке не отображать значек Новойе сообщение

        makeFirstListView();
        makeSocketConnect();
        setClientChatAdapter();

        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wash_chat);

        Context ctx = getApplicationContext();
        initializePreferences(ctx);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.startClientWorkScreenActionBar)));
        // Gson obj
        gson = DependenciesFactory.getGsonEncode();
        // get unique client key
        sharedPreferences = DependenciesFactory.getPreferencesGet();
        key = sharedPreferences.getString(MySettings.CLIENT_KEY, "null");
        // get data from store activity
        Intent intent = getIntent();
        myLogin = intent.getStringExtra("receiverLogin"); // client login
      //  senderName = intent.getStringExtra("senderName"); // short wash name
        senderLogin = intent.getStringExtra("senderLogin");

        getSupportActionBar().setTitle(senderLogin);// set wash name to Action Bar
        // make view element
        chat = findViewById(R.id.washChatListViewElement);
        messageForSend = findViewById(R.id.washChatMessage);
        sendBtn = findViewById(R.id.washChatSend);
        sendBtn.setEnabled(false);
        // make body, connect and send auth data
        authorization = gson.toJson(getAuthObjForRequest());
        webSocket = new WebSocket(authorization); // make auth body for send server after connect
      //  Log.d(MySettings.LOG_TAG, "CHAT " + "myLogin " + myLogin + " senderLogin " + senderLogin + " key " + key);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.client_chat_screen_menu, menu);
        menuItemStatus = menu.findItem(R.id.chatScreenUserStatus);
        menuItemStatus.setEnabled(false);
        return true;
    }

    /**
     * Button in ActionBar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chatScreenUserStatus:
                webSocket.connectWebSocket();
                return true;
            case R.id.chatScreenDeleteChat:
                final RealmResults results = selectRows(senderLogin);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        results.deleteAllFromRealm();
                    }
                });
                goList();
                finish();
                return true;
            case android.R.id.home:
                goList();
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void makeSocketConnect(){
        webSocket.connectWebSocket();
    }

    private void setClientChatAdapter(){
        // make and setup list adapter
        clientChatAdapter = new ClientChatAdapter(this, chatItemArrayList);
        chat.setAdapter(clientChatAdapter);
    }

    // NETWORK CALLBACK
    /**
     * After connect
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receivedData(WebSocketConnect event) {
    }

    /**
     * After received data call this method
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receivedData(WebSocketReceived event) {
        if (event.receivedData.equals(CHAT_AUTH_OK)){
            sendBtn.setEnabled(true);
            menuItemStatus.setIcon(R.drawable.ic_chat_actionbar_online);
        }
        else {
            messageBodyDecode(event.receivedData);
        }
    }

    /**
     * After Disconnect
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receivedDisconnect(WebSocketDisconnect event) {
        addItemError();
        sendBtn.setEnabled(false);
        menuItemStatus.setIcon(R.drawable.ic_chat_actionbar_check_connection);
        menuItemStatus.setEnabled(true);
    }

    /**
     * After error
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receivedError(WebSocketError event) {
        addItemError();
        sendBtn.setEnabled(false);
        menuItemStatus.setIcon(R.drawable.ic_chat_actionbar_check_connection);
        menuItemStatus.setEnabled(true);
    }

    /**
     * Event send message from client to server correct
     */
    private void addItemSender(){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ChatNoteItem noteItem = realm.createObject(ChatNoteItem.class);
                noteItem.setSender(1);
                noteItem.setReceiver(myLogin); // sender
                noteItem.setSenderLogin(senderLogin);
                noteItem.setSenderName(senderLogin);
                noteItem.setMessDate(getDate());
                noteItem.setMessage(messageForSend.getText().toString());
                noteItem.setIsRead(true); // свое сообщение всегда прочитано
            }
        });
        chatItemArrayList.add(new ClientChatItem(1, messageForSend.getText().toString(), getDate()));
        // make JSON onj and send
        webSocket.sendMessage(messageBodyEncode(senderLogin, messageForSend.getText().toString()));
        messageForSend.setText("");
        setClientChatAdapter();
    }

    /**
     * Event receive message from server from sender correct
     * @param messageBody message
     */
    private void addItemReceiver(final String messageBody){
        if (messageBody != null & from != null & senderLogin.equals(from)) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    ChatNoteItem noteItem = realm.createObject(ChatNoteItem.class);
                    noteItem.setSender(0); // flag
                    noteItem.setReceiver(myLogin); //receiver
                    noteItem.setSenderName(senderLogin); // sender
                    noteItem.setMessDate(getDate()); // curr date
                    noteItem.setMessage(messageBody); // message body
                    noteItem.setSenderLogin(senderLogin);
                    noteItem.setIsRead(true); // прочитано, так как текущий чат совпадает с собеседником
                }
            });
            chatItemArrayList.add(new ClientChatItem(0, messageBody, getDate()));
            setClientChatAdapter();
        }
        // Check receivers, maybe receive mess from other receiver, not open it this chat session
        else if (messageBody != null & from != null & !senderLogin.equals(from)){
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    ChatNoteItem noteItem = realm.createObject(ChatNoteItem.class);
                    noteItem.setSender(0); // flag
                    noteItem.setReceiver(myLogin); //receiver
                    noteItem.setSenderName(from); // sender
                    noteItem.setMessDate(getDate()); // curr date
                    noteItem.setMessage(messageBody); // message body
                    noteItem.setSenderLogin(from);
                    noteItem.setIsRead(false); // не прочитано, так как текущий чат НЕ совпадает с собеседником
                }
            });
        }
    }

    /**
     * Event to Error received data or Socket disconnect
     */
    private void addItemError(){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ChatNoteItem noteItem = realm.createObject(ChatNoteItem.class);
                noteItem.setSender(-1);
                noteItem.setReceiver(myLogin);
                noteItem.setSenderName(senderLogin);
                noteItem.setSenderLogin(senderLogin);
                noteItem.setMessDate(getDate());
                noteItem.setMessage(getResources().getString(R.string.chatScreenDisconnectInfo));
            }
        });
        chatItemArrayList.add(new ClientChatItem(-1, getResources().getString(R.string.chatScreenDisconnectInfo), getDate()));
        setClientChatAdapter();
    }

    /**
     * Select rows via selected login and receivers for first make View AND delete this chat
     * @param senderLogin receiver name
     * @return return RealmResults via List interfaces
     */
    private RealmResults selectRows(String senderLogin){
        RealmResults<ChatNoteItem> resultQuery = realm.where(ChatNoteItem.class)
  //              .equalTo("myLogin", login)
  //              .and()
                .equalTo("senderLogin", senderLogin)
                .findAll();
        return resultQuery;
    }


    private void makeFirstListView(){
        chatItemArrayList.clear();
        RealmResults<ChatNoteItem> existRows = selectRows(senderLogin);
        if (!existRows.isEmpty()){
            for (ChatNoteItem temp : existRows){
                chatItemArrayList.add(new ClientChatItem(temp.getSender(), temp.getMessage(), temp.getMessDate()));
            }
        }
       // Log.d(MySettings.LOG_TAG, "rows" + chatItemArrayList.get(0));
        //clientChatAdapter.notifyDataSetChanged();
    }

    private JsonChatAuthorization getAuthObjForRequest(){
        JsonChatAuthorization jsonChatAuthorization = new JsonChatAuthorization("0","admin", myLogin, key);
        return jsonChatAuthorization;
    }

    private String messageBodyEncode(String loginWash, String message){
        String body;
        body = gson.toJson(new JsonChatMessage("1", "0", myLogin, senderLogin, message));
        return body;
    }

    // parsing received message
    private void messageBodyDecode(String receivedBody){
        jsonChatMessageReceive = gson.fromJson(receivedBody, JsonChatMessage.class);
        this.isMessage = jsonChatMessageReceive.isMessage;
        this.to = jsonChatMessageReceive.to;
        this.from = jsonChatMessageReceive.from;
        this.message = jsonChatMessageReceive.message;
        if (this.message != null) addItemReceiver(message);
    }


    private void setReadChat(final String senderLogin){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<ChatNoteItem> realmRow = realm.where(ChatNoteItem.class).equalTo("senderLogin", senderLogin).findAll();
                if (!realmRow.isEmpty()) realmRow.last().setIsRead(true);
            }
        });
    }

    private void initializePreferences(Context ctx){
        SharedPreferences preferences = getPreferences(ctx.MODE_PRIVATE);
        DependenciesFactory.preferencesSet(preferences);
    }

    private void goList(){
        finish();
        Intent intent = new Intent(this, WashChatList.class);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent intent = new Intent(this, WashChatList.class);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private String getDate(){
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = formatter.format(today);
        return date;
    //    return formatter.format(today);
    }

    public void washChatSend(View view) {
        if (messageForSend.length() > 0) addItemSender(); // add to view and local DB
    }
}