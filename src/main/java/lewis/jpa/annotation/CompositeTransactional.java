package lewis.jpa.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Composite annotation that combines multiple transaction attributes
 * for read-only operations with specific timeout and isolation level.
 * 
 * This demonstrates how to create a meta-annotation that includes
 * multiple settings from an existing annotation.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional(
    readOnly = true,
    timeout = 20,
    isolation = Isolation.READ_COMMITTED,
    propagation = Propagation.REQUIRED
)
public @interface CompositeTransactional {
    // No additional attributes needed, all come from @Transactional
} 