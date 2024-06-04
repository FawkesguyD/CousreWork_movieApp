package com.gnitetskiy.coursework_movies;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public class UserFragment extends Fragment {

    MaterialButton btnSignOut, btnUpdateEmail, btnUpdatePassword, btnShowPassword;
    MaterialTextView tvEmail, tvPassword;
    boolean isPasswordVisible = false;
    FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        btnSignOut = view.findViewById(R.id.signOutButton);
        btnUpdatePassword = view.findViewById(R.id.updatePasswordButton);
        tvEmail = view.findViewById(R.id.displayEmail);
        tvPassword = view.findViewById(R.id.displayPassword);

        if (currentUser != null) {
            tvEmail.setText("Email: " + currentUser.getEmail());
            tvPassword.setText("Password: ********");
        }

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                currentUser = null;
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        btnUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdatePasswordDialog();
            }
        });

        return view;
    }

    private void showUpdatePasswordDialog() {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle("Update Password");

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_update_password, null);
        builder.setView(customLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText etNewPassword = customLayout.findViewById(R.id.newPassword);
                EditText etCurrentPassword = customLayout.findViewById(R.id.currentPassword);

                String newPassword = etNewPassword.getText().toString().trim();
                String currentPassword = etCurrentPassword.getText().toString().trim();

                if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(currentPassword)) {
                    Toast.makeText(getContext(), "New password and current password are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
                currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            currentUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
