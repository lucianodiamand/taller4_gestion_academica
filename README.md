# Sistema de Gestión Académica

Plataforma web integral para la administración académica universitaria: gestión de usuarios, materias, cursos, inscripciones a cursadas, mesas de examen e historia académica.

---

## 🛠️ Tecnologías Utilizadas

### Backend
- **Lenguaje:** Java 21
- **Framework:** Spring Boot 4.1.0
- **Seguridad:** Spring Security con autenticación Stateless mediante Tokens **JWT** (JSON Web Tokens)
- **Persistencia:** Spring Data JPA / Hibernate
- **Validaciones:** Bean Validation (`jakarta.validation`)
- **Herramienta de construcción:** Gradle

### Frontend
- **Framework:** Angular 21
- **Arquitectura:** Componentes **Standalone** y gestión reactiva con **Signals**
- **Formularios:** Reactive Forms (`FormBuilder`, validaciones sincrónicas)
- **Lenguaje:** TypeScript 5.9
- **Estilos:** CSS3 nativo centralizado con variables y diseño responsive

### Base de Datos
- **Motor:** PostgreSQL

### Inteligencia Artificial
- **Asistente:** *academ.ia* integrado con la API de DeepSeek mediante *function calling* para consultas académicas en lenguaje natural.

---

## 📂 Estructura del Proyecto

El repositorio está organizado como un monorepo dividido en dos carpetas principales:

```text
taller4_gestion_academica/
├── backend/
│   ├── src/main/java/com/ips/gestion_academica/
│   │   ├── controller/      # Endpoints REST (Auth, Usuarios, Materias, Cursos, Examenes, Inscripciones, Chat)
│   │   ├── dto/             # Objetos de transferencia de datos (Request y Response)
│   │   ├── exception/       # Manejo centralizado de errores (GlobalExceptionHandler)
│   │   ├── model/           # Entidades JPA (Usuario, Materia, Curso, Examen, Inscripcion, etc.)
│   │   ├── repository/      # Interfaces Spring Data JPA
│   │   ├── security/        # Configuración de Spring Security, filtros y utilidades JWT
│   │   └── service/         # Lógica de negocio y reglas de dominio
│   └── src/main/resources/
│       └── application.properties
│
├── frontend/
│   ├── src/app/
│   │   ├── core/            # Servicios transversales (Auth, Confirm, etc.), interceptores, guards y componentes compartidos
│   │   ├── features/        # Módulos por funcionalidad:
│   │   │   ├── login/               # Inicio de sesión
│   │   │   ├── home/                # Panel principal con módulos disponibles
│   │   │   ├── usuarios/            # ABM de usuarios (ADMIN)
│   │   │   ├── materias/            # ABM y consulta de materias
│   │   │   ├── cursos/              # Gestión de comisiones y oferta académica
│   │   │   ├── examenes/            # Programación de mesas de examen
│   │   │   ├── inscripciones/       # Inscripción a cursadas y seguimiento de estados
│   │   │   ├── inscripciones-examen/# Anotarse a exámenes y carga de notas
│   │   │   ├── historia-academica/  # Rendimiento académico del alumno
│   │   │   └── perfil/              # Edición de datos personales y cambio de contraseña
│   │   ├── app.routes.ts    # Enrutamiento lazy-loaded protegido por Guards
│   │   └── styles.css       # Sistema de estilos global
│   └── proxy.conf.json      # Proxy inverso para redirigir /api al backend (puerto 8080)
```

---

## 👥 Roles y Permisos

El sistema contempla tres perfiles de usuario con permisos segmentados:

1. **ADMIN:**
   - Alta, modificación y baja de Usuarios (Alumnos, Profesores y Administradores).
   - ABM completo de Materias y Cursos (comisiones y asignación docente).
   - Gestión y cancelación de inscripciones a cursadas y exámenes.
2. **PROFESOR:**
   - Consulta de catálogo de materias y cursos.
   - Gestión de exámenes de las comisiones donde está asignado como docente.
   - Carga y edición de notas de exámenes a los alumnos de sus cursos.
   - Modificación del estado de cursada de los alumnos (`REGULAR`, `APROBADO`, `LIBRE`).
3. **ALUMNO:**
   - Consulta de la oferta de materias y comisiones disponibles.
   - Autogestión de inscripción a cursos.
   - Inscripción a mesas de examen (requiere estar inscripto a la cursada).
   - Consulta de su **Historia Académica** (notas obtenidas y estados de cursada).

> *Todos los roles:* Tienen acceso a la edición de su propio perfil y al asistente virtual *academ.ia*.

---

## 🚀 Cómo Levantar el Proyecto Localmente

### 1. Requisitos Previos
- **Java JDK 21** instalado.
- **Node.js** (versión 18 o superior) y **npm**.
- **PostgreSQL** corriendo en `localhost:5432`.

---

### 2. Configurar la Base de Datos
Crear una base de datos en PostgreSQL y configurar las credenciales correspondientes en `backend/src/main/resources/application.properties`:

- **Base de datos:** `gestion_academica`
- **Puerto por defecto:** `5432`
- **Credenciales:** Ajustar `spring.datasource.username` y `spring.datasource.password` con los de tu entorno local.

---

### 3. Iniciar el Backend
Abrir una terminal en la raíz del proyecto:

```bash
cd backend
./gradlew bootRun
```

El servidor Spring Boot iniciará en **`http://localhost:8080`**.

---

### 4. Iniciar el Frontend
En otra terminal:

```bash
cd frontend
npm install
npm start
```

La aplicación web estará accesible en **`http://localhost:4200`**.  
El frontend ya cuenta con un proxy de desarrollo que redirige automáticamente las peticiones `/api` al backend.

---

## 🔐 Acceso Inicial
Para comenzar a operar en la plataforma, se debe ingresar con una cuenta con rol **ADMIN** para la gestión y configuración inicial de usuarios, materias y comisiones.
