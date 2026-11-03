package it.commerce.backend;

import it.commerce.backend.category.CategoryInUseException;
import it.commerce.backend.category.CategoryNotFoundException;
import it.commerce.backend.product.ProductNotFoundException;
import it.commerce.backend.user.EmailAlreadyExistsException;
import it.commerce.backend.user.EmailNotVerifiedException;
import it.commerce.backend.user.InvalidUserStatusTransitionException;
import it.commerce.backend.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> productNotFoundHandler (ProductNotFoundException exception){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<String> categoryNotFoundHandler (CategoryNotFoundException exception){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }
    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<String> categoryInUseHandler (CategoryInUseException exception){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> userNotFoundHandler (UserNotFoundException exception){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }
    @ExceptionHandler(InvalidUserStatusTransitionException.class)
    public ResponseEntity<String> invalidUserStatusTransitionHandler (InvalidUserStatusTransitionException exception){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<String> emailNotVerifiedHandler (EmailNotVerifiedException exception){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> emailAlreadyExistsHandler (EmailAlreadyExistsException exception){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(
            BadCredentialsException exception) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid email or password");
    }
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<String> handleDisabledAccount(
            DisabledException exception) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid email or password");
    }
}
