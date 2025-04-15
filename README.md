# Spring JPA Transaction Test Project

## Overview
This project demonstrates two primary concepts:
1. The behavior of Spring JPA's `@Transactional` annotation and optimistic locking using JPA entities.
2. Java annotation patterns and best practices, including composite annotations, custom annotations with AOP, and common pitfalls.

## Features
- Independent JPA entities (User and Post) with version-based optimistic locking
- Transaction propagation and rollback demonstration
- H2 in-memory database integration
- Comprehensive examples of JPA transaction behavior
- Custom annotations with Spring AOP aspects
- Composite annotation demonstrations
- Annotation processing and testing

## Technology Stack
- Java 17
- Spring Boot 3.4.4
- Spring Data JPA
- Spring AOP
- H2 Database
- JUnit 5 & AssertJ
- Mockito
- Lombok
- Maven

## Project Structure
```
src/main/java/lewis/jpa/
├── JpaApplication.java            # Main application and transaction demos
├── JpaAnnotationDemoRunner.java   # Annotation demonstration runner
├── annotation/                    # Custom annotations
│   ├── Audited.java               # Method audit annotation
│   ├── CompositeTransactional.java# Composite transaction annotation
│   ├── Retry.java                 # Method retry annotation
│   └── SecuredOperation.java      # Security+transaction+audit annotation
├── aspect/                        # Annotation processors (AOP)
│   ├── AuditAspect.java           # Handles @Audited annotation
│   └── RetryAspect.java           # Handles @Retry annotation
├── dto
│   └── UserPostDto.java           # Data transfer object
├── entity
│   ├── Post.java                  # Post entity with @Version field
│   └── User.java                  # User entity with @Version field
├── repository
│   ├── PostRepository.java        # JPA repository for Post entity
│   └── UserRepository.java        # JPA repository for User entity
├── service
│   ├── AnnotationDemoService.java # Service demonstrating annotations
│   └── DataService.java           # Service with transaction demos
└── util
    └── ColoredOutput.java         # Console coloring utility
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

### 5. Java Annotation Patterns
The project demonstrates various Java annotation patterns, primarily through `AnnotationDemoService.java` and processed by aspects in the `lewis.jpa.aspect` package. The `JpaAnnotationDemoRunner.java` executes these demonstrations.

1.  **Custom Annotations with Attributes:**
    *   **`@Audited`**: Marks methods for auditing. Parameters: `action` (String description), `includeParams` (boolean). Processed by `AuditAspect` using `@Before`, `@AfterReturning`, and `@AfterThrowing` advice to log method entry, success, and failure.
    *   **`@Retry`**: Marks methods for automatic retry on specific exceptions. Parameters: `maxAttempts` (int), `delay` (long ms), `retryFor` (Class<? extends Throwable>[]), `noRetryFor` (Class<? extends Throwable>[]). Processed by `RetryAspect` using `@Around` advice to implement the retry logic.

2.  **Composite / Meta-Annotations:**
    *   **`@CompositeTransactional`**: Combines multiple `@Transactional` attributes (`readOnly=true`, `timeout=20`, `isolation=READ_COMMITTED`, `propagation=REQUIRED`) into a single, reusable annotation for standard read-only operations.
    *   **`@SecuredOperation`**: Combines transaction management (`@Transactional`) and method security (`@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")`) into one annotation. It also includes its own `value` attribute for description.

3.  **Annotation Processing with AOP:**
    *   Demonstrates how Spring AOP aspects (`AuditAspect`, `RetryAspect`) can intercept methods marked with custom annotations to add cross-cutting concerns like auditing and retry logic declaratively.

4.  **Testing Annotations:**
    *   Tests in `src/test/java/lewis/jpa/annotation/` show how to test annotation behavior, including verifying AOP aspect logic (log messages for `@Audited`, retry attempts for `@Retry`) and meta-annotation attribute aggregation (`@CompositeTransactional`, `@SecuredOperation`). Requires techniques like `@SpyBean` for testing aspects applied via AOP proxies.

## Running the Application
1.  Clone the repository
2.  Build with Maven: `mvn clean install`
3.  Run the application: `mvn spring-boot:run`

The application runs two sets of demonstrations by default:
*   **Transaction Demos:** Implemented as `CommandLineRunner` beans in `JpaApplication.java`. These run first.
*   **Annotation Demos:** Implemented as a single `CommandLineRunner` in `JpaAnnotationDemoRunner.java`. This runs after the transaction demos.

To enable or disable specific **transaction demos** (managed in `JpaApplication.java`):

### Option 1: Using Spring profiles

Create an `application.properties` file with the following settings to enable specific demo runners:

```properties
# Enable/disable specific transaction demo runners (setup is usually required)
spring.profiles.active=setup,demo1,demo3,demo5

# Alternatively, disable specific transaction demo runners
# spring.profiles.default=all # Keep 'all' if you want annotation demos to run
# spring.profiles.inactive=demo2,demo4
```

All transaction demo runners are annotated with both the "all" profile and their specific demo profile (e.g., "demo1", "demo2", etc.). The "setup" profile is used for the initial data setup. The annotation demos in `JpaAnnotationDemoRunner` run if the "all" profile (or no specific profile) is active.

### Option 2: Using command line arguments

Run with specific profiles via command line:

```bash
# Run setup, demo1, demo3, demo5 transaction demos AND the annotation demos
mvn spring-boot:run -Dspring.profiles.active=setup,demo1,demo3,demo5,all 

# Run only the annotation demos (no transaction demos or setup)
mvn spring-boot:run -Dspring.profiles.active=annotation-demo 
# (Note: JpaAnnotationDemoRunner would need @Profile("annotation-demo") added)
```

### Option 3: Modify source code

Open `JpaApplication.java` and comment out the `@Bean` annotation for any transaction demo you want to disable. The annotation demo runner can be disabled similarly in `JpaAnnotationDemoRunner.java`.

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

### 3. Self-Invocation Problem with AOP Annotations (@Transactional, @Retry, @Audited, etc.)
- **Problem**: Calling an annotated method (e.g., `@Transactional`, `@Retry`, `@Audited`) directly from another method within the same class instance bypasses the Spring AOP proxy. The proxy is responsible for applying the annotation's behavior (starting a transaction, applying retry logic, auditing). When bypassed, the annotation has no effect.
- **Demonstration**: See `AnnotationDemoService.badAnnotationUsage()` (USE CASE 7) which directly calls the `@Transactional` `findUserAndUpdate()` method, bypassing the transaction.
- **Solution**: To ensure the annotation logic is applied, you must call the method through the Spring-managed proxy instance of the class.
    - **Dependency Injection**: Inject the service dependency into itself (e.g., using `@Autowired` or constructor injection on the service itself). Call the method on the injected proxy instance.
    - **ApplicationContext**: Obtain the proxy instance from the `ApplicationContext`.
    - **Refactoring**: Move the annotated method to a separate Spring bean and inject that bean.
- **Correct Example**: See `AnnotationDemoService.correctAnnotationUsage()` (USE CASE 8) which demonstrates the concept of calling the proxied self (though the example implementation for getting the proxy is simplified for demonstration).
- **Testing**: When testing methods affected by self-invocation issues related to AOP, using `@SpyBean` is often necessary to ensure method calls go through the AOP proxy so that annotations like `@Retry` or `@Audited` are processed correctly by their aspects.

### 4. Testing Custom AOP Aspects
- Verifying aspect logic (e.g., log messages, method invocations, retry attempts) often requires capturing logs (`ListAppender` for Logback) or verifying interactions with spied beans (`@SpyBean`).
- Ensure the test context correctly loads the aspects and related configurations (`@SpringBootTest`, relevant profiles).

## Additional Resources
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Hibernate ORM Documentation](https://hibernate.org/orm/documentation/)

### Colorful Console Output

The application uses ANSI color codes to display colorful demo headers and footers in the console. This makes it easier to distinguish between different demos and their outputs.

If your terminal doesn't support ANSI colors, you may see escape sequences like `\u001B[32m` in the output. In that case, you can modify the `ColoredOutput` class to disable colors.
