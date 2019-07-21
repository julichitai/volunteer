package com.here.android.example.volunteer;

import android.app.Application;

public class Globals extends Application
{
    private String jwtToken = "";

    public String getJwtToken()
    {
        return jwtToken;
    }

    public void setJwtToken(String s)
    {
        jwtToken = s;
    }
}