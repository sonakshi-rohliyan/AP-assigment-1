package capabilities;

import exceptions.InsufficientResourceException;
import exceptions.InvalidOperationException;

public interface SupplyCarrier {
    void loadSupplies(int number) throws InvalidOperationException;
    void useSupplies(int number) throws InvalidOperationException, InsufficientResourceException;
    int getSupplyLevel();
    int getSupplyCapacity();
}
