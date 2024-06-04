package com.gnitetskiy.coursework_movies;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {

    private User user;
    private MovieAdapterEditable movieAdapter;
    private List<Movie> movieList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        Button addButton = view.findViewById(R.id.add_movie_btn);

        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapterEditable(movieList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(movieAdapter);

        user = new User(movieAdapter);
        loadMovies();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddMovieDialog();
            }
        });

        return view;
    }

    private void loadMovies() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
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
                                for (DocumentSnapshot document : value.getDocuments()) {
                                    Movie movie = document.toObject(Movie.class);
                                    movie.setDocumentId(document.getId());
                                    movieList.add(movie);
                                }
                                movieAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }

    private void showAddMovieDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_movie, null);
        final TextInputEditText titleEditText = dialogView.findViewById(R.id.title_edit_text);
        final TextInputEditText genreEditText = dialogView.findViewById(R.id.genre_edit_text);
        final TextInputEditText yearEditText = dialogView.findViewById(R.id.year_edit_text);
        final Spinner statusSpinner = dialogView.findViewById(R.id.status_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.movie_status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Add Movie")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = titleEditText.getText().toString();
                    String genre = genreEditText.getText().toString();
                    String year = yearEditText.getText().toString();
                    String status = statusSpinner.getSelectedItem().toString();

                    if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(year)) {
                        user.addMovie(title, genre, Integer.parseInt(year), status);
                    } else {
                        Toast.makeText(getContext(), "Title and Year are required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
