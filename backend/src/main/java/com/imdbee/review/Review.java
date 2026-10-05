package com.imdbee.review;

import com.imdbee.movie.MovieData;
import com.imdbee.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "review", uniqueConstraints = @UniqueConstraint(
        name = "uk_review_user_movie", columnNames = {"user_id", "movie_id"}))
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id")
    private MovieData movie;

    /** 1 to 5 stars. */
    @Column(nullable = false)
    private int rating;

    @Column(length = 2000)
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    protected Review() {
    }

    public Review(User user, MovieData movie, int rating, String content) {
        this.user = user;
        this.movie = movie;
        this.rating = rating;
        this.content = content;
    }

    public void update(int rating, String content) {
        this.rating = rating;
        this.content = content;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public MovieData getMovie() { return movie; }
    public int getRating() { return rating; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
