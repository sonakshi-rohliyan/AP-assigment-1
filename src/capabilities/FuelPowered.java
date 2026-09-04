package capabilities;

public interface FuelPowered {
    void refuel(double amount);
    double getFuelLevel();
    double getFuelCapacity();
    double consumeFuel(double distance);
    boolean hasFuelFor(double distance);
}
