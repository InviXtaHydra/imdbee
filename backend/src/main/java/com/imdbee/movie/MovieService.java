package com.imdbee.movie;

import com.imdbee.common.ApiException;
import com.imdbee.movie.MovieDtos.*;
import com.imdbee.review.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private static final Logger log = LoggerFactory.getLogger(MovieService.class);

    private final MovieProvider provider;
    private final MovieDataRepository movieRepo;
    private final ReviewRepository reviewRepo;

    public MovieService(@Value("${app.tmdb.api-key}") String apiKey,
                        @Value("${app.tmdb.base-url}") String baseUrl,
                        @Value("${app.tmdb.language}") String language,
                        MovieDataRepository movieRepo,
                        ReviewRepository reviewRepo) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("No TMDB_API_KEY set: running with the built-in demo catalog.");
            this.provider = new DemoMovieProvider();
        } else {
            this.provider = new TmdbMovieProvider(baseUrl, apiKey.trim(), language);
        }
        this.movieRepo = movieRepo;
        this.reviewRepo = reviewRepo;
    }

    public HomeResponse home() {
        List<MovieRow> rows = provider.homeRows();
        // Hero: the first trending movie that has a backdrop (any movie in demo mode)
        MovieSummary hero = rows.isEmpty() ? null : rows.getFirst().movies().stream()
                .filter(m -> provider.isDemo() || m.backdropUrl() != null)
                .findFirst().orElse(null);
        return new HomeResponse(hero, rows, provider.isDemo());
    }

    public MoviePage search(String query, Integer genreId, Double minRating, int page) {
        return provider.search(query, genreId, minRating, Math.max(1, Math.min(page, 500)));
    }

    public List<Genre> genres() {
        return provider.genres();
    }

    @Transactional
    public MovieDetail details(long id) {
        MovieDetail detail = provider.details(id)
                .orElseThrow(() -> ApiException.notFound("Deze film bestaat niet (meer)."));
        cache(detail);
        Double avg = reviewRepo.averageRatingForMovie(id);
        long count = reviewRepo.countByMovieId(id);
        return detail.withCommunity(new CommunityRating(avg == null ? null : Math.round(avg * 10) / 10.0, count));
    }

    /** Returns the cached movie, fetching and storing it first if needed. Used by reviews. */
    @Transactional
    public MovieData getOrFetch(long id) {
        return movieRepo.findById(id).orElseGet(() -> cache(provider.details(id)
                .orElseThrow(() -> ApiException.notFound("Deze film bestaat niet (meer)."))));
    }

    private MovieData cache(MovieDetail d) {
        MovieData m = movieRepo.findById(d.id()).orElseGet(() -> new MovieData(d.id(), d.title()));
        m.setTitle(d.title());
        m.setOverview(d.overview());
        m.setPosterPath(d.posterUrl());
        m.setBackdropPath(d.backdropUrl());
        m.setReleaseDate(d.releaseDate());
        m.setVoteAverage(d.voteAverage());
        m.setGenres(d.genres().stream().map(Genre::name).collect(Collectors.joining(", ")));
        m.setCachedAt(Instant.now());
        return movieRepo.save(m);
    }
}
