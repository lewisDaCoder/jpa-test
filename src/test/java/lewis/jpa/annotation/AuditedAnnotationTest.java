package lewis.jpa.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lewis.jpa.aspect.AuditAspect;
import lewis.jpa.entity.User;
import lewis.jpa.repository.UserRepository;
import lewis.jpa.service.AnnotationDemoService;
import org.slf4j.LoggerFactory;

@SpringBootTest
@ActiveProfiles("test")
class AuditedAnnotationTest {

    @Autowired
    private AnnotationDemoService annotationDemoService;
    
    @MockitoBean
    private UserRepository userRepository;
    
    @Test
    void shouldLogAuditEntriesWhenMethodSucceeds() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .username("test_user")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .active(true)
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // Create a log appender to capture log messages
        Logger auditLogger = (Logger) LoggerFactory.getLogger(AuditAspect.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        auditLogger.addAppender(listAppender);
        
        try {
            // Act
            User result = annotationDemoService.getUserByIdWithAudit(1L);
            
            // Assert
            assertThat(result).isEqualTo(user);
            
            // Verify audit logs were created
            assertThat(listAppender.list).isNotEmpty();
            
            // Look for method entry audit
            assertThat(listAppender.list)
                    .anyMatch(event -> 
                            event.getLevel() == Level.INFO && 
                            event.getFormattedMessage().contains("AUDIT: Get User By ID called"));
            
            // Look for method success audit
            assertThat(listAppender.list)
                    .anyMatch(event -> 
                            event.getLevel() == Level.INFO && 
                            event.getFormattedMessage().contains("AUDIT: Get User By ID completed successfully"));
        } finally {
            // Cleanup
            auditLogger.detachAppender(listAppender);
        }
    }
    
    @Test
    void shouldLogAuditEntriesWhenMethodFails() {
        // Arrange
        String errorMessage = "User not found";
        when(userRepository.findById(anyLong())).thenThrow(new RuntimeException(errorMessage));
        
        // Create a log appender to capture log messages
        Logger auditLogger = (Logger) LoggerFactory.getLogger(AuditAspect.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        auditLogger.addAppender(listAppender);
        
        try {
            // Act & Assert
            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> annotationDemoService.getUserByIdWithAudit(1L))
                    .withMessage(errorMessage);
            
            // Verify audit logs were created
            assertThat(listAppender.list).isNotEmpty();
            
            // Look for method entry audit
            assertThat(listAppender.list)
                    .anyMatch(event -> 
                            event.getLevel() == Level.INFO && 
                            event.getFormattedMessage().contains("AUDIT: Get User By ID called"));
            
            // Look for method failure audit
            assertThat(listAppender.list)
                    .anyMatch(event -> 
                            event.getLevel() == Level.ERROR && 
                            event.getFormattedMessage().contains("AUDIT: Get User By ID failed with exception"));
        } finally {
            // Cleanup
            auditLogger.detachAppender(listAppender);
        }
    }
} 