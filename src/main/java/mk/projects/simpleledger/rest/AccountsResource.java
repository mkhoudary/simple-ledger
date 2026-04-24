/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mk.projects.simpleledger.rest;

import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import mk.projects.simpleledger.core.DatabaseManager;
import mk.projects.simpleledger.rest.errors.ApiErrorResponse;
import mk.projects.simpleledger.utils.GsonUtils;
import mk.projects.simpleledger.utils.ResponseUtils;
import mk.projects.simpleledger.utils.Utils;

/**
 * REST Web Service
 *
 * @author mkhoudary
 */
@Path("/Accounts")
public class AccountsResource {

    @POST
    @Consumes(ResponseUtils.JSON_UTF8)
    @Produces(ResponseUtils.JSON_UTF8)
    public Response createAccount(String body) throws SQLException {

        try ( Connection con = DatabaseManager.getConnection()) {
            JsonObject json = GsonUtils.INSTANCE.fromJson(body, JsonObject.class);

            String name = GsonUtils.getNotBlankString(json, "name", "'name' field is mandatory");
            String normalBalance = GsonUtils.getNotBlankString(json, "normalBalance", "'normalBalance' field is mandatory");

            if (!Utils.instance().in(normalBalance, "DEBIT", "CREDIT")) {
                throw new IllegalArgumentException("'normalBalance' field value is either 'DEBIT' or 'CREDIT'");
            }

            String sql = "INSERT INTO sld_accounts (name, normal_balance) VALUES (?, ?)";

            try ( PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, Utils.instance().safeString(name));
                ps.setString(2, Utils.instance().safeString(normalBalance));

                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                long id = -1;

                if (rs.next()) {
                    id = rs.getLong(1);
                }

                String response = GsonUtils.jsonObjectBuilder().prop("id", id).build().toString();

                return Response.ok(response)
                        .type(ResponseUtils.JSON_UTF8)
                        .build();
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(AccountsResource.class.getSimpleName()).log(Level.SEVERE, ex.getMessage(), ex);

            int status = Response.Status.BAD_REQUEST.getStatusCode();
            String response = ApiErrorResponse.build(status, ex.getMessage(), null);

            return Response.status(status)
                    .type(ResponseUtils.JSON_UTF8)
                    .entity(response)
                    .build();
        }
    }

    @GET
    @Produces(ResponseUtils.JSON_UTF8)
    public Response getAccounts(
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit) throws SQLException {

        if (offset < 0) {
            offset = 0;
        }

        if (limit <= 0 || limit > 100) {
            limit = 20;
        }

        try ( Connection con = DatabaseManager.getConnection()) {
            String sql = "SELECT id, name, normal_balance FROM sld_accounts ORDER BY id LIMIT ? OFFSET ?";

            try ( PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, limit);
                ps.setInt(2, offset);

                try ( ResultSet rs = ps.executeQuery()) {

                    GsonUtils.JsonArrayBuilder accountsBuilder = GsonUtils.jsonArrayBuilder();

                    while (rs.next()) {
                        accountsBuilder.prop(
                                GsonUtils.jsonObjectBuilder()
                                        .prop("id", rs.getLong("id"))
                                        .prop("name", rs.getString("name"))
                                        .prop("normalBalance", rs.getString("normal_balance"))
                                        .build()
                        );
                    }

                    String response = GsonUtils.jsonObjectBuilder()
                            .prop("offset", offset)
                            .prop("limit", limit)
                            .prop("accounts", accountsBuilder.build())
                            .build()
                            .toString();

                    return Response.ok(response)
                            .type(ResponseUtils.JSON_UTF8)
                            .build();
                }
            }
        }
    }

    @GET
    @Path("{accountId}")
    @Produces(ResponseUtils.JSON_UTF8)
    public Response getAccount(@PathParam("accountId") long accountId) throws SQLException {
        if (accountId <= 0) {
            int status = Response.Status.BAD_REQUEST.getStatusCode();
            String response = ApiErrorResponse.build(status, "'accountId' must be a positive number", null);

            return Response.status(status)
                    .type(ResponseUtils.JSON_UTF8)
                    .entity(response)
                    .build();
        }

        try ( Connection con = DatabaseManager.getConnection()) {
            String sql = "SELECT id, name, normal_balance FROM sld_accounts WHERE id = ?";

            try ( PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, accountId);

                try ( ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        int status = Response.Status.NOT_FOUND.getStatusCode();
                        
                        String response = ApiErrorResponse.build(status, "Account not found", null);

                        return Response.status(status)
                                .type(ResponseUtils.JSON_UTF8)
                                .entity(response)
                                .build();
                    }

                    String response = GsonUtils.jsonObjectBuilder()
                            .prop("id", rs.getLong("id"))
                            .prop("name", rs.getString("name"))
                            .prop("normalBalance", rs.getString("normal_balance"))
                            .build()
                            .toString();

                    return Response.ok(response)
                            .type(ResponseUtils.JSON_UTF8)
                            .build();
                }
            }
        }
    }

}
