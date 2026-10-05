package com.imdbee.review;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"user", "movie"})
    List<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId);

    @EntityGraph(attributePaths = {"user", "movie"})
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "movie"})
    Optional<Review> findWithUserAndMovieById(Long id);

    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    long countByMovieId(Long movieId);

    @Query("select avg(r.rating) from Review r where r.movie.id = :movieId")
    Double averageRatingForMovie(Long movieId);
}
