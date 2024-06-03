package com.gnitetskiy.coursework_movies;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class User {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    List<Movie> movies;
    private MovieAdapter movieAdapter;


    public void addMovie(String movieTitle, String movieGenre, int year) {


        if (user != null) {
            String userId = user.getUid();
            String movieId = db.collection("users").document(userId).collection("movies").document().getId();

            Map<String, Object> movie = new HashMap<>();
            movie.put("title", movieTitle);
            movie.put("genre", movieGenre);
            movie.put("year", year);

            db.collection("users").document(userId).collection("movies").document(movieId).set(movie)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Firestore", "Movie successfully added!");
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

    public void getUserMovies() {
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).collection("movies")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot value,FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.w("Firestore", "Listen failed.", error);
                                return;
                            }

                            if (value != null) {
                                movies.clear();
                                movies.addAll(value.toObjects(Movie.class));
                                movieAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }
}
