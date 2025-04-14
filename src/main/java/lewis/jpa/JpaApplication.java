package lewis.jpa;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.OptimisticLockingFailureException;

import lewis.jpa.dto.UserPostDto;
import lewis.jpa.entity.User;
import lewis.jpa.service.DataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class JpaApplication implements CommandLineRunner {
	
	private final DataService dataService;

	public static void main(String[] args) {
		SpringApplication.run(JpaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("Starting application...");
		
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
		
		// // Demonstrate optimistic locking
		demonstrateOptimisticLocking();
		
		// // Demonstrate transaction rollback
		demonstrateTransactionRollback();
		
		log.info("Application completed successfully.");
	}
	
	private void demonstrateOptimisticLocking() {
		try {
			log.info("Demonstrating optimistic locking...");
			
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
	}
	
	private void demonstrateTransactionRollback() {
		log.info("\n\n=== DEMONSTRATING TRANSACTION ROLLBACK ===\n");
		
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
		
		log.info("\n=== TRANSACTION ROLLBACK DEMONSTRATION COMPLETE ===\n");
	}
}
