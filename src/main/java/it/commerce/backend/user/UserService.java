package it.commerce.backend.user;

import it.commerce.backend.product.dto.ProductResponse;
import it.commerce.backend.user.dto.CreateUserRequest;
import it.commerce.backend.user.dto.UpdateUserRequest;
import it.commerce.backend.user.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserType(),
                user.getUserStatus(),
                user.isEmailVerified(),
                user.getCreatedAt());
    }

    UserService(UserRepository userRepository,  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setUserType(request.userType());

        if (request.userType() == UserType.PRIVATE) {
            user.setUserStatus(UserStatus.ACTIVE);
        } else {
            user.setUserStatus(UserStatus.PENDING_APPROVAL);
        }

        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException(id));

        return toResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new UserNotFoundException(email));
        return toResponse(user);
    }

    public UserResponse updateUserProfile(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException(id));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }














}
