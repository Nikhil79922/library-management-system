package com.nikkssy.library_management.dto.genre;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

import jakarta.validation.constraints.*;

public record GenreRequest(

        @JsonDeserialize(using = StrictStringDeserializer.class)
        @NotBlank(message = "Genre code is required")
        @Size(max = 30, message = "Genre code cannot exceed 30 characters")
        @Pattern(
                regexp = "^[A-Z0-9_-]+$",
                message = "Genre code must contain only uppercase letters, numbers, hyphens or underscores"
        )
        String code,

        @NotBlank(message = "Genre name is required")
        @Size(min = 2, max = 100)
        String name,

        @Size(max = 500)
        String description,

        @NotNull
        @Min(0)
        Integer displayOrder,

        @NotNull
        Boolean active,

        @Positive
        Long parentGenreId

) {

        public static class StrictStringDeserializer
                extends ValueDeserializer<String> {

                @Override
                public String deserialize(
                        JsonParser parser,
                        DeserializationContext context
                ) throws tools.jackson.core.JacksonException {

                        if (!parser.hasToken(JsonToken.VALUE_STRING)) {
                                return context.reportInputMismatch(
                                        this,
                                        "Expected a string value"
                                );
                        }

                        return parser.getString();
                }
        }
}