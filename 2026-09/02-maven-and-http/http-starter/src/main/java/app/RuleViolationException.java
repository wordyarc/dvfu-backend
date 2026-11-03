package app;

public class RuleViolationException extends RuntimeException {
    public RuleViolationException(String message) {
        super(message);
    }
}