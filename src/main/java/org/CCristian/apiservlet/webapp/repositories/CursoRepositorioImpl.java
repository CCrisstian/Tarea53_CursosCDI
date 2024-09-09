package org.CCristian.apiservlet.webapp.repositories;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.CCristian.apiservlet.webapp.config.MySQLConn;
import org.CCristian.apiservlet.webapp.models.Curso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
@Named
public class CursoRepositorioImpl implements Repositorio<Curso> {

    @Inject
    @MySQLConn
    private Connection conn;

    @Inject
    private Logger log;

    @PostConstruct
    public void inicializar(){
        log.info("Inicializando el beans " + this.getClass().getName());
    }

    @PreDestroy
    public void destruir(){
        log.info("Destruyendo el beans " + this.getClass().getName());
    }

    @Override
    public List<Curso> listar() throws SQLException {
        List<Curso> cursos = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM cursos AS c ORDER BY c.id")) {
            while (rs.next()) {
                Curso curso = getCurso(rs);
                cursos.add(curso);
            }
        }
        return cursos;
    }

    @Override
    public List<Curso> porNombre(String nombre) throws SQLException {
        List<Curso> cursos = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM cursos as c WHERE c.nombre like ?")) {
            stmt.setString(1, "%" + nombre + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cursos.add(getCurso(rs));
                }
            }
        }
        return cursos;
    }

    @Override
    public void guardar(Curso curso) throws SQLException {
        String sql;
        if (curso.getId() > 0) {
            sql = "UPDATE cursos SET nombre=?, descripcion=?, instructor=?, duracion=? WHERE id=?";
        } else {
            sql = "INSERT INTO cursos (nombre, descripcion, instructor, duracion) VALUES(?,?,?,?)";
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, curso.getNombre());
            stmt.setString(2, curso.getDescripcion());
            stmt.setString(3, curso.getInstructor());
            stmt.setFloat(4, curso.getDuracion());
            if (curso.getId() > 0) {
                stmt.setInt(5, curso.getId());
            }
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM cursos WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public Curso porId(int id) throws SQLException {
        Curso curso = null;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM cursos AS c WHERE c.id=?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    curso = getCurso(rs);
                }
            }
        }
        return curso;
    }

    private static Curso getCurso(ResultSet rs) throws SQLException {
        Curso c = new Curso();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setInstructor(rs.getString("instructor"));
        c.setDuracion(rs.getFloat("duracion"));
        return c;
    }
}
