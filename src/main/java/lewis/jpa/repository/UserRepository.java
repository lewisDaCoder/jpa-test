package lewis.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import lewis.jpa.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
} 