package org.CCristian.apiservlet.webapp.config;

import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Qualifier
@Retention(RetentionPolicy.RUNTIME) /*Se aplica en tiempo de ejecución*/
@Target({METHOD, FIELD, PARAMETER, TYPE, CONSTRUCTOR})   /*Donde se aplicará*/
public @interface CursoServicePrincipal {
}
