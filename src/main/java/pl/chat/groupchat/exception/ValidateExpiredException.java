package pl.chat.groupchat.exception;

public class ValidateExpiredException extends RuntimeException {
    public ValidateExpiredException(String message) {
        super(message);
    }
}
