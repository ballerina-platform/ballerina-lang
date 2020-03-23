package exception;

public class GeneratorException extends Exception {
    public GeneratorException(String message) {
        super(message);
    }

    public GeneratorException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
