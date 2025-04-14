package lewis.jpa;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;

import lewis.jpa.dto.UserPostDto;
import lewis.jpa.entity.User;
import lewis.jpa.service.DataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring JPA Transaction Demo Application
 * 
 * This application demonstrates various aspects of @Transactional behavior in Spring JPA.
 * 
 * Each demo is implemented as a separate CommandLineRunner bean and can be enabled/disabled
 * using Spring profiles. By default, all demos run when the "all" profile is active.
 * 
 * How to run specific demos:
 * 
 * 1. Run with specific profiles activated:
 *    java -jar jpa-demo.jar --spring.profiles.active=setup,demo1,demo3
 * 
 * 2. Set active profiles in application.properties:
 *    spring.profiles.active=setup,demo2,demo5
 * 
 * 3. Always include "setup" profile to initialize test data
 * 
 * Available profiles:
 * - setup  : Initialize test data
 * - demo1  : Optimistic Locking
 * - demo2  : Transaction Rollback
 * - demo3  : Transaction Propagation REQUIRES_NEW
 * - demo4  : Read-Only Transactions
 * - demo5  : Transaction Isolation Levels
 * - demo6  : Custom Rollback Rules
 * - demo7  : Transaction Timeout
 * - demo8  : Nested Transactional Methods
 * - demo9  : Bulk Operations
 * - demo10 : Programmatic Transaction Management
 * - demo11 : Self-Invocation Problem
 * - demo12 : Transaction Event Listeners
 * - all    : Run all demos (default)
 */
@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class JpaApplication {
	
	private final DataService dataService;

	public static void main(String[] args) {
		SpringApplication.run(JpaApplication.class, args);
	}

	/**
	 * Initial data setup runner - always runs first
	 */
	@Bean
	@Order(1)
	@org.springframework.context.annotation.Profile({"all", "setup"})
	public CommandLineRunner initialDataSetup() {
		return args -> {
			log.info("SETUP: Initializing test data...");
			
			// Create test data
			List<UserPostDto> testData = Arrays.asList(
				UserPostDto.builder()
					.username("john_doe")
					.email("john.doe@example.com")
					.firstName("John")
					.lastName("Doe")
					.postTitle("My First Post")
					.postContent("This is the content of my first post.")
					.build(),
					
				UserPostDto.builder()
					.username("jane_smith")
					.email("jane.smith@example.com")
					.firstName("Jane")
					.lastName("Smith")
					.postTitle("Hello World")
					.postContent("Hello World! This is my first post.")
					.build(),
					
				UserPostDto.builder()
					.username("bob_johnson")
					.email("bob.johnson@example.com")
					.firstName("Bob")
					.lastName("Johnson")
					.postTitle("Spring JPA is Great")
					.postContent("I'm learning Spring JPA and it's great!")
					.build()
			);
			
			// Process test data using the transactional service method
			dataService.processUserPosts(testData);
			
			log.info("SETUP: Initial test data created successfully");
		};
	}
	
	/**
	 * Demo 1: Basic Optimistic Locking
	 */
	@Bean
	@Order(2)
	@org.springframework.context.annotation.Profile({"all", "demo1"})
	public CommandLineRunner optimisticLockingDemo() {
		return args -> {
			log.info("\n\n=== DEMO 1: OPTIMISTIC LOCKING ===\n");
			
			try {
				// Update user with ID 1
				User updatedUser = dataService.updateUserEmail(1L, "john.doe.updated@example.com");
				log.info("Successfully updated user email. New version: {}", updatedUser.getVersion());
				
				// Simulate concurrent update by getting the same entity in a separate transaction
				User userForConcurrentUpdate = dataService.simulateConcurrentModification(1L);
				
				// First update the user again through the regular method to increment version in database
				dataService.updateUserEmail(1L, "another.update@example.com");
				
				// Now try to update the stale entity (userForConcurrentUpdate has old version)
				// This will cause OptimisticLockingFailureException
				log.info("Attempting update with stale entity version: {}", userForConcurrentUpdate.getVersion());
				userForConcurrentUpdate.setEmail("concurrent.update@example.com");
				
				// This will fail due to version mismatch
				dataService.updateExistingUser(userForConcurrentUpdate);
				
				log.info("Concurrent update succeeded (this shouldn't happen with proper version checking)");
			} catch (OptimisticLockingFailureException e) {
				log.info("Optimistic locking worked! Concurrent update was prevented: {}", e.getMessage());
			} catch (Exception e) {
				log.error("Error during demonstration: {}", e.getMessage(), e);
			}
			
			log.info("\n=== DEMO 1: OPTIMISTIC LOCKING COMPLETE ===\n");
		};
	}
	
	/**
	 * Demo 2: Transaction Rollback
	 */
	@Bean
	@Order(3)
	@org.springframework.context.annotation.Profile({"all", "demo2"})
	public CommandLineRunner transactionRollbackDemo() {
		return args -> {
			log.info("\n\n=== DEMO 2: TRANSACTION ROLLBACK ===\n");
			
			try {
				// First successful transaction
				log.info("Executing successful transaction (no rollback)");
				boolean successResult = dataService.updateUserAndPost(1L, 1L, false);
				log.info("Transaction completed successfully: {}", successResult);
				
				// Then failing transaction
				log.info("\nExecuting failing transaction (should rollback)");
				dataService.updateUserAndPost(2L, 2L, true);
				log.info("This line shouldn't be reached due to exception");
			} catch (RuntimeException e) {
				log.info("Transaction was rolled back as expected: {}", e.getMessage());
			}
			
			log.info("\n=== DEMO 2: TRANSACTION ROLLBACK COMPLETE ===\n");
		};
	}
	
	/**
	 * Demo 3: Transaction Propagation REQUIRES_NEW
	 */
	@Bean
	@Order(4)
	@org.springframework.context.annotation.Profile({"all", "demo3"})
	public CommandLineRunner requiresNewPropagationDemo() {
		return args -> {
			log.info("\n\n=== DEMO 3: REQUIRES_NEW PROPAGATION ===\n");
			
			UserPostDto user1 = UserPostDto.builder()
					.username("propagation_outer")
					.email("outer@example.com")
					.firstName("Outer")
					.lastName("Transaction")
					.build();
			
			UserPostDto user2 = UserPostDto.builder()
					.username("propagation_inner")
					.email("inner@example.com")
					.firstName("Inner")
					.lastName("Transaction")
					.build();
			
			// Test that inner transaction commits even if outer fails
			try {
				log.info("Testing REQUIRES_NEW propagation with outer transaction failure");
				dataService.nestedTransactionExample(user1, user2, true);
			} catch (Exception e) {
				log.info("Outer transaction failed as expected: {}", e.getMessage());
				
				// Inner transaction's user should still exist, but outer's should not
				boolean innerUserExists = dataService.getAllUsersReadOnly().stream()
						.anyMatch(u -> "propagation_inner".equals(u.getUsername()));
				boolean outerUserExists = dataService.getAllUsersReadOnly().stream()
						.anyMatch(u -> "propagation_outer".equals(u.getUsername()));
				
				log.info("Inner transaction commit status: User exists = {}", innerUserExists);
				log.info("Outer transaction rollback status: User exists = {}", outerUserExists);
			}
			
			log.info("\n=== DEMO 3: REQUIRES_NEW PROPAGATION COMPLETE ===\n");
		};
	}
	
	/**
	 * Demo 4: Read-Only Transactions
	 */
	@Bean
	@Order(5)
	@org.springframework.context.annotation.Profile({"all", "demo4"})
	public CommandLineRunner readOnlyTransactionDemo() {
		return args -> {
			log.info("\n\n=== DEMO 4: READ-ONLY TRANSACTIONS ===\n");
			
			// Get users with read-only optimization
			List<User> users = dataService.getAllUsersReadOnly();
			log.info("Retrieved {} users with read-only transaction", users.size());
			
			// If we try to modify entities from a read-only transaction
			// and save them, there may be unexpected behavior depending on
			// the persistence provider
			
			log.info("\n=== DEMO 4: READ-ONLY TRANSACTIONS COMPLETE ===\n");
		};
	}
	
	/**
	 * Demo 5: Transaction Isolation Levels
	 */
	@Bean
	@Order(6)
	@org.springframework.context.annotation.Profile({"all", "demo5"})
	public CommandLineRunner isolationLevelsDemo() {
		return args -> {
			log.info("\n\n=== DEMO 5: TRANSACTION ISOLATION LEVELS ===\n");
			
			try {
				// Update with serializable isolation - strongest isolation level
				User updatedUser = dataService.updateUserWithSerializableIsolation(1L, "serializable@example.com");
				log.info("Updated user with SERIALIZABLE isolation level. New version: {}", updatedUser.getVersion());
			} catch (Exception e) {
				log.error("Error during isolation level demonstration: {}", e.getMessage(), e);
			}
			
			log.info("\n=== DEMO 5: TRANSACTION ISOLATION LEVELS COMPLETE ===\n");
		};
	}
	
	/**
	 * Demo 6: Custom Rollback Rules
	 */
	@Bean
	@Order(7)
	@org.springframework.context.annotation.Profile({"all", "demo6"})
	public CommandLineRunner customRollbackRulesDemo() {
		return args -> {
			log.info("\n\n=== DEMO 6: CUSTOM ROLLBACK RULES ===\n");
			
			try {
				log.info("Testing transaction with IllegalArgumentException (should rollback)");
				dataService.updateUserWithCustomRollbackRules(1L, "will.rollback@example.com", true, "illegalArgument");
			} catch (IllegalArgumentException e) {
				log.info("IllegalArgumentException caught, transaction should rollback: {}", e.getMessage());
			} catch (Exception e) {
				log.error("Unexpected exception: {}", e.getMessage(), e);
			}
			
			try {
				log.info("\nTesting transaction with UnsupportedOperationException (should NOT rollback)");
				dataService.updateUserWithCustomRollbackRules(2L, "wont.rollback@example.com", true, "unsupportedOperation");
			} catch (UnsupportedOperationException e) {
				log.info("UnsupportedOperationException caught, transaction should NOT rollback: {}", e.getMessage());
				
				// Verify that changes were committed despite the exception
				User user = dataService.getAllUsersReadOnly().stream()
						.filter(u -> u.getId().equals(2L))
						.findFirst()
						.orElse(null);
				
				if (user != null) {
					log.info("Verified user email was updated to: {}", user.getEmail());
				}
			} catch (Exception e) {
				log.error("Unexpected exception: {}", e.getMessage(), e);
			}
			
			log.info("\n=== DEMO 6: CUSTOM ROLLBACK RULES COMPLETE ===\n");
		};
	}
	
	/**
	 * Demo 7: Transaction Timeout
	 */
	@Bean
	@Order(8)
	@org.springframework.context.annotation.Profile({"all", "demo7"})
	public CommandLineRunner transactionTimeoutDemo() {
		return args -> {
			log.info("\n\n=== DEMO 7: TRANSACTION TIMEOUT ===\n");
			
			try {
				log.info("Testing transaction with timeout (4-second operation, 5-second timeout)");
				User user = dataService.updateUserWithTimeout(1L, "timeout@example.com", true);
				log.info("Transaction completed within timeout. Updated user version: {}", user.getVersion());
			} catch (Exception e) {
				log.error("Error during timeout demonstration: {}", e.getMessage(), e);
			}
			
			log.info("\n=== DEMO 7: TRANSACTION TIMEOUT COMPLETE ===\n");
		};
	}
	
	/**
	 * Demo 8: Nested Transactional Methods
	 */
	@Bean
	@Order(9)
	@org.springframework.context.annotation.Profile({"all", "demo8"})
	public CommandLineRunner nestedTransactionsDemo() {
		return args -> {
			log.info("\n\n=== DEMO 8: NESTED TRANSACTIONS ===\n");
			
			UserPostDto user1 = UserPostDto.builder()
					.username("nested_outer")
					.email("outer@example.com")
					.firstName("Outer")
					.lastName("Transaction")
					.build();
			
			UserPostDto user2 = UserPostDto.builder()
					.username("nested_inner")
					.email("inner@example.com")
					.firstName("Inner")
					.lastName("Transaction")
					.build();
			
			// Test successful nested transactions
			try {
				log.info("Testing successful nested transactions");
				dataService.nestedTransactionExample(user1, user2, false);
				log.info("Both transactions completed successfully");
			} catch (Exception e) {
				log.error("Unexpected error: {}", e.getMessage(), e);
			}
			
			log.info("\n=== DEMO 8: NESTED TRANSACTIONS COMPLETE ===\n");
		};
	}
	
	/**
	 * Demo 9: Bulk Operations in Transactions
	 */
	@Bean
	@Order(10)
	@org.springframework.context.annotation.Profile({"all", "demo9"})
	public CommandLineRunner bulkOperationsDemo() {
		return args -> {
			log.info("\n\n=== DEMO 9: BULK OPERATIONS ===\n");
			
			try {
				// First set all users to inactive
				int inactiveCount = dataService.bulkUpdateUserStatus(false);
				log.info("Set {} users to inactive", inactiveCount);
				
				// Then set all back to active
				int activeCount = dataService.bulkUpdateUserStatus(true);
				log.info("Set {} users to active", activeCount);
			} catch (Exception e) {
				log.error("Error during bulk operations: {}", e.getMessage(), e);
			}
			
			log.info("\n=== DEMO 9: BULK OPERATIONS COMPLETE ===\n");
		};
	}
	
	/**
	 * Demo 10: Programmatic Transaction Management
	 */
	@Bean
	@Order(11)
	@org.springframework.context.annotation.Profile({"all", "demo10"})
	public CommandLineRunner programmaticTransactionsDemo() {
		return args -> {
			log.info("\n\n=== DEMO 10: PROGRAMMATIC TRANSACTIONS ===\n");
			
			UserPostDto dto = UserPostDto.builder()
					.username("programmatic_tx")
					.email("programmatic@example.com")
					.firstName("Programmatic")
					.lastName("Transaction")
					.build();
			
			try {
				dataService.programmaticTransactionExample(dto);
				log.info("Programmatic transaction completed successfully");
			} catch (Exception e) {
				log.error("Error in programmatic transaction: {}", e.getMessage(), e);
			}
			
			log.info("\n=== DEMO 10: PROGRAMMATIC TRANSACTIONS COMPLETE ===\n");
		};
	}
	
	/**
	 * Demo 11: Self-Invocation Problem
	 */
	@Bean
	@Order(12)
	@org.springframework.context.annotation.Profile({"all", "demo11"})
	public CommandLineRunner selfInvocationProblemDemo() {
		return args -> {
			log.info("\n\n=== DEMO 11: SELF-INVOCATION PROBLEM ===\n");
			
			try {
				dataService.demonstrateSelfInvocationIssue(null);
				
				// Verify if transaction actually took place
				User user = dataService.getAllUsersReadOnly().stream()
						.filter(u -> u.getId().equals(1L))
						.findFirst()
						.orElse(null);
				
				if (user != null) {
					log.info("User email after self-invocation attempt: {}", user.getEmail());
				}
			} catch (Exception e) {
				log.error("Error in self-invocation demonstration: {}", e.getMessage(), e);
			}
			
			log.info("\n=== DEMO 11: SELF-INVOCATION PROBLEM COMPLETE ===\n");
		};
	}
	
	/**
	 * Demo 12: Transaction Event Listeners
	 */
	@Bean
	@Order(13)
	@org.springframework.context.annotation.Profile({"all", "demo12"})
	public CommandLineRunner transactionEventsDemo() {
		return args -> {
			log.info("\n\n=== DEMO 12: TRANSACTION EVENTS ===\n");
			
			UserPostDto dto = UserPostDto.builder()
					.username("event_user")
					.email("events@example.com")
					.firstName("Event")
					.lastName("User")
					.build();
			
			try {
				User savedUser = dataService.saveUserWithTransactionEvents(dto);
				log.info("Transaction with events completed successfully for user ID: {}", savedUser.getId());
			} catch (Exception e) {
				log.error("Error in transaction events demonstration: {}", e.getMessage(), e);
			}
			
			log.info("\n=== DEMO 12: TRANSACTION EVENTS COMPLETE ===\n");
		};
	}
}
