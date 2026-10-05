package com.imdbee.movie;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieDataRepository extends JpaRepository<MovieData, Long> {
}
