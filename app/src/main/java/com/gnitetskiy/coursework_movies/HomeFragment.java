package com.gnitetskiy.coursework_movies;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ImageButton filterBtn;
    private EditText searchEditText;
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        searchEditText = view.findViewById(R.id.searchEditText);
        filterBtn = view.findViewById(R.id.filterButton);
        recyclerView = view.findViewById(R.id.recycler_view_home);

        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(movieList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(movieAdapter);

        user = new User(movieAdapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // No action needed
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Filter the movie list based on the entered text
                showFilterMovies(charSequence.toString(), getSelectedStatus(), isFavoriteChecked());
            }

            public void afterTextChanged(Editable editable) {
                // No action needed
            }
        });

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });

        loadMovies();

        return view;
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

    private void showFilterMovies(String filter, String status, boolean isFavorite) {
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

                            movieList.clear();
                            for (DocumentSnapshot document : value.getDocuments()) {
                                Movie movie = document.toObject(Movie.class);
                                movie.setDocumentId(document.getId());
                                if (matchesFilter(movie, filter, status, isFavorite)) {
                                    movieList.add(movie);
                                }
                            }
                            movieAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    private boolean matchesFilter(Movie movie, String filter, String status, boolean isFavorite) {
        String lowerCaseFilter = filter.toLowerCase();
        String title = movie.getTitle() != null ? movie.getTitle().toLowerCase() : "";
        String genre = movie.getGenre() != null ? movie.getGenre().toLowerCase() : "";
        String year = String.valueOf(movie.getYear());

        boolean matchesText = title.contains(lowerCaseFilter) || genre.contains(lowerCaseFilter) || year.contains(lowerCaseFilter);
        boolean matchesStatus = status == null || movie.getStatus().equals(status);
        boolean matchesFavorite = !isFavorite || movie.isFavorite();

        return matchesText && matchesStatus && matchesFavorite;
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter_movie, null);
        final EditText searchEditText = dialogView.findViewById(R.id.search_edit_text);
        final Spinner statusSpinner = dialogView.findViewById(R.id.status_spinner);
        final CheckBox favoriteCheckBox = dialogView.findViewById(R.id.favorite_checkbox);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.movie_status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Filter Movies")
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    String filter = searchEditText.getText().toString();
                    String status = statusSpinner.getSelectedItem().toString();
                    boolean isFavorite = favoriteCheckBox.isChecked();

                    showFilterMovies(filter, status, isFavorite);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String getSelectedStatus() {
        // Implement this method to get the currently selected status from the spinner
        return null;
    }

    private boolean isFavoriteChecked() {
        // Implement this method to check if the favorite checkbox is checked
        return false;
    }
}
