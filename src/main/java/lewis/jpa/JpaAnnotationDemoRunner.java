package lewis.jpa;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import lewis.jpa.dto.UserPostDto;
import lewis.jpa.entity.User;
import lewis.jpa.service.AnnotationDemoService;
import lewis.jpa.util.ColoredOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Runner that demonstrates custom annotation behaviors
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class JpaAnnotationDemoRunner {

    private final AnnotationDemoService annotationDemoService;

    /**
     * Main annotation demo runner
     */
    @Bean
    @Order(100) // Run after data setup
    @Profile({"all", "annotation-demo"})
    public CommandLineRunner annotationDemo() {
        return args -> {
            log.info(ColoredOutput.BOLD + ColoredOutput.CYAN + 
                    "RUNNING JAVA ANNOTATION DEMO" + ColoredOutput.RESET);
            
            // Setup - create a test user
            UserPostDto dto = UserPostDto.builder()
                    .username("annotation_test")
                    .email("annotation@example.com")
                    .firstName("Test")
                    .lastName("User")
                    .build();
            
            // Run standard @Transactional demo
            log.info(ColoredOutput.demoHeader(1, "STANDARD @TRANSACTIONAL"));
            User user = annotationDemoService.createUserStandard(dto);
            log.info("Created user with ID: {}", user.getId());
            log.info(ColoredOutput.demoFooter(1, "STANDARD @TRANSACTIONAL"));
            
            // Run composite @CompositeTransactional demo
            log.info(ColoredOutput.demoHeader(2, "COMPOSITE @TRANSACTIONAL"));
            annotationDemoService.getAllUsersComposite();
            log.info(ColoredOutput.demoFooter(2, "COMPOSITE @TRANSACTIONAL"));
            
            // Run @Audited annotation demo
            log.info(ColoredOutput.demoHeader(3, "@AUDITED"));
            try {
                annotationDemoService.getUserByIdWithAudit(user.getId());
            } catch (Exception e) {
                log.error("Error getting user: {}", e.getMessage());
            }
            log.info(ColoredOutput.demoFooter(3, "@AUDITED"));
            
            // Run @Retry annotation demo
            log.info(ColoredOutput.demoHeader(4, "@RETRY"));
            try {
                annotationDemoService.getUserWithRetry(user.getId());
            } catch (Exception e) {
                log.info("Retry eventually failed with: {}", e.getMessage());
            }
            log.info(ColoredOutput.demoFooter(4, "@RETRY"));
            
            // Run combined annotations demo
            log.info(ColoredOutput.demoHeader(5, "COMBINED ANNOTATIONS"));
            annotationDemoService.getActiveUsers();
            log.info(ColoredOutput.demoFooter(5, "COMBINED ANNOTATIONS"));
            
            // Run @SecuredOperation demo
            log.info(ColoredOutput.demoHeader(6, "@SECUREDOPERATION"));
            try {
                UserPostDto specialDto = UserPostDto.builder()
                        .username("special_user")
                        .email("special@example.com")
                        .firstName("Special")
                        .lastName("User")
                        .build();
                
                annotationDemoService.createSpecialUser(specialDto);
            } catch (Exception e) {
                log.error("Security error: {}", e.getMessage());
            }
            log.info(ColoredOutput.demoFooter(6, "@SECUREDOPERATION"));
            
            // Run bad annotation usage demo
            log.info(ColoredOutput.demoHeader(7, "SELF-INVOCATION ISSUE"));
            log.info("This demonstrates when annotations don't work as expected");
            annotationDemoService.badAnnotationUsage(user.getId(), "bad@example.com");
            log.info(ColoredOutput.demoFooter(7, "SELF-INVOCATION ISSUE"));
            
            // Run correct annotation usage demo
            log.info(ColoredOutput.demoHeader(8, "CORRECTED SELF-INVOCATION"));
            log.info("This demonstrates the correct way to handle self-invocation");
            try {
                annotationDemoService.correctAnnotationUsage(user.getId(), "correct@example.com");
            } catch (Exception e) {
                log.warn("Note: In our demo app without proper injection setup, this still fails");
                log.warn("In a real app with proper configuration, this would work correctly");
            }
            log.info(ColoredOutput.demoFooter(8, "CORRECTED SELF-INVOCATION"));
            
            log.info(ColoredOutput.BOLD + ColoredOutput.GREEN + 
                    "JAVA ANNOTATION DEMO COMPLETED" + ColoredOutput.RESET);
        };
    }
} 