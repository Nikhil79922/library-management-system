package com.nikkssy.library_management.dto.genre;

import java.time.LocalDateTime;
import java.util.List;

public record GenreResponse(

        Long id,
        String code,
        String name,
        String description,
        Integer displayOrder,
        boolean active,

        Long parentGenreId,

        List<SubGenreResponse> subGenres,

        LocalDateTime createdDate,
        LocalDateTime updatedDate

) {}