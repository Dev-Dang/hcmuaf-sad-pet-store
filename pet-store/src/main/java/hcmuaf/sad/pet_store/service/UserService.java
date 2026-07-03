package hcmuaf.sad.pet_store.service;

import hcmuaf.sad.pet_store.entity.User;
import hcmuaf.sad.pet_store.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerUser(String fullName, String email, String password) {
        if (userRepository.existsByEmail(email.toLowerCase().trim())) {
            throw new RuntimeException("Email này đã được sử dụng. Vui lòng dùng email khác hoặc đăng nhập.");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email.toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("CUSTOMER");
        user.setActive(true);
        user.setVerified(false);
        
        userRepository.save(user);
    }
}
