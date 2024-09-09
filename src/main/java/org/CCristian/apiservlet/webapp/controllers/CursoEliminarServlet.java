package org.CCristian.apiservlet.webapp.controllers;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.CCristian.apiservlet.webapp.config.CursoServicePrincipal;
import org.CCristian.apiservlet.webapp.models.Curso;
import org.CCristian.apiservlet.webapp.services.CursoService;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/cursos/eliminar")
public class CursoEliminarServlet extends HttpServlet {

    @Inject
    @CursoServicePrincipal
    private CursoService service;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        int id;
        try {
            id = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e) {
            id = 0;
        }
        if (id > 0) {   /*Verifica que se haya enviado el parámetro 'id'*/
            Optional<Curso> cursoOptional = service.porId(id);
            if (cursoOptional.isPresent()) {    /*Verifica que ese 'id' exista en la Base de Datos*/
                service.eliminar(id);   /*Se Elimina el curso al que le corresponde el 'id'*/
                resp.sendRedirect(req.getContextPath() + "/cursos");    /*Direcciona al Servlet /cursos*/
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No existe ese Curso en la Base de Datos!");
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "ERROR el id es null, se debe enviar como parámetro en la url!");
        }
    }
}
