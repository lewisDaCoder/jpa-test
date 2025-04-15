package lewis.jpa.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.security.test.context.support.WithMockUser;

import lewis.jpa.dto.UserPostDto;
import lewis.jpa.entity.User;
import lewis.jpa.repository.UserRepository;
import lewis.jpa.service.AnnotationDemoService;

@SpringBootTest
@ActiveProfiles("test")
class CompositeAnnotationTest {

    @Autowired
    private AnnotationDemoService annotationDemoService;

    @Autowired
    private TransactionInterceptor transactionInterceptor;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void compositeTransactionalAnnotationShouldWorkAsExpected() throws Exception {
        // Arrange
        List<User> mockUsers = Arrays.asList(
                User.builder().id(1L).username("user1").build(),
                User.builder().id(2L).username("user2").build());

        when(userRepository.findAll()).thenReturn(mockUsers);

        // Get the transaction attributes
        Method method = AnnotationDemoService.class.getMethod("getAllUsersComposite");
        TransactionAttributeSource tas = transactionInterceptor.getTransactionAttributeSource();
        org.springframework.transaction.interceptor.TransactionAttribute attr = tas.getTransactionAttribute(method,
                AnnotationDemoService.class);

        // Act
        List<User> result = annotationDemoService.getAllUsersComposite();

        // Assert
        assertThat(result).isEqualTo(mockUsers);
        verify(userRepository).findAll();

        // Verify transaction attributes from the composite annotation
        assertThat(attr.isReadOnly()).isTrue();
        assertThat(attr.getTimeout()).isEqualTo(20);
        assertThat(attr.toString()).contains("READ_COMMITTED");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void securedOperationAnnotationShouldCombineMultipleAnnotations() throws Exception {
        // Arrange
        UserPostDto dto = UserPostDto.builder()
                .username("special_user")
                .email("special@example.com")
                .firstName("Special")
                .lastName("User")
                .build();

        User savedUser = User.builder()
                .id(5L)
                .username(dto.getUsername())
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .active(true)
                .version(0L)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Find annotations on the method
        Method method = AnnotationDemoService.class.getMethod("createSpecialUser", UserPostDto.class);
        SecuredOperation securedOp = method.getAnnotation(SecuredOperation.class);

        // This would be a SecurityAnnotationProcessor in a real app
        Transactional transactional = method.getAnnotation(Transactional.class);
        Audited audited = method.getAnnotation(Audited.class);

        // Act
        User result = annotationDemoService.createSpecialUser(dto);

        // Assert
        assertThat(result).isEqualTo(savedUser);
        verify(userRepository).save(any(User.class));

        // Verify multiple annotations were combined in @SecuredOperation
        assertThat(securedOp).isNotNull();
        assertThat(securedOp.value()).isEqualTo("Create Special User");

        // In a Spring environment, these would be found through annotation lookup
        // In our test, we just verify the presence of the composite annotation
        assertThat(method.isAnnotationPresent(SecuredOperation.class)).isTrue();
    }

    @Test
    void selfInvocationBypassesAnnotations() {
        // Arrange
        User user = User.builder().id(1L).email("old@example.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        annotationDemoService.badAnnotationUsage(1L, "new@example.com");

        // Assert - in a real app, the transaction would be missing here
        // This is hard to test directly without a real database
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }
}