package com.imdbee.movie;

import java.time.LocalDate;
import java.util.List;

/** API shapes sent to the frontend. Image fields are full URLs (or null). */
public final class MovieDtos {

    private MovieDtos() {
    }

    public record Genre(int id, String name) {
    }

    public record MovieSummary(
            long id,
            String title,
            String overview,
            String posterUrl,
            String backdropUrl,
            LocalDate releaseDate,
            Double voteAverage,
            List<Integer> genreIds) {
    }

    public record CastMember(String name, String character, String profileUrl) {
    }

    public record MovieDetail(
            long id,
            String title,
            String tagline,
            String overview,
            String posterUrl,
            String backdropUrl,
            LocalDate releaseDate,
            Integer runtime,
            Double voteAverage,
            List<Genre> genres,
            List<CastMember> cast,
            /** YouTube video key of the best trailer, or null. */
            String trailerKey,
            CommunityRating community) {

        MovieDetail withCommunity(CommunityRating c) {
            return new MovieDetail(id, title, tagline, overview, posterUrl, backdropUrl, releaseDate,
                    runtime, voteAverage, genres, cast, trailerKey, c);
        }
    }

    /** Ratings from IMDBee users (1–5 stars), separate from TMDB's 0–10 score. */
    public record CommunityRating(Double average, long count) {
    }

    public record MovieRow(String key, String title, List<MovieSummary> movies) {
    }

    public record HomeResponse(MovieSummary hero, List<MovieRow> rows, boolean demoMode) {
    }

    public record MoviePage(int page, int totalPages, long totalResults, List<MovieSummary> results) {
    }
}
