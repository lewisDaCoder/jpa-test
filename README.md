# Spring JPA Transaction Test Project

## Overview
This project demonstrates the behavior of Spring JPA's `@Transactional` annotation and optimistic locking using JPA entities. It provides practical examples of transaction management, version-based concurrency control, and rollback scenarios.

## Features
- Independent JPA entities (User and Post) with version-based optimistic locking
- Transaction propagation and rollback demonstration
- H2 in-memory database integration
- Comprehensive examples of JPA transaction behavior

## Technology Stack
- Java 17
- Spring Boot 3.4.4
- Spring Data JPA
- H2 Database
- Lombok
- Maven

## Project Structure
```
src/main/java/lewis/jpa/
├── JpaApplication.java         # Main application and demonstration runner
├── dto
│   └── UserPostDto.java        # Data transfer object for User and Post data
├── entity
│   ├── Post.java               # Post entity with @Version field
│   └── User.java               # User entity with @Version field
├── repository
│   ├── PostRepository.java     # JPA repository for Post entity
│   └── UserRepository.java     # JPA repository for User entity
└── service
    └── DataService.java        # Service with @Transactional methods
```

## Key Concepts Demonstrated

### 1. @Transactional Annotation
The project demonstrates how the `@Transactional` annotation works in Spring JPA:
- Transaction boundaries
- All-or-nothing execution
- Automatic rollback on exceptions

### 2. Optimistic Locking
Using JPA's `@Version` annotation to implement optimistic locking:
- Detecting concurrent modifications
- Preventing data inconsistency
- Handling `OptimisticLockingFailureException`

### 3. Entity Management
- Independent entities with proper version field handling
- Entity lifecycle within transactions

### 4. Additional Transaction Use Cases
The project includes 10 additional transaction use cases that demonstrate:

1. **Transaction Propagation (REQUIRES_NEW)**
   - Creating new independent transactions from within existing ones
   - How child transactions can commit even if parent transactions roll back

2. **Read-Only Transactions**
   - Performance optimization for read-only operations
   - How read-only mode disables dirty checking and other write operations

3. **Transaction Isolation Levels**
   - Using SERIALIZABLE, REPEATABLE_READ, READ_COMMITTED, and READ_UNCOMMITTED
   - How isolation levels affect transaction behavior

4. **Custom Rollback Rules**
   - Configuring transaction rollback for specific exception types
   - Using `rollbackFor` and `noRollbackFor` attributes

5. **Transaction Timeout**
   - Setting time limits for long-running transactions
   - Handling transaction timeout exceptions

6. **Nested Transactional Methods**
   - Behavior of nested transactional method calls
   - How propagation affects nested transactions

7. **Bulk Operations in Transactions**
   - Performing batch updates within a transaction
   - Optimizing bulk operations

8. **Programmatic Transaction Management**
   - Using `TransactionTemplate` for manual transaction control
   - Advantages of programmatic vs. declarative transactions

9. **Self-Invocation Problem**
   - Understanding why calling @Transactional methods from within the same class fails
   - Solutions to the self-invocation problem

10. **Transaction Event Listeners**
    - Using `@TransactionEventListener` to respond to transaction events
    - Handling actions before/after transaction completion

## Running the Application
1. Clone the repository
2. Build with Maven: `mvn clean install`
3. Run the application: `mvn spring-boot:run`

The application runs all demonstration scenarios by default. Each scenario is implemented as a separate `CommandLineRunner` bean and runs in a specific order. To enable or disable specific demos:

### Option 1: Using Spring profiles

Create an `application.properties` file with the following settings to enable specific demo runners:

```properties
# Enable/disable specific demo runners
spring.profiles.active=setup,demo1,demo3,demo5

# Alternatively, disable specific demo runners
spring.profiles.default=all
spring.profiles.inactive=demo2,demo4
```

All demo runners are annotated with both the "all" profile and their specific demo profile (e.g., "demo1", "demo2", etc.). The "setup" profile is used for the initial data setup.

### Option 2: Using command line arguments

Run with specific profiles via command line:

```bash
mvn spring-boot:run -Dspring.profiles.active=setup,demo1,demo3,demo5
```

### Option 3: Modify source code

Open `JpaApplication.java` and comment out the `@Bean` annotation for any demo you want to disable.

## H2 Console
The H2 database console is available at: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: ` ` (empty)

## Common Issues and Solutions

### 1. NullPointerException with @Version fields
If you encounter NullPointerExceptions related to version fields:
- Ensure version fields are initialized with a default value (typically 0)
- Add null checks when accessing version values
- Use `@Builder.Default` when using Lombok's builder pattern

### 2. Transaction Rollback Issues
- Verify exceptions are properly propagated (not caught and suppressed)
- Check transaction boundaries are correctly defined
- Ensure proxied method calls (from same class) don't bypass transaction management

## Additional Resources
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Hibernate ORM Documentation](https://hibernate.org/orm/documentation/)

### Colorful Console Output

The application uses ANSI color codes to display colorful demo headers and footers in the console. This makes it easier to distinguish between different demos and their outputs.

If your terminal doesn't support ANSI colors, you may see escape sequences like `\u001B[32m` in the output. In that case, you can modify the `ColoredOutput` class to disable colors.
