package exceptions;

public class DuplicateIdException extends Exception{
    public DuplicateIdException(String message) {
        super(message);
    }

    public DuplicateIdException() {
        super("Invalid operation because of duplicate ID's. ");
    }

}
