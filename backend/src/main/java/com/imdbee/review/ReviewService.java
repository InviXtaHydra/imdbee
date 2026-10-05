package com.imdbee.review;

import com.imdbee.common.ApiException;
import com.imdbee.movie.MovieData;
import com.imdbee.movie.MovieService;
import com.imdbee.review.ReviewDtos.*;
import com.imdbee.security.AuthUser;
import com.imdbee.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviews;
    private final UserRepository users;
    private final MovieService movies;

    public ReviewService(ReviewRepository reviews, UserRepository users, MovieService movies) {
        this.reviews = reviews;
        this.users = users;
        this.movies = movies;
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> forMovie(long movieId) {
        return reviews.findByMovieIdOrderByCreatedAtDesc(movieId).stream().map(ReviewDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> forUser(long userId) {
        return reviews.findByUserIdOrderByCreatedAtDesc(userId).stream().map(ReviewDto::from).toList();
    }

    @Transactional
    public ReviewDto create(AuthUser me, CreateReviewRequest req) {
        if (reviews.existsByUserIdAndMovieId(me.id(), req.movieId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Je hebt deze film al beoordeeld. Bewerk je bestaande review.");
        }
        MovieData movie = movies.getOrFetch(req.movieId());
        var user = users.getReferenceById(me.id());
        Review saved = reviews.save(new Review(user, movie, req.rating(), clean(req.content())));
        return ReviewDto.from(reviews.findWithUserAndMovieById(saved.getId()).orElseThrow());
    }

    @Transactional
    public ReviewDto update(AuthUser me, long id, UpdateReviewRequest req) {
        Review review = ownedReview(me, id);
        review.update(req.rating(), clean(req.content()));
        return ReviewDto.from(review);
    }

    @Transactional
    public void delete(AuthUser me, long id) {
        reviews.delete(ownedReview(me, id));
    }

    /** Owners can edit their own reviews; admins can moderate any review. */
    private Review ownedReview(AuthUser me, long id) {
        Review review = reviews.findWithUserAndMovieById(id)
                .orElseThrow(() -> ApiException.notFound("Review niet gevonden."));
        boolean isOwner = review.getUser().getId().equals(me.id());
        if (!isOwner && !"ADMIN".equals(me.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Je kunt alleen je eigen reviews aanpassen.");
        }
        return review;
    }

    private static String clean(String content) {
        return content == null || content.isBlank() ? null : content.trim();
    }
}
