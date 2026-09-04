package exceptions;

public class InvalidOperationException extends Exception{
    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException() {
        super("Invalid operation. For example, negative distance, invalid severity");
    }

}
