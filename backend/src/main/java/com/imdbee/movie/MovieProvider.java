package com.imdbee.movie;

import com.imdbee.movie.MovieDtos.*;

import java.util.List;
import java.util.Optional;

/** Source of movie data: TMDB when an API key is configured, otherwise the demo catalog. */
public interface MovieProvider {

    List<MovieRow> homeRows();

    /** Search by title and/or filter by genre and minimum TMDB score (0–10). All filters optional. */
    MoviePage search(String query, Integer genreId, Double minRating, int page);

    List<Genre> genres();

    Optional<MovieDetail> details(long id);

    boolean isDemo();
}
