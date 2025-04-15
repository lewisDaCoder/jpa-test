package lewis.jpa.aspect;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lewis.jpa.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

/**
 * Aspect that handles retry logic for methods annotated with @Retry
 */
@Aspect
@Component
@Slf4j
public class RetryAspect {

    /**
     * Apply retry logic to methods annotated with @Retry
     */
    @Around("@annotation(lewis.jpa.annotation.Retry)")
    public Object retry(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get method signature and retry annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Retry retryAnnotation = signature.getMethod().getAnnotation(Retry.class);
        
        // Extract retry parameters
        int maxAttempts = retryAnnotation.maxAttempts();
        long delay = retryAnnotation.delay();
        Class<? extends Throwable>[] retryFor = retryAnnotation.retryFor();
        Class<? extends Throwable>[] noRetryFor = retryAnnotation.noRetryFor();
        
        // Log retry configuration
        log.debug("Method {} configured for retry: max={}, delay={}ms, retryFor={}, noRetryFor={}",
                signature.toShortString(), maxAttempts, delay,
                Arrays.toString(retryFor), Arrays.toString(noRetryFor));
        
        // Attempt execution with retry logic
        int attempts = 0;
        Throwable lastException = null;
        
        while (attempts < maxAttempts) {
            attempts++;
            try {
                // Attempt to execute the method
                return joinPoint.proceed();
            } catch (Throwable ex) {
                lastException = ex;
                
                // Check if this exception should not be retried
                if (isExceptionInList(ex, noRetryFor)) {
                    log.debug("Exception {} is in noRetryFor list, not retrying", ex.getClass().getName());
                    throw ex;
                }
                
                // Check if this exception should be retried
                if (!isExceptionInList(ex, retryFor)) {
                    log.debug("Exception {} is not in retryFor list, not retrying", ex.getClass().getName());
                    throw ex;
                }
                
                // Log retry attempt
                if (attempts < maxAttempts) {
                    log.info("Retry {}/{} for method {} after exception: {}", 
                            attempts, maxAttempts, signature.toShortString(), ex.getMessage());
                    
                    // Wait before retrying
                    try {
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw ex;
                    }
                } else {
                    log.warn("Max retries ({}) reached for method {}", maxAttempts, signature.toShortString());
                }
            }
        }
        
        // If we got here, all retry attempts failed
        throw lastException;
    }
    
    /**
     * Check if an exception is in the list of exception types
     */
    private boolean isExceptionInList(Throwable ex, Class<? extends Throwable>[] exceptionList) {
        if (exceptionList.length == 0) {
            return false;
        }
        
        for (Class<? extends Throwable> exceptionClass : exceptionList) {
            if (exceptionClass.isInstance(ex)) {
                return true;
            }
        }
        
        return false;
    }
} 