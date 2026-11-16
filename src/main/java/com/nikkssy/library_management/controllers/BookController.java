package com.nikkssy.library_management.controllers;

import com.nikkssy.library_management.dto.book.BookResponse;
import com.nikkssy.library_management.dto.book.BookSearchSpecification;
import com.nikkssy.library_management.dto.book.CreateBookRequest;
import com.nikkssy.library_management.dto.book.UpdateBookRequest;
import com.nikkssy.library_management.dto.commonRes.PageResponse;
import com.nikkssy.library_management.dto.commonRes.SuccessResponse;
import com.nikkssy.library_management.services.impl.BookServicesImpl;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Validated
public class BookController {

    private final BookServicesImpl bookService;

    // Create book
    @PostMapping
    public ResponseEntity<SuccessResponse<BookResponse>> createBook(
            @Valid @RequestBody CreateBookRequest request) {

        BookResponse resData = bookService.createBook(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SuccessResponse<>(
                        true,
                        "Book created successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.CREATED.value()
                )
        );
    }

    // Create books in bulk
    @PostMapping("/bulk")
    public ResponseEntity<SuccessResponse<List<BookResponse>>> createBooksInBulk(
            @RequestBody
            @NotEmpty(message = "Book list cannot be empty")
            @Size(max = 20, message = "You can create a maximum of 100 books at a time")
            List<@Valid CreateBookRequest> requests) {

        List<BookResponse> resData =
                bookService.createBooksInBulk(requests);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SuccessResponse<>(
                        true,
                        "Books created successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.CREATED.value()
                )
        );
    }

    // Get book by ID
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<BookResponse>> getBookById(
            @PathVariable Long id) {

        BookResponse resData = bookService.getBookById(id);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Book retrieved successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // Get book by ISBN
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<SuccessResponse<BookResponse>> getBookByISBN(
            @PathVariable String isbn) {

        BookResponse resData = bookService.getBookByISBN(isbn);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Book retrieved successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // Get total active books
    @GetMapping("/count/active")
    public ResponseEntity<SuccessResponse<Long>> getTotalActiveBooks() {

        long resData = bookService.getTotalActiveBooks();

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Active book count retrieved successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // Get total available copies
    @GetMapping("/count/available")
    public ResponseEntity<SuccessResponse<Long>> getTotalAvailableBooks() {

        long resData = bookService.getTotalAvailableBooks();

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Available book copies count retrieved successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // Update book
    @PatchMapping("/{id}")
    public ResponseEntity<SuccessResponse<BookResponse>> updateBookById(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequest request) {

        BookResponse resData =
                bookService.updateBookById(id, request);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Book updated successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // Soft delete book
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> softDeleteBookById(
            @PathVariable Long id) {

        bookService.softDeleteBookById(id);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Book deactivated successfully",
                        null,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // Permanently delete book
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<SuccessResponse<Void>> hardDeleteBookById(
            @PathVariable Long id) {

        bookService.hardDeleteBookById(id);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Book permanently deleted successfully",
                        null,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // Search books with filters, pagination and sorting
    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<PageResponse<BookResponse>>>
    searchBookWithFilters(
            @Valid @ModelAttribute BookSearchSpecification filter,
            @PageableDefault(
                    size = 10,
                    sort = "title",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        PageResponse<BookResponse> resData =
                bookService.searchBookWithFilters(filter, pageable);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Books search completed successfully",
                        resData,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }
}
