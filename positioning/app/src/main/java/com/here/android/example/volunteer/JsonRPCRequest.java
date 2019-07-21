package com.here.android.example.volunteer;

import android.util.Log;

import org.json.JSONObject;

public class JsonRPCRequest {
    public JsonRPCRequest(){
        m_object = new JSONObject();
        try {
            m_object.put("jsonrpc", "2.0");
        }  catch (Exception e){
            Log.e("JSON", e.getMessage());
        }
    }
    public void setMethodName(String methodName){
        try {
            m_object.put("method", methodName);
        }  catch (Exception e){
            Log.e("JSON", e.getMessage());
        }
    }

    public void setParams(JSONObject obj){
        try {
            m_object.put("params", obj);
        }  catch (Exception e){
            Log.e("JSON", e.getMessage());
        }
    }

    public String toString(){
        return m_object.toString();
    }

    private JSONObject m_object;
}
