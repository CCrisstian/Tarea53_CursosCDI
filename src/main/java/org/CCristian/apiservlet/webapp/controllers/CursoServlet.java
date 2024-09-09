package org.CCristian.apiservlet.webapp.controllers;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.CCristian.apiservlet.webapp.models.Curso;
import org.CCristian.apiservlet.webapp.services.CursoService;
import java.io.IOException;
import java.util.List;

@WebServlet({"/index.html", "/cursos"})
public class CursoServlet extends HttpServlet {

    @Inject
    private CursoService service;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        List<Curso> cursos = service.listar();  /*Obtiene una lista con los Cursos*/

        cursos = service.listar();

        /*Pasando parámetros*/
        req.setAttribute("cursos", cursos);
        req.setAttribute("title", req.getAttribute("title") + "Listado de Cursos");
        getServletContext().getRequestDispatcher("/listar.jsp").forward(req, resp);
    }
}
