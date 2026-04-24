/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mk.projects.simpleledger.rest.errors;

import javax.ws.rs.WebApplicationException;
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
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(WebApplicationException exception) {
        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        
        if (exception.getResponse() != null) {
            status = exception.getResponse().getStatus();
        }

        String message = exception.getMessage();
        if (exception.getResponse() != null && exception.getResponse().getStatusInfo() != null) {
            message = exception.getResponse().getStatusInfo().getReasonPhrase();
        }

        String payload = ApiErrorResponse.build(status, message, getPath());

        return Response.status(status)
                .type(ResponseUtils.JSON_UTF8)
                .entity(payload)
                .build();
    }

    private String getPath() {
        return uriInfo == null ? null : uriInfo.getRequestUri().getPath();
    }
}
