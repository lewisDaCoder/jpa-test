package lewis.jpa.aspect;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lewis.jpa.annotation.Audited;
import lombok.extern.slf4j.Slf4j;

/**
 * Aspect that handles auditing for methods annotated with @Audited
 */
@Aspect
@Component
@Slf4j
public class AuditAspect {

    /**
     * Log method entry for audited methods
     */
    @Before("@annotation(lewis.jpa.annotation.Audited)")
    public void auditMethodEntry(JoinPoint joinPoint) {
        // Get method signature and audit annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Audited auditAnnotation = signature.getMethod().getAnnotation(Audited.class);
        
        // Get action description or use method name if not specified
        String action = auditAnnotation.action();
        if (action.isEmpty()) {
            action = signature.getName();
        }
        
        // Log method call with or without parameters
        if (auditAnnotation.includeParams()) {
            log.info("AUDIT: {} called with parameters: {}", 
                    action, Arrays.toString(joinPoint.getArgs()));
        } else {
            log.info("AUDIT: {} called", action);
        }
    }
    
    /**
     * Log successful method execution
     */
    @AfterReturning(pointcut = "@annotation(lewis.jpa.annotation.Audited)", returning = "result")
    public void auditMethodSuccess(JoinPoint joinPoint, Object result) {
        // Get method signature and audit annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Audited auditAnnotation = signature.getMethod().getAnnotation(Audited.class);
        
        // Get action description or use method name if not specified
        String action = auditAnnotation.action();
        if (action.isEmpty()) {
            action = signature.getName();
        }
        
        // Log method completion
        log.info("AUDIT: {} completed successfully", action);
    }
    
    /**
     * Log method execution failure
     */
    @AfterThrowing(pointcut = "@annotation(lewis.jpa.annotation.Audited)", throwing = "ex")
    public void auditMethodFailure(JoinPoint joinPoint, Throwable ex) {
        // Get method signature and audit annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Audited auditAnnotation = signature.getMethod().getAnnotation(Audited.class);
        
        // Get action description or use method name if not specified
        String action = auditAnnotation.action();
        if (action.isEmpty()) {
            action = signature.getName();
        }
        
        // Log method failure
        log.error("AUDIT: {} failed with exception: {}", action, ex.getMessage());
    }
} 