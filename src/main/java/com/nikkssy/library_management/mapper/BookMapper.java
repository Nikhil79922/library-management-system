
package com.nikkssy.library_management.mapper;

import com.nikkssy.library_management.dto.book.BookResponse;
import com.nikkssy.library_management.dto.book.CreateBookRequest;
import com.nikkssy.library_management.dto.book.UpdateBookRequest;
import com.nikkssy.library_management.entity.Book;
import com.nikkssy.library_management.entity.Genre;
import com.nikkssy.library_management.exception.NotFoundException;
import com.nikkssy.library_management.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookMapper {

    private final GenreRepository genreRepository;

    public Book toEntity(CreateBookRequest request) {

        Genre genreDetails = genreRepository.findById(request.genreId())
                .orElseThrow(() ->
                        new NotFoundException("Genre not found with id: " + request.genreId())
                );

        Book book = new Book();

        book.setIsbn(request.isbn());
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setGenre(genreDetails);
        book.setPublisher(request.publisher());
        book.setPublishedDate(request.publishedDate());
        book.setLanguage(request.language());
        book.setPages(request.pages());
        book.setDescription(request.description());
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.availableCopies());
        book.setPrice(request.price());
        book.setCoverImageUrl(request.coverImageUrl());
        book.setActive(true);

        return book;
    }

    public void updateEntity(
            Book book,
            UpdateBookRequest request
    ) {

        if (request.isbn() != null)
            book.setIsbn(request.isbn());

        if (request.title() != null)
            book.setTitle(request.title());

        if (request.author() != null)
            book.setAuthor(request.author());

        if (request.genreId() != null){
            Genre genreDetails = genreRepository.findById(request.genreId())
                    .orElseThrow(() ->
                            new NotFoundException("Genre not found with id: " + request.genreId())
                    );
            book.setGenre(genreDetails);
        }

        if (request.publisher() != null)
            book.setPublisher(request.publisher());

        if (request.publishedDate() != null)
            book.setPublishedDate(request.publishedDate());

        if (request.language() != null)
            book.setLanguage(request.language());

        if (request.pages() != null)
            book.setPages(request.pages());

        if (request.description() != null)
            book.setDescription(request.description());

        if (request.totalCopies() != null)
            book.setTotalCopies(request.totalCopies());

        if (request.availableCopies() != null)
            book.setAvailableCopies(request.availableCopies());

        if (request.price() != null)
            book.setPrice(request.price());

        if (request.coverImageUrl() != null)
            book.setCoverImageUrl(request.coverImageUrl());

        if (request.active() != null)
            book.setActive(request.active());
    }

    public BookResponse toResponse(Book book) {

        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),

                book.getGenre().getId(),
                book.getGenre().getName(),

                book.getPublisher(),
                book.getPublishedDate(),
                book.getLanguage(),
                book.getPages(),
                book.getDescription(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getPrice(),
                book.getCoverImageUrl(),
                book.getActive(),

                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}
