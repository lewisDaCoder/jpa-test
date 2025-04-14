package lewis.jpa.event;

import lewis.jpa.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Event that is published when a new user is created.
 * Used with transaction event listeners to demonstrate transaction phases.
 */
@Getter
@AllArgsConstructor
public class UserCreatedEvent {
    private final User user;
} 