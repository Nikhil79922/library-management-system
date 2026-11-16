package com.nikkssy.library_management.controllers;
import com.nikkssy.library_management.dto.commonRes.PageResponse;
import com.nikkssy.library_management.dto.commonRes.SuccessResponse;
import com.nikkssy.library_management.dto.genre.GenreRequest;
import com.nikkssy.library_management.dto.genre.GenreResponse;
import com.nikkssy.library_management.services.impl.GenreServicesImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreServicesImpl genreService;

    // Create genre
    @PostMapping
    public ResponseEntity<SuccessResponse<GenreResponse>> createGenre(
            @Valid @RequestBody GenreRequest genreRequest) {

        GenreResponse resData = genreService.createGenre(genreRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SuccessResponse<>(
                        true,
                        "Genre created successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.CREATED.value()
                )
        );
    }

    // Get all genres with pagination
    @GetMapping
    public ResponseEntity<SuccessResponse<PageResponse<GenreResponse>>> getAllGenres(
            @PageableDefault(
                    size = 10,
                    sort = "displayOrder",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        PageResponse<GenreResponse> resData = genreService.getAllGenres(pageable);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Genres retrieved successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // Get genre by ID
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<GenreResponse>> getGenreById(
            @PathVariable Long id) {

        GenreResponse resData = genreService.getGenreById(id);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Genre retrieved successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // Update genre
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<GenreResponse>> updateGenre(
            @PathVariable Long id,
            @Valid @RequestBody GenreRequest genreRequest) {

        GenreResponse resData = genreService.updateGenre(id, genreRequest);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Genre updated successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // Soft delete genre
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteGenre(
            @PathVariable Long id) {

        genreService.deleteGenre(id);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Genre deactivated successfully",
                        null,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // Permanently delete genre
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<SuccessResponse<Void>> hardDeleteGenre(
            @PathVariable Long id) {

        genreService.hardDeleteGenre(id);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Genre permanently deleted successfully",
                        null,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // Get active genres with subgenres
    @GetMapping("/active")
    public ResponseEntity<SuccessResponse<PageResponse<GenreResponse>>>
    getAllActiveGenresWithSubGenres(
            @PageableDefault(
                    size = 10,
                    sort = "displayOrder",
                    direction = Sort.Direction.ASC
            ) Pageable pageable
    ) {

        PageResponse<GenreResponse> resData =
                genreService.getAllActiveGenresWithSubGenres(pageable);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Active genres retrieved successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // Get genres ordered by display order
    @GetMapping("/top-order")
    public ResponseEntity<SuccessResponse<PageResponse<GenreResponse>>>
    getTopOrderGenres(
            @PageableDefault(
                    size = 10,
                    sort = "displayOrder",
                    direction = Sort.Direction.ASC
            ) Pageable pageable
    ) {

        PageResponse<GenreResponse> resData = genreService.getTopOrderGenres(pageable);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Top ordered genres retrieved successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // Search genres
    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<PageResponse<GenreResponse>>> searchGenres(
            @RequestParam String searchTerm,
            @PageableDefault(size = 10) Pageable pageable) {

        PageResponse<GenreResponse> resData =
                genreService.searchGenres(searchTerm, pageable);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Genres search completed successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // Get total active genres
    @GetMapping("/count/active")
    public ResponseEntity<SuccessResponse<Long>> getTotalActiveGenres() {

        long resData = genreService.getTotalActiveGenres();

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Active genre count retrieved successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }
}
