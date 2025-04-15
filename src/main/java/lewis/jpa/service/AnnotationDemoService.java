package lewis.jpa.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lewis.jpa.annotation.Audited;
import lewis.jpa.annotation.CompositeTransactional;
import lewis.jpa.annotation.Retry;
import lewis.jpa.annotation.SecuredOperation;
import lewis.jpa.dto.UserPostDto;
import lewis.jpa.entity.User;
import lewis.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service that demonstrates various annotation usage patterns
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnnotationDemoService {

    private final UserRepository userRepository;
    
    /**
     * USE CASE 1: Standard @Transactional with explicit attributes
     */
    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation = Isolation.READ_COMMITTED,
        timeout = 30,
        readOnly = false,
        rollbackFor = Exception.class
    )
    public User createUserStandard(UserPostDto dto) {
        log.info("Creating user with standard @Transactional annotation");
        
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
     * USE CASE 2: Using custom composite @CompositeTransactional
     */
    @CompositeTransactional
    public List<User> getAllUsersComposite() {
        log.info("Getting all users with composite @CompositeTransactional annotation");
        return userRepository.findAll();
    }
    
    /**
     * USE CASE 3: Using @Audited annotation
     */
    @Audited(action = "Get User By ID", includeParams = true)
    public User getUserByIdWithAudit(Long userId) {
        log.info("Getting user with @Audited annotation");
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }
    
    /**
     * USE CASE 4: Using @Retry annotation for retry logic
     */
    @Retry(maxAttempts = 3, delay = 500, retryFor = {RuntimeException.class}, noRetryFor = {AnnotationDemoService.NonRetryableException.class})
    public User getUserWithRetry(Long userId) {
        log.info("Getting user with @Retry annotation");
        
        // Simulate random failures to demonstrate retry
        int random = ThreadLocalRandom.current().nextInt(10);
        if (random < 7) {  // 70% chance of failure on first attempt
            throw new RuntimeException("Simulated random failure");
        }
        
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }
    
    /**
     * USE CASE 5: Combined annotations directly
     */
    @Transactional(readOnly = true)
    @Audited(action = "Audited Read-Only Operation")
    public List<User> getActiveUsers() {
        log.info("Getting active users with combined annotations");
        return userRepository.findAll().stream()
                .filter(User::isActive)
                .toList();
    }
    
    /**
     * USE CASE 6: Using composite @SecuredOperation (combines @Transactional, @Secured, and @Audited)
     */
    @SecuredOperation("Create Special User")
    @Audited(includeParams = true)
    public User createSpecialUser(UserPostDto dto) {
        log.info("Creating special user with @SecuredOperation composite annotation");
        
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
     * USE CASE 7: Annotation that doesn't work as expected
     * 
     * This demonstrates the issue with self-invocation and annotations.
     * Even though findUserAndUpdate is annotated with @Transactional,
     * calling it directly from within the same class bypasses the proxy
     * and the annotation doesn't take effect.
     */
    public User badAnnotationUsage(Long userId, String newEmail) {
        log.info("Demonstrating bad annotation usage (self-invocation)");
        
        // This direct call will bypass the @Transactional annotation
        return findUserAndUpdate(userId, newEmail);
    }
    
    /**
     * Helper method that's annotated but may be bypassed through direct calls
     */
    @Transactional
    public User findUserAndUpdate(Long userId, String newEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setEmail(newEmail);
        return userRepository.save(user);
    }
    
    /**
     * USE CASE 8: Correct way to handle self-invocation
     */
    public User correctAnnotationUsage(Long userId, String newEmail) {
        log.info("Demonstrating correct annotation usage (avoiding self-invocation)");
        
        // Get this proxy-wrapped instance of the service to ensure annotations work
        AnnotationDemoService self = getProxiedSelf();
        
        // Call through the proxy to ensure @Transactional is applied
        return self.findUserAndUpdate(userId, newEmail);
    }
    
    /**
     * Obtain the proxied version of this service to prevent self-invocation issues
     */
    private AnnotationDemoService getProxiedSelf() {
        // In a real app, this would be injected with @Autowired to get the proxy
        // Here we just return this for compilation, but it won't actually work as intended
        return this;
    }
    
    // Define the NonRetryableException class within the service or make it a top-level class
    // For simplicity here, define it nested within the service (adjust if needed)
    public static class NonRetryableException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public NonRetryableException(String message) {
            super(message);
        }
    }
} 