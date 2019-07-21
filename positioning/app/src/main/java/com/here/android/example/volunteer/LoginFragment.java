package com.here.android.example.volunteer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import static android.content.Context.MODE_PRIVATE;

public class LoginFragment extends Fragment implements EchoWebSocketListener.ResultListener {

    private EditText loginET;
    private EditText passwordET;
    private String username;
    public LoginFragment() {

    }

    @Override
    public void dataFinished(String res){
        SharedPreferences.Editor editor = getActivity().getApplicationContext().getSharedPreferences("secure", MODE_PRIVATE).edit();
        editor.putString("token", res);
        editor.putString("username", username);
        editor.apply();
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        Button btnLogin = view.findViewById(R.id.loginButton);
        loginET = view.findViewById(R.id.loginEdit);
        passwordET = view.findViewById(R.id.passwordEditLogin);
        final LoginFragment coo = this;
        View.OnClickListener oclBtnLogin = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String login = "";
                String password = "";
                if(!loginET.getText().toString().isEmpty())
                login = loginET.getText().toString();
                if(!passwordET.getText().toString().isEmpty())
                password = passwordET.getText().toString();
                if(!login.isEmpty()
                && !password.isEmpty()) {
                    API api = new API();
                    try {
                        api.authUser(login, password, coo);
                        username = login;
                        startActivity(new Intent(getActivity(), VolunteerActivity.class));
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                    startActivity(new Intent(getActivity(), VolunteerActivity.class));
                }
            }
        };
        btnLogin.setOnClickListener(oclBtnLogin);
        return view;
    }
}