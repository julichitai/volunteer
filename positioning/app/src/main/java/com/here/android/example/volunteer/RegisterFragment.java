package com.here.android.example.volunteer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class RegisterFragment extends Fragment {

    public RegisterFragment() {

    }

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        Button btnRegister = view.findViewById(R.id.registerButton);
        View.OnClickListener oclBtnRegister = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(v.findViewById(R.id.nameEdit).toString().isEmpty()
                || v.findViewById(R.id.emailEdit).toString().isEmpty()
                || v.findViewById(R.id.phoneEdit).toString().isEmpty()
                || v.findViewById(R.id.birthDateEdit).toString().isEmpty()
                || !v.findViewById(R.id.passwordEdit).toString().equals(v.findViewById(R.id.passwordRepeat).toString())
                || v.findViewById(R.id.passwordEdit).toString().isEmpty()){
                    Toast.makeText(getActivity().getApplicationContext(),"Ошибка ввода", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(getActivity(), VolunteerActivity.class));
                }
            }
        };
        btnRegister.setOnClickListener(oclBtnRegister );
        return view;
    }
}