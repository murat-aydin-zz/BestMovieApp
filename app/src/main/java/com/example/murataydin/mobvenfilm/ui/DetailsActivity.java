package com.example.murataydin.mobvenfilm.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.murataydin.mobvenfilm.BuildConfig;
import com.example.murataydin.mobvenfilm.R;
import com.example.murataydin.mobvenfilm.adapters.MovieRecyclerViewAdapter;
import com.example.murataydin.mobvenfilm.data.RealmDataSource;
import com.example.murataydin.mobvenfilm.model.Crew;
import com.example.murataydin.mobvenfilm.model.Movie;
import com.example.murataydin.mobvenfilm.model.MovieRecyclerView;
import com.example.murataydin.mobvenfilm.model.TMDBCreditsResponse;
import com.example.murataydin.mobvenfilm.model.TMDBDetailsResponse;
import com.example.murataydin.mobvenfilm.network.RetrofitAPI;
import com.example.murataydin.mobvenfilm.utils.GlideApp;
import com.example.murataydin.mobvenfilm.utils.NetworkUtils;

import org.aviran.cookiebar2.CookieBar;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {

    private Context mContext;
    private byte[] imageBytes;
    Movie tempMovie, mMovie;

    @BindView(R.id.appbar) AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rating_value_tv)
    TextView mRatingTextView;
    @BindView(R.id.date_value_tv)
    TextView mDateTextView;
    @BindView(R.id.title_tv)
    TextView mTitleTextView;
    @BindView(R.id.plot_tv)
    TextView mPlotTextView;
    @BindView(R.id.poster_image_view)
    ImageView mPosterImageView;

    @BindView(R.id.fav_button) FloatingActionButton mFavoriteButton;
    @BindView(R.id.backdrop_iv)
    ImageView mBackdropImageView;
    @BindView(R.id.director_value_tv)
    TextView mDirectorTextView;
    @BindView(R.id.tagline_tv)
    TextView mTaglineTextView;
    @BindView(R.id.votes_value_tv)
    TextView mVotesTextView;
    @BindView(R.id.minutes_value_tv)
    TextView mMinutesTextView;

    private RealmDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        mContext = getApplicationContext();
        if (Build.VERSION.SDK_INT >= 21) {
            Slide slide = new Slide(Gravity.BOTTOM);
            getWindow().setEnterTransition(slide);
            postponeEnterTransition();
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mMovie = getIntent().getParcelableExtra("movie");
        collapsingToolbarLayout.setTitle(mMovie.getTitle());
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(android.R.color.white));
        appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0)
                mPosterImageView.setVisibility(View.GONE);
            else
                mPosterImageView.setVisibility(View.VISIBLE);
        });

        dataSource = new RealmDataSource();
        dataSource.open();

        mRatingTextView.setText(mMovie.getRating());
        if (mMovie.getDate() != null && !mMovie.getDate().equals(""))
            mDateTextView.setText(prettifyDate(mMovie.getDate()));
        mTitleTextView.setText(mMovie.getTitle());
        mPlotTextView.setText(mMovie.getPlot());
        favButtonInit(mMovie.getId());
        GlideApp.with(getApplicationContext())
                .load(RetrofitAPI.BACKDROP_BASE_URL + mMovie.getBackdropPath())
                .centerCrop()
                .placeholder(R.drawable.tmdb_placeholder_land)
                .error(R.drawable.tmdb_placeholder_land)
                .fallback(R.drawable.tmdb_placeholder_land)
                .transition(new DrawableTransitionOptions().crossFade())
                .into(mBackdropImageView);
        if (mMovie.getPosterBytes() != null) {
            GlideApp.with(getApplicationContext())
                    .load(mMovie.getPosterBytes())
                    .centerCrop()
                    .error(R.drawable.tmdb_placeholder)
                    .fallback(R.drawable.tmdb_placeholder)
                    .transition(new DrawableTransitionOptions().crossFade())
                    .into(mPosterImageView);
        } else {
            GlideApp.with(mContext)
                    .load(RetrofitAPI.POSTER_BASE_URL + mMovie.getPosterPath())
                    .error(R.drawable.tmdb_placeholder)
                    .fallback(R.drawable.tmdb_placeholder)
                    .centerCrop()
                    .transition(new DrawableTransitionOptions().crossFade())
                    .into(mPosterImageView);
        }

        if(!NetworkUtils.hasNetwork(mContext)) {
            (findViewById(R.id.tagline_tv)).setVisibility(View.GONE);
            (findViewById(R.id.votes_label_tv)).setVisibility(View.GONE);
            (findViewById(R.id.votes_value_tv)).setVisibility(View.GONE);
            (findViewById(R.id.minutes_label_tv)).setVisibility(View.GONE);
            (findViewById(R.id.minutes_value_tv)).setVisibility(View.GONE);
            (findViewById(R.id.director_label_tv)).setVisibility(View.GONE);
            (findViewById(R.id.director_value_tv)).setVisibility(View.GONE);
        } else {
            fetchCredits();
            fetchMoreDetails();



        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startPostponedEnterTransition();
        }
    }

    private void fetchCredits() {

        RetrofitAPI retrofitAPI = NetworkUtils.getCacheEnabledRetrofit(getApplicationContext()).create(RetrofitAPI.class);
        final Call<TMDBCreditsResponse> creditsCall = retrofitAPI.getCredits(mMovie.getId(), BuildConfig.TMDB_API_TOKEN);
        creditsCall.enqueue(new Callback<TMDBCreditsResponse>() {
            @Override
            public void onResponse(Call<TMDBCreditsResponse> call, Response<TMDBCreditsResponse> response) {
                TMDBCreditsResponse creditsResponse = response.body();

                // Get director info
                if (creditsResponse != null) {
                    for (Crew crew : creditsResponse.getCrew()) {
                        if (crew.getJob().equals("Director")) {
                            mDirectorTextView.setText(crew.getName());
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<TMDBCreditsResponse> call, Throwable t) {
            }
        });
    }




    private void fetchMoreDetails() {
        RetrofitAPI retrofitAPI = NetworkUtils.getCacheEnabledRetrofit(getApplicationContext()).create(RetrofitAPI.class);
        Call<TMDBDetailsResponse> detailsResponseCall = retrofitAPI.getDetails(mMovie.getId(), BuildConfig.TMDB_API_TOKEN, "en-US");
        detailsResponseCall.enqueue(new Callback<TMDBDetailsResponse>() {
            @Override
            public void onResponse(Call<TMDBDetailsResponse> call, Response<TMDBDetailsResponse> response) {
                final TMDBDetailsResponse tmdbDetailsResponse = response.body();
                String tagline = null;
                if (tmdbDetailsResponse != null) {
                    tagline = tmdbDetailsResponse.getTagline();
                }
                if (tagline != null && !tagline.equals("")) {
                    mTaglineTextView.setText(tagline);
                } else {
                    mTaglineTextView.setVisibility(View.GONE);
                }
                mVotesTextView.setText(String.valueOf(tmdbDetailsResponse.getVoteCount()));
                mMinutesTextView.setText(String.valueOf(tmdbDetailsResponse.getRuntime()));

            }

            @Override
            public void onFailure(Call<TMDBDetailsResponse> call, Throwable t) {
                // Why bother doing anything here
            }
        });
    }


    private String prettifyDate(String jsonDate) {
        DateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sourceDateFormat.parse(jsonDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat destDateFormat = new SimpleDateFormat("MMM dd\nyyyy");
        String dateStr = destDateFormat.format(date);
        return dateStr;
    }

    private void favButtonInit(final int id) {
        Movie checkedMovie = dataSource.findMovieWithId(id);
        if (checkedMovie == null)
            mFavoriteButton.setImageResource(R.drawable.ic_favorite_border);
        else
            mFavoriteButton.setImageResource(R.drawable.ic_favorite_black_24dp);
        mFavoriteButton.setOnClickListener(view -> {
            Movie transactedMovie = dataSource.findMovieWithId(id);
            if (transactedMovie == null) {
                tempMovie = mMovie;
                GlideApp.with(mContext)
                        .load(tempMovie.getPosterPath())
                        .centerCrop()
                        .into(new Target<Drawable>() {
                            @Override
                            public void onLoadStarted(@Nullable Drawable placeholder) {

                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {

                            }

                            @Override
                            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                imageBytes = stream.toByteArray();
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }

                            @Override
                            public void getSize(SizeReadyCallback cb) {

                            }

                            @Override
                            public void removeCallback(SizeReadyCallback cb) {

                            }

                            @Override
                            public void setRequest(@Nullable Request request) {

                            }

                            @Nullable
                            @Override
                            public Request getRequest() {
                                return null;
                            }

                            @Override
                            public void onStart() {

                            }

                            @Override
                            public void onStop() {

                            }

                            @Override
                            public void onDestroy() {

                            }
                        });
                tempMovie.setPosterBytes(imageBytes);
                dataSource.addMovieToFavs(tempMovie);
                mFavoriteButton.setImageResource(R.drawable.ic_favorite_border);
                CookieBar.build(DetailsActivity.this)
                        .setBackgroundColor(android.R.color.holo_blue_dark)
                        .setTitle(getString(R.string.fav_add))
                        .setMessage(getString(R.string.fav_msg))
                        .show();
            } else {
                CookieBar.build(DetailsActivity.this)
                        .setBackgroundColor(android.R.color.holo_red_dark)
                        .setTitle(getString(R.string.fav_rmv))
                        .setMessage(getString(R.string.fav_rmv_msg))
                        .show();
                dataSource.deleteMovieFromFavs(transactedMovie);
                mFavoriteButton.setImageResource(R.drawable.ic_favorite_border);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSource.close();
    }

}
