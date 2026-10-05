package com.imdbee.movie;

import com.imdbee.movie.MovieDtos.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movies;

    public MovieController(MovieService movies) {
        this.movies = movies;
    }

    @GetMapping("/home")
    public HomeResponse home() {
        return movies.home();
    }

    @GetMapping("/search")
    public MoviePage search(@RequestParam(required = false) String query,
                            @RequestParam(required = false) Integer genre,
                            @RequestParam(required = false) Double minRating,
                            @RequestParam(defaultValue = "1") int page) {
        return movies.search(query, genre, minRating, page);
    }

    @GetMapping("/genres")
    public List<Genre> genres() {
        return movies.genres();
    }

    @GetMapping("/{id}")
    public MovieDetail details(@PathVariable long id) {
        return movies.details(id);
    }
}
