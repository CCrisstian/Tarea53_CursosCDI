package org.CCristian.apiservlet.webapp.interceptors;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.CCristian.apiservlet.webapp.config.MySQLConn;
import org.CCristian.apiservlet.webapp.services.ServiceJdbcException;


import java.sql.Connection;
import java.util.logging.Logger;

@TransactionalJdbc
@Interceptor
public class TransactionalInterceptor {

    @Inject
    @MySQLConn
    private Connection conn;

    @Inject
    private Logger log;

    @AroundInvoke
    public Object transactional(InvocationContext invocation) throws Exception {
        if (conn.getAutoCommit()) {
            conn.setAutoCommit(false);
        }
        try {
            log.info(" ------> Iniciando Transacción " + invocation.getMethod().getName()
                    + " de la clase " + invocation.getMethod().getDeclaringClass());
            Object resultado = invocation.proceed();
            conn.commit();
            log.info(" ------> Realizando 'commit' y Finalizando Transacción " + invocation.getMethod().getName()
                    + " de la clase " + invocation.getMethod().getDeclaringClass());
            return resultado;
        } catch (ServiceJdbcException e) {
            conn.rollback();
            throw e;
        }
    }
}
