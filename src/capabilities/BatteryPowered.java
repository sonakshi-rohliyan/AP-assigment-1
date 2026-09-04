package capabilities;

public interface BatteryPowered {
    void recharge(double amount);
    double getBatteryLevel();
    double getBatteryCapacity();
    double consumeBattery(double amount);
    boolean hasBatteryFor(double amount);
}
