[English](README.md) | [Español](README.es.md)

# Prices Service

Servicio REST desarrollado como solución a una prueba técnica de backend con Java y Spring Boot.

La aplicación permite consultar el precio aplicable a un producto de una cadena en una fecha y hora determinadas.

Cuando existen varias tarifas aplicables para el mismo producto y cadena, se selecciona la tarifa con **mayor prioridad**.

---

## Índice

* [Tecnologías](#tecnologías)
* [Arquitectura](#arquitectura)
* [Estructura del proyecto](#estructura-del-proyecto)
* [Regla de negocio](#regla-de-negocio)
* [API](#api)
* [Gestión de errores](#gestión-de-errores)
* [Persistencia](#persistencia)
* [Tests](#tests)
* [Postman](#postman)
* [Principios SOLID](#principios-solid)
* [Decisiones técnicas](#decisiones-técnicas)
* [Posibles mejoras para producción](#posibles-mejoras-para-producción)
* [Ejecución](#ejecución)

---

## Tecnologías

* Java 21
* Spring Boot 4.1.1
* Spring Web MVC
* Spring Data JPA
* H2 Database
* Maven
* JUnit 5
* AssertJ
* MockMvc
* Bean Validation

---

## Arquitectura

El proyecto utiliza una **arquitectura hexagonal**, separando el dominio de los detalles de infraestructura y de los mecanismos de entrada y salida.

```text
                         HTTP
                          │
                          ▼
                ┌──────────────────┐
                │ PriceController  │
                │   REST Adapter   │
                └────────┬─────────┘
                         │
                         ▼
                ┌──────────────────┐
                │   PriceService   │
                │    Use Case      │
                └────────┬─────────┘
                         │
                         ▼
                ┌──────────────────┐
                │      Price       │
                │     Domain       │
                └────────┬─────────┘
                         │
                         ▼
                ┌───────────────────┐
                │PriceRepositoryPort│
                │   Output Port     │
                └────────┬──────────┘
                         │
                         ▼
                ┌──────────────────┐
                │Repository Adapter│
                └────────┬─────────┘
                         │
                         ▼
                ┌──────────────────┐
                │ Spring Data JPA  │
                └────────┬─────────┘
                         │
                         ▼
                ┌──────────────────┐
                │   H2 Database    │
                └──────────────────┘
```

El diagrama representa el **flujo de una petición**. El sentido de las dependencias es diferente: el dominio define `PriceRepositoryPort` y el adaptador de infraestructura proporciona su implementación.

El dominio no depende de Spring, JPA ni H2.

### Separación de modelos

Se utilizan modelos independientes para cada responsabilidad:

```text
PriceEntity
     │
     │ PriceEntityMapper
     ▼
   Price
     │
     │ PriceRestMapper
     ▼
PriceResponse
```

* `PriceEntity`: modelo de persistencia dependiente de JPA.
* `Price`: modelo de dominio independiente de infraestructura.
* `PriceResponse`: DTO que define el contrato de la API REST.

---

## Estructura del proyecto

```text
src/
├── main/
│   ├── java/
│   │   └── com/example/prices_service/
│   │       ├── domain/
│   │       │   ├── exception/
│   │       ├── model/
│   │       ├── port/
│   │       └── service/
│   │
│   │       └── infrastructure/
│   │           └── adapter/
│   │               ├── in/
│   │               │   └── rest/
│   │               └── out/
│   │                   └── persistence/
│   │
│   └── resources/
│       ├── application.yml
│       └── data.sql
│
├── test/
│   └── java/
│       └── com/example/prices_service/
│           └── infrastructure/
│               └── adapter/
│                   ├── in/
│                   │   └── rest/
│                   └── out/
│                       └── persistence/
│
└── postman/
    ├── prices-service.postman_collection.json
    └── prices-service.postman_environment.json
```

---

## Regla de negocio

La consulta recibe:

* `brandId`
* `productId`
* `applicationDate`

Una tarifa es aplicable cuando:

```text
startDate <= applicationDate
AND
endDate >= applicationDate
```

Si existen varias tarifas aplicables, se selecciona la de mayor `priority`.

La selección se delega en la base de datos mediante una consulta equivalente a:

```sql
WHERE brand_id = ?
  AND product_id = ?
  AND start_date <= ?
  AND end_date >= ?
ORDER BY priority DESC
LIMIT 1
```

De esta forma, la aplicación no necesita recuperar múltiples registros para realizar posteriormente el filtrado y la ordenación en memoria.

---

## API

### Obtener precio aplicable

```http
GET /api/v1/prices
```

### Parámetros

| Parámetro         | Tipo            | Obligatorio | Descripción                                    |
| ----------------- | --------------- | ----------: | ---------------------------------------------- |
| `applicationDate` | `LocalDateTime` |          Sí | Fecha y hora para la que se consulta el precio |
| `productId`       | `Long`          |          Sí | Identificador del producto                     |
| `brandId`         | `Long`          |          Sí | Identificador de la cadena                     |

`applicationDate` utiliza el formato ISO-8601:

```text
yyyy-MM-dd'T'HH:mm:ss
```

Ejemplo:

```text
2020-06-14T16:00:00
```

La `T` es el separador estándar definido por ISO-8601 entre la fecha y la hora.

### Ejemplo

```http
GET http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1
```

Con `curl`:

```bash
curl "http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1"
```

### Respuesta 200 OK

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 2,
  "startDate": "2020-06-14T15:00:00",
  "endDate": "2020-06-14T18:30:00",
  "price": 25.45,
  "currency": "EUR"
}
```

---

## Gestión de errores

Las excepciones se gestionan de forma centralizada mediante `GlobalExceptionHandler`.

| Situación                     | HTTP              |
| ----------------------------- | ----------------- |
| Precio encontrado             | `200 OK`          |
| No existe tarifa aplicable    | `404 Not Found`   |
| Parámetro obligatorio ausente | `400 Bad Request` |
| Formato de fecha incorrecto   | `400 Bad Request` |
| Parámetro inválido            | `400 Bad Request` |

### 404 Not Found

```json
{
  "timestamp": "2026-08-30T20:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "No se ha encontrado ninguna tarifa aplicable para brandId=1, productId=35455, applicationDate=2019-01-01T00:00"
}
```

### 400 Bad Request

```json
{
  "timestamp": "2026-08-30T20:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Parametros de entrada invalidos: Required parameter 'applicationDate' is not present"
}
```

---

## Persistencia

Para la prueba técnica se utiliza **H2 en memoria**, evitando dependencias externas y permitiendo ejecutar el proyecto directamente.

El esquema se genera mediante Hibernate y los datos de ejemplo se cargan desde `data.sql`.

Configuración principal:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:pricesdb;DB_CLOSE_DELAY=-1
  jpa:
    hibernate:
      ddl-auto: create-drop
```

La consola H2 está disponible en:

```text
http://localhost:8080/h2-console
```

Datos de conexión:

| Campo      | Valor                  |
| ---------- | ---------------------- |
| JDBC URL   | `jdbc:h2:mem:pricesdb` |
| Usuario    | `sa`                   |
| Contraseña | *(vacía)*              |

### Índice

`PriceEntity` define un índice compuesto sobre:

```text
brand_id
product_id
start_date
end_date
```

Estas columnas participan directamente en el filtrado de la consulta de búsqueda de tarifas. El índice está orientado a mejorar la eficiencia de este filtrado a medida que aumenta el volumen de datos.

En un entorno productivo, su efectividad debería validarse mediante planes de ejecución sobre la base de datos utilizada.

---

## Tests

La aplicación incluye tests en diferentes niveles.

### Tests de persistencia

`PriceRepositoryAdapterTest` utiliza `@DataJpaTest` y H2 en memoria para verificar el comportamiento del adaptador de persistencia.

Se comprueban:

* Precio base.
* Selección de la tarifa con mayor prioridad.
* Ausencia de tarifas aplicables.
* Cadena desconocida.
* Diferentes fechas y franjas horarias.

### Tests de integración REST

`PriceControllerIntegrationTest` arranca el contexto completo de Spring Boot y utiliza `MockMvc` para probar el endpoint REST.

Se cubren los cinco escenarios principales del ejercicio:

| Fecha            | Resultado esperado |
| ---------------- | ------------------ |
| 14/06/2020 10:00 | Tarifa 1           |
| 14/06/2020 16:00 | Tarifa 2           |
| 14/06/2020 21:00 | Tarifa 1           |
| 15/06/2020 10:00 | Tarifa 3           |
| 16/06/2020 21:00 | Tarifa 4           |

Además:

* `404` cuando no existe una tarifa aplicable.
* `400` cuando falta `applicationDate`.
* `400` cuando `applicationDate` tiene un formato incorrecto.

---

## Postman

El proyecto incluye una colección de Postman y un entorno local:

```text
postman/
├── prices-service.postman_collection.json
└── prices-service.postman_environment.json
```

La colección contiene los cinco casos principales del ejercicio y diferentes escenarios de validación y error.

El entorno define:

```text
baseUrl = http://localhost:8080
```

### Uso

1. Arrancar la aplicación.
2. Abrir Postman.
3. Importar la colección.
4. Importar el entorno `Prices-service - Local`.
5. Seleccionar el entorno.
6. Ejecutar cualquiera de las peticiones.

El uso de `{{baseUrl}}` permite cambiar el servidor objetivo sin modificar las peticiones de la colección.

---

## Principios SOLID

El diseño aplica los principios SOLID de forma práctica:

| Principio                 | Aplicación                                                                                                                                     |
| ------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| **Single Responsibility** | Cada componente tiene una responsabilidad concreta: controlador REST, caso de uso, mappers, persistencia y gestión de errores están separados. |
| **Open/Closed**           | Es posible incorporar nuevos adaptadores de entrada o salida sin modificar el dominio.                                                         |
| **Liskov Substitution**   | Cualquier implementación de `PriceRepositoryPort` puede sustituir al adaptador actual sin modificar el caso de uso.                            |
| **Interface Segregation** | `PriceRepositoryPort` expone únicamente la operación que necesita el caso de uso.                                                              |
| **Dependency Inversion**  | El dominio depende de `PriceRepositoryPort`, no de Spring Data JPA ni de la implementación concreta de persistencia.                           |

---

## Decisiones técnicas

### `BigDecimal`

El precio utiliza `BigDecimal` para evitar problemas de precisión asociados a `float` y `double` en operaciones monetarias.

### `LocalDateTime`

Se utiliza `LocalDateTime` porque el ejercicio requiere fecha y hora, pero no especifica información de zona horaria.

### DTO independiente

`PriceResponse` está separado del modelo de dominio para mantener independiente el contrato público de la API.

### Dominio independiente de JPA

`Price` no contiene anotaciones ni dependencias de JPA.

La persistencia se realiza mediante `PriceEntity` y la conversión entre ambos modelos se centraliza en `PriceEntityMapper`.

### Repository Port

El dominio define `PriceRepositoryPort` y la infraestructura proporciona su implementación mediante `PriceRepositoryAdapter`.

Esto permite cambiar la tecnología de persistencia sin modificar el caso de uso.

### Gestión centralizada de excepciones

`GlobalExceptionHandler` concentra la conversión de excepciones a respuestas HTTP, manteniendo el controlador centrado en la gestión de la petición.

---

## Posibles mejoras para producción

La implementación está orientada a resolver la prueba técnica manteniendo una solución sencilla y mantenible.

En un entorno productivo podrían incorporarse:

* Base de datos persistente.
* Configuración mediante perfiles (`dev`, `test`, `prod`).
* OpenAPI/Swagger.
* Logs estructurados.
* Métricas y trazabilidad distribuida.
* Tests unitarios adicionales del dominio.
* Revisión de índices mediante planes de ejecución sobre la base de datos real.
* Configuración externa de credenciales y propiedades de infraestructura.
* Gestión de secretos mediante un sistema especializado.
* Health checks y observabilidad.

---

## Ejecución

### Ejecutar tests

```bash
mvn clean test
```

### Arrancar la aplicación

```bash
mvn spring-boot:run
```

La API estará disponible en:

```text
http://localhost:8080
```
