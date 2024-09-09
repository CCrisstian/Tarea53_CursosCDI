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
