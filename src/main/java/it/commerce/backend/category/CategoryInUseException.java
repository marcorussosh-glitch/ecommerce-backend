package it.commerce.backend.category;

public class CategoryInUseException extends RuntimeException {

    public CategoryInUseException(String message) {super(message);}
}
