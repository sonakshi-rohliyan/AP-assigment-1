package capabilities;

import exceptions.InsufficientResourceException;
import exceptions.InvalidOperationException;

public interface FuelPowered {
    void refuel(double amount) throws InvalidOperationException;
    double getFuelLevel();
    double getFuelCapacity();
    double consumeFuel(double distance) throws InvalidOperationException, InsufficientResourceException;
    boolean hasFuelFor(double distance);
}
