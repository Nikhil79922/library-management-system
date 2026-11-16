package com.nikkssy.library_management.services.impl;

import com.nikkssy.library_management.dto.book.BookResponse;
import com.nikkssy.library_management.dto.book.BookSearchSpecification;
import com.nikkssy.library_management.dto.book.CreateBookRequest;
import com.nikkssy.library_management.dto.book.UpdateBookRequest;
import com.nikkssy.library_management.dto.commonRes.PageResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BookServicesImpl {

    BookResponse createBook(CreateBookRequest request);

    List<BookResponse> createBooksInBulk(List<CreateBookRequest> requests);

    BookResponse getBookById(Long id);

    BookResponse getBookByISBN(String ISBN);

    long getTotalActiveBooks();

    long getTotalAvailableBooks() ;

    BookResponse updateBookById(Long id, UpdateBookRequest request);

    void  softDeleteBookById(Long id);

    void hardDeleteBookById(Long id);

    PageResponse<BookResponse> searchBookWithFilters(BookSearchSpecification bookSearchSpecification, Pageable pageRequest);

}
