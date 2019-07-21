package com.here.android.example.volunteer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment implements EchoWebSocketListener.ResultListener {

    public HomeFragment() {

    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Button btnMap = view.findViewById(R.id.openMapButton);
        View.OnClickListener oclBtnMap = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(new Intent(getActivity(), BasicPositioningActivity.class));

            }
        };
        btnMap.setOnClickListener(oclBtnMap);
        SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences("secure", MODE_PRIVATE);
        String login = prefs.getString("username", "");
        Log.w("PROTO", login);
                    API api = new API();
                    try {
                        api.getVolunteer(login, this);
                        //startActivity(new Intent(getActivity(), VolunteerActivity.class));
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
        return view;
    }

    @Override
    public void dataFinished(String result) {

        View view = getView();
        TextView fio = view.findViewById(R.id.userFio);
        TextView rating = view.findViewById(R.id.ratingText);
        try {
            JSONObject obj = new JSONObject(result);
            fio.setText(obj.getString("name"));
            rating.setText(String.format("Рейтинг: %d", obj.getInt("score")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // fio.setText();
        Log.w("PROTO", result);
    }
}