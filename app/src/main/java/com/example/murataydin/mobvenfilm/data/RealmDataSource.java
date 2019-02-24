package com.example.murataydin.mobvenfilm.data;

import com.example.murataydin.mobvenfilm.model.Movie;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;



public class RealmDataSource {
    private Realm realm;

    public RealmDataSource() {
        // Empty constructor
    }

    public void open() {
        realm = Realm.getDefaultInstance();
    }

    public void close() {
        realm.close();
    }

    public ArrayList<Movie> getAllFavMovies() {
        RealmResults<Movie> movies = realm.where(Movie.class).findAll();
        ArrayList<Movie> movieArrayList = new ArrayList<>();
        movieArrayList.addAll(realm.copyFromRealm(movies));
        return movieArrayList;
    }

    public Movie findMovieWithId(int id) {
        return realm.where(Movie.class)
                .equalTo("id", id)
                .findFirst();
    }

    public void addMovieToFavs(final Movie movie) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(movie));
    }

    public void deleteMovieFromFavs(final Movie movie) {
        realm.executeTransaction(realm -> movie.deleteFromRealm());
    }

}
