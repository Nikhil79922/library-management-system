package com.nikkssy.library_management.repository;

import com.nikkssy.library_management.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>,
        JpaSpecificationExecutor<Book> {

    @EntityGraph(attributePaths = {"genre"})
    Optional<Book> findByIsbn(String isbn);

    @Query("""
        SELECT b.isbn
        FROM Book b
        WHERE b.isbn IN :isbns
        """)
    List<String> findExistingIsbns(
            @Param("isbns") Collection<String> isbns
    );

    boolean existsByIsbn(String isbn);


    @EntityGraph(attributePaths = {"genre"})
    Page<Book> findAll(Specification<Book> spec, Pageable pageable);

    long countByActiveTrue();

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    @Query("""
    SELECT COALESCE(SUM(b.availableCopies), 0)
    FROM Book b
    WHERE b.active = true
""")

    long sumAvailableCopiesForActiveBooks();

}
