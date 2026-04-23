/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mk.projects.simpleledger.core;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import mk.projects.simpleledger.rest.HelloWorldResource;

/**
 *
 * @author Mohammad
 */
@javax.ws.rs.ApplicationPath("api")
public class SimpleLedgerRestApplication extends Application {

    private final static Set<Class<?>> REST_CLASSES = new HashSet<>();

    static {
        REST_CLASSES.add(HelloWorldResource.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return REST_CLASSES;
    }
}
