# Cartex Backend

Backend del proyecto Cartex construido con Java 21, Spring Boot 3.2, Maven y Arquitectura Hexagonal.

## Stack

- **Java 21**
- **Spring Boot 3.2.5**
- **Maven**
- **PostgreSQL**
- **Arquitectura Hexagonal**
- **Railway** (despliegue)

## Estructura (Hexagonal)

```
com.cartex
├── domain          # Modelos, puertos (interfaces de repositorio)
├── application     # Casos de uso, DTOs, lógica de aplicación
└── infrastructure  # Controladores REST, adaptadores de persistencia, config, API externa
```

## Variables de Entorno

Copia `.env.example` a `.env` y ajusta los valores:

```bash
# Base de datos
DB_HOST=localhost
DB_PORT=5432
DB_NAME=cartex_db
DB_USER=postgres
DB_PASSWORD=postgres

# API externa (SAdmin)
EXTERNAL_API_AUTH_URL=https://security.sadmin.net/security/login
EXTERNAL_API_REPORTS_BASE_URL=https://reports.sadmin.net/api
EXTERNAL_API_USERNAME=tu_usuario
EXTERNAL_API_PASSWORD=tu_password

# Server
SERVER_PORT=8080

# JPA
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
```

## Ejecución Local

```bash
# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run
```

O ejecuta el JAR:

```bash
java -jar target/cartex-backend-0.0.1-SNAPSHOT.jar
```

## Endpoints Principales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST   | /api/users | Crear usuario |
| GET    | /api/users | Listar usuarios |
| GET    | /api/users/{id} | Obtener usuario |
| PUT    | /api/users/{id} | Actualizar usuario |
| DELETE | /api/users/{id} | Eliminar usuario |
| POST   | /api/payments | Crear pago |
| GET    | /api/payments/{id} | Obtener pago |
| GET    | /api/payments/user/{userId} | Pagos por usuario |
| GET    | /api/payments/user/{userId}/active | Verificar pago activo |
| PATCH  | /api/payments/{id}/cancel | Cancelar pago |

## Despliegue en Railway

1. Conecta el repositorio a Railway
2. Configura las variables de entorno en el dashboard de Railway
3. Railway detectará el `pom.xml` y desplegará automáticamente
4. Añade un servicio de PostgreSQL desde el marketplace de Railway
