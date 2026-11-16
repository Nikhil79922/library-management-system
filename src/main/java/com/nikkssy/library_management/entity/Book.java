
package com.nikkssy.library_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "books",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_book_isbn",
                        columnNames = "isbn"
                )
        },
        indexes = {
                @Index(
                        name = "idx_book_title",
                        columnList = "title"
                ),
                @Index(
                        name = "idx_book_author",
                        columnList = "author"
                ),
                @Index(
                        name = "idx_book_genre_active",
                        columnList = "genre_id, active"
                ),
                @Index(
                        name = "idx_book_published_date",
                        columnList = "published_date"
                )
        }
)
@Check(
        name = "chk_book_available_copies",
        constraints = "available_copies <= total_copies"
)
@Check(
        name = "chk_book_total_copies",
        constraints = "total_copies >= 1"
)
@Check(
        name = "chk_book_available_non_negative",
        constraints = "available_copies >= 0"
)
@Check(
        name = "chk_book_pages",
        constraints = "pages >= 1"
)
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "ISBN is required")
    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    @Pattern(
            regexp = "^(?:[0-9]{9}[0-9X]|[0-9]{13})$",
            message = "ISBN must be a valid ISBN-10 or ISBN-13 format without hyphens"
    )
    @Column(nullable = false, length = 20)
    private String isbn;

    @NotBlank(message = "Book title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Author name is required")
    @Size(min = 2, max = 150, message = "Author name must be between 2 and 150 characters")
    @Column(nullable = false, length = 150)
    private String author;

    @NotNull(message = "Genre is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    @Size(max = 150, message = "Publisher must not exceed 150 characters")
    @Column(length = 150)
    private String publisher;

    @NotNull(message = "Published date is required")
    @PastOrPresent(message = "Published date cannot be in the future")
    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    @NotBlank(message = "Language is required")
    @Size(max = 50, message = "Language must not exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String language;

    @NotNull(message = "Number of pages is required")
    @Min(value = 1, message = "A book must have at least 1 page")
    @Max(value = 100000, message = "Number of pages cannot exceed 100000")
    @Column(nullable = false)
    private Integer pages;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    @Column(nullable = false, length = 2000)
    private String description;

    @NotNull(message = "Total copies are required")
    @Min(value = 1, message = "Total copies must be at least 1")
    @Column(name = "total_copies", nullable = false)
    private Integer totalCopies;

    @NotNull(message = "Available copies are required")
    @Min(value = 0, message = "Available copies cannot be negative")
    @Column(name = "available_copies", nullable = false)
    private Integer availableCopies;

    @NotNull(message = "Book price is required")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Price must be greater than 0"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Price must have up to 10 integer digits and 2 decimal places"
    )
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Size(max = 500, message = "Cover image URL must not exceed 500 characters")
    @Column(length = 500)
    private String coverImageUrl;

    @NotNull(message = "Active status is required")
    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @AssertTrue(message = "Available copies cannot exceed total copies")
    public boolean isAvailableCopiesValid() {
        if (totalCopies == null || availableCopies == null) {
            return true;
        }

        return availableCopies <= totalCopies;
    }
}
