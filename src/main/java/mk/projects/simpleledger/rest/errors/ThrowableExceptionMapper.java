/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mk.projects.simpleledger.rest.errors;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import mk.projects.simpleledger.utils.ResponseUtils;

/**
 *
 * @author Mohammad
 */
@Provider
public class ThrowableExceptionMapper implements ExceptionMapper<Throwable> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {
        Logger.getLogger(ThrowableExceptionMapper.class.getSimpleName())
                .log(Level.SEVERE, exception.getMessage(), exception);

        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String payload = ApiErrorResponse.build(status, "Internal server error", getPath());

        return Response.status(status)
                .type(ResponseUtils.JSON_UTF8)
                .entity(payload)
                .build();
    }

    private String getPath() {
        return uriInfo == null ? null : uriInfo.getRequestUri().getPath();
    }
}
