package mk.projects.simpleledger.rest.errors;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import mk.projects.simpleledger.utils.ResponseUtils;

@Provider
public class NotAllowedExceptionMapper implements ExceptionMapper<NotAllowedException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(NotAllowedException exception) {
        int status = Response.Status.METHOD_NOT_ALLOWED.getStatusCode();
        String payload = ApiErrorResponse.build(status, "Method not allowed", getPath());

        return Response.status(status)
                .type(ResponseUtils.JSON_UTF8)
                .entity(payload)
                .build();
    }

    private String getPath() {
        return uriInfo == null ? null : uriInfo.getRequestUri().getPath();
    }
}
