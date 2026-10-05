package com.imdbee.movie;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Local cache of a movie from TMDB. Reviews point to this entity, so a movie
 * is stored the first time someone reviews it or opens its detail page.
 */
@Entity
@Table(name = "movie_data")
public class MovieData {

    /** TMDB id, used as primary key so external and local ids are the same. */
    @Id
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 4000)
    private String overview;

    private String posterPath;
    private String backdropPath;
    private LocalDate releaseDate;
    private Double voteAverage;

    /** Comma-separated genre names, e.g. "Actie, Drama". */
    private String genres;

    @Column(nullable = false)
    private Instant cachedAt = Instant.now();

    protected MovieData() {
    }

    public MovieData(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }
    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }
    public String getBackdropPath() { return backdropPath; }
    public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public Double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }
    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }
    public Instant getCachedAt() { return cachedAt; }
    public void setCachedAt(Instant cachedAt) { this.cachedAt = cachedAt; }
}
