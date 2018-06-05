package ru.findcarwash.network;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

import ru.findcarwash.ru.helpers.settings.MySettings;

public class WebSocket{

    private WebSocketClient mWebSocketClient;
    private String authorizationRequest;

    public WebSocket(String authorizationRequest){
        this.authorizationRequest = authorizationRequest;
    }

    public void connectWebSocket() {
        URI uri;
        try {
            uri = new URI(MySettings.CHAT_URL_PORT);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                EventBus.getDefault().post(new WebSocketConnect());
                mWebSocketClient.send(authorizationRequest); // try authorization after connect
            }

            @Override
            public void onMessage(String message) {

                EventBus.getDefault().post(new WebSocketReceived(message));
            //    Log.d(MySettings.LOG_TAG, "fromServer=" + message.toString());
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                EventBus.getDefault().post(new WebSocketDisconnect());
            }

            @Override
            public void onError(Exception e) {
                EventBus.getDefault().post(new WebSocketError());
               // Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    /**
     * Send message to server
     * @param message body
     */
    public void sendMessage(String message){
        mWebSocketClient.send(message);
    }

    /**
     * Close socket connection
     * @return result
     */
    public boolean closeConnection(){
        mWebSocketClient.close();
        return true;
    }
}