package org.CCristian.apiservlet.webapp.interceptors;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.logging.Logger;

@Logging
@Interceptor
public class LoggingInterceptor {

    @Inject
    private Logger log;

    @AroundInvoke
    public Object logging(InvocationContext invocation) throws Exception {
        log.info(" ***** Entrando antes de invocar el método "
                + invocation.getMethod().getName() + " del componente CDI " + invocation.getMethod().getDeclaringClass());
        Object resultado = invocation.proceed();    /*Invocación del método*/
        log.info(" ***** Saliendo de la invocación del método " + invocation.getMethod().getName());
        return resultado;   /*Devolviendo el método*/
    }
}
