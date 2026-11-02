package it.commerce.backend.user;

import it.commerce.backend.user.dto.CreateUserRequest;
import it.commerce.backend.user.dto.UpdateUserRequest;
import it.commerce.backend.user.dto.UpdateUserStatusRequest;
import it.commerce.backend.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserShouldReturnUserWhenPrivateUserIsCreated() {
        //Arrange
        CreateUserRequest createUserRequest = new CreateUserRequest(
                "test@email.com",
                "testpassword123",
                "Mario",
                "Rossi",
                UserType.PRIVATE
        );

        when(userRepository.existsByEmail(createUserRequest.email())).thenReturn(false);

        when(passwordEncoder.encode(createUserRequest.password()))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User userToSave = invocation.getArgument(0);
                    ReflectionTestUtils.setField(userToSave, "id", 1L);
                    return userToSave;
                });

        //Act
        UserResponse createdUser = userService.createUser(createUserRequest);

        //Assert
        assertEquals(1L, createdUser.id());
        assertEquals("test@email.com", createdUser.email());
        assertEquals("Mario", createdUser.firstName());
        assertEquals("Rossi", createdUser.lastName());
        assertEquals(UserStatus.ACTIVE, createdUser.userStatus());
        assertEquals(UserType.PRIVATE, createdUser.userType());
        assertEquals(false, createdUser.emailVerified());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(passwordEncoder).encode(createUserRequest.password());
        verify(userRepository).save(userCaptor.capture());

        assertEquals("hashed-password", userCaptor.getValue().getPasswordHash());
    }
    @Test
    void createUserShouldReturnUserWhenBusinessUserIsCreated() {
        //Arrange
        CreateUserRequest createUserRequest = new CreateUserRequest(
                "test@email.com",
                "testpassword123",
                "Mario",
                "Rossi",
                UserType.BUSINESS
        );

        when(userRepository.existsByEmail(createUserRequest.email())).thenReturn(false);

        when(passwordEncoder.encode(createUserRequest.password()))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User userToSave = invocation.getArgument(0);
                    ReflectionTestUtils.setField(userToSave, "id", 1L);
                    return userToSave;
                });

        //Act
        UserResponse createdUser = userService.createUser(createUserRequest);

        //Assert
        assertEquals(1L, createdUser.id());
        assertEquals("test@email.com", createdUser.email());
        assertEquals("Mario", createdUser.firstName());
        assertEquals("Rossi", createdUser.lastName());
        assertEquals(UserStatus.PENDING_APPROVAL, createdUser.userStatus());
        assertEquals(UserType.BUSINESS, createdUser.userType());
        assertEquals(false, createdUser.emailVerified());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(passwordEncoder).encode(createUserRequest.password());
        verify(userRepository).save(userCaptor.capture());

        assertEquals("hashed-password", userCaptor.getValue().getPasswordHash());
    }

    @Test
    void createUserShouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        CreateUserRequest createUserRequest = new CreateUserRequest(
                "test@email.com",
                "testpassword123",
                "Mario",
                "Rossi",
                UserType.BUSINESS
        );

        when(userRepository.existsByEmail("test@email.com")).thenReturn(true);

        // Act & Assert
        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.createUser(createUserRequest));

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }
    @Test
    void findAllShouldReturnPageOfUsers() {
        //Arrange
        User userTest1 = new User();
        ReflectionTestUtils.setField(userTest1, "id", 1L);
        userTest1.setEmail("test1@email.com");
        userTest1.setFirstName("test1");
        userTest1.setLastName("testtest1");
        userTest1.setPasswordHash("hashed-password-1");
        userTest1.setUserStatus(UserStatus.PENDING_APPROVAL);
        userTest1.setUserType(UserType.BUSINESS);
        userTest1.setEmailVerified(false);
        userTest1.setCreatedAt(LocalDateTime.now());

        User userTest2 = new User();
        ReflectionTestUtils.setField(userTest2, "id", 2L);
        userTest2.setEmail("test2@email.com");
        userTest2.setFirstName("test2");
        userTest2.setLastName("testtest2");
        userTest2.setPasswordHash("hashed-password-2");
        userTest2.setUserStatus(UserStatus.ACTIVE);
        userTest2.setUserType(UserType.PRIVATE);
        userTest2.setEmailVerified(true);
        userTest2.setCreatedAt(LocalDateTime.now());

        Pageable pageable = PageRequest.of(
                0, 2, Sort.by("id").ascending()
        );

        Page<User> userPages = new PageImpl<>(Arrays.asList(userTest1, userTest2), pageable, 3);

        when(userRepository.findAll(pageable)).thenReturn(userPages);

        //Act
        Page<UserResponse> pageResult = userService.findAll(pageable);

        //Assert
        assertEquals(2, pageResult.getTotalPages());
        assertEquals(3, pageResult.getTotalElements());
        assertEquals(2, pageResult.getContent().size());

        assertEquals(1L, pageResult.getContent().get(0).id());
        assertEquals("test1@email.com", pageResult.getContent().get(0).email());
        assertEquals("test1", pageResult.getContent().get(0).firstName());
        assertEquals("testtest1", pageResult.getContent().get(0).lastName());
        assertEquals(UserStatus.PENDING_APPROVAL,  pageResult.getContent().get(0).userStatus());
        assertEquals(UserType.BUSINESS,  pageResult.getContent().get(0).userType());
        assertEquals(false, pageResult.getContent().get(0).emailVerified());
        assertEquals(userTest1.getCreatedAt(), pageResult.getContent().get(0).createdAt());

        assertEquals(2L, pageResult.getContent().get(1).id());
        assertEquals("test2@email.com", pageResult.getContent().get(1).email());
        assertEquals("test2", pageResult.getContent().get(1).firstName());
        assertEquals("testtest2", pageResult.getContent().get(1).lastName());
        assertEquals(UserStatus.ACTIVE,  pageResult.getContent().get(1).userStatus());
        assertEquals(UserType.PRIVATE,  pageResult.getContent().get(1).userType());
        assertEquals(true, pageResult.getContent().get(1).emailVerified());
        assertEquals(userTest2.getCreatedAt(), pageResult.getContent().get(1).createdAt());

        verify(userRepository).findAll(pageable);
    }

    @Test
    void getUserByIdShouldReturnUser() {

        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setEmail("test@email.com");
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setPasswordHash("hashed-password");
        user.setUserStatus(UserStatus.PENDING_APPROVAL);
        user.setUserType(UserType.BUSINESS);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse userResponse = userService.getUserById(1L);

        assertEquals(1L, userResponse.id());
        assertEquals("test@email.com", userResponse.email());
        assertEquals("Mario", userResponse.firstName());
        assertEquals("Rossi", userResponse.lastName());
        assertEquals(UserStatus.PENDING_APPROVAL, userResponse.userStatus());
        assertEquals(UserType.BUSINESS, userResponse.userType());
        assertFalse(userResponse.emailVerified());
        assertEquals(user.getCreatedAt(), userResponse.createdAt());

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserByIdShouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(1L)
        );

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserByEmailShouldReturnUser() {

        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setEmail("test@email.com");
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setPasswordHash("hashed-password");
        user.setUserStatus(UserStatus.PENDING_APPROVAL);
        user.setUserType(UserType.BUSINESS);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        UserResponse userResponse = userService.getUserByEmail("test@email.com");

        assertEquals(1L, userResponse.id());
        assertEquals("test@email.com", userResponse.email());
        assertEquals("Mario", userResponse.firstName());
        assertEquals("Rossi", userResponse.lastName());
        assertEquals(UserStatus.PENDING_APPROVAL, userResponse.userStatus());
        assertEquals(UserType.BUSINESS, userResponse.userType());
        assertFalse(userResponse.emailVerified());
        assertEquals(user.getCreatedAt(), userResponse.createdAt());

        verify(userRepository).findByEmail("test@email.com");
    }

    @Test
    void getUserByEmailShouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserByEmail("test@email.com")
        );

        verify(userRepository).findByEmail("test@email.com");
    }


    @Test
    void updateUserProfileShouldReturnUserWhenUserIsUpdated() {
        // Arrange
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);

        user.setEmail("test@email.com");
        user.setFirstName("Old FirstName");
        user.setLastName("Old LastName");
        user.setPasswordHash("hashed-password");
        user.setUserStatus(UserStatus.ACTIVE);
        user.setUserType(UserType.PRIVATE);
        user.setEmailVerified(true);

        LocalDateTime createdAt = LocalDateTime.now();
        user.setCreatedAt(createdAt);

        UpdateUserRequest updateUserRequest = new UpdateUserRequest(
                "Mario",
                "Rossi"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponse updatedUser = userService.updateUserProfile(
                1L,
                updateUserRequest
        );

        // Assert - UserResponse
        assertEquals(1L, updatedUser.id());
        assertEquals("test@email.com", updatedUser.email());
        assertEquals("Mario", updatedUser.firstName());
        assertEquals("Rossi", updatedUser.lastName());
        assertEquals(UserStatus.ACTIVE, updatedUser.userStatus());
        assertEquals(UserType.PRIVATE, updatedUser.userType());
        assertTrue(updatedUser.emailVerified());
        assertEquals(createdAt, updatedUser.createdAt());

        // Assert - Entity salvata
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("Mario", savedUser.getFirstName());
        assertEquals("Rossi", savedUser.getLastName());

        // Verify
        verify(userRepository).findById(1L);
    }

    @Test
    void updateUserProfileShouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUserProfile(1L, new UpdateUserRequest("Mario", "Rossi"))
        );

        assertEquals("User not found with id: " + 1L, userNotFoundException.getMessage());

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserStatusShouldReturnUserWhenStatusIsUpdated() {

        // Arrange
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);

        user.setEmail("test@email.com");
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setPasswordHash("hashed-password");
        user.setUserStatus(UserStatus.PENDING_APPROVAL);
        user.setUserType(UserType.BUSINESS);
        user.setEmailVerified(true);

        LocalDateTime createdAt = LocalDateTime.now();
        user.setCreatedAt(createdAt);

        UpdateUserStatusRequest updateUserStatusRequest = new UpdateUserStatusRequest(UserStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponse updatedUser = userService.updateUserStatus(
                1L,
                updateUserStatusRequest
        );

        // Assert - UserResponse
        assertEquals(1L, updatedUser.id());
        assertEquals("test@email.com", updatedUser.email());
        assertEquals("Mario", updatedUser.firstName());
        assertEquals("Rossi", updatedUser.lastName());
        assertEquals(UserStatus.ACTIVE, updatedUser.userStatus());
        assertEquals(UserType.BUSINESS, updatedUser.userType());
        assertTrue(updatedUser.emailVerified());
        assertEquals(createdAt, updatedUser.createdAt());

        // Assert - Entity salvata
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(UserStatus.ACTIVE, savedUser.getUserStatus());

        // Verify
        verify(userRepository).findById(1L);
    }

    @Test
    void updateUserStatusShouldReturnUserWhenUserIsSuspended() {

        // Arrange
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);

        user.setEmail("test@email.com");
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setPasswordHash("hashed-password");
        user.setUserStatus(UserStatus.ACTIVE);
        user.setUserType(UserType.BUSINESS);
        user.setEmailVerified(true);

        LocalDateTime createdAt = LocalDateTime.now();
        user.setCreatedAt(createdAt);

        UpdateUserStatusRequest updateUserStatusRequest = new UpdateUserStatusRequest(UserStatus.SUSPENDED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponse updatedUser = userService.updateUserStatus(
                1L,
                updateUserStatusRequest
        );

        // Assert - UserResponse
        assertEquals(1L, updatedUser.id());
        assertEquals("test@email.com", updatedUser.email());
        assertEquals("Mario", updatedUser.firstName());
        assertEquals("Rossi", updatedUser.lastName());
        assertEquals(UserStatus.SUSPENDED, updatedUser.userStatus());
        assertEquals(UserType.BUSINESS, updatedUser.userType());
        assertTrue(updatedUser.emailVerified());
        assertEquals(createdAt, updatedUser.createdAt());

        // Assert - Entity salvata
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(UserStatus.SUSPENDED, savedUser.getUserStatus());

        // Verify
        verify(userRepository).findById(1L);
    }

    @Test
    void updateUserStatusShouldThrowExceptionWhenTransitionToPendingApprovalIsInvalid() {
        // Arrange
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);

        user.setEmail("test@email.com");
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setPasswordHash("hashed-password");
        user.setUserStatus(UserStatus.ACTIVE);
        user.setUserType(UserType.PRIVATE);
        user.setEmailVerified(true);

        LocalDateTime createdAt = LocalDateTime.now();
        user.setCreatedAt(createdAt);

        UpdateUserStatusRequest updateUserStatusRequest = new UpdateUserStatusRequest(UserStatus.PENDING_APPROVAL);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        InvalidUserStatusTransitionException exception = assertThrows(
                InvalidUserStatusTransitionException.class,
                () -> userService.updateUserStatus(1L, updateUserStatusRequest));

        // Assert
        assertEquals("Invalid user status transition from " + user.getUserStatus() + " to " + updateUserStatusRequest.userStatus(),
                exception.getMessage());

        // Verify
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserStatusShouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUserStatus(1L, new UpdateUserStatusRequest(UserStatus.ACTIVE))
        );

        assertEquals("User not found with id: " + 1L, userNotFoundException.getMessage());

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

}
