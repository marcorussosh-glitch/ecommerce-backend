# Electrical Shop E-Commerce

A custom e-commerce platform for an electrical supplies store, built with **Java, Spring Boot, and PostgreSQL**. The project aims to provide a maintainable backend for managing products, categories, and the core operations of an online shop.

**Status:** In development 

## Tech Stack

- **Java 25** — programming language
- **Spring Boot** — application framework
- **Spring Web** — REST API development
- **Spring Data JPA / Hibernate** — database access and ORM
- **PostgreSQL** — relational database
- **Flyway** — database schema migrations
- **Maven** — dependency management and build automation
- **JUnit / Mockito** — unit testing

## Current Features

### Product Management
- Retrieve products through a REST API.
- Create products using dedicated request DTOs.
- Retrieve individual products by ID.
- Associate products with categories.
- Handle missing resources through structured exceptions.

### Category Management
- Retrieve all categories and individual categories.
- Create and update categories.
- Prevent the deletion of categories associated with existing products.
- Return appropriate errors when a category does not exist or cannot be deleted.

### Backend Architecture
- Separation of responsibilities between controllers, services, repositories, and DTOs.
- Request validation using Jakarta Bean Validation.
- Centralized exception handling.
- Database schema management through versioned Flyway migrations.
- Unit tests for service-layer business logic.

## Project Structure

The backend follows a layered architecture:

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/it/commerce/backend/
│   │   │   ├── product/
│   │   │   ├── category/
│   │   │   └── ...
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/
│   └── test/
│       └── java/
├── pom.xml
└── README.md
```

The `product` and `category` packages contain the components responsible for their respective domain features.

## Getting Started

### Prerequisites

Make sure you have installed:

- Java 25 or a compatible configured JDK
- PostgreSQL
- Git

### Installation

1. Clone the repository:

   ```bash
   git clone <repository-url>
   ```

2. Navigate to the backend directory:

   ```bash
   cd backend
   ```

3. Create a PostgreSQL database for the application.

4. Configure the database connection in `application.properties` or through the environment variables supported by the application.

5. Run the application using the Maven wrapper on Windows:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   On Linux or macOS:

   ```bash
   ./mvnw spring-boot:run
   ```

Flyway applies the configured database migrations when the application starts.

> Database credentials should be provided through local configuration or environment variables and must not be committed to the repository.

## Running Tests

Execute the test suite with the Maven wrapper:

**Windows**
```powershell
.\mvnw.cmd test
```

**Linux / macOS**
```bash
./mvnw test
```

The tests focus on verifying service-layer behavior, including successful operations and error scenarios.

## Roadmap

Planned improvements include:

- [ ] Complete and expand automated test coverage.
- [ ] Implement additional product filtering and pagination features.
- [ ] Develop the Next.js frontend.
- [ ] Implement user authentication and authorization.
- [ ] Add customer registration and business-account approval.
- [ ] Introduce shopping cart and order management.
- [ ] Implement payment workflows.
- [ ] Add CSV-based product import and stock management.
- [ ] Build an administrative interface for managing products and categories.

## Project Goals

This project is also a learning and portfolio initiative focused on:

- Applying Java and Spring Boot to a real-world business scenario.
- Designing REST APIs and maintainable backend architectures.
- Practicing relational database design and schema migrations.
- Writing meaningful unit tests and handling errors consistently.
- Building a foundation for a full-stack e-commerce application.

## License

The project is currently under development. A license will be added once the distribution terms have been defined.
