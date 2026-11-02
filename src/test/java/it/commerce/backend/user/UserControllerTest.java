package it.commerce.backend.user;

import it.commerce.backend.user.dto.CreateUserRequest;
import it.commerce.backend.user.dto.UpdateUserRequest;
import it.commerce.backend.user.dto.UpdateUserStatusRequest;
import it.commerce.backend.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void findAllShouldReturnUsers() throws Exception {

        List<UserResponse> users = List.of(
                new UserResponse(
                        1L,
                        "test@email.com",
                        "Mario",
                        "Rossi",
                        UserType.PRIVATE,
                        UserStatus.ACTIVE,
                        true,
                        LocalDateTime.now()));

        Pageable pageable = PageRequest.of(
                0,
                2,
                Sort.by("id").ascending());
        Page<UserResponse> userPage = new PageImpl<>(users, pageable, 3);

        when(userService.findAll(any(Pageable.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].email").value("test@email.com"))
                .andExpect(jsonPath("$.content[0].firstName").value("Mario"))
                .andExpect(jsonPath("$.content[0].lastName").value("Rossi"))
                .andExpect(jsonPath("$.content[0].userType").value("PRIVATE"))
                .andExpect(jsonPath("$.content[0].userStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.content[0].emailVerified").value(true))
                .andExpect(jsonPath("$.content[0].createdAt").exists())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2));

        verify(userService).findAll(any(Pageable.class));
    }

    @Test
    void findByIdShouldReturnUser() throws Exception {

        UserResponse user =
                new UserResponse(
                        1L,
                        "test@email.com",
                        "Mario",
                        "Rossi",
                        UserType.PRIVATE,
                        UserStatus.ACTIVE,
                        true,
                        LocalDateTime.now()
                );

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.firstName").value("Mario"))
                .andExpect(jsonPath("$.lastName").value("Rossi"))
                .andExpect(jsonPath("$.userType").value("PRIVATE"))
                .andExpect(jsonPath("$.userStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.emailVerified").value(true))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(userService).getUserById(1L);
    }

    @Test
    void findByIdShouldReturnNotFound() throws Exception {

        when(userService.getUserById(99L)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(99L);
    }

    @Test
    void createShouldReturnCreated() throws Exception {

        UserResponse userResponse = new UserResponse(
                        1L,
                        "test@email.com",
                        "Mario",
                        "Rossi",
                        UserType.PRIVATE,
                        UserStatus.ACTIVE,
                        false,
                        LocalDateTime.now()
                );

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@email.com",
                                    "password": "Password123!",
                                    "firstName": "Mario",
                                    "lastName": "Rossi",
                                    "userType": "PRIVATE"
                                }
                                """))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.firstName").value("Mario"))
                .andExpect(jsonPath("$.lastName").value("Rossi"))
                .andExpect(jsonPath("$.userType").value("PRIVATE"))
                .andExpect(jsonPath("$.userStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.emailVerified").value(false))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    void createShouldReturnConflictWhenEmailAlreadyExists() throws Exception {

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already exists"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@email.com",
                                    "password": "Password123!",
                                    "firstName": "Mario",
                                    "lastName": "Rossi",
                                    "userType": "PRIVATE"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value("Email already exists"));

        verify(userService).createUser(any(CreateUserRequest.class));
    }
    @Test
    void updateProfileShouldReturnUpdatedUser() throws Exception {

        UserResponse userResponse = new UserResponse(
                1L,
                "test@email.com",
                "Mario",
                "Rossi",
                UserType.PRIVATE,
                UserStatus.ACTIVE,
                false,
                LocalDateTime.now()
        );

        when(userService.updateUserProfile(eq(1L), any(UpdateUserRequest.class)))
                .thenReturn(userResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Mario",
                                    "lastName": "Rossi"
                                }
                                """))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.firstName").value("Mario"))
                .andExpect(jsonPath("$.lastName").value("Rossi"))
                .andExpect(jsonPath("$.userType").value("PRIVATE"))
                .andExpect(jsonPath("$.userStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.emailVerified").value(false))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(userService).updateUserProfile(eq(1L), any(UpdateUserRequest.class));
    }

    @Test
    void updateProfileShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {

        when(userService.updateUserProfile(eq(99L), any(UpdateUserRequest.class)))
                .thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Mario",
                                    "lastName": "Rossi"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found with id: " + 99L));

        verify(userService).updateUserProfile(eq(99L), any(UpdateUserRequest.class));
    }

    @Test
    void updateStatusShouldReturnUpdatedUser() throws Exception {

        UserResponse userResponse = new UserResponse(
                1L,
                "test@email.com",
                "Mario",
                "Rossi",
                UserType.PRIVATE,
                UserStatus.ACTIVE,
                false,
                LocalDateTime.now()
        );

        when(userService.updateUserStatus(eq(1L), any(UpdateUserStatusRequest.class)))
                .thenReturn(userResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userStatus": "ACTIVE"
                                }
                                """))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.firstName").value("Mario"))
                .andExpect(jsonPath("$.lastName").value("Rossi"))
                .andExpect(jsonPath("$.userType").value("PRIVATE"))
                .andExpect(jsonPath("$.userStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.emailVerified").value(false))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(userService).updateUserStatus(eq(1L), any(UpdateUserStatusRequest.class));
    }

    @Test
    void updateStatusShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {

        when(userService.updateUserStatus(eq(99L), any(UpdateUserStatusRequest.class)))
                .thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/{id}/status", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userStatus": "ACTIVE"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found with id: " + 99L));

        verify(userService).updateUserStatus(eq(99L), any(UpdateUserStatusRequest.class));
    }

    @Test
    void updateStatusShouldReturnConflictWhenEmailIsNotVerified() throws Exception {

        when(userService.updateUserStatus(eq(99L), any(UpdateUserStatusRequest.class)))
                .thenThrow(new EmailNotVerifiedException("Email not verified"));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/{id}/status", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userStatus": "ACTIVE"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email not verified"));

        verify(userService).updateUserStatus(eq(99L), any(UpdateUserStatusRequest.class));
    }

    @Test
    void updateStatusShouldReturnConflictWhenTransitionIsInvalid() throws Exception {

        UserStatus currentStatus = UserStatus.ACTIVE;
        UserStatus newStatus = UserStatus.PENDING_APPROVAL;

        when(userService.updateUserStatus(eq(99L), any(UpdateUserStatusRequest.class)))
                .thenThrow(new InvalidUserStatusTransitionException(currentStatus, newStatus));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/{id}/status", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userStatus": "PENDING_APPROVAL"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().string("Invalid user status transition from " + currentStatus + " to " + newStatus));

        verify(userService).updateUserStatus(eq(99L), any(UpdateUserStatusRequest.class));
    }

    @Test
    void createShouldReturnBadRequestWhenRequestIsInvalid()  throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "",
                                    "password": "Password123!",
                                    "firstName": "",
                                    "lastName": "Rossi",
                                    "userType": "PRIVATE"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }
}
