# eventos-api

API REST para el registro y venta de entradas de eventos.

**Stack:** Java 21 · Spring Boot 3.3.4 (Web, Data JPA, Validation) · Spring Security · MySQL 8 · H2 (tests) · Docker · GitHub Actions · AWS EC2

---

## Endpoints

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET  | `/auth/csrf` | Público | Entrega el token CSRF (y la cookie `XSRF-TOKEN`). Pídelo antes de cualquier POST. |
| POST | `/auth/registro` | Público | Crea una cuenta `CLIENTE`. Body: `{ "nombre", "email", "password" }` (mín. 8 caracteres). |
| POST | `/auth/login` | Público | Inicia sesión (cookie `JSESSIONID`). Body: `{ "email", "password" }`. |
| POST | `/auth/logout` | Autenticado | Cierra la sesión. |
| GET  | `/eventos` | Público | Lista eventos: `nombre`, `fecha`, `precio`. |
| GET  | `/eventos/{nombre}` | Público | Detalle de un evento, incluido su `id` (se usa para comprar). |
| POST | `/eventos` | ADMINISTRADOR | Crea un evento. Body: `{ "nombre", "fecha" (YYYY-MM-DD), "precio" }`. |
| POST | `/compras` | CLIENTE | Compra entradas. Body: `{ "eventoId", "cantidad" }`. Devuelve `evento`, `cantidadEntradas`, `precioUnitario`, `precioTotal`. |

**Flujo de compra:** `GET /eventos` → `GET /eventos/{nombre}` (obtener `id`) → `POST /auth/registro` → `POST /auth/login` → `POST /compras`.

## Seguridad

- **Sesión + CSRF:** el login abre una sesión en el servidor (cookie `JSESSIONID`). Todo POST debe enviar el header `X-XSRF-TOKEN` con el token de `GET /auth/csrf`; sin él la API responde **403**. HTTP Basic y formLogin están desactivados.
- **Roles** con `@PreAuthorize`: solo `ADMINISTRADOR` crea eventos y solo `CLIENTE` compra.
- **Contraseñas** cifradas con BCrypt.
- **DTOs:** las respuestas nunca exponen entidades ni contraseñas.
- **Administrador inicial:** al arrancar, `AdminBootstrap` crea un admin con `ADMIN_EMAIL` / `ADMIN_PASSWORD` / `ADMIN_NOMBRE` si todavía no existe ninguno.
- **Errores:** `GlobalExceptionHandler` responde 400, 401, 403, 404 y 409. La ruta `/error` es pública para que los errores de Spring Security, las rutas inexistentes y los 500 no se muestren como 401.

---

## Desarrollo local

Requiere solo **Docker Desktop**.

1. Crea tu archivo de variables copiando la plantilla y cambia las claves:
   ```bash
   cp .env.example .env
   ```
   `DB_USER` no puede ser `root`. Evita el carácter `$` en las claves.

2. Levanta la API y MySQL:
   ```bash
   docker compose up --build
   ```

3. Prueba:
   - API: `http://localhost:8080` (con Postman: primero `GET /auth/csrf`).
   - MySQL Workbench: host `127.0.0.1`, puerto **`3307`**, usuario `eventos_app` o `root`.

4. Bajar:
   ```bash
   docker compose down      # mantiene los datos
   docker compose down -v   # borra los datos (necesario si cambias usuario o clave de MySQL)
   ```

**Tests** (usan H2 en memoria, no necesitan MySQL ni Docker):
```bash
mvn test
```

---

## Producción (AWS + GitHub Actions)

```
GitHub (push a main) ──▶ tests ──▶ imagen en Docker Hub ──▶ deploy por SSH
                                                               │
Internet ──:80──▶ EC2 eventos-app (IP elástica) ──IP privada:3306──▶ EC2 eventos-db (MySQL)
```

La app y la base van en **dos instancias** para que Spring Boot y MySQL no compitan por el 1 GB de RAM.

### 1. Infraestructura en AWS (manual, una vez)

**Security Groups**

| Grupo | Puerto | Origen |
|-------|--------|--------|
| `sg-eventos-app` | 22 (SSH) | `0.0.0.0/0` (GitHub Actions despliega por SSH) |
| `sg-eventos-app` | 80 (HTTP) | `0.0.0.0/0` |
| `eventos-db` | 3306 (MySQL) | **`sg-eventos-app`** (solo la app) |
| `eventos-db` | 22 (SSH) | Mi IP |

**Instancias EC2** (las dos iguales, en la misma subred):

- `eventos-app` y `eventos-db`: Ubuntu Server 24.04 LTS **ARM64**, `t4g.micro`, key pair `vockey`, IP pública habilitada, cada una con su Security Group.
- En *Detalles avanzados → User data*, este script para instalar Docker:
  ```bash
  #!/bin/bash
  set -eux
  apt-get update
  apt-get install -y curl
  curl -fsSL https://get.docker.com -o docker.sh
  sh docker.sh
  apt-get install -y docker-compose-plugin
  systemctl enable docker
  systemctl start docker
  usermod -aG docker ubuntu
  ```
- **IP elástica:** asignar una y asociarla a `eventos-app`. Es la IP fija para el deploy y para usar la API.

### 2. Base de datos (manual, una vez)

Conéctate a `eventos-db` (botón **Conectar** en la consola de AWS o por SSH) y:

```bash
mkdir -p ~/eventos-db && cd ~/eventos-db
nano docker-compose.db.yml   # pegar el contenido de docker-compose.db.yml del repo
nano .env                    # DB_NAME, DB_USER, DB_PASSWORD, DB_ROOT_PASSWORD con claves de producción
docker compose -f docker-compose.db.yml up -d
```

Comprueba con `docker compose -f docker-compose.db.yml logs mysql | grep "ready for connections"`.
Anota la **IP privada** de `eventos-db`: va en el secreto `DB_HOST`.

### 3. Secrets en GitHub

*Settings → Secrets and variables → Actions → New repository secret*

| Grupo | Secrets |
|-------|---------|
| Docker Hub | `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN` (token Read & Write), `DOCKER_IMAGE_NAME` |
| EC2 app | `EC2_HOST` (IP elástica), `EC2_USER` (`ubuntu`), `EC2_SSH_KEY` (contenido completo del `.pem`) |
| Base de datos | `DB_HOST` (IP privada de `eventos-db`), `DB_NAME`, `DB_USER`, `DB_PASSWORD` (**iguales** al `.env` de la base) |
| Admin | `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NOMBRE` |

### 4. Deploy

Cada **push a `main`** (o *Actions → Run workflow*) ejecuta `.github/workflows/deploy.yml`:

1. **tests:** `mvn test` con H2.
2. **docker:** construye la imagen (amd64 + arm64) y la publica en Docker Hub.
3. **deploy-ec2:** copia `docker-compose.prod.yml` a la EC2, hace `pull` de la imagen y levanta el contenedor con el perfil `prod`.

Al arrancar, la API se conecta a MySQL por la IP privada, crea las tablas y el usuario admin.
Verifica en `http://<IP_ELASTICA>/eventos`.

**Al terminar de trabajar:** detén las dos instancias (no las termines) y cierra el lab.

---

## Archivos de configuración

| Archivo | Uso |
|---------|-----|
| `docker-compose.yml` | Desarrollo local: API + MySQL |
| `docker-compose.prod.yml` | EC2 `eventos-app`: solo la API, imagen desde Docker Hub |
| `docker-compose.db.yml` | EC2 `eventos-db`: solo MySQL |
| `application.properties` | Configuración base (desarrollo) |
| `application-prod.properties` | Producción: sin valores por defecto |
| `application-test.properties` | Tests: H2 en memoria |
| `.env.example` | Plantilla de variables (`.env` nunca se sube a git) |

Test
