package com.gnitetskiy.coursework_movies;

public class Movie {
    private String title;
    private String genre;
    private int year;
    private String status;
    private boolean favorite;
    private String documentId;

    public Movie() {
        // Пустой конструктор, необходимый для Firestore
    }

    public Movie(String title, String genre, int year, String status, boolean favorite) {
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.status = status;
        this.favorite = favorite;
    }

    // Геттеры и сеттеры
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
