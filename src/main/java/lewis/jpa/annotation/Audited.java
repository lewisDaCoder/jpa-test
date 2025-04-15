package lewis.jpa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking methods that should be audited.
 * 
 * This demonstrates a simple marker annotation that can be used with
 * a Spring AOP aspect.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    /**
     * Action description for the audit log
     * @return the action description
     */
    String action() default "";
    
    /**
     * Whether to include method parameters in the audit log
     * @return true if parameters should be included
     */
    boolean includeParams() default false;
} 