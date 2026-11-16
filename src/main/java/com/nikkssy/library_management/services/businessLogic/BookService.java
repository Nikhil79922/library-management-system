package com.nikkssy.library_management.services.businessLogic;

import com.nikkssy.library_management.dto.book.BookResponse;
import com.nikkssy.library_management.dto.book.BookSearchSpecification;
import com.nikkssy.library_management.dto.book.CreateBookRequest;
import com.nikkssy.library_management.dto.book.UpdateBookRequest;
import com.nikkssy.library_management.dto.commonRes.PageResponse;
import com.nikkssy.library_management.entity.Book;
import com.nikkssy.library_management.exception.NotFoundException;
import com.nikkssy.library_management.exception.ValidationException;
import com.nikkssy.library_management.mapper.BookMapper;
import com.nikkssy.library_management.repository.BookRepository;
import com.nikkssy.library_management.services.impl.BookServicesImpl;
import com.nikkssy.library_management.specification.BookSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookService implements BookServicesImpl {

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;
    private final BookSpecification bookSpecification;

    @Override
    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        if(bookRepository.existsByIsbn(request.isbn())){
            throw new ValidationException("Book already exists");
        }
        Book bookDetails = bookMapper.toEntity(request);
        Book savedBook = bookRepository.save(bookDetails);
        return bookMapper.toResponse(savedBook);
    }

    @Override
    @Transactional
    public List<BookResponse> createBooksInBulk(
            List<CreateBookRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            throw new ValidationException("Book list cannot be empty");
        }

        // Check duplicate ISBNs within the incoming request
        Set<String> isbnSet = new HashSet<>();

        for (CreateBookRequest request : requests) {
            if (!isbnSet.add(request.isbn())) {
                throw new ValidationException(
                        "Duplicate ISBN in request: " + request.isbn()
                );
            }
        }

        List<String> existingIsbns =
                bookRepository.findExistingIsbns(isbnSet);

        if (!existingIsbns.isEmpty()) {
            throw new ValidationException(
                    "Books already exist with ISBNs: " + existingIsbns
            );
        }

        List<Book> books = requests.stream()
                .map(bookMapper::toEntity)
                .toList();

        List<Book> savedBooks = bookRepository.saveAll(books);

        return savedBooks.stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Override
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(
                        ()-> new NotFoundException(
                                "Book not found with id: " + id
                        )
                );
        return bookMapper.toResponse(book);
    }

    @Override
    public BookResponse getBookByISBN(String ISBN) {
        Book book = bookRepository.findByIsbn(ISBN)
                .orElseThrow(
                        ()-> new NotFoundException(
                                "Book not found with ISBN: " + ISBN
                        )
                );
        return bookMapper.toResponse(book);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalActiveBooks() {
        return bookRepository.countByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalAvailableBooks() {
        return bookRepository.sumAvailableCopiesForActiveBooks();
    }

    @Override
    @Transactional
    public BookResponse updateBookById(Long id, UpdateBookRequest request) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Book not found with id: " + id
                        )
                );

        if (request.isbn() != null &&
                bookRepository.existsByIsbnAndIdNot(request.isbn(), id)) {
            throw new ValidationException("Book already exists with this ISBN");
        }

        bookMapper.updateEntity(book, request);

        if (book.getAvailableCopies() > book.getTotalCopies()) {
            throw new ValidationException(
                    "Available copies cannot exceed total copies"
            );
        }

        return bookMapper.toResponse(book);
    }

    @Override
    @Transactional
    public void softDeleteBookById(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Book not found with id: " + id
                        )
                );

        book.setActive(false);
    }

    @Override
    @Transactional
    public void hardDeleteBookById(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Book not found with id: " + id
                        )
                );

        bookRepository.delete(book);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookResponse> searchBookWithFilters(
            BookSearchSpecification bookSearchSpecification,
            Pageable pageRequest) {

        var specification = bookSpecification.getBookSpecification(
                bookSearchSpecification
        );

        Page<BookResponse> bookPage = bookRepository.findAll(
                specification,
                pageRequest
        ).map(bookMapper::toResponse);


        return PageResponse.from(bookPage);
    }

}
