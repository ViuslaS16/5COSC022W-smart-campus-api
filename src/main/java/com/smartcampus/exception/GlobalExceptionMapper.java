package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable exception) {
        // Prevent generic stack trace leakage as per requirements
        ErrorResponse errorDetails = new ErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                System.currentTimeMillis()
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorDetails)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
