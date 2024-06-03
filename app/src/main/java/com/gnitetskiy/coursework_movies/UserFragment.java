package com.gnitetskiy.coursework_movies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserFragment extends Fragment {

    Button btn;
    TextView tv;

    FirebaseUser currentUser;
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        btn = view.findViewById(R.id.signOutButton);
        tv = view.findViewById(R.id.displayUser);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                currentUser = null;
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });


        if (currentUser != null) {
            tv.setText(currentUser.getEmail());
        }
        return view;
    }
}