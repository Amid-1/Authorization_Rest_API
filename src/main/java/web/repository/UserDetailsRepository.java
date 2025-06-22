package web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.model.UserDetails;

import java.util.Optional;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {
    /**
     * Ищет детали по ID пользователя (user_id).
     */
    Optional<UserDetails> findByUserId(Long userId);
}