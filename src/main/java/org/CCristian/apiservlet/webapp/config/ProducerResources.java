package org.CCristian.apiservlet.webapp.config;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/*Conexión a la BASE DE DATOS usando C.D.I.*/
@ApplicationScoped
public class ProducerResources {
    @Resource(name = "jdbc/mysqlDB")
    private DataSource ds;

    @Inject
    private Logger log;

    /*Devuelve la conexión a la BaseDeDatos*/
    @Produces
    @RequestScoped
    @MySQLConn
    private Connection beanConnection() throws NamingException, SQLException {
        return ds.getConnection();
    }

    /*Cierra la conexión a la BaseDeDatos*/
    public void close(@Disposes @MySQLConn Connection connection) throws SQLException {
        connection.close();
        log.info("Cerrando la conexión a la BD MySQL");   /*Para visualizarlo en la consola de 'Tomcat'*/
    }

    @Produces
    private Logger beanLogger(InjectionPoint injectionPoint){
        return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
    }
}
