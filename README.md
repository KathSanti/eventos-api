# eventos-api

Sistema de registro y venta de entradas para eventos (Java 21 + Spring Boot 3 + Spring Security).

## Endpoints

| Método | Ruta              | Acceso | Descripción |
|--------|-------------------|--------|-------------|
| GET    | `/auth/csrf`      | Público | Entrega la cookie `XSRF-TOKEN` y el nombre del header a usar. Pídelo antes de cualquier POST. |
| POST   | `/auth/registro`  | Público | Crea una cuenta con rol `CLIENTE`. Body: `{ "nombre", "email", "password" }` (password mínimo 8 caracteres). |
| POST   | `/auth/login`     | Público | Inicia sesión (cookie `JSESSIONID`). Body: `{ "email", "password" }`. |
| POST   | `/auth/logout`    | Autenticado | Cierra la sesión actual. |
| GET    | `/eventos`        | Público | Lista eventos: `nombre`, `fecha`, `precio` (sin exponer el `id`). |
| POST   | `/eventos`        | ADMINISTRADOR | Crea un evento nuevo. Body: `{ "nombre", "fecha" (YYYY-MM-DD), "precio" }`. |
| GET    | `/eventos/{nombre}` | Público | Devuelve el detalle de un evento, incluyendo su `id`. Se usa antes de comprar. |
| POST   | `/compras`        | CLIENTE | Compra entradas. Body: `{ "eventoId", "cantidad" }`. El cliente sale de la sesión autenticada, no del body. Devuelve `evento`, `cantidadEntradas`, `precioUnitario`, `precioTotal`. |

### Seguridad

- **Login con sesión + CSRF real**: tras `POST /auth/login` el servidor abre una sesión (cookie `JSESSIONID`). Todo POST/PUT/DELETE debe incluir el header `X-XSRF-TOKEN` con el valor que entrega `GET /auth/csrf` (que también llega como cookie `XSRF-TOKEN`, legible por JS). Sin ese token, una página externa que intente disparar un POST contra esta API es rechazada automáticamente.
- **Roles** (`Rol`: `CLIENTE`, `ADMINISTRADOR`) controlados con `@PreAuthorize` en los métodos de los controladores:
  - `EventoController.agregarEvento` → `hasRole('ADMINISTRADOR')`.
  - `CompraController.comprarEntradas` → `hasRole('CLIENTE')`.
- **Un cliente debe registrarse primero** (`POST /auth/registro`) e iniciar sesión (`POST /auth/login`) antes de poder comprar.
- **Administrador inicial**: al arrancar, si no existe ningún usuario `ADMINISTRADOR`, se crea uno automáticamente con los valores de `ADMIN_EMAIL` / `ADMIN_PASSWORD` / `ADMIN_NOMBRE` (por defecto `admin@eventos.com` / `Admin1234!` para desarrollo). **Cambia esa contraseña en cualquier entorno real.**

Flujo típico desde un cliente:
1. `GET /eventos` → muestra la lista al usuario (sin login).
2. Usuario elige un evento por nombre → `GET /eventos/{nombre}` para obtener su `id` (sin login).
3. Si no tiene cuenta: `POST /auth/registro`. Luego `POST /auth/login`.
4. `POST /compras` con ese `id` y la cantidad, usando la sesión ya autenticada.

### Ejemplo con curl

```bash
# 1) token CSRF (guarda cookies en cookies.txt)
TOKEN=$(curl -s -c cookies.txt http://localhost:8080/auth/csrf | python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")

# 2) registro
curl -b cookies.txt -c cookies.txt -X POST http://localhost:8080/auth/registro \
  -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $TOKEN" \
  -d '{"nombre":"Kat","email":"kat@test.com","password":"12345678"}'

# 3) login (reusa el mismo token/cookies)
curl -b cookies.txt -c cookies.txt -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $TOKEN" \
  -d '{"email":"kat@test.com","password":"12345678"}'

# 4) comprar (requiere un token CSRF fresco pedido con la misma sesión)
TOKEN=$(curl -s -b cookies.txt -c cookies.txt http://localhost:8080/auth/csrf | python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")
curl -b cookies.txt -c cookies.txt -X POST http://localhost:8080/compras \
  -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $TOKEN" \
  -d '{"eventoId":1,"cantidad":2}'
```

## Correr todo en Docker (sin instalar nada local)

Requiere solo Docker Desktop.

```bash
docker compose up --build
```

Esto construye la imagen de la API (Maven + JRE 21 en dos etapas) y levanta MySQL 8 en otro contenedor, conectados por red interna. La API queda en `http://localhost:8080`. Variables configurables en `docker-compose.yml` (`DB_*`, `ADMIN_*`).

Para bajar todo:

```bash
docker compose down        # mantiene los datos (volumen eventos_mysql_data)
docker compose down -v     # borra también los datos de MySQL
```

## Correr el proyecto sin Docker

- Java 21 (verifica con `java -version`; si tienes varias versiones, exporta `JAVA_HOME` apuntando a la 21).
- Maven (`brew install maven`).
- MySQL corriendo localmente con una base `eventos_db` creada:
  ```
  mysql -u root -e "CREATE DATABASE IF NOT EXISTS eventos_db;"
  ```

```bash
mvn spring-boot:run
```

o generando el jar:

```bash
mvn package -DskipTests
java -jar target/eventos-api-1.0.0.jar
```

La app queda en `http://localhost:8080`.

## Migrar de MySQL a Oracle Cloud (Autonomous Database)

Instrucciones detalladas dentro de `src/main/resources/application.properties`. En resumen:

1. En `pom.xml`: comenta `mysql-connector-j` y descomenta `ojdbc11`.
2. Descarga el wallet de tu Autonomous Database desde la consola de OCI.
3. Cambia `spring.datasource.*` en `application.properties` para apuntar al wallet (variable `TNS_ADMIN`) y usa el dialecto `OracleDialect`.
4. No subas el wallet ni las credenciales al repositorio (ya están en `.gitignore`); usa variables de entorno para usuario/contraseña.

Con `spring.jpa.hibernate.ddl-auto=update`, Hibernate crea/actualiza las tablas automáticamente también en Oracle, así que no necesitas migrar el esquema a mano.
