package com.nikkssy.library_management.security.handler;

import tools.jackson.databind.json.JsonMapper;
import com.nikkssy.library_management.dto.commonRes.ErrorResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler
        implements AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        HttpStatus status = HttpStatus.FORBIDDEN;

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                "You do not have permission to access this resource",
                LocalDateTime.now(),
                status.value(),
                request.getRequestURI()
        );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        jsonMapper.writeValue(
                response.getOutputStream(),
                errorResponse
        );
    }
}
