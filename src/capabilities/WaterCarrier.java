package capabilities;

import exceptions.InsufficientResourceException;
import exceptions.InvalidOperationException;

public interface WaterCarrier {
    void refillWater(double amount) throws InvalidOperationException;
    void useWater(double amount)throws InvalidOperationException, InsufficientResourceException;
    double getWaterLevel();
    double getWaterCapacity();
}
