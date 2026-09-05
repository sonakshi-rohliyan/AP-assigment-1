package capabilities;

import exceptions.InsufficientResourceException;
import exceptions.InvalidOperationException;

public interface BatteryPowered {
    void recharge(double amount) throws InvalidOperationException;
    double getBatteryLevel();
    double getBatteryCapacity();
    double consumeBattery(double amount) throws InvalidOperationException, InsufficientResourceException;
    boolean hasBatteryFor(double amount);
}
