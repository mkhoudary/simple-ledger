/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mk.projects.simpleledger.rest.errors;

import java.sql.SQLException;
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
public class SqlExceptionMapper implements ExceptionMapper<SQLException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(SQLException exception) {
        Logger.getLogger(SqlExceptionMapper.class.getSimpleName())
                .log(Level.SEVERE, exception.getMessage(), exception);

        int status = Response.Status.BAD_REQUEST.getStatusCode();
        String message = String.format("%s (sqlState=%s, errorCode=%s)",
                exception.getMessage(),
                exception.getSQLState(),
                exception.getErrorCode());
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
