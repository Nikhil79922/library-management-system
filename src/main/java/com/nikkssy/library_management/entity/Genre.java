
package com.nikkssy.library_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "genres",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_genre_code",
                        columnNames = "code"
                )
        },
        indexes = {
                @Index(name = "idx_genre_parent", columnList = "parent_genre_id"),
                @Index(name = "idx_genre_active", columnList = "active")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Genre code is required")
    @Size(max = 30, message = "Genre code cannot exceed 30 characters")
    @Pattern(
            regexp = "^[A-Z0-9_-]+$",
            message = "Genre code must contain only uppercase letters, numbers, hyphens or underscores"
    )
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @NotBlank(message = "Genre name is required")
    @Size(min = 2, max = 100, message = "Genre name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Display order is required")
    @Min(value = 0, message = "Display order cannot be negative")
    @Column(nullable = false)
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private boolean active = true;

    // Self-referencing ManyToOne relationship
    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "parent_genre_id")
    private Genre parentGenre;

    // Inverse side of the relationship
    @OneToMany(
            mappedBy = "parentGenre",
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE
    )
    private List<Genre> subGenres = new ArrayList<>();

    // Audit fields
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedDate;

    @PrePersist
    @PreUpdate
    private void normalizeFields() {
        if (code != null) {
            code = code.trim().toUpperCase();
        }
    }
}
