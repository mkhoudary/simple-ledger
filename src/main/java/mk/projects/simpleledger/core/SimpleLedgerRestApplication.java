/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mk.projects.simpleledger.core;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import mk.projects.simpleledger.rest.AccountsResource;
import mk.projects.simpleledger.rest.TransactionsResource;
import mk.projects.simpleledger.rest.errors.IllegalArgumentExceptionMapper;
import mk.projects.simpleledger.rest.errors.NotAllowedExceptionMapper;
import mk.projects.simpleledger.rest.errors.NotFoundExceptionMapper;
import mk.projects.simpleledger.rest.errors.SqlExceptionMapper;
import mk.projects.simpleledger.rest.errors.ThrowableExceptionMapper;
import mk.projects.simpleledger.rest.errors.WebApplicationExceptionMapper;

/**
 *
 * @author Mohammad
 */
@javax.ws.rs.ApplicationPath("api")
public class SimpleLedgerRestApplication extends Application {

    private final static Set<Class<?>> REST_CLASSES = new HashSet<>();

    static {
        REST_CLASSES.add(AccountsResource.class);
        REST_CLASSES.add(TransactionsResource.class);
        REST_CLASSES.add(CorsFilter.class);
        REST_CLASSES.add(NotFoundExceptionMapper.class);
        REST_CLASSES.add(NotAllowedExceptionMapper.class);
        REST_CLASSES.add(IllegalArgumentExceptionMapper.class);
        REST_CLASSES.add(SqlExceptionMapper.class);
        REST_CLASSES.add(WebApplicationExceptionMapper.class);
        REST_CLASSES.add(ThrowableExceptionMapper.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return REST_CLASSES;
    }
}
