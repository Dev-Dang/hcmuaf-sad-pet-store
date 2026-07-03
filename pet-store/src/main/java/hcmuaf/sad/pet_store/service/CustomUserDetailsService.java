package hcmuaf.sad.pet_store.service;

import hcmuaf.sad.pet_store.entity.User;
import hcmuaf.sad.pet_store.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByEmail(email.toLowerCase().trim());
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("Email hoặc mật khẩu không đúng.");
        }

        User user = userOptional.get();
        if (!user.isActive()) {
            throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa.");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}
