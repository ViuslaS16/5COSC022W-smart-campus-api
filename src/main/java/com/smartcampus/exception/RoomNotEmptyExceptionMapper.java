package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        ErrorResponse errorDetails = new ErrorResponse(
                Response.Status.CONFLICT.getStatusCode(),
                "Conflict",
                exception.getMessage(),
                System.currentTimeMillis()
        );
        return Response.status(Response.Status.CONFLICT)
                .entity(errorDetails)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
