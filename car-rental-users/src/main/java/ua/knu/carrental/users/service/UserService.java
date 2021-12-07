package ua.knu.carrental.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.knu.carrental.users.model.User;
import ua.knu.carrental.users.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KeycloakService keycloakService;

    public boolean shouldEnableAdminFrontend(String username) {
        return username.equals("admin");
    }

//    public long getUserId(KeycloakAuthenticationToken token) {
//        return keycloakService.getUserId(token);
//    }
    public User getUserById(long userId) {
        return userRepository.findById(userId).get();
    }

    public User getUserByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId);
    }

    @Transactional
    public String registerAndLogIn(long passportId, String username, String password) {
        User user = userRepository.findByPassportId(passportId);
        if (user == null) {
            user = new User();
            user.setPassportId(passportId);
            user.setName(username);

            user.setKeycloakId(keycloakService.register(user, password));

            userRepository.save(user);
        }
        return logIn(username, password);
    }

    public String logIn(String username, String password) {
        User user = userRepository.findByName(username);
        if (user == null)
            return null;
        return keycloakService.login(username, password);
    }
}
