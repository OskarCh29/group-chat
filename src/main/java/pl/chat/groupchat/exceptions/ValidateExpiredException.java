package pl.chat.groupchat.exceptions;

public class ValidateExpiredException extends RuntimeException {
    public ValidateExpiredException(String message) {
        super(message);
    }
}
