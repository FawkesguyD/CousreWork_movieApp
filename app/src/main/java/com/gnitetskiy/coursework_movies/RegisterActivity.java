package com.gnitetskiy.coursework_movies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    EditText addEmail, addPassword, addConfirm;
    Button btn;
    TextView tv;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        addEmail = findViewById(R.id.editTextEmail);
        addPassword = findViewById(R.id.editTextPassword);
        addConfirm =  findViewById(R.id.editTextSubmitPassword);
        btn = findViewById(R.id.SignUnButton);
        tv = findViewById(R.id.textViewNeedLogin);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = addEmail.getText().toString();
                String password = addPassword.getText().toString();
                String confirm = addConfirm.getText().toString();
                if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    if (password.equals(confirm) && isValidPassword(password)) {
//                      db.register(username,email, password);
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "account created", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    } else {
                        Toast.makeText(getApplicationContext(), "password and confirm password didn't match", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }


    public boolean isValidPassword(String password) {
        if (password.length() < 8) {
            Toast.makeText(getApplicationContext(), "password should include 8 symbols", Toast.LENGTH_SHORT).show();
            return false;
        }

        // check for at least one capital letter
        if (!password.matches(".*[A-Z].*")) {
            Toast.makeText(getApplicationContext(), "Password must contain at least one capital letter", Toast.LENGTH_SHORT).show();
            return false;
        }

        // check for (letters, digits, symbols)
        if (!password.matches("[a-zA-Z0-9!@#$%^&*()-_+=<>?{}\\[\\]~`|\\\\/.,:;\"']*")) {
            Toast.makeText(getApplicationContext(), "Password contains invalid characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}