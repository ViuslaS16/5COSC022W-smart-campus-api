package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        ErrorResponse errorDetails = new ErrorResponse(
                422,
                "Unprocessable Entity",
                exception.getMessage(),
                System.currentTimeMillis()
        );
        return Response.status(422)
                .entity(errorDetails)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
