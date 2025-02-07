package pl.chat.groupchat.exceptions;

public class ValidationExpiredException extends RuntimeException {
    public ValidationExpiredException(String message) {
        super(message);
    }
}
