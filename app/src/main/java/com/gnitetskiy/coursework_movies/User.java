package com.gnitetskiy.coursework_movies;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class User {
    private FirebaseFirestore db;
    private FirebaseUser user;
    public List<Movie> movies;
    private RecyclerView.Adapter movieAdapter;

    public User(RecyclerView.Adapter adapter) {
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        movies = new ArrayList<>();
        movieAdapter = adapter;
    }

    public void getUserMovies() {
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).collection("movies")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.w("Firestore", "Listen failed.", error);
                                return;
                            }

                            if (value != null) {
                                movies.clear();
                                for (DocumentSnapshot document : value.getDocuments()) {
                                    Movie movie = document.toObject(Movie.class);
                                    movie.setDocumentId(document.getId());
                                    movies.add(movie);
                                }
                                movieAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }

    public void addMovie(String title, String genre, int year, String status) {
        if (user != null) {
            String userId = user.getUid();
            String movieId = db.collection("users").document(userId).collection("movies").document().getId();

            Movie movie = new Movie(title, genre, year, status, false); // Добавляем поле favorite
            movie.setDocumentId(movieId);

            db.collection("users").document(userId).collection("movies").document(movieId).set(movie)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Firestore", "Movie successfully added!");
                            getUserMovies(); // Reload movies after adding new one
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Firestore", "Error adding movie", e);
                        }
                    });
        }
    }
}
