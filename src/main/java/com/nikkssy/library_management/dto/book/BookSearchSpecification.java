
package com.nikkssy.library_management.dto.book;

import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookSearchSpecification(

        String searchTerm,
        String isbn,
        String title,
        String author,
        Long genreId,
        String publisher,
        String language,

        BigDecimal minPrice,
        BigDecimal maxPrice,

        LocalDate publishedAfter,
        LocalDate publishedBefore,

        Integer minPages,
        Integer maxPages,

        Boolean active,

        Integer page,
        Integer size,

        String sortBy,
        Sort.Direction direction

) {
}
