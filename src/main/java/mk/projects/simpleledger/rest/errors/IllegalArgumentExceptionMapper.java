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
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        Logger.getLogger(IllegalArgumentExceptionMapper.class.getSimpleName())
                .log(Level.SEVERE, exception.getMessage(), exception);

        int status = Response.Status.BAD_REQUEST.getStatusCode();
        
        String payload = ApiErrorResponse.build(status, exception.getMessage(), getPath());

        return Response.status(status)
                .type(ResponseUtils.JSON_UTF8)
                .entity(payload)
                .build();
    }

    private String getPath() {
        return uriInfo == null ? null : uriInfo.getRequestUri().getPath();
    }
}
