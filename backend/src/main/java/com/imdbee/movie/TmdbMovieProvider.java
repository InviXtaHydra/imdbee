package com.imdbee.movie;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.imdbee.common.ApiException;
import com.imdbee.movie.MovieDtos.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/** Calls The Movie Database (TMDB) v3 API. The key never leaves the backend. */
public class TmdbMovieProvider implements MovieProvider {

    private static final Logger log = LoggerFactory.getLogger(TmdbMovieProvider.class);
    private static final String IMG = "https://image.tmdb.org/t/p/";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final RestClient http;
    private final String apiKey;
    private final boolean bearer;
    private final String language;
    private final Map<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();

    private record CacheEntry<T>(T value, Instant expires) {
    }

    public TmdbMovieProvider(String baseUrl, String apiKey, String language) {
        this.apiKey = apiKey;
        // v4 "API Read Access Token" is a JWT and goes in a header; a v3 key goes in the query string
        this.bearer = apiKey.startsWith("eyJ");
        this.language = language;
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);
        if (bearer) {
            builder.defaultHeader("Authorization", "Bearer " + apiKey);
        }
        this.http = builder.build();
    }

    // --- TMDB response shapes ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbMovie(long id, String title, String overview,
                     @JsonProperty("poster_path") String posterPath,
                     @JsonProperty("backdrop_path") String backdropPath,
                     @JsonProperty("release_date") String releaseDate,
                     @JsonProperty("vote_average") Double voteAverage,
                     @JsonProperty("genre_ids") List<Integer> genreIds) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbPage(int page, @JsonProperty("total_pages") int totalPages,
                    @JsonProperty("total_results") long totalResults, List<TmdbMovie> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbGenres(List<Genre> genres) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbCast(String name, String character, @JsonProperty("profile_path") String profilePath) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbCredits(List<TmdbCast> cast) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbVideo(String key, String site, String type, boolean official,
                     @JsonProperty("iso_639_1") String language) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbVideos(List<TmdbVideo> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TmdbDetail(long id, String title, String tagline, String overview,
                      @JsonProperty("poster_path") String posterPath,
                      @JsonProperty("backdrop_path") String backdropPath,
                      @JsonProperty("release_date") String releaseDate,
                      Integer runtime,
                      @JsonProperty("vote_average") Double voteAverage,
                      List<Genre> genres, TmdbCredits credits, TmdbVideos videos) {
    }

    // --- MovieProvider ---

    @Override
    public List<MovieRow> homeRows() {
        return cached("home", () -> List.of(
                row("trending", "Trending deze week", "/trending/movie/week", Map.of()),
                row("popular", "Populair op IMDBee", "/movie/popular", Map.of()),
                row("now_playing", "Nu in de bioscoop", "/movie/now_playing", Map.of()),
                row("top_rated", "Best beoordeeld", "/movie/top_rated", Map.of()),
                row("scifi", "Sciencefiction", "/discover/movie", Map.of("with_genres", "878", "sort_by", "popularity.desc")),
                row("animation", "Animatie", "/discover/movie", Map.of("with_genres", "16", "sort_by", "popularity.desc"))));
    }

    @Override
    public MoviePage search(String query, Integer genreId, Double minRating, int page) {
        boolean hasQuery = query != null && !query.isBlank();
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if (!hasQuery) {
            // Discover supports genre and rating filters natively
            params.put("sort_by", "popularity.desc");
            if (genreId != null) params.put("with_genres", genreId.toString());
            if (minRating != null) {
                params.put("vote_average.gte", minRating.toString());
                params.put("vote_count.gte", "100"); // skip obscure titles with 1 perfect vote
            }
            return toPage(get("/discover/movie", params, TmdbPage.class));
        }

        // Search by title; TMDB search has no genre/rating filters, so apply them to the results
        params.put("query", query.trim());
        MoviePage p = toPage(get("/search/movie", params, TmdbPage.class));
        List<MovieSummary> filtered = p.results().stream()
                .filter(m -> genreId == null || m.genreIds().contains(genreId))
                .filter(m -> minRating == null || (m.voteAverage() != null && m.voteAverage() >= minRating))
                .toList();
        // The total is unknown after filtering a page client-side; -1 tells the UI not to show a count
        boolean filteredOut = genreId != null || minRating != null;
        return new MoviePage(p.page(), p.totalPages(), filteredOut ? -1 : p.totalResults(), filtered);
    }

    @Override
    public List<Genre> genres() {
        return cached("genres", () -> get("/genre/movie/list", Map.of(), TmdbGenres.class).genres());
    }

    @Override
    public Optional<MovieDetail> details(long id) {
        try {
            // Trailers are often only in English, so include those next to the UI language
            TmdbDetail d = get("/movie/" + id, Map.of(
                    "append_to_response", "credits,videos",
                    "include_video_language", language.substring(0, 2) + ",en,null"), TmdbDetail.class);
            List<CastMember> cast = d.credits() == null || d.credits().cast() == null ? List.of()
                    : d.credits().cast().stream().limit(12)
                    .map(c -> new CastMember(c.name(), c.character(), img("w185", c.profilePath())))
                    .toList();
            return Optional.of(new MovieDetail(d.id(), d.title(), blankToNull(d.tagline()), d.overview(),
                    img("w500", d.posterPath()), img("w1280", d.backdropPath()), date(d.releaseDate()),
                    d.runtime(), d.voteAverage(), d.genres() == null ? List.of() : d.genres(), cast, bestTrailer(d.videos()), null));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isDemo() {
        return false;
    }

    // --- helpers ---

    /** Prefers an official trailer in the UI language, then English, then any trailer or teaser on YouTube. */
    private String bestTrailer(TmdbVideos videos) {
        if (videos == null || videos.results() == null) return null;
        String lang = language.substring(0, 2);
        return videos.results().stream()
                .filter(v -> "YouTube".equals(v.site()) && v.key() != null)
                .filter(v -> "Trailer".equals(v.type()) || "Teaser".equals(v.type()))
                .min(Comparator
                        .comparing((TmdbVideo v) -> !"Trailer".equals(v.type()))
                        .thenComparing(v -> !lang.equals(v.language()))
                        .thenComparing(v -> !v.official()))
                .map(TmdbVideo::key)
                .orElse(null);
    }

    private MovieRow row(String key, String title, String path, Map<String, String> params) {
        return new MovieRow(key, title, toPage(get(path, params, TmdbPage.class)).results());
    }

    private <T> T get(String path, Map<String, String> params, Class<T> type) {
        Function<UriBuilder, URI> uri = b -> {
            b.path(path).queryParam("language", language);
            if (!bearer) b.queryParam("api_key", apiKey);
            params.forEach(b::queryParam);
            return b.build();
        };
        try {
            return http.get().uri(uri).retrieve().body(type);
        } catch (HttpClientErrorException.NotFound e) {
            throw e;
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("TMDB rejected the API key. Check TMDB_API_KEY.");
            throw new ApiException(HttpStatus.BAD_GATEWAY, "De filmdienst weigert de API-sleutel. Controleer TMDB_API_KEY.");
        } catch (RestClientException e) {
            log.warn("TMDB request {} failed: {}", path, e.getMessage());
            throw new ApiException(HttpStatus.BAD_GATEWAY, "De filmdienst is nu niet bereikbaar. Probeer het later opnieuw.");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T cached(String key, Supplier<T> loader) {
        CacheEntry<?> e = cache.get(key);
        if (e != null && e.expires().isAfter(Instant.now())) return (T) e.value();
        T value = loader.get();
        cache.put(key, new CacheEntry<>(value, Instant.now().plus(CACHE_TTL)));
        return value;
    }

    private MoviePage toPage(TmdbPage p) {
        // Skip movies without a poster: they would show up as empty cards
        List<MovieSummary> results = p.results() == null ? List.of() : p.results().stream()
                .filter(m -> m.posterPath() != null && !m.posterPath().isBlank())
                .map(this::toSummary)
                .toList();
        // TMDB caps paging at 500 pages
        return new MoviePage(p.page(), Math.min(p.totalPages(), 500), p.totalResults(), results);
    }

    private MovieSummary toSummary(TmdbMovie m) {
        return new MovieSummary(m.id(), m.title(), m.overview(), img("w500", m.posterPath()),
                img("w1280", m.backdropPath()), date(m.releaseDate()), m.voteAverage(),
                m.genreIds() == null ? List.of() : m.genreIds());
    }

    private static String img(String size, String path) {
        return path == null || path.isBlank() ? null : IMG + size + path;
    }

    private static LocalDate date(String s) {
        try {
            return s == null || s.isBlank() ? null : LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
