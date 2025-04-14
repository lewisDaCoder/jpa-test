package lewis.jpa.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.extern.slf4j.Slf4j;

/**
 * Demonstrates the use of transaction event listeners.
 * In a real application, these events can be used for:
 * - Auditing
 * - Notifications
 * - Cache invalidation
 * - Integration with other systems
 */
@Component
@Slf4j
public class UserTransactionEventListener {
    
    /**
     * This listener will be triggered before the transaction commits.
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleUserEventBeforeCommit(UserCreatedEvent event) {
        log.info("BEFORE_COMMIT: User {} is about to be committed", 
                event.getUser().getUsername());
    }
    
    /**
     * This listener will be triggered after the transaction commits.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserEventAfterCommit(UserCreatedEvent event) {
        log.info("AFTER_COMMIT: User {} has been successfully committed to the database", 
                event.getUser().getUsername());
        
        // This is a good place for non-critical operations that should happen
        // only after the transaction is successful, such as:
        // - Sending emails
        // - Pushing notifications
        // - Updating caches
    }
    
    /**
     * This listener will be triggered after the transaction rolls back.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleUserEventAfterRollback(UserCreatedEvent event) {
        log.info("AFTER_ROLLBACK: Transaction for user {} has been rolled back", 
                event.getUser().getUsername());
    }
    
    /**
     * This listener will be triggered after the transaction completes (commit or rollback).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleUserEventAfterCompletion(UserCreatedEvent event) {
        log.info("AFTER_COMPLETION: Transaction for user {} has completed (either committed or rolled back)", 
                event.getUser().getUsername());
        
        // This is a good place for cleanup operations that should happen
        // regardless of transaction outcome
    }
} 