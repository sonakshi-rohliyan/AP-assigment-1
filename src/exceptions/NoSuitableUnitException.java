package exceptions;

public class NoSuitableUnitException extends Exception{
    public NoSuitableUnitException(String message) {
        super(message);
    }

    public NoSuitableUnitException() {
        super("Invalid operation because no empty unit found. ");
    }

}
