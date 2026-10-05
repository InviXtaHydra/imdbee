package com.imdbee.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class ReviewDtos {

    private ReviewDtos() {
    }

    public record CreateReviewRequest(
            @NotNull(message = "Kies een film.") Long movieId,
            @NotNull(message = "Geef 1 tot 5 sterren.")
            @Min(value = 1, message = "Geef minstens 1 ster.")
            @Max(value = 5, message = "Geef hoogstens 5 sterren.") Integer rating,
            @Size(max = 2000, message = "Je review mag maximaal 2000 tekens lang zijn.") String content) {
    }

    public record UpdateReviewRequest(
            @NotNull(message = "Geef 1 tot 5 sterren.")
            @Min(value = 1, message = "Geef minstens 1 ster.")
            @Max(value = 5, message = "Geef hoogstens 5 sterren.") Integer rating,
            @Size(max = 2000, message = "Je review mag maximaal 2000 tekens lang zijn.") String content) {
    }

    public record ReviewDto(
            Long id,
            Long movieId,
            String movieTitle,
            String moviePosterUrl,
            Long userId,
            String username,
            int rating,
            String content,
            Instant createdAt,
            Instant updatedAt) {

        static ReviewDto from(Review r) {
            return new ReviewDto(r.getId(), r.getMovie().getId(), r.getMovie().getTitle(), r.getMovie().getPosterPath(),
                    r.getUser().getId(), r.getUser().getUsername(), r.getRating(), r.getContent(),
                    r.getCreatedAt(), r.getUpdatedAt());
        }
    }
}
