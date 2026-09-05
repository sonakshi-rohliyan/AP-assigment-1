package capabilities;

import exceptions.InsufficientResourceException;
import exceptions.InvalidOperationException;

public interface ContainmentCarrier {
    void loadContainment(double amount) throws InvalidOperationException;
    void useContainment(double amount)throws InvalidOperationException, InsufficientResourceException;
    double getContainmentLevel();
    double getContainmentCapacity();
}