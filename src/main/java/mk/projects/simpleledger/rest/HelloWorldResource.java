/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mk.projects.simpleledger.rest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import mk.projects.simpleledger.core.DatabaseManager;

/**
 * REST Web Service
 *
 * @author mkhoudary
 */
@Path("/Hello")
public class HelloWorldResource {

    @GET
    @Produces("application/json; charset=UTF-8")
    public Response generateAuthenticationToken() {
        String now = null;

        try ( Connection con = DatabaseManager.getConnection();  Statement stmt = con.createStatement();  ResultSet rs = stmt.executeQuery("SELECT NOW()")) {

            if (rs.next()) {
                now = rs.getString(1);
            }

        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }

        String json = "{\"now\":\"" + now + "\"}";

        return Response.ok(json)
                .type("application/json; charset=UTF-8")
                .build();
    }

}
