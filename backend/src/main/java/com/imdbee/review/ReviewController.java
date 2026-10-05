package com.imdbee.review;

import com.imdbee.review.ReviewDtos.*;
import com.imdbee.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviews;

    public ReviewController(ReviewService reviews) {
        this.reviews = reviews;
    }

    /** Public: all reviews for one movie. */
    @GetMapping("/movie/{movieId}")
    public List<ReviewDto> forMovie(@PathVariable long movieId) {
        return reviews.forMovie(movieId);
    }

    /** The logged-in user's own reviews. */
    @GetMapping("/me")
    public List<ReviewDto> mine(@AuthenticationPrincipal AuthUser me) {
        return reviews.forUser(me.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto create(@AuthenticationPrincipal AuthUser me, @Valid @RequestBody CreateReviewRequest request) {
        return reviews.create(me, request);
    }

    @PutMapping("/{id}")
    public ReviewDto update(@AuthenticationPrincipal AuthUser me, @PathVariable long id,
                            @Valid @RequestBody UpdateReviewRequest request) {
        return reviews.update(me, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser me, @PathVariable long id) {
        reviews.delete(me, id);
    }
}
