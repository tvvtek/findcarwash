package ru.findcarwash.findcarwash.workscreens.ChatClient;

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
import ru.findcarwash.findcarwash.workscreens.ChatHelpers.ChatNoteItem;
import ru.findcarwash.findcarwash.workscreens.ClientWorkScreen.ClientWorkScreen;
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
 * receiverLogin = "tvvtek" мой логин
 * senderLogin = "Intercabel" логин отпрвивтеля
 * senderName = "Интеркабель" имя отправителя
 */


public class ClientChat extends AppCompatActivity {
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
    private String myLogin, senderLogin, senderName, key;
    // for json mess decode and write DB
    private String isMessage, to, from, message;
    private String senderNameFromDB;
    // Adapter for List
    private ArrayList<ClientChatItem> chatItemArrayList = new ArrayList<>();

    private final String CHAT_AUTH_OK = "auth:ok";

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        webSocket.closeConnection();
        finish();
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
        setContentView(R.layout.client_chat);

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
        myLogin = intent.getStringExtra("receiverLogin"); // мой логин
        senderName = intent.getStringExtra("senderName"); // имя отправителя
        senderLogin = intent.getStringExtra("senderLogin"); // логин получателя
      //  Log.d(MySettings.LOG_TAG, "senderLogin=" + senderLogin);

        getSupportActionBar().setTitle(senderName);// set wash name to Action Bar
        // make view element
        chat = findViewById(R.id.clientChatListViewElement);
        messageForSend = findViewById(R.id.clientChatMessage);
        sendBtn = findViewById(R.id.clientChatSend);
        sendBtn.setEnabled(false);
        // make body, connect and send auth data
        authorization = gson.toJson(getAuthObjForRequest());
        webSocket = new WebSocket(authorization); // make auth body for send server after connect

       // Log.d(MySettings.LOG_TAG, "CHAT " + "loginWash " + loginWash + " short_name " + short_name + " id " + id + " login " + login + " key " + key);
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
                goCatalog();
                finish();
               // onBackPressed();
                return true;
            case android.R.id.home:
                goCatalog();
                finish();
                //onBackPressed();
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

 /*   public void sendToast(String text){
        Log.d(MySettings.LOG_TAG, "sendToast");
        Toast toast = Toast.makeText(this,
                text, Toast.LENGTH_SHORT);
        toast.show();
    } */

    // NETWORK CALLBACK
    /**
     * After connect
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receivedData(WebSocketConnect event) {
   //     sendToast("Connect OKAY");
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
        //    addItemReceiver(this.message);
        }
        // make parser message
     //   sendToast(event.receivedData);
    }

    /**
     * After Disconnect
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receivedDisconnect(WebSocketDisconnect event) {
        addItemError();
       // sendToast("You disconnect");
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
        //sendToast("You error");
        sendBtn.setEnabled(false);
        menuItemStatus.setIcon(R.drawable.ic_chat_actionbar_check_connection);
        menuItemStatus.setEnabled(true);
    }

    /**
     * Button send message
     * @param view view
     */
    public void clientChatSend(View view) {
        if (messageForSend.length() > 0) addItemSender(); // add to view and local DB
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
                noteItem.setSenderName(senderName); // receiver
                noteItem.setMessDate(getDate());
                noteItem.setMessage(messageForSend.getText().toString());
                noteItem.setSenderLogin(senderLogin);
                noteItem.setIsRead(true); // прочитано сообщение или нет
            }
        });
        chatItemArrayList.add(new ClientChatItem(1, messageForSend.getText().toString(), getDate()));
        // make JSON onj and send
        webSocket.sendMessage(messageBodyEncode(senderLogin, messageForSend.getText().toString()));
        messageForSend.setText("");
        setClientChatAdapter();
    }


    /**
     * Event receive message from server from sender correct, ALL senders!!!
     * @param messageBody message
     */
    private void addItemReceiver(final String messageBody){
    //    Log.d(MySettings.LOG_TAG, "senderLogin=" + senderLogin);
     //   Log.d(MySettings.LOG_TAG, "from=" + from);

        // Проверяем пришедшее сообщение, оно адресовано для текущего октрытого чата или для другого, если для текущего, значит пишем в базу
        // и обновляем ListView, если для другого, нужно определить есть ли такой чат в базе и если есть, то дописать пришелшие сообщения в него
        // если нет, просто проигнорировать, это нужно чтобы клиенту доходили только сообщения от тех, кому он сам писал и не удалял диалог
        if (messageBody != null & from != null & senderLogin.equals(from)) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    ChatNoteItem noteItem = realm.createObject(ChatNoteItem.class);
                    noteItem.setSender(0); // flag
                    noteItem.setReceiver(myLogin);  // мой логин
                    noteItem.setSenderLogin(senderLogin); // логин отправителя
                    noteItem.setSenderName(senderName); // имя отправителя
                    noteItem.setMessDate(getDate()); // текущая дата
                    noteItem.setMessage(messageBody); // сообщение
                    noteItem.setIsRead(true); // прочитано сообщение или нет

                }
            });
            chatItemArrayList.add(new ClientChatItem(0, messageBody, getDate()));
            setClientChatAdapter();
        }
        else if (messageBody != null & from != null & !senderLogin.equals(from)) {
            // Делаем выборку из базы и находим хотя бы одну запись с таким логином, значит пользователь уже писал нужному логину и ждет ответа
            // значит записываем в базу данных сообщения.
            senderNameFromDB = selectSenderName(from);
            if( senderNameFromDB != null ){
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        ChatNoteItem noteItem = realm.createObject(ChatNoteItem.class);
                        noteItem.setSender(0); // флаг отправителя или получателя
                        noteItem.setReceiver(myLogin);  // мой логин
                        noteItem.setSenderLogin(from); // логин отправителя
                        noteItem.setSenderName(senderNameFromDB); // имя отправителя
                        noteItem.setMessDate(getDate()); // текущая дата
                        noteItem.setMessage(messageBody); // сообщение
                        noteItem.setIsRead(false); // прочитано сообщение или нет
                    }
                });
            }
        }
    }

    private void test(){
        for (ClientChatItem temp: chatItemArrayList){
            Log.d(MySettings.LOG_TAG, "sender=" + Integer.toString(temp.sender));
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
                noteItem.setSenderName(senderName);
                noteItem.setMessDate(getDate());
                noteItem.setMessage(getResources().getString(R.string.chatScreenDisconnectInfo));
                noteItem.setSenderLogin(senderLogin);
            }
        });
        chatItemArrayList.add(new ClientChatItem(-1, getResources().getString(R.string.chatScreenDisconnectInfo), getDate()));
        setClientChatAdapter();
    }


    /**
     * Get senderName for write DB
     * @param senderLogin sender login message
     * @return senderName from DB
     */
    private String selectSenderName(String senderLogin){
        String name;
        RealmResults<ChatNoteItem> resultQuery = realm.where(ChatNoteItem.class)
                .equalTo("senderLogin", senderLogin)
                .findAll();
        if (!resultQuery.isEmpty()) return resultQuery.get(0).getSenderName();
        else return null;
    }
    /**
     * Select rows via selected login and receivers for first make View AND delete this chat
=     * @param short_name receiver name
     * @return return RealmResults via List interfaces
     */
    private RealmResults selectRows(String senderLogin){
        RealmResults<ChatNoteItem> resultQuery = realm.where(ChatNoteItem.class)
                .equalTo("senderLogin", senderLogin)
                .findAll();
        return resultQuery;
    }


    private void makeFirstListView(){
        chatItemArrayList.clear();
        RealmResults<ChatNoteItem> existRows = selectRows(senderLogin);
        if (!existRows.isEmpty()){

            for (ChatNoteItem temp : existRows){
                // getSender() = 1 to SENDER or getSender() = 0 to my message
                chatItemArrayList.add(new ClientChatItem(temp.getSender(), temp.getMessage(), temp.getMessDate()));
            }
        }
       // Log.d(MySettings.LOG_TAG, "rows" + chatItemArrayList.get(0));
        //clientChatAdapter.notifyDataSetChanged();
    }

    private JsonChatAuthorization getAuthObjForRequest(){
        JsonChatAuthorization jsonChatAuthorization = new JsonChatAuthorization("0","client", myLogin, key);
        return jsonChatAuthorization;
    }

    private String messageBodyEncode(String loginWash, String message){
        String body;
        body = gson.toJson(new JsonChatMessage("1", "1", myLogin, loginWash, message));
        return body;
    }

    // parsing received message
    private void messageBodyDecode(String receivedBody){
        JsonChatMessage jsonChatMessageReceive = gson.fromJson(receivedBody, JsonChatMessage.class);
        this.isMessage = jsonChatMessageReceive.isMessage;
        this.to = jsonChatMessageReceive.to;
        this.from = jsonChatMessageReceive.from;
        this.message = jsonChatMessageReceive.message;
        if (this.message != null) addItemReceiver(this.message);
     //   Log.d(MySettings.LOG_TAG, "isMessage=" + isMessage);
    //    Log.d(MySettings.LOG_TAG, "to=" + to);
    //    Log.d(MySettings.LOG_TAG, "from=" + from);
    //    Log.d(MySettings.LOG_TAG, "message=" + message);
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

    private void goCatalog(){
        finish();
        Intent intent = new Intent(this, ClientWorkScreen.class);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent intent = new Intent(this, ClientWorkScreen.class);
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
    }
}