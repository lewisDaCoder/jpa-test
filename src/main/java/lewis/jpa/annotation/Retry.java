package lewis.jpa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to mark methods that should be retried on exceptions.
 * 
 * This demonstrates how to create an annotation with attributes that can be
 * processed by an aspect or annotation processor.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {
    /**
     * Maximum number of retry attempts
     * @return the maximum retry count
     */
    int maxAttempts() default 3;
    
    /**
     * Delay between retry attempts in milliseconds
     * @return the delay in ms
     */
    long delay() default 1000L;
    
    /**
     * Array of exception types that should trigger a retry
     * @return the exception types
     */
    Class<? extends Throwable>[] retryFor() default {Exception.class};
    
    /**
     * Array of exception types that should not trigger a retry
     * @return the exception types
     */
    Class<? extends Throwable>[] noRetryFor() default {};
} 