package capabilities;

import exceptions.InsufficientResourceException;
import exceptions.InvalidOperationException;

public interface ContainmentCarrier {
    void loadContainmentAgent(double amount) throws InvalidOperationException;
    void useContainment(double amount)throws InvalidOperationException, InsufficientResourceException;
    double getContainmentLevel();
    double getContainmentCapacity();
}