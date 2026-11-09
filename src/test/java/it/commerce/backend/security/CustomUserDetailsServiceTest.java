package it.commerce.backend.security;

import it.commerce.backend.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@email.com");
        user.setPasswordHash("hashed-password");
        user.setUserRole(UserRole.USER);
        user.setUserStatus(UserStatus.ACTIVE);
    }

    @Test
    void loadUserByUsernameShouldReturnUserWithUserRoleWhenUserIsFound() {

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        assertTrue(
                userDetails.getAuthorities().stream()
                        .anyMatch(authority -> Objects.equals(authority.getAuthority(), "ROLE_USER"))
        );

        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void loadUserByUsernameShouldReturnUserWithAdminRoleWhenUserIsFound() {

        user.setUserRole(UserRole.ADMIN);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        assertTrue(
                userDetails.getAuthorities().stream()
                        .anyMatch(authority -> Objects.equals(authority.getAuthority(), "ROLE_ADMIN"))
        );
        assertEquals(1, userDetails.getAuthorities().size());

        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void loadUserByUsernameShouldReturnDisabledUserWhenUserIsSuspended() {

        user.setUserStatus(UserStatus.SUSPENDED);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        assertFalse(userDetails.isEnabled());

        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void loadUserByUsernameShouldThrowExceptionWhenUserIsNotFound() {

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(user.getEmail()));

        verify(userRepository).findByEmail(user.getEmail());
    }
}