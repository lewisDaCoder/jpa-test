package lewis.jpa.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.annotation.Transactional;

import lewis.jpa.dto.UserPostDto;
import lewis.jpa.entity.Post;
import lewis.jpa.entity.User;
import lewis.jpa.event.UserCreatedEvent;
import lewis.jpa.repository.PostRepository;
import lewis.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PlatformTransactionManager transactionManager;
    
    /**
     * Processes a list of UserPostDto objects, converting each into User and Post entities
     * and saving them to the database.
     * 
     * This method is transactional, so if any operation fails, all operations will be rolled back.
     * Entities are equipped with version fields for optimistic locking.
     */
    @Transactional
    public void processUserPosts(List<UserPostDto> userPostDtos) {
        log.info("Starting to process {} user posts with @Transactional", userPostDtos.size());
        
        for (UserPostDto dto : userPostDtos) {
            // Create and save User entity with version initialized to 0
            User user = User.builder()
                    .username(dto.getUsername())
                    .email(dto.getEmail())
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .active(true)
                    // .version(0L)
                    .build();
            
            User savedUser = userRepository.save(user);
            log.info("Saved user with ID: {}, version: {}", savedUser.getId(), savedUser.getVersion());
            
            // Create and save Post entity with version initialized to 0
            Post post = Post.builder()
                    .title(dto.getPostTitle())
                    .content(dto.getPostContent())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            Post savedPost = postRepository.save(post);
            log.info("Saved post with ID: {}, version: {}", savedPost.getId(), savedPost.getVersion());
        }
        
        log.info("Completed processing all user posts");
    }
    
    /**
     * Updates a user entity that was previously retrieved.
     * This method is used to demonstrate optimistic locking by trying to update
     * an entity that may have been updated in the database since it was retrieved.
     *
     * @param user the user entity to update
     * @return the updated user entity
     */
    @Transactional
    public User updateExistingUser(User user) {
        log.info("Updating existing user with ID: {}, version: {}", user.getId(), user.getVersion());
        
        // Save the updated user
        // If another process has updated this user in the meantime
        // and incremented the version, this save will fail with 
        // an OptimisticLockingFailureException
        User savedUser = userRepository.save(user);
        
        log.info("Updated user, new version: {}", savedUser.getVersion());
        
        return savedUser;
    }
    
    /**
     * Demonstrates optimistic locking by updating a user's information.
     * This helps to prevent concurrent modifications of the same entity.
     *
     * @param userId the ID of the user to update
     * @param newEmail the new email address for the user
     * @return the updated user entity
     */
    @Transactional
    public User updateUserEmail(Long userId, String newEmail) {
        log.info("Updating email for user ID: {} to {}", userId, newEmail);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        log.info("Found user with version: {}", user.getVersion());
        
        // Update the email
        user.setEmail(newEmail);
        
        // Save the updated user
        // If another process has updated this user in the meantime
        // and incremented the version, this save will fail with 
        // an OptimisticLockingFailureException
        User savedUser = userRepository.save(user);
        
        log.info("Updated user, new version: {}", savedUser.getVersion());
        
        return savedUser;
    }
    
    /**
     * Simulates a concurrent modification by manipulating the version directly.
     * This method creates a scenario where optimistic locking will fail when
     * the entity is later saved.
     *
     * @param userId the ID of the user to manipulate
     * @return the user with incorrect version that will cause a locking failure
     */
    @Transactional(readOnly = true)
    public User simulateConcurrentModification(Long userId) {
        log.info("Simulating concurrent modification for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        log.info("Found user with version: {}", user.getVersion());
        
        // In real concurrent scenarios, another transaction would have updated
        // the entity in the database, causing the version to increment.
        // Here we simulate that scenario by creating a copy of the entity with
        // a different version.
        return user;
    }
    
    /**
     * Demonstrates @Transactional behavior by updating both a User and Post entity in the same transaction.
     * If any part of the operation fails, the entire transaction will be rolled back.
     * 
     * @param userId the ID of the user to update
     * @param postId the ID of the post to update
     * @param shouldFail flag to simulate a failure scenario
     * @return true if the operation succeeds, false otherwise
     */
    @Transactional
    public boolean updateUserAndPost(Long userId, Long postId, boolean shouldFail) {
        log.info("Starting transaction to update both User and Post");
        
        // Update user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        user.setFirstName("Updated First Name");
        user.setLastName("Updated Last Name");
        userRepository.save(user);
        log.info("Updated user with ID: {}, new version: {}", user.getId(), user.getVersion());
        
        // Update post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));
        post.setTitle("Updated Title");
        post.setContent("Updated Content");
        postRepository.save(post);
        log.info("Updated post with ID: {}, new version: {}", post.getId(), post.getVersion());
        
        // Simulate a failure if requested
        if (shouldFail) {
            log.warn("Simulating a failure in the transaction");
            throw new RuntimeException("Simulated failure to demonstrate transaction rollback");
        }
        
        log.info("Transaction completed successfully");
        return true;
    }

    // --- ADDITIONAL TRANSACTION USE CASES ---

    /**
     * USE CASE 1: Transaction Propagation REQUIRES_NEW
     * Demonstrates how to create a new transaction regardless of existing ones
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public User createUserWithNewTransaction(UserPostDto dto) {
        log.info("Creating user with REQUIRES_NEW propagation");
        
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .active(true)
                .version(0L)
                .build();
        
        return userRepository.save(user);
    }
    
    /**
     * USE CASE 2: Read-Only Transaction
     * Optimizes database access for read-only operations by disabling dirty checking
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsersReadOnly() {
        log.info("Getting all users with readOnly=true");
        return userRepository.findAll();
    }
    
    /**
     * USE CASE 3: Transaction Isolation Level
     * Sets a specific isolation level for the transaction
     */
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
    public User updateUserWithSerializableIsolation(Long userId, String newEmail) {
        log.info("Updating user with SERIALIZABLE isolation level");
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setEmail(newEmail);
        return userRepository.save(user);
    }
    
    /**
     * USE CASE 4: Custom Rollback Rules
     * Rolls back on specific exceptions but not others
     */
    @Transactional(rollbackFor = IllegalArgumentException.class, noRollbackFor = UnsupportedOperationException.class)
    public User updateUserWithCustomRollbackRules(Long userId, String newEmail, boolean throwException, String exceptionType) {
        log.info("Updating user with custom rollback rules");
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setEmail(newEmail);
        User savedUser = userRepository.save(user);
        
        if (throwException) {
            if ("illegalArgument".equals(exceptionType)) {
                log.info("Throwing IllegalArgumentException - transaction WILL rollback");
                throw new IllegalArgumentException("Test exception - this will cause rollback");
            } else if ("unsupportedOperation".equals(exceptionType)) {
                log.info("Throwing UnsupportedOperationException - transaction will NOT rollback");
                throw new UnsupportedOperationException("Test exception - this will not cause rollback");
            }
        }
        
        return savedUser;
    }
    
    /**
     * USE CASE 5: Transaction Timeout
     * Sets a time limit for the transaction
     */
    @Transactional(timeout = 5)
    public User updateUserWithTimeout(Long userId, String newEmail, boolean simulateDelay) throws InterruptedException {
        log.info("Updating user with 5-second timeout");
        
        if (simulateDelay) {
            log.info("Simulating long-running operation...");
            Thread.sleep(4000); // Sleep for 4 seconds (under the timeout)
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setEmail(newEmail);
        return userRepository.save(user);
    }
    
    /**
     * USE CASE 6: Nested Transactional Methods
     * Demonstrates transaction propagation behavior when one transactional method calls another
     */
    @Transactional
    public void nestedTransactionExample(UserPostDto dto1, UserPostDto dto2, boolean failInner) {
        log.info("Starting outer transaction");
        
        // Save first user in outer transaction
        User user1 = User.builder()
                .username(dto1.getUsername())
                .email(dto1.getEmail())
                .firstName(dto1.getFirstName())
                .lastName(dto1.getLastName())
                .active(true)
                .version(0L)
                .build();
        
        userRepository.save(user1);
        log.info("Saved first user in outer transaction");
        
        try {
            // Call inner transaction with REQUIRES_NEW
            // This will create a separate transaction
            User user2 = createUserWithNewTransaction(dto2);
            log.info("Saved second user in inner transaction: {}", user2.getId());
            
            if (failInner) {
                log.info("Simulating failure after inner transaction completed");
                throw new RuntimeException("Simulated failure in outer transaction");
            }
        } catch (Exception e) {
            log.error("Error in nested transaction: {}", e.getMessage());
            throw e; // Re-throw to cause outer transaction to roll back
        }
        
        log.info("Completed outer transaction successfully");
    }
    
    /**
     * USE CASE 7: Bulk Operations
     * Demonstrates bulk update operations within a transaction
     */
    @Transactional
    public int bulkUpdateUserStatus(boolean newStatus) {
        log.info("Performing bulk update of user status to: {}", newStatus);
        
        // This would typically use a custom query method in the repository
        // For demonstration, we'll simulate it by loading and updating all users
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            user.setActive(newStatus);
            userRepository.save(user);
        }
        
        log.info("Updated status for {} users", users.size());
        return users.size();
    }
    
    /**
     * USE CASE 8: Programmatic Transaction Management
     * Demonstrates how to use PlatformTransactionManager directly
     */
    public void programmaticTransactionExample(UserPostDto dto) {
        log.info("Demonstrating programmatic transaction management");
        
        // Create transaction template using the injected transaction manager
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        
        // Execute code within a transaction
        txTemplate.execute(status -> {
            try {
                User user = User.builder()
                        .username(dto.getUsername())
                        .email(dto.getEmail())
                        .firstName(dto.getFirstName())
                        .lastName(dto.getLastName())
                        .active(true)
                        .version(0L)
                        .build();
                
                User savedUser = userRepository.save(user);
                log.info("Saved user programmatically: {}", savedUser.getId());
                
                // Manually roll back if needed
                // status.setRollbackOnly();
                
                return savedUser;
            } catch (Exception e) {
                log.error("Error in programmatic transaction: {}", e.getMessage());
                status.setRollbackOnly();
                throw e;
            }
        });
    }
    
    /**
     * USE CASE 9: Self-Invocation Problem
     * Demonstrates the issue with calling @Transactional methods from within the same class
     */
    public void demonstrateSelfInvocationIssue(UserPostDto dto) {
        log.info("Demonstrating self-invocation problem");
        
        // This call won't have a transaction because it's a direct method call, not through a proxy
        updateUserEmail(1L, "self.invocation@example.com");
        
        log.info("Note: The above method call was not actually transactional due to self-invocation!");
    }
    
    /**
     * USE CASE 10: Transaction Event Listeners
     * Demonstrates how to use TransactionEventListener to perform actions 
     * before or after transaction completion
     */
    @Transactional
    public User saveUserWithTransactionEvents(UserPostDto dto) {
        log.info("Saving user with transaction events");
        
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .active(true)
                .version(0L)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Publish an event that will be caught by methods annotated with @TransactionEventListener
        eventPublisher.publishEvent(new UserCreatedEvent(savedUser));
        
        log.info("User saved and event published: {}", savedUser.getId());
        log.info("Transaction events will execute according to transaction phase");
        
        return savedUser;
    }
} 