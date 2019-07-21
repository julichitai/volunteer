package com.here.android.example.volunteer;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public final class EchoWebSocketListener extends WebSocketListener {
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private String message;
    interface ResultListener{
        void dataFinished(String result);
    }
    public EchoWebSocketListener() {

    }
    private List<ResultListener> listeners = new ArrayList<ResultListener>();

    public void addListener(ResultListener toAdd) {
        listeners.add(toAdd);
    }

    public void sayHello(String result) {
        // Notify everybody that may be interested.
        for (ResultListener hl : listeners)
            hl.dataFinished(result);
        //webSocket.close(1000, "Goodbye !");
    }
    public void setMessage(String message){
        this.message = message;
    }
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        webSocket.send(message);
        //webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
    }
    @Override
    public void onMessage(WebSocket webSocket, String text) {
        try {
            JSONObject obj = new JSONObject(text);
            //if(obj.has("error")) throw new SecurityException("Not authed");
            //System.out.println(text);
            String data = obj.getString("result");
            Log.w("PROTO", data);
            sayHello(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}