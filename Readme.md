# HuertoHogar Backend

Backend de HuertoHogar desarrollado con Spring Boot
y Oracle Database.

Asignatura: DSY1104 - Desarrollo Fullstack II.

Integrantes:
- Jazmin Lopez
- Bryam Arciniegas

## Como ejecutarlo

1. En SQL Developer, conectado como el usuario `HUERTO_API`, ejecutar en orden los scripts de `database/`:
   `00_limpiar.sql`, `01_tablas.sql` y `02_datos_iniciales.sql`.
2. Copiar `application-local.properties.example` como `application-local.properties` (en la raiz),
   y completar la contrasena de la base de datos y un `app.jwt.secret` de al menos 32 caracteres.
   Ese archivo no se sube a Git.
3. Arrancar:

```bash
./mvnw spring-boot:run
```

- API: <http://localhost:8085/api/v1/productos>
- Swagger: <http://localhost:8085/swagger-ui.html>

## Autenticacion (JWT) y roles

Roles: `Administrador`, `Vendedor` y `Cliente`.

| Endpoint | Quien puede |
|---|---|
| `POST /api/v1/auth/login`, `POST /api/v1/auth/registro` | Todos (el registro siempre crea un Cliente) |
| `GET /api/v1/auth/me`, `POST /api/v1/auth/refresh` | Cualquier usuario con sesion |
| `GET` productos y categorias | Todos |
| `POST`, `PUT`, `DELETE` productos y categorias | Solo Administrador (el Vendedor solo consulta) |
| `POST /api/v1/boletas` (comprar) | Cliente y Administrador (el Vendedor no puede comprar) |
| `GET /api/v1/boletas` | Administrador y Vendedor |
| `/api/v1/usuarios` | Solo Administrador |

Para probar en Swagger: `POST /api/v1/auth/login`, copiar el `token` de la respuesta, pulsar **Authorize** y pegarlo.

Al arrancar se crean 3 cuentas de demostracion si no existen (se desactivan con `app.demo.usuarios=false`):

| Rol | Correo | Contrasena |
|---|---|---|
| Administrador | admin@duoc.cl | Admin2026! |
| Vendedor | vendedor@duoc.cl | Vendedor2026! |
| Cliente | cliente@gmail.com | Cliente2026! |

## Pruebas

```bash
./mvnw test -Dtest='JwtServiceTest,AuthServiceTest,SeguridadRolesTest'
```
Estas pruebas no necesitan base de datos.
