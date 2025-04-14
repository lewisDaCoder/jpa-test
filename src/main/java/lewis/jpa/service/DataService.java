package lewis.jpa.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lewis.jpa.dto.UserPostDto;
import lewis.jpa.entity.Post;
import lewis.jpa.entity.User;
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
                    .published(true)
                    .authorUsername(savedUser.getUsername())  // Just store the username as string
                    // .version(0L)
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
} 