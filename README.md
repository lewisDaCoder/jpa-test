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

## Running the Application
1. Clone the repository
2. Build with Maven: `mvn clean install`
3. Run the application: `mvn spring-boot:run`

The application will:
1. Create sample User and Post entities
2. Demonstrate optimistic locking with version conflicts
3. Show transaction rollback scenarios

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
