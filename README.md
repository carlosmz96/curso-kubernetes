# Curso Kubernetes · Microservicios (Spring Boot 3)

Monorepo **Maven parent + 4 módulos** con un ejemplo funcional de arquitectura de microservicios para aprender/desplegar en **Docker Compose** y **Kubernetes**.

## 🧭 Estructura del repo

```
curso-kubernetes/
├─ pom.xml                     # Parent (packaging: pom)
├─ docker-compose.yml          # Entorno local (MySQL + Postgres + usuarios + cursos)
├─ msvc-usuarios/              # Microservicio de usuarios (MySQL, JWT Resource Server)
├─ msvc-cursos/                # Microservicio de cursos (PostgreSQL)
├─ msvc-gateway/               # Spring Cloud Gateway (descubrimiento y rutas)
├─ msvc-auth/                  # Authorization Server (OAuth2/JWT)
├─ *.yaml                      # Manifiestos de Kubernetes (deployments, services, configmap, secrets, PV/PVC)
└─ .vscode/, logs/             # Utilidades de desarrollo
```

### 🧩 Módulos

- **msvc-usuarios**  
  CRUD de usuarios y operaciones auxiliares. Persistencia en **MySQL**. Seguridad con **JWT (Resource Server)**; `BCryptPasswordEncoder` para hashes. Incluye cliente HTTP hacia *cursos* (rest-template/webclient) y configuración Kubernetes (`spring.cloud.kubernetes.*`).

- **msvc-cursos**  
  Gestión de cursos y relación **curso–usuario** (tabla/intermedia `CursoUsuario`). Persistencia en **PostgreSQL**. Cliente HTTP hacia *usuarios*. Preparado para descubrimiento en Kubernetes.

- **msvc-gateway**  
  **Spring Cloud Gateway** con rutas balanceadas por servicio:  
  - `/api/cursos/**` → `lb://msvc-cursos`  
  - `/api/usuarios/**` → `lb://msvc-usuarios`  
  Filtro `StripPrefix=2`. Actúa como punto de entrada único en k8s.

- **msvc-auth**  
  Servidor de **autorización OAuth2** que emite **JWT** (issuer propio). Los recursos (*usuarios*, *cursos*) validan el token vía **Resource Server**. `WebClient` con `@LoadBalanced` para llamadas internas.

## 🏗️ Stack

- **Java 17** · Spring Boot **3.5.6**
- Spring Web, Data JPA, Validation
- Spring Security · OAuth2 Resource Server · Authorization Server
- Spring Cloud Gateway · Spring Cloud Kubernetes (discovery, config/secret import)
- **MySQL 8**, **PostgreSQL 14**
- **Docker Compose**, **Kubernetes** (Deployments, Services, ConfigMaps, Secrets, PV/PVC)

## 🖼️ Arquitectura (resumen)

```text
[ Client ] → [ Gateway ] →  /api/usuarios → msvc-usuarios (MySQL)
                     └──→  /api/cursos   → msvc-cursos   (PostgreSQL)

                 [ Auth Server (msvc-auth) ] → emite JWT (issuer propio)
                 [ Descubrimiento: Spring Cloud Kubernetes ]
```

## ▶️ Arranque rápido (local con Docker Compose)

Requisitos: Docker Desktop/Engine y Maven.

```bash
# 1) Build de los microservicios
mvn -q -DskipTests package

# 2) Levantar stack local
docker compose up -d

# Servicios expuestos
# - msvc-usuarios:  http://localhost:8001
# - msvc-cursos:    http://localhost:8002
# - MySQL:          localhost:3307 (root/root, db msvc_usuarios)
# - PostgreSQL:     localhost:5532 (postgres/admin, db msvc_cursos)
```

> El `docker-compose.yml` incluye `mysql8`, `postgres14`, `msvc-usuarios` y `msvc-cursos`. El **gateway** y el **auth** están pensados para **Kubernetes** (ver más abajo).

## 🔐 Seguridad

- **msvc-auth** (puerto 9000 en k8s) actúa como **Authorization Server** y **issuer** de tokens.
- **msvc-usuarios** funciona como **Resource Server** y espera **JWT** válido en `Authorization: Bearer <token>`.
- Passwords de usuario cifradas con **BCrypt**.

> Variables como `LB_AUTH_ISSUER_URI` y `LB_USUARIOS_URI` se inyectan desde `ConfigMap` en k8s (ver `configmap.yaml`).

## ⚙️ Configuración (entorno)

### Variables típicas

**msvc-usuarios** (`application.properties`)
- `server.port` → `8001`
- `spring.datasource.url` → `jdbc:mysql://{DB_HOST}/{DB_DATABASE}`
- `spring.datasource.username` / `spring.datasource.password`
- `spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect`
- `spring.cloud.kubernetes.secrets.enable-api=true`
- `spring.cloud.kubernetes.discovery.all-namespaces=true`

**msvc-cursos** (`application.properties`)
- `server.port` → `8002`
- `spring.datasource.url` → `jdbc:postgresql://{DB_HOST}/{DB_DATABASE}`
- `spring.datasource.username` / `spring.datasource.password`
- `spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect`

**msvc-gateway** (`application.yml`)
- Rutas:
  ```yaml
  spring:
    cloud:
      gateway:
        routes:
          - id: msvc-cursos
            uri: lb://msvc-cursos
            predicates: [ Path=/api/cursos/** ]
            filters:   [ StripPrefix=2 ]
          - id: msvc-usuarios
            uri: lb://msvc-usuarios
            predicates: [ Path=/api/usuarios/** ]
            filters:   [ StripPrefix=2 ]
  ```

## 🗃️ Persistencia

- **Usuarios** (MySQL): entidad `Usuario` con validación Bean Validation.
- **Cursos** (PostgreSQL): entidad `Curso` + entidad de unión `CursoUsuario` para matriculaciones.
- `spring.jpa.generate-ddl=true` activa auto DDL en desarrollo.

## 📡 Endpoints (orientativos)

> Los controladores definen CRUD y operaciones de asociación. Nombres habituales:
- **Usuarios**: `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}` y endpoints auxiliares (búsqueda por email, listado por ids, etc.).
- **Cursos**: `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}`, inscripción/desinscripción de usuario en curso (`/asignar-usuario`, `/eliminar-usuario/{cursoId}`, etc.).

> En Kubernetes, consumir a través del **Gateway**:  
`GET http://<gateway-host>:8090/api/usuarios/...` · `GET http://<gateway-host>:8090/api/cursos/...`

## ☸️ Despliegue en Kubernetes

Archivos clave en la raíz del repo:

- **Bases de datos**
  - `mysql-pv.yaml`, `mysql-pvc.yaml`
  - `postgres-pv.yaml`, `postgres-pvc.yaml`
  - `deployment-mysql.yaml`, `svc-mysql.yaml`
  - `deployment-postgres.yaml`, `svc-postgres.yaml`

- **Aplicaciones**
  - `deployment-usuarios.yaml` (UTF‑16), `svc-usuarios.yaml` (LoadBalancer)
  - `deployment-cursos.yaml`, `svc-cursos.yaml`
  - `auth.yaml` (msvc-auth + Service LoadBalancer 9000)
  - `gateway.yaml` (msvc-gateway + Service LoadBalancer 8090)

- **Configuración**
  - `configmap.yaml` (URIs, puertos, URLs entre servicios; incluye bloques on-profile)
  - `secret.yaml` (credenciales DB, base64)

**Ejemplo de despliegue:**
```bash
# Namespace (opcional)
kubectl create ns demo || true
kubectl -n demo apply -f secret.yaml -f configmap.yaml

# Volúmenes y BDs
kubectl -n demo apply -f mysql-pv.yaml -f mysql-pvc.yaml                        -f postgres-pv.yaml -f postgres-pvc.yaml
kubectl -n demo apply -f deployment-mysql.yaml -f svc-mysql.yaml
kubectl -n demo apply -f deployment-postgres.yaml -f svc-postgres.yaml

# Servicios de negocio
kubectl -n demo apply -f deployment-usuarios.yaml -f svc-usuarios.yaml
kubectl -n demo apply -f deployment-cursos.yaml  -f svc-cursos.yaml

# Auth + Gateway
kubectl -n demo apply -f auth.yaml -f gateway.yaml
```

> **Notas**:  
> - `deployment-usuarios.yaml` está codificado en **UTF‑16**. Si necesitas editarlo, conviértelo a UTF‑8.  
> - `ConfigMap` define `lb_auth_issuer_uri`, `lb_usuarios_uri`, etc. Ajusta IP/puertos a tu clúster o Ingress.  
> - Los Services están en modo `LoadBalancer` (cámbialo a `NodePort/ClusterIP` según tu entorno).

## 🧪 Desarrollo local (sin Docker)

```bash
# BD locales levantadas (o en Docker)
# MySQL → jdbc:mysql://localhost:3306/msvc_usuarios
# Postgres → jdbc:postgresql://localhost:5432/msvc_cursos

# Lanzar cada microservicio desde su carpeta
cd msvc-usuarios && mvn spring-boot:run
cd msvc-cursos   && mvn spring-boot:run
# (gateway y auth están orientados a k8s, también pueden ejecutarse con mvn)
```

## 🧰 Troubleshooting

- Errores de conexión DB → revisa `DB_HOST`, `DB_DATABASE`, usuario/clave o Secrets en k8s.
- 401/403 al llamar a **usuarios** → asegura **JWT** válido emitido por `msvc-auth` y que `issuer` coincide.
- Descubrimiento en k8s → `spring.cloud.kubernetes.discovery.all-namespaces=true` permite resolver `lb://...` entre namespaces.
- Logs en contenedor → `logging.file.path=/app/logs` en *usuarios* (mapea volumen si quieres persistencia).

---

**Autor:** Carlos Muñoz · *Proyecto de aprendizaje de microservicios + Kubernetes*
