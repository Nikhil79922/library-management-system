
package com.nikkssy.library_management.specification;

import com.nikkssy.library_management.dto.book.BookSearchSpecification;
import com.nikkssy.library_management.entity.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BookSpecification {

    public Specification<Book> getBookSpecification(
            BookSearchSpecification filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Global search: title OR author OR ISBN OR publisher
            if (filter.searchTerm() != null &&
                    !filter.searchTerm().isBlank()) {

                String searchTerm =
                        "%" + filter.searchTerm().trim().toLowerCase() + "%";

                Predicate title = cb.like(
                        cb.lower(root.get("title")),
                        searchTerm
                );

                Predicate author = cb.like(
                        cb.lower(root.get("author")),
                        searchTerm
                );

                Predicate isbn = cb.like(
                        cb.lower(root.get("isbn")),
                        searchTerm
                );

                Predicate publisher = cb.like(
                        cb.lower(root.get("publisher")),
                        searchTerm
                );

                predicates.add(
                        cb.or(title, author, isbn, publisher)
                );
            }

            // Exact ISBN
            if (filter.isbn() != null && !filter.isbn().isBlank()) {
                predicates.add(
                        cb.equal(
                                root.get("isbn"),
                                filter.isbn().trim()
                        )
                );
            }

            // Title
            if (filter.title() != null && !filter.title().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("title")),
                                "%" + filter.title().trim().toLowerCase() + "%"
                        )
                );
            }

            // Author
            if (filter.author() != null && !filter.author().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("author")),
                                "%" + filter.author().trim().toLowerCase() + "%"
                        )
                );
            }

            // Genre
            if (filter.genreId() != null) {
                predicates.add(
                        cb.equal(
                                root.get("genre").get("id"),
                                filter.genreId()
                        )
                );
            }

            // Publisher
            if (filter.publisher() != null && !filter.publisher().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("publisher")),
                                "%" + filter.publisher().trim().toLowerCase() + "%"
                        )
                );
            }

            // Language
            if (filter.language() != null && !filter.language().isBlank()) {
                predicates.add(
                        cb.equal(
                                cb.lower(root.get("language")),
                                filter.language().trim().toLowerCase()
                        )
                );
            }

            // Minimum price
            if (filter.minPrice() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("price"),
                                filter.minPrice()
                        )
                );
            }

            // Maximum price
            if (filter.maxPrice() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("price"),
                                filter.maxPrice()
                        )
                );
            }

            // Published after
            if (filter.publishedAfter() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("publishedDate"),
                                filter.publishedAfter()
                        )
                );
            }

            // Published before
            if (filter.publishedBefore() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("publishedDate"),
                                filter.publishedBefore()
                        )
                );
            }

            // Minimum pages
            if (filter.minPages() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("pages"),
                                filter.minPages()
                        )
                );
            }

            // Maximum pages
            if (filter.maxPages() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("pages"),
                                filter.maxPages()
                        )
                );
            }

            // Active status
            if (filter.active() != null) {
                predicates.add(
                        cb.equal(
                                root.get("active"),
                                filter.active()
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
