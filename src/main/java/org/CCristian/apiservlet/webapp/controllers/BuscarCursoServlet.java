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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/buscar")
public class BuscarCursoServlet extends HttpServlet {

    @Inject
    @CursoServicePrincipal
    private CursoService service;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String nombre = req.getParameter("nombre");
        List<Curso> cursos = new ArrayList<>();

        cursos = service.porNombre(nombre);  /*Obtiene una lista con los Productos*/

        req.setAttribute("cursos", cursos);
        getServletContext().getRequestDispatcher("/listar.jsp").forward(req, resp);
    }
}
