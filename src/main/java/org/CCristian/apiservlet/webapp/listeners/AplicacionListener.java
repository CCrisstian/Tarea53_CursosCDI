package org.CCristian.apiservlet.webapp.listeners;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AplicacionListener implements ServletContextListener, ServletRequestListener {
    private ServletContext servletContext;

    /*ServletRequestListener*/
    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        sre.getServletRequest().setAttribute("title","TAREA 53: Pool de Conexiones + C.D.i. - ");   /*title por defecto - header.jsp*/
    }
}
