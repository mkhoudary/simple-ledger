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
@Path("/Transactions")
public class TransactionsResource {

    @POST
    @Consumes(ResponseUtils.JSON_UTF8)
    @Produces(ResponseUtils.JSON_UTF8)
    public Response createTransaction(String body) throws SQLException {
        try ( Connection con = DatabaseManager.getConnection()) {
            con.setAutoCommit(false);
            
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
        }
    }
}
