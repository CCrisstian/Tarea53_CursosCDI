package org.CCristian.apiservlet.webapp.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.CCristian.apiservlet.webapp.interceptors.TransactionalJdbc;
import org.CCristian.apiservlet.webapp.models.Curso;
import org.CCristian.apiservlet.webapp.repositories.Repositorio;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CursoServiceImpl implements CursoService {

    @Inject
    private Repositorio<Curso> repositoryJBDC;

    @Override
    public List<Curso> listar() throws SecurityException {
        try {
            return repositoryJBDC.listar();
        } catch (SQLException throwables) {
            throw new ServiceJdbcException(throwables.getMessage(), throwables.getCause());
        }
    }

    @Override
    public List<Curso> porNombre(String nombre) {
        try {
            return repositoryJBDC.porNombre(nombre);
        } catch (SQLException throwables) {
            throw new ServiceJdbcException(throwables.getMessage(), throwables.getCause());
        }
    }

    @Override
    @TransactionalJdbc
    public void guardar(Curso curso) {
        try {
            repositoryJBDC.guardar(curso);
        } catch (SQLException throwables) {
            throw new ServiceJdbcException(throwables.getMessage(), throwables.getCause());
        }
    }

    @Override
    public Optional<Curso> porId(int id) {
        try {
            return Optional.ofNullable(repositoryJBDC.porId(id));
        } catch (SQLException throwables) {
            throw new ServiceJdbcException(throwables.getMessage(), throwables.getCause());
        }
    }

    @Override
    @TransactionalJdbc
    public void eliminar(int id) {
        try {
            repositoryJBDC.eliminar(id);
        } catch (SQLException throwables) {
            throw new ServiceJdbcException(throwables.getMessage(), throwables.getCause());
        }
    }
}
