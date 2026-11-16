package com.nikkssy.library_management.dto.genre;

public record SubGenreResponse(

        Long id,
        String code,
        String name,
        Integer displayOrder,
        boolean active

) {}