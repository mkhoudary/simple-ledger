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
    
    private final static String QUERY_ACCOUNTS = "SELECT id, name, normal_balance FROM sld_accounts ORDER BY id LIMIT ? OFFSET ?";
    
    private final static String QUERY_ACCOUNT = "SELECT id, name, normal_balance FROM sld_accounts WHERE id = ?";
    
    private final static String QUERY_ACCOUNT_JOURNAL_ENTRIES = "SELECT je.id, je.transaction_id, je.type, je.amount, t.created_at "
            + "FROM sld_journal_entries je "
            + "JOIN sld_transactions t ON t.id = je.transaction_id "
            + "WHERE je.account_id = ? "
            + "ORDER BY je.id LIMIT ? OFFSET ?";
    
    private final static String QUERY_ACCOUNT_BALANCE = "SELECT "
            + "COALESCE(SUM("
            + "CASE "
            + "WHEN type = 'DEBIT' THEN amount "
            + "WHEN type = 'CREDIT' THEN -amount "
            + "END"
            + "), 0) AS balance "
            + "FROM sld_journal_entries "
            + "WHERE account_id = ?";

    private final static String INSERT_ACCOUNT = "INSERT INTO sld_accounts (name, normal_balance) VALUES (?, ?)";
    
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

            try ( PreparedStatement ps = con.prepareStatement(INSERT_ACCOUNT, Statement.RETURN_GENERATED_KEYS)) {
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
            try ( PreparedStatement ps = con.prepareStatement(QUERY_ACCOUNTS)) {
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
            try ( PreparedStatement ps = con.prepareStatement(QUERY_ACCOUNT)) {
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
    
    @GET
    @Path("{accountId}/JournalEntries")
    @Produces(ResponseUtils.JSON_UTF8)
    public Response getAccountJournalEntries(
            @PathParam("accountId") long accountId,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit) throws SQLException {
        if (accountId <= 0) {
            int status = Response.Status.BAD_REQUEST.getStatusCode();
            String response = ApiErrorResponse.build(status, "'accountId' must be a positive number", null);

            return Response.status(status)
                    .type(ResponseUtils.JSON_UTF8)
                    .entity(response)
                    .build();
        }

        if (offset < 0) {
            offset = 0;
        }

        if (limit <= 0 || limit > 100) {
            limit = 20;
        }

        try ( Connection con = DatabaseManager.getConnection()) {
            try ( PreparedStatement accountPs = con.prepareStatement(QUERY_ACCOUNT)) {
                accountPs.setLong(1, accountId);

                try ( ResultSet accountRs = accountPs.executeQuery()) {
                    if (!accountRs.next()) {
                        int status = Response.Status.NOT_FOUND.getStatusCode();
                        String response = ApiErrorResponse.build(status, "Account not found", null);

                        return Response.status(status)
                                .type(ResponseUtils.JSON_UTF8)
                                .entity(response)
                                .build();
                    }
                }
            }

            try ( PreparedStatement entriesPs = con.prepareStatement(QUERY_ACCOUNT_JOURNAL_ENTRIES)) {
                entriesPs.setLong(1, accountId);
                entriesPs.setInt(2, limit);
                entriesPs.setInt(3, offset);

                try ( ResultSet rs = entriesPs.executeQuery()) {
                    GsonUtils.JsonArrayBuilder entriesBuilder = GsonUtils.jsonArrayBuilder();

                    while (rs.next()) {
                        entriesBuilder.prop(
                                GsonUtils.jsonObjectBuilder()
                                        .prop("id", rs.getLong("id"))
                                        .prop("transactionId", rs.getLong("transaction_id"))
                                        .prop("type", rs.getString("type"))
                                        .prop("amount", rs.getBigDecimal("amount"))
                                        .prop("createdAt", rs.getTimestamp("created_at").toString())
                                        .build()
                        );
                    }

                    String response = GsonUtils.jsonObjectBuilder()
                            .prop("accountId", accountId)
                            .prop("offset", offset)
                            .prop("limit", limit)
                            .prop("journalEntries", entriesBuilder.build())
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
    @Path("{accountId}/balance")
    @Produces(ResponseUtils.JSON_UTF8)
    public Response getAccountBalance(@PathParam("accountId") long accountId) throws SQLException {
        if (accountId <= 0) {
            int status = Response.Status.BAD_REQUEST.getStatusCode();
            String response = ApiErrorResponse.build(status, "'accountId' must be a positive number", null);

            return Response.status(status)
                    .type(ResponseUtils.JSON_UTF8)
                    .entity(response)
                    .build();
        }

        try ( Connection con = DatabaseManager.getConnection()) {
            try ( PreparedStatement accountPs = con.prepareStatement(QUERY_ACCOUNT)) {
                accountPs.setLong(1, accountId);

                try ( ResultSet accountRs = accountPs.executeQuery()) {
                    if (!accountRs.next()) {
                        int status = Response.Status.NOT_FOUND.getStatusCode();
                        String response = ApiErrorResponse.build(status, "Account not found", null);

                        return Response.status(status)
                                .type(ResponseUtils.JSON_UTF8)
                                .entity(response)
                                .build();
                    }
                }
            }

            try ( PreparedStatement balancePs = con.prepareStatement(QUERY_ACCOUNT_BALANCE)) {
                balancePs.setLong(1, accountId);

                try ( ResultSet rs = balancePs.executeQuery()) {
                    rs.next();

                    String response = GsonUtils.jsonObjectBuilder()
                            .prop("accountId", accountId)
                            .prop("balance", rs.getBigDecimal("balance"))
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
