package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException exception) {
        ErrorResponse errorDetails = new ErrorResponse(
                403,
                "Forbidden",
                exception.getMessage(),
                System.currentTimeMillis()
        );
        return Response.status(Response.Status.FORBIDDEN)
                .entity(errorDetails)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
