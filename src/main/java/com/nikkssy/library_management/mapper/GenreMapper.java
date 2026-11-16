
package com.nikkssy.library_management.mapper;

import com.nikkssy.library_management.dto.genre.GenreRequest;
import com.nikkssy.library_management.dto.genre.GenreResponse;
import com.nikkssy.library_management.dto.genre.SubGenreResponse;
import com.nikkssy.library_management.entity.Genre;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GenreMapper {

    // Request DTO -> Entity
    public Genre toEntity(GenreRequest request) {

        Genre genre = new Genre();

        genre.setCode(request.code());
        genre.setName(request.name());
        genre.setDescription(request.description());
        genre.setDisplayOrder(request.displayOrder());
        genre.setActive(request.active());

        return genre;
    }

    // Entity -> Response DTO
    public GenreResponse toResponse(Genre genre) {

        List<SubGenreResponse> subGenres = genre.getSubGenres()
                .stream()
                .map(subGenre -> new SubGenreResponse(
                        subGenre.getId(),
                        subGenre.getCode(),
                        subGenre.getName(),
                        subGenre.getDisplayOrder(),
                        subGenre.isActive()
                ))
                .toList();

        return new GenreResponse(
                genre.getId(),
                genre.getCode(),
                genre.getName(),
                genre.getDescription(),
                genre.getDisplayOrder(),
                genre.isActive(),
                genre.getParentGenre() != null
                        ? genre.getParentGenre().getId()
                        : null,
                subGenres,
                genre.getCreatedDate(),
                genre.getUpdatedDate()
        );
    }

    // Update existing entity
    public void updateEntity(
            GenreRequest request,
            Genre genre
    ) {

        genre.setCode(request.code());
        genre.setName(request.name());
        genre.setDescription(request.description());
        genre.setDisplayOrder(request.displayOrder());
        genre.setActive(request.active());

    }
}
