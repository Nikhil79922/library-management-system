package com.nikkssy.library_management.services.impl;

import com.nikkssy.library_management.dto.commonRes.PageResponse;
import com.nikkssy.library_management.dto.genre.GenreRequest;
import com.nikkssy.library_management.dto.genre.GenreResponse;
import com.nikkssy.library_management.entity.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GenreServicesImpl {

    GenreResponse createGenre(GenreRequest genre);

    PageResponse<GenreResponse> getAllGenres(Pageable pageable);

    GenreResponse getGenreById(Long id);

    GenreResponse updateGenre( Long id , GenreRequest genre);

    void deleteGenre(Long id);

    void  hardDeleteGenre(Long id);

    PageResponse<GenreResponse> getAllActiveGenresWithSubGenres(Pageable pageable);

    PageResponse<GenreResponse> getTopOrderGenres(Pageable pageable);

    PageResponse<GenreResponse> searchGenres(String searchTerm, Pageable pageable);

    long getTotalActiveGenres();

//    long getBookCountByGenreId(Long id);


}
