package ua.knu.carrental.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.knu.carrental.users.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByName(String name);

    User findByPassportId(long passportId);

    User findByKeycloakId(String keycloakId);
}
