package com.gnitetskiy.coursework_movies;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ListFragment extends Fragment {

    private Button addButton;
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    User user;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        user = new User();

        addButton = view.findViewById(R.id.add_movie_btn);
        recyclerView = view.findViewById(R.id.recycler_view);

        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(movieList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(movieAdapter);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddMovieDialog();
            }
        });

        loadMovies();

        return view;
    }

    private void showAddMovieDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_movie, null);
        final TextInputEditText titleEditText = dialogView.findViewById(R.id.title_edit_text);
        final TextInputEditText genreEditText = dialogView.findViewById(R.id.genre_edit_text);
        final TextInputEditText yearEditText = dialogView.findViewById(R.id.year_edit_text);


        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Add Movie")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = titleEditText.getText().toString();
                    String genre = genreEditText.getText().toString();
                    String year = yearEditText.getText().toString();

                    if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(year)) {
                        user.addMovie(title, genre, Integer.parseInt(year));
                    } else {
                        Toast.makeText(getContext(), "Title and Year are required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadMovies() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).collection("movies")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.w("Firestore", "Listen failed.", error);
                                return;
                            }

                            if (value != null) {
                                movieList.clear();
                                movieList.addAll(value.toObjects(Movie.class));
                                movieAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }
}