/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package mk.projects.simpleledger.rest.errors;

import javax.ws.rs.NotFoundException;
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
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(NotFoundException exception) {
        int status = Response.Status.NOT_FOUND.getStatusCode();
        String payload = ApiErrorResponse.build(status, "Resource not found", getPath());

        return Response.status(status)
                .type(ResponseUtils.JSON_UTF8)
                .entity(payload)
                .build();
    }

    private String getPath() {
        return uriInfo == null ? null : uriInfo.getRequestUri().getPath();
    }
}
