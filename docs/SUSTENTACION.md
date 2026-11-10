# Guía de sustentación — Gestor de Torneos (Guía 7)

## 1. Qué hace el sistema en una frase

API REST que gestiona torneos de videojuegos. Los usuarios se autentican con **JWT**, tienen roles (**ADMIN**, **USER**) y solo el dueño de un torneo (o un administrador) puede modificarlo o borrarlo.

## 2. Arquitectura (para explicar en 2 minutos)

```
Controller  →  Service  →  Repository (interfaz)
  (HTTP)       (reglas)     (datos)
```

- **Controller**: recibe la petición, valida el cuerpo y devuelve un DTO. No tiene lógica de negocio.
- **Service**: reglas del negocio, por ejemplo el control de propiedad del torneo.
- **Repository**: es una **interfaz**. Hoy la implementación es en memoria; para usar PostgreSQL con JPA se agrega una nueva clase sin tocar controladores ni servicios.
- **Entidad `Torneo` ≠ DTO `TorneoResponse`**: la API nunca expone el modelo interno (por ejemplo, no devuelve `creadoPor`).

Paquetes: `auth` (login), `security` (JWT, filtro, roles, anti fuerza bruta), `torneo` (CRUD), `common` (errores).

## 3. Decisiones de diseño y por qué

| Decisión | Razón |
|---|---|
| API **stateless** con JWT, sin sesiones | Escala sin memoria compartida y no necesita CSRF (el token viaja en el header). |
| Contraseñas con **BCrypt** | Solo se guarda el hash; nunca el texto plano. |
| Secretos por **variables de entorno** | No hay claves en el código ni en Git. El `JWT_SECRET` debe tener al menos 32 bytes. |
| Tokens de **15 minutos** | Limita el daño si un token se filtra. |
| **Mismo mensaje** para usuario inexistente y clave incorrecta | Evita que un atacante descubra qué cuentas existen. |
| Bloqueo tras **5 fallos** durante **15 minutos** | Defensa contra fuerza bruta. |
| Errores con formato único y sin trazas | No se revela información interna (`include-stacktrace=never`). |
| Paginación con **máximo 50** | Evita consultas que consuman demasiados recursos. |
| Campos desconocidos en el JSON → **400** | Evita asignación masiva (un cliente no puede enviar `estado` ni `creadoPor`). |
| `Clock` inyectado | Las pruebas de expiración corren sin esperar minutos reales. |

## 4. Mapeo a OWASP API Security Top 10 (2023)

| Riesgo | Cómo se mitiga | Prueba que lo demuestra |
|---|---|---|
| API1 — BOLA (acceso a objetos ajenos) | Solo dueño o ADMIN modifica/borra | `otroUsuarioNoPuedeModificarUnTorneoAjeno` |
| API2 — Autenticación rota | JWT firmado, BCrypt, token alterado rechazado | `tokenAlteradoDevuelve401` |
| API3 — Propiedad de objeto a nivel de propiedad | DTO de respuesta + rechazo de campos extra | `unCampoNoPermitidoEnElCuerpoDevuelve400` |
| API4 — Consumo sin restricción | Tamaño de página máximo | `elListadoLimitaElTamanoDePagina` |
| API5 — Autorización de función (RBAC) | `/admin/**` solo ADMIN | `userNoPuedeEntrarAlEndpointDeAdmin` |
| API8 — Mala configuración | Sin trazas, cabeceras de seguridad, rutas no declaradas denegadas | `lasRespuestasIncluyenCabecerasDeSeguridad` |

## 5. Calidad y DevSecOps

- **Pruebas + cobertura**: JaCoCo genera el reporte y SonarCloud lo lee.
- **Mutation testing (PIT)**: modifica el código a propósito y verifica que las pruebas detecten el cambio. El build falla si el score es menor a **50 %**.
- **CI (GitHub Actions)**: compila, prueba, ejecuta SonarCloud con *Quality Gate* y PIT en cada push a `main`.
- **CodeQL** para análisis estático de seguridad y **Dependabot** para actualizar dependencias.

## 6. Cambios de esta entrega respecto a la versión recibida

1. **Error corregido en `LoginAttemptService`**: al vencer el bloqueo, el contador no se reiniciaba. Resultado: un solo fallo nuevo bloqueaba la cuenta otra vez. Se corrigió y se agregaron 2 pruebas (`trasDesbloquearseUnSoloFalloNoVuelveABloquear` y `trasDesbloquearseSeBloqueaDeNuevoAlTercerFallo`). Verificado de forma aislada: con el código original fallaban 2 casos; con la corrección pasan todos.
2. README: se documentan los tres usuarios de demostración (faltaba `user2`), la forma de correr las pruebas y el enlace a esta guía.

## 7. Preguntas probables y respuestas cortas

**¿Por qué JWT y no sesiones?** La API no guarda estado en el servidor; cada petición lleva su token. Facilita escalar horizontalmente.

**¿Cómo evitan que un usuario borre el torneo de otro?** El servicio compara el `creadoPor` del torneo con el usuario del token. Si no coincide y no es ADMIN, responde 403. Esto está en la capa de servicio, no solo en el controlador.

**¿Por qué no devuelven el dueño del torneo?** Porque es un dato interno. El DTO `TorneoResponse` no lo incluye, y la prueba `crearTorneoDevuelve201SinExponerAlPropietario` lo verifica.

**¿Qué pasa si alguien intenta enviar `"estado": "FINALIZADO"` al crear un torneo?** Jackson rechaza el campo desconocido y la API responde 400. El estado siempre lo decide el servidor.

**¿Por qué mensajes de error genéricos en el login?** Para no revelar si un usuario existe.

**¿Qué es mutation testing y para qué sirve?** Es un análisis que introduce pequeños errores en el código (por ejemplo, cambia `>=` por `>`) y comprueba que alguna prueba falle. Si ninguna falla, la prueba es débil aunque la cobertura sea alta. Por eso PIT mide algo que la cobertura no mide.

**¿Cómo cambiaría la base de datos a PostgreSQL?** Creando una clase `JpaTorneoRepository` que implemente `TorneoRepository`. Servicio y controlador no cambian.

## 8. Limitaciones conocidas (decirlas antes de que pregunten)

- **Datos en memoria**: al reiniciar la aplicación se pierden los torneos.
- **Bloqueo por usuario**: un atacante puede bloquear la cuenta de otra persona a propósito. Es un compromiso conocido entre seguridad y disponibilidad.
- **Contador de bloqueos por instancia**: con varios servidores, cada uno lleva su propio conteo. Se resolvería con Redis.
- **Memoria del contador de fallos**: los nombres de usuario inexistentes también se guardan, y no hay limpieza automática. Se podría mejorar con un tiempo máximo de vida por entrada.
- **JWT sin revocación**: un token válido sirve hasta que expira (15 min). Para cerrar sesión de forma inmediata haría falta una lista de revocación.
- **Estado del torneo**: existe el campo `estado`, pero no hay endpoint para cambiarlo. Queda como mejora.
- **Verificación**: el entorno donde se preparó esta entrega no tenía Maven ni acceso a Maven Central, así que las pruebas no se ejecutaron allí. **Antes de sustentar, ejecute `mvn verify` y confirme que todo pasa.** La corrección del bloqueo sí se verificó por separado con un script de comprobación.
