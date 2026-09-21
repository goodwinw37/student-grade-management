# Student Grade Management

A full-stack student gradebook with role-aware REST APIs and a React dashboard. The backend uses an in-memory database seeded with synthetic demo data, so a local run needs no external database.

## Stack

- Java 17, Spring Boot 3.4.12, Spring Data JPA and Spring Data REST
- Spring Security HTTP Basic with stateless role-based access control
- H2 in-memory database with SQL schema and seed scripts
- React 19, TypeScript, Vite 7, and npm
- JUnit 5, MockMvc, JaCoCo, Checkstyle, and SpotBugs

## Run locally

Requirements: JDK 17, Maven 3.9+, Node.js 18+, and npm.

Start the backend:

```bash
cd backend
mvn spring-boot:run
```

The API listens on `http://localhost:2800`. Swagger UI is available at `/swagger-ui.html`; the H2 console is at `/h2-console` with JDBC URL `jdbc:h2:mem:test;MODE=PostgreSQL;`.

Start the frontend in a second terminal:

```bash
cd frontend
npm ci
npm run dev
```

The Vite development server runs on `http://localhost:5173`. The database resets when the backend restarts. The bundled accounts and seed records are for local demonstration only and must not be reused in production.

For local demo credentials, set `DEMO_ADMIN_PASSWORD`, `DEMO_TEACHER_PASSWORD`, and `DEMO_STUDENT_PASSWORD` for the backend. Set the corresponding `VITE_DEMO_*_PASSWORD` variables for the frontend, using `frontend/.env.example` as a template. The development fallback values are intentionally non-secret and must be replaced outside local development.

## Architecture

The backend is organised into configuration, controllers, DTOs, JPA entities, repositories, and exception handling. `Student` and `Module` each have one-to-many relationships with `Registration` and `Grade`. `Registration` joins one student to one module; `Grade` joins one student to one module and stores score, academic year, and semester. A database uniqueness constraint prevents duplicate grades for the same student, module, year, and semester.

Spring Data REST exposes repository resources at `/students`, `/modules`, `/registrations`, and `/grades`. Custom controllers provide the application workflows:

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET, POST | `/management/students` | List and create students |
| GET, POST | `/management/modules` | List and create modules |
| GET, POST | `/management/registrations` | List and create registrations |
| GET | `/grades` | List grades |
| GET | `/grades/my` | List the authenticated student's grades |
| POST | `/grades/addGrade` | Add a grade |
| DELETE | `/grades/{gradeId}` | Delete a grade |
| GET | `/analytics/module-averages` | Calculate module averages |
| GET | `/analytics/student-averages` | Calculate student averages |
| GET | `/analytics/top-students` | Rank students by average |
| GET | `/analytics/top-modules` | Rank modules by average |

Management writes require the admin role. Grade creation is available to admins and teachers, grade viewing is available to all roles, and analytics require authentication.

## Testing and quality

Run the backend verification pipeline:

```bash
cd backend
mvn clean verify
```

The suite contains 56 JUnit 5 tests. The current build reports 96% line coverage, with a 90% per-package JaCoCo gate. Checkstyle uses Google style and SpotBugs runs with a high-severity threshold. The frontend has TypeScript/Vite production builds and an ESLint script:

```bash
cd frontend
npm run build
npm run lint
```

## License

MIT. See [LICENSE](LICENSE).
