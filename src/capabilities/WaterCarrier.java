package capabilities;

public interface WaterCarrier {
    void refillWater(double amount);
    void useWater(double amount);
    double getWaterLevel();
    double getWaterCapacity();
}
