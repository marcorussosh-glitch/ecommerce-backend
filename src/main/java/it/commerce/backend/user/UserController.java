package it.commerce.backend.user;

import it.commerce.backend.user.dto.CreateUserRequest;
import it.commerce.backend.user.dto.UpdateUserRequest;
import it.commerce.backend.user.dto.UpdateUserStatusRequest;
import it.commerce.backend.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Page<UserResponse> findAll(
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(
            @Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    public UserResponse updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUserProfile(id,request);
    }
    @PutMapping("/{id}/status")
    public UserResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return userService.updateUserStatus(id,request);
    }
}
