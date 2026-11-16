package com.nikkssy.library_management.repository;

import com.nikkssy.library_management.dto.commonRes.PageResponse;
import com.nikkssy.library_management.dto.genre.GenreResponse;
import com.nikkssy.library_management.entity.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    // Fetch active genres along with their subgenres
    @EntityGraph(attributePaths = "subGenres")
    Page<Genre> findAllByActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = "subGenres")
    Page<Genre> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"subGenres", "parentGenre"})
    Optional<Genre> findById(Long id);

    // Fetch active genres ordered by display order
    @EntityGraph(attributePaths = "subGenres")
    Page<Genre> findAllByActiveTrueOrderByDisplayOrderAsc(Pageable pageable);

    // Search by name or code
    @EntityGraph(attributePaths = "subGenres")
    Page<Genre> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name,
            String code,
            Pageable pageable
    );

    // Count active genres
    long countByActiveTrue();
}