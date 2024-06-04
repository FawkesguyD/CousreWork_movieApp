package com.gnitetskiy.coursework_movies;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MovieAdapterEditable extends RecyclerView.Adapter<MovieAdapterEditable.MovieViewHolder> {

    private List<Movie> movieList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    public MovieAdapterEditable(List<Movie> movieList) {
        this.movieList = movieList;
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_list_movies_editable, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.titleTextView.setText(movie.getTitle());
        holder.genreTextView.setText(movie.getGenre());
        holder.yearTextView.setText(String.valueOf(movie.getYear()));
        holder.statusTextView.setText(movie.getStatus());
        holder.favoriteButton.setSelected(movie.isFavorite());

        if (movie.isFavorite()) {
            holder.favoriteButton.setImageResource(R.drawable.ic_favorite);
        } else {
            holder.favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
        }

        String documentId = movie.getDocumentId();

        holder.editButton.setOnClickListener(v -> showEditMovieDialog(holder.itemView, movie, documentId));
        holder.deleteButton.setOnClickListener(v -> deleteMovie(documentId, position));
        holder.favoriteButton.setOnClickListener(v -> toggleFavorite(movie, documentId));
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public void setMovies(List<Movie> movies) {
        this.movieList = movies;
        notifyDataSetChanged();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, genreTextView, yearTextView, statusTextView;
        ImageButton editButton, deleteButton, favoriteButton;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.movie_title);
            genreTextView = itemView.findViewById(R.id.movie_genre);
            yearTextView = itemView.findViewById(R.id.movie_year);
            statusTextView = itemView.findViewById(R.id.movie_status);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
        }
    }

    private void showEditMovieDialog(View itemView, Movie movie, String documentId) {
        View dialogView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.dialog_add_movie, null);
        final TextInputEditText titleEditText = dialogView.findViewById(R.id.title_edit_text);
        final TextInputEditText genreEditText = dialogView.findViewById(R.id.genre_edit_text);
        final TextInputEditText yearEditText = dialogView.findViewById(R.id.year_edit_text);
        final Spinner statusSpinner = dialogView.findViewById(R.id.status_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(itemView.getContext(),
                R.array.movie_status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        titleEditText.setText(movie.getTitle());
        genreEditText.setText(movie.getGenre());
        yearEditText.setText(String.valueOf(movie.getYear()));
        statusSpinner.setSelection(adapter.getPosition(movie.getStatus()));

        new MaterialAlertDialogBuilder(itemView.getContext())
                .setTitle("Edit Movie")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = titleEditText.getText().toString();
                    String newGenre = genreEditText.getText().toString();
                    String newYear = yearEditText.getText().toString();
                    String newStatus = statusSpinner.getSelectedItem().toString();

                    if (!newTitle.isEmpty() && !newYear.isEmpty()) {
                        updateMovie(documentId, newTitle, newGenre, Integer.parseInt(newYear), newStatus);
                    } else {
                        Toast.makeText(itemView.getContext(), "Title and Year are required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateMovie(String documentId, String title, String genre, int year, String status) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).collection("movies").document(documentId)
                    .update("title", title, "genre", genre, "year", year, "status", status)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Movie successfully updated!");
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Log.w("Firestore", "Error updating movie", e));
        }
    }

    private void deleteMovie(String documentId, int position) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).collection("movies").document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Movie successfully deleted!");
                        movieList.remove(position);
                        notifyItemRemoved(position);
                    })
                    .addOnFailureListener(e -> Log.w("Firestore", "Error deleting movie", e));
        }
    }

    private void toggleFavorite(Movie movie, String documentId) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            boolean newFavoriteStatus = !movie.isFavorite();
            db.collection("users").document(userId).collection("movies").document(documentId)
                    .update("favorite", newFavoriteStatus)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Favorite status successfully updated!");
                        movie.setFavorite(newFavoriteStatus);
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Log.w("Firestore", "Error updating favorite status", e));
        }
    }
}
