
package com.nikkssy.library_management.dto.book;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateBookRequest(

        @NotBlank(message = "ISBN is required")
        @Size(max = 20, message = "ISBN must not exceed 20 characters")
        @Pattern(
                regexp = "^(?:[0-9]{9}[0-9X]|[0-9]{13})$",
                message = "ISBN must be a valid ISBN-10 or ISBN-13 format without hyphens"
        )
        String isbn,

        @NotBlank(message = "Book title is required")
        @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
        String title,

        @NotBlank(message = "Author name is required")
        @Size(min = 2, max = 150, message = "Author name must be between 2 and 150 characters")
        String author,

        @NotNull(message = "Genre ID is required")
        @Positive(message = "Genre ID must be positive")
        Long genreId,

        @Size(max = 150, message = "Publisher must not exceed 150 characters")
        String publisher,

        @NotNull(message = "Published date is required")
        @PastOrPresent(message = "Published date cannot be in the future")
        LocalDate publishedDate,

        @NotBlank(message = "Language is required")
        @Size(max = 50, message = "Language must not exceed 50 characters")
        String language,

        @NotNull(message = "Number of pages is required")
        @Min(value = 1, message = "A book must have at least 1 page")
        @Max(value = 100000, message = "Number of pages cannot exceed 100000")
        Integer pages,

        @NotBlank(message = "Description is required")
        @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
        String description,

        @NotNull(message = "Total copies are required")
        @Min(value = 1, message = "Total copies must be at least 1")
        Integer totalCopies,

        @NotNull(message = "Available copies are required")
        @Min(value = 0, message = "Available copies cannot be negative")
        Integer availableCopies,

        @NotNull(message = "Book price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price must have up to 10 integer digits and 2 decimal places")
        BigDecimal price,

        @Size(max = 500, message = "Cover image URL must not exceed 500 characters")
        String coverImageUrl

) {

    @AssertTrue(message = "Available copies cannot exceed total copies")
    public boolean isAvailableCopiesValid() {
        if (totalCopies == null || availableCopies == null) {
            return true;
        }
        return availableCopies <= totalCopies;
    }
}
