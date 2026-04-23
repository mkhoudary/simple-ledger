/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/ServletListener.java to edit this template
 */
package mk.projects.simpleledger.listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import mk.projects.simpleledger.core.DatabaseManager;

/**
 * Web application lifecycle listener.
 *
 * @author Mohammad
 */
public class ContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DatabaseManager.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DatabaseManager.destroy();
    }
}
