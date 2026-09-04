package exceptions;

public class InsufficientResourceException extends Exception{
    public InsufficientResourceException(String message) {
        super(message);
    }

    public InsufficientResourceException() {
        super("Invalid operation because insufficient fuel, battery, water etc. ");
    }

}
