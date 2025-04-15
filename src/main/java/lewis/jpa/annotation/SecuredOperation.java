package lewis.jpa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

/**
 * Composite annotation that combines security and transaction requirements.
 * This demonstrates using multiple annotations in a single custom meta-annotation.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Transactional
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public @interface SecuredOperation {
    /**
     * Description of the operation
     * @return operation description
     */
    String value() default "";
} 