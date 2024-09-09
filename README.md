<h1 align="center">Tarea 53: Migrar penúltima tarea (pool conexiones cursos) a CDI con anotaciones</h1>
<p>Migrar penúltima tarea (pool conexiones cursos) a CDI con anotaciones</p>
<p>Para este nuevo desafío se requerirá modificar el proyecto de la penúltima tarea una vez más (de los cursos), para migrar todo a CDI con anotaciones, inyección de dependencia, contextos, interceptor transaccional, en general lo mismo que vimos en los videos pero aplicado al proyecto de cursos.</p>

- `MySQLConn` (Qualifier)
  - <b>Propósito</b>: Esta anotación personalizada (`@Qualifier`) es utilizada para diferenciar la inyección de dependencias cuando hay múltiples implementaciones posibles de un mismo tipo. En este caso, `MySQLConn` se aplica a las conexiones de la base de datos MySQL.
  - <b>Relación</b>: Se usa en la clase `ProducerResources` para identificar la conexión MySQL que se inyectará en otros componentes.
- `ProducerResources` (ApplicationScoped)
  - <b>Propósito</b>: Esta clase es un productor de recursos (como la conexión a la base de datos y el logger) que serán inyectados en otros componentes del proyecto.
  - <b>Relación</b>: Proporciona la conexión a la base de datos MySQL, marcada con `@MySQLConn`, que se inyecta en otros componentes como repositorios y servicios. También gestiona la creación y cierre de estas conexiones.
- `TransactionalJdbc` (InterceptorBinding)
  - <b>Propósito</b>: Esta anotación marca métodos o clases para que sus transacciones sean gestionadas de manera automática. Es decir, indica que la ejecución de esos métodos debe estar envuelta en una transacción JDBC.
  - <b>Relación</b>: Es utilizada en la clase `TransactionalInterceptor` para aplicar la lógica de manejo de transacciones a los métodos o clases que lo necesiten.
- `TransactionalInterceptor` (Interceptor)
  - <b>Propósito</b>: Este interceptor gestiona el ciclo de vida de las transacciones JDBC. Inicia la transacción, la confirma (commit) si no hay errores, o la revierte (rollback) si ocurre alguna excepción.
  - <b>Relación</b>: Se activa automáticamente en los métodos o clases anotadas con `@TransactionalJdbc`, asegurando que las operaciones con la base de datos se manejen dentro de una transacción adecuada.
- `CursoRepositorioImpl` (RequestScoped)
  - <b>Propósito</b>: Esta clase es un repositorio que maneja las operaciones CRUD (crear, leer, actualizar, eliminar) para la entidad `Curso`.
  - <b>Relación</b>: Se inyecta una conexión MySQL (producida por `ProducerResources`) y se utiliza para ejecutar las consultas SQL necesarias. Los métodos del repositorio están sujetos a la gestión transaccional proporcionada por `TransactionalInterceptor`.
- `CursoServiceImpl` (ApplicationScoped)
  - <b>Propósito</b>: Esta clase proporciona servicios de negocio relacionados con la entidad `Curso`, como listar, buscar, guardar y eliminar cursos.
  - <b>Relación</b>: Inyecta el repositorio `CursoRepositorioImpl` y encapsula la lógica de negocio, delegando las operaciones CRUD al repositorio. La clase también está anotada con `@TransactionalJdbc` y `@Logging`, lo que significa que sus métodos están envueltos en transacciones y son interceptados para registrar su ejecución.
- `BuscarCursoServlet`, `CursoEliminarServlet`, `CursoFormServlet` (Servlets)
  - <b>Propósito</b>: Estas clases son servlets que manejan las solicitudes HTTP relacionadas con los cursos (búsqueda, eliminación y formularios).
  - <b>Relación</b>: Inyectan el servicio `CursoService` para interactuar con la capa de negocio y, a través de esta, con la base de datos. Los servlets gestionan la lógica de presentación y la interacción con el usuario final.

<h1>Resoluciòn del Profesor</h1>

- Clase `ProducerResources`
```java
package org.aguzman.apiservlet.webapp.jdbc.tarea.configs;

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

@ApplicationScoped
public class ProducerResources {

    @Inject
    private Logger log;

    @Resource(name="jdbc/mysqlDB")
    private DataSource ds;

    @Produces
    @RequestScoped
    private Connection beanConnection() throws NamingException, SQLException {
        return ds.getConnection();
    }

    @Produces
    private Logger beanLogger(InjectionPoint injectionPoint){
        return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
    }

    public void close(@Disposes Connection connection) throws SQLException {
        connection.close();
        log.info("cerrando la conexion a la bbdd mysql!");
    }
}
```

- Anotación `TransactionalJdbc`
```java
package org.aguzman.apiservlet.webapp.jdbc.tarea.interceptors;

import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface TransactionalJdbc {
}
```

- Clase `TransactionalInterceptor`
```java
package org.aguzman.apiservlet.webapp.jdbc.tarea.interceptors;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.sql.Connection;
import java.util.logging.Logger;
import org.aguzman.apiservlet.webapp.jdbc.tarea.services.ServiceJdbcException;

@TransactionalJdbc
@Interceptor
public class TransactionalInterceptor {

    @Inject
    private Connection conn;

    @Inject
    private Logger log;

    @AroundInvoke
    public Object transactional(InvocationContext invocation) throws Exception {
        if (conn.getAutoCommit()) {
            conn.setAutoCommit(false);
        }
        try {
            log.info(" ------> iniciando transaccion " + invocation.getMethod().getName() +
                    " de la clase " + invocation.getMethod().getDeclaringClass());
            Object resultado = invocation.proceed();
            conn.commit();
            log.info(" ------> realizando commit y finalizando transaccion " + invocation.getMethod().getName() +
                    " de la clase " + invocation.getMethod().getDeclaringClass());
            return resultado;
        } catch (ServiceJdbcException e){
            conn.rollback();
            throw e;
        }
    }
}
```

- Clase `CursoRepositorioImpl`
```java
package org.aguzman.apiservlet.webapp.jdbc.tarea.repositories;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.aguzman.apiservlet.webapp.jdbc.tarea.models.Curso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CursoRepositorioImpl implements Repository<Curso> {

    @Inject
    private Connection conn;

    @Override
    public List<Curso> listar() throws SQLException {
        List<Curso> cursos = new ArrayList<>();

        try ( Statement stmt = conn.createStatement();  ResultSet rs = stmt.executeQuery("SELECT * FROM cursos as c")) {
            while (rs.next()) {
                Curso p = getCurso(rs);
                cursos.add(p);
            }
        }
        return cursos;
    }

    @Override
    public List<Curso> porNombre(String nombre) throws SQLException {
        List<Curso> cursos = new ArrayList<>();

        try ( PreparedStatement stmt = conn.prepareStatement("SELECT * FROM cursos as c WHERE c.nombre like ?")) {
            stmt.setString(1, "%" + nombre + "%");

            try ( ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cursos.add(getCurso(rs));
                }
            }
        }
        return cursos;
    }

    @Override
    public Curso porId(Long id) throws SQLException {
        Curso curso = null;
        try ( PreparedStatement stmt = conn.prepareStatement("SELECT * FROM cursos as c WHERE c.id = ?")) {
            stmt.setLong(1, id);

            try ( ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    curso = getCurso(rs);
                }
            }
        }
        return curso;
    }

    @Override
    public void guardar(Curso curso) throws SQLException {
        String sql;
        if (curso.getId() != null && curso.getId() > 0) {
            sql = "update cursos set nombre=?, descripcion=?, instructor=?, duracion=? where id=?";
        } else {
            sql = "insert into cursos (nombre, descripcion, instructor, duracion) values (?,?,?,?)";
        }
        try ( PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, curso.getNombre());
            stmt.setString(2, curso.getDescripcion());
            stmt.setString(3, curso.getInstructor());
            stmt.setDouble(4, curso.getDuracion());

            if (curso.getId() != null && curso.getId() > 0) {
                stmt.setLong(5, curso.getId());
            }
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        String sql = "delete from cursos where id=?";
        try ( PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    private Curso getCurso(ResultSet rs) throws SQLException {
        Curso c = new Curso();
        c.setId(rs.getLong("id"));
        c.setNombre(rs.getString("nombre"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setInstructor(rs.getString("instructor"));
        c.setDuracion(rs.getDouble("duracion"));
        return c;
    }
}
```

- Clase `CursoServiceImpl`
```java
package org.aguzman.apiservlet.webapp.jdbc.tarea.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.aguzman.apiservlet.webapp.jdbc.tarea.models.Curso;
import org.aguzman.apiservlet.webapp.jdbc.tarea.repositories.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.aguzman.apiservlet.webapp.jdbc.tarea.interceptors.TransactionalJdbc;

@ApplicationScoped
public class CursoServiceImpl implements CursoService{
    
    @Inject
    private Repository<Curso> repository;

    @Override
    public List<Curso> listar() {
        try {
            return repository.listar();
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public List<Curso> porNombre(String nombre) {
        try {
            return repository.porNombre(nombre);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Optional<Curso> porId(Long id) {
        try {
            return Optional.ofNullable(repository.porId(id));
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    @TransactionalJdbc
    public void guardar(Curso curso) {
        try {
            repository.guardar(curso);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }

    @Override
    @TransactionalJdbc
    public void eliminar(Long id) {
        try {
            repository.eliminar(id);
        } catch (SQLException e) {
            throw new ServiceJdbcException(e.getMessage(), e.getCause());
        }
    }
}
```

- Clases `Servlets`:
```java
package org.aguzman.apiservlet.webapp.jdbc.tarea.controllers;

import jakarta.inject.Inject;
import org.aguzman.apiservlet.webapp.jdbc.tarea.services.CursoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import org.aguzman.apiservlet.webapp.jdbc.tarea.models.Curso;

@WebServlet({"/index.html", "/cursos"})
public class CursoServlet extends HttpServlet {
    
    @Inject
    private CursoService service;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Curso> cursos = service.listar();

        req.setAttribute("titulo", "Tarea: Listado de cursos");
        req.setAttribute("cursos", cursos);
        getServletContext().getRequestDispatcher("/listar.jsp").forward(req, resp);
    }
}
```
```java

package org.aguzman.apiservlet.webapp.jdbc.tarea.controllers;

import jakarta.inject.Inject;
import org.aguzman.apiservlet.webapp.jdbc.tarea.services.CursoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.aguzman.apiservlet.webapp.jdbc.tarea.models.Curso;

@WebServlet("/cursos/form")
public class CursoFormServlet extends HttpServlet {

    @Inject
    private CursoService service;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long id;
        try {
            id = Long.parseLong(req.getParameter("id"));
        } catch (NumberFormatException e) {
            id = 0L;
        }
        Curso curso = new Curso();
        if (id > 0) {
            Optional<Curso> o = service.porId(id);
            if (o.isPresent()) {
                curso = o.get();
            }
        }
        req.setAttribute("curso", curso);
        req.setAttribute("titulo", id > 0 ? "Tarea: Editar curso" : "Tarea 10: Crear curso");
        getServletContext().getRequestDispatcher("/form.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String nombre = req.getParameter("nombre");
        String descripcion = req.getParameter("descripcion");
        String instructor = req.getParameter("instructor");

        double duracion;
        try {
            duracion = Double.parseDouble(req.getParameter("duracion"));
        } catch (NumberFormatException e) {
            duracion = 0;
        }

        Map<String, String> errores = new HashMap<>();
        if (nombre == null || nombre.isBlank()) {
            errores.put("nombre", "el nombre es requerido!");
        }
        if (descripcion == null || descripcion.isBlank()) {
            errores.put("descripcion", "la descripcion es requerida!");
        }

        if (instructor == null || instructor.isBlank()) {
            errores.put("instructor", "el instructor es requerido");
        }
        if (duracion == 0) {
            errores.put("duracion", "la duracion es requerida!");
        }

        long id;
        try {
            id = Long.parseLong(req.getParameter("id"));
        } catch (NumberFormatException e) {
            id = 0L;
        }
        Curso curso = new Curso();
        curso.setId(id);
        curso.setNombre(nombre);
        curso.setDescripcion(descripcion);
        curso.setInstructor(instructor);
        curso.setDuracion(duracion);

        if (errores.isEmpty()) {
            service.guardar(curso);
            resp.sendRedirect(req.getContextPath() + "/cursos");
        } else {
            req.setAttribute("errores", errores);
            req.setAttribute("curso", curso);
            req.setAttribute("titulo", id > 0 ? "Tarea: Editar curso" : "Tarea 10: Crear curso");
            getServletContext().getRequestDispatcher("/form.jsp").forward(req, resp);
        }
    }
}
```
```java

package org.aguzman.apiservlet.webapp.jdbc.tarea.controllers;

import jakarta.inject.Inject;
import org.aguzman.apiservlet.webapp.jdbc.tarea.services.CursoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;
import org.aguzman.apiservlet.webapp.jdbc.tarea.models.Curso;

@WebServlet("/cursos/eliminar")
public class CursoEliminarServlet extends HttpServlet {

    @Inject
    private CursoService service;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        long id;
        try {
            id = Long.parseLong(req.getParameter("id"));
        } catch (NumberFormatException e) {
            id = 0L;
        }
        if (id > 0) {
            Optional<Curso> o = service.porId(id);
            if (o.isPresent()) {
                service.eliminar(id);
                resp.sendRedirect(req.getContextPath() + "/cursos");
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No existe el cursos en la base de datos!");
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Error el id es null, se debe enviar como parametro en la url!");
        }
    }
}
```
```java

package org.aguzman.apiservlet.webapp.jdbc.tarea.controllers;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import org.aguzman.apiservlet.webapp.jdbc.tarea.models.Curso;
import org.aguzman.apiservlet.webapp.jdbc.tarea.services.CursoService;

@WebServlet("/cursos/buscar")
public class BuscarCursoServlet extends HttpServlet {

    @Inject
    private CursoService service;
        
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String nombre = req.getParameter("nombre");
        
        List<Curso> cursos = service.porNombre(nombre);

        req.setAttribute("titulo", "Tarea: filtrando cursos");
        req.setAttribute("cursos", cursos);
        getServletContext().getRequestDispatcher("/listar.jsp").forward(req, resp);
    }
}
```

- Clase filtro `ConexionFilter`:
```java
package org.aguzman.apiservlet.webapp.jdbc.tarea.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.aguzman.apiservlet.webapp.jdbc.tarea.services.ServiceJdbcException;

import java.io.IOException;

@WebFilter("/*")
public class ConexionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        try {
            chain.doFilter(request, response);
        } catch (ServiceJdbcException e) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            e.printStackTrace();
        }
    }
}
```

- Archivo de configuración `beans.xml`
```xml
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/beans_3_0.xsd"
       version="3.0" bean-discovery-mode="annotated">

    <interceptors>
        <class>
            org.aguzman.apiservlet.webapp.jdbc.tarea.interceptors.TransactionalInterceptor
        </class>
    </interceptors>
</beans>
```

- Archivo de configuración `pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.aguzman.apiservlet.webapp.cdi.tarea16</groupId>
    <artifactId>webapp-cursoscdi-tarea16</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>
    <properties>
        <maven.compiler.source>16</maven.compiler.source>
        <maven.compiler.target>16</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>9.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.glassfish.web</groupId>
            <artifactId>jakarta.servlet.jsp.jstl</artifactId>
            <version>2.0.0</version>
        </dependency>
        <dependency>
            <groupId>org.jboss.weld.servlet</groupId>
            <artifactId>weld-servlet-core</artifactId>
            <version>4.0.1.SP1</version>
        </dependency>
    </dependencies>
    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
            </plugin>
            <plugin>
                <groupId>org.apache.tomcat.maven</groupId>
                <artifactId>tomcat7-maven-plugin</artifactId>
                <version>2.2</version>
                <configuration>
                    <url>http://localhost:8080/manager/text</url>
                    <username>admin</username>
                    <password>12345</password>
                </configuration>
            </plugin>
            <plugin>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.2.3</version>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```
