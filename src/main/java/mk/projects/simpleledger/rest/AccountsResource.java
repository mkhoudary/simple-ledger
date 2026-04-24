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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import mk.projects.simpleledger.core.DatabaseManager;
import mk.projects.simpleledger.utils.GsonUtils;
import mk.projects.simpleledger.utils.Utils;

/**
 * REST Web Service
 *
 * @author mkhoudary
 */
@Path("/Accounts")
public class AccountsResource {

    @POST
    @Consumes("application/json; charset=UTF-8")
    @Produces("application/json; charset=UTF-8")
    @Path("create")
    public Response createAccount(String body) {

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
                        .type("application/json; charset=UTF-8")
                        .build();
            }
        } catch (SQLException ex) {
            Logger.getLogger(AccountsResource.class.getSimpleName()).log(Level.SEVERE, ex.getMessage(), ex);

            int status = Response.Status.BAD_REQUEST.getStatusCode();

            String response = GsonUtils.jsonObjectBuilder()
                    .prop("error", ex.getMessage())
                    .prop("sqlState", ex.getSQLState())
                    .prop("errorCode", ex.getErrorCode())
                    .build()
                    .toString();

            return Response.status(status)
                    .type("application/json; charset=UTF-8")
                    .entity(response)
                    .build();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(AccountsResource.class.getSimpleName()).log(Level.SEVERE, ex.getMessage(), ex);

            int status = Response.Status.BAD_REQUEST.getStatusCode();

            String response = GsonUtils.jsonObjectBuilder()
                    .prop("error", ex.getMessage())
                    .build()
                    .toString();

            return Response.status(status)
                    .type("application/json; charset=UTF-8")
                    .entity(response)
                    .build();
        } catch (Exception ex) {
            Logger.getLogger(AccountsResource.class.getSimpleName()).log(Level.SEVERE, ex.getMessage(), ex);

            String response = GsonUtils.jsonObjectBuilder().prop("error", ex.getMessage()).build().toString();

            return Response.serverError()
                    .entity(response)
                    .build();
        }
    }

}
