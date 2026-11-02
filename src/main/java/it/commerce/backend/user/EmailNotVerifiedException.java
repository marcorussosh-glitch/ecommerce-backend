package it.commerce.backend.user;

public class EmailNotVerifiedException extends RuntimeException{

    public EmailNotVerifiedException(String message) {
        super(message);
    }

}