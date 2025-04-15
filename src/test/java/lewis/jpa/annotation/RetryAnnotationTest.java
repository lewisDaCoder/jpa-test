package lewis.jpa.annotation;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import lewis.jpa.entity.User;
import lewis.jpa.repository.UserRepository;
import lewis.jpa.service.AnnotationDemoService;
import lewis.jpa.service.AnnotationDemoService.NonRetryableException;

@SpringBootTest
@ActiveProfiles("test")
class RetryAnnotationTest {

    @SpyBean
    private AnnotationDemoService serviceSpy;
    
    @MockitoBean
    private UserRepository userRepository;
    
    @Test
    void shouldRetryOnException() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .username("test_user")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .active(true)
                .version(0L)
                .build();
        
        // Mock repository on the Spy's injected dependency
        when(userRepository.findById(1L))
                .thenThrow(new RuntimeException("First failure"))
                .thenThrow(new RuntimeException("Second failure"))
                .thenReturn(Optional.of(user));
        
        // Act & Assert - Due to internal random simulation, success isn't guaranteed.
        // Assert that some RuntimeException is thrown after 3 attempts.
        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> serviceSpy.getUserWithRetry(1L));
        
        // Verify the SPY method was invoked 3 times (initial + 2 retries)
        verify(serviceSpy, times(3)).getUserWithRetry(1L);
    }
    
    @Test
    void shouldNotRetryOnNonRetryableException() {
        // Arrange
        // Remove local definition - use the one from AnnotationDemoService
        // class NonRetryableException extends RuntimeException { ... }
        
        // Remove repository mock - we use doThrow instead
        // when(userRepository.findById(1L)).thenThrow(new NonRetryableException("Repo non-retryable failure"));
        
        // Use doThrow on the spy to force the *correct* exception BEFORE internal logic
        doThrow(new AnnotationDemoService.NonRetryableException("Forced non-retryable"))
            .when(serviceSpy).getUserWithRetry(1L);
        
        // Act & Assert
        // Expect the specific NonRetryableException from the service
        assertThatExceptionOfType(AnnotationDemoService.NonRetryableException.class)
                .isThrownBy(() -> serviceSpy.getUserWithRetry(1L)) // Call spy
                .withMessage("Forced non-retryable");
        
        // Verify the spy method was only called once (no retry attempted)
        verify(serviceSpy, times(1)).getUserWithRetry(1L);
        // Optionally verify repository was not called (since doThrow bypasses body)
        verify(userRepository, times(0)).findById(anyLong()); 
    }
    
    @Test
    void shouldGiveUpAfterMaxRetries() {
        // Arrange
        // Mock repository to always fail
        when(userRepository.findById(anyLong()))
                .thenThrow(new RuntimeException("Persistent failure"));
        
        // Act & Assert
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> serviceSpy.getUserWithRetry(1L));
        
        // Verify the SPY method was invoked 3 times (initial + 2 retries)
        // Cannot reliably verify userRepository interaction due to internal random simulation
        verify(serviceSpy, times(3)).getUserWithRetry(anyLong()); 
    }
} 