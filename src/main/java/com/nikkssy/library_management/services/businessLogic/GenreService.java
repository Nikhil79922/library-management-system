package com.nikkssy.library_management.services.businessLogic;

import com.nikkssy.library_management.dto.commonRes.PageResponse;
import com.nikkssy.library_management.dto.genre.GenreRequest;
import com.nikkssy.library_management.dto.genre.GenreResponse;
import com.nikkssy.library_management.entity.Genre;
import com.nikkssy.library_management.exception.NotFoundException;
import com.nikkssy.library_management.mapper.GenreMapper;
import com.nikkssy.library_management.repository.GenreRepository;
import com.nikkssy.library_management.services.impl.GenreServicesImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenreService implements GenreServicesImpl {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    @Override
    @Transactional
    public GenreResponse createGenre(GenreRequest genre) {

      Genre genreEntity = genreMapper.toEntity(genre);
      if(genre.parentGenreId() != null){
          Genre parentGenreEntity =  genreRepository.findById(genre.parentGenreId())
                  .orElseThrow(() -> new NotFoundException("Parent Genre Not Found with id:" +  genre.parentGenreId()));

          genreEntity.setParentGenre(parentGenreEntity);
      }
      genreRepository.save(genreEntity);
      return genreMapper.toResponse(genreEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GenreResponse> getAllGenres(Pageable pageable) {

        Page<GenreResponse> page = genreRepository.findAll(pageable)
                .map(genreMapper::toResponse);

        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public GenreResponse getGenreById(Long id) {
        Genre genreDetails =  genreRepository.findById(id)
                .orElseThrow(
                () -> new NotFoundException("Genre Not Found with id:" + id
                ));
        return genreMapper.toResponse(genreDetails);
    }

    @Transactional
    @Override
    public GenreResponse updateGenre(Long id, GenreRequest request) {

        Genre genreDetails = genreRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Genre not found with id: " + id)
                );

        genreMapper.updateEntity(request, genreDetails);

        if (request.parentGenreId() != null) {
            Genre parentGenre = genreRepository.findById(request.parentGenreId())
                    .orElseThrow(() ->
                            new NotFoundException(
                                    "Parent genre not found with id: "
                                            + request.parentGenreId()
                            )
                    );

            genreDetails.setParentGenre(parentGenre);
        } else {
            genreDetails.setParentGenre(null);
        }

        return genreMapper.toResponse(genreDetails);
    }

    @Transactional
    @Override
    public void deleteGenre(Long id) {
        Genre genreDetails =  genreRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Genre Not Found with id:" + id
                        ));
        genreDetails.setActive(false);
    }

    @Transactional
    @Override
    public void hardDeleteGenre(Long id) {

        Genre genre = genreRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Genre not found with id: " + id)
                );

        genreRepository.delete(genre);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GenreResponse> getAllActiveGenresWithSubGenres(Pageable pageable) {
        Page<GenreResponse> genreEntities = genreRepository.findAllByActiveTrue(pageable)
                                            .map(genreMapper::toResponse);
        return PageResponse.from(genreEntities);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GenreResponse> getTopOrderGenres(Pageable pageable) {
        Page<GenreResponse> genreEntities = genreRepository.findAllByActiveTrueOrderByDisplayOrderAsc(pageable)
                                            .map(genreMapper::toResponse);
        return PageResponse.from(genreEntities);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GenreResponse> searchGenres(
            String searchTerm, Pageable pageable) {

        Page<GenreResponse> genreEntities =  genreRepository
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        searchTerm, searchTerm, pageable
                )
                .map(genreMapper::toResponse);

        return PageResponse.from(genreEntities);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalActiveGenres() {
        return genreRepository.countByActiveTrue();
    }

//    @Override
//    @Transactional(readOnly = true)
//    public long getBookCountByGenreId(Long id) {
//        if (!genreRepository.existsById(id)) {
//            throw new NotFoundException("Genre not found with id: " + id);
//        }
//
//        return bookRepository.countByGenre_Id(id);
//    }
}
