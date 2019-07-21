package com.here.android.example.volunteer;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class API extends AppCompatActivity implements EchoWebSocketListener.ResultListener {
    private OkHttpClient client;
    private Request request;


    @Override
    public void dataFinished(String res){
        System.out.println(res);
    }

    private static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
    public API(){
        client = new OkHttpClient();
        request = new Request.Builder().url("ws://192.168.1.230:8765").build();

    }

    public void authUser(String username, String password, EchoWebSocketListener.ResultListener callback) {
        final JsonRPCRequest req = new JsonRPCRequest();
        req.setMethodName("authentification");
        JSONObject obj = new JSONObject();
        try {
            obj.put("username", username);
            obj.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        req.setParams(obj);
        Log.d("TEST", req.toString());


        EchoWebSocketListener listener = new EchoWebSocketListener();
        listener.setMessage(req.toString());
        listener.addListener(callback);
        WebSocket ws = client.newWebSocket(this.request, listener);
        //throw new SecurityException("Not authed");
        client.dispatcher().executorService().shutdown();
    }

    public void getVolunteer(String username, EchoWebSocketListener.ResultListener callback){
        final JsonRPCRequest req = new JsonRPCRequest();
        req.setMethodName("get_volunteer");
        JSONObject obj = new JSONObject();
        try {
            obj.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        req.setParams(obj);
        Log.d("TEST", req.toString());


        EchoWebSocketListener listener = new EchoWebSocketListener();
        listener.setMessage(req.toString());
        listener.addListener(callback);
        WebSocket ws = client.newWebSocket(this.request, listener);
        client.dispatcher().executorService().shutdown();
    }
}
