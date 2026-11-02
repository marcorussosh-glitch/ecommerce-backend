package it.commerce.backend.user;

public class InvalidUserStatusTransitionException extends RuntimeException{

    public InvalidUserStatusTransitionException(UserStatus currentStatus, UserStatus newStatus) {
        super("Invalid user status transition from " + currentStatus + " to " + newStatus);
    }

}