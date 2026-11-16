package com.nikkssy.library_management.dto.book;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookResponse(

        Long id,
        String isbn,
        String title,
        String author,

        Long genreId,
        String genreName,

        String publisher,
        LocalDate publishedDate,
        String language,
        Integer pages,
        String description,
        Integer totalCopies,
        Integer availableCopies,
        BigDecimal price,
        String coverImageUrl,
        Boolean active,

        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
