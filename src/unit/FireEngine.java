package unit;

import exceptions.*;
import capabilities.FuelPowered;
import capabilities.WaterCarrier;
import incidents.Incident;
import dispatch.DispatchPolicy;

public class FireEngine extends GroundResponseUnit implements FuelPowered, WaterCarrier {

    private double currentFuel;
    private double fuelCapacity;
    private static final double FUEL_EFFICIENCY = 5.0; // km per litre
    private double waterLevel;
    private double waterCapacity;

    public FireEngine(String id, String name, double maxSpeed, double trafficFactor)
            throws InvalidOperationException {
        super(id, name, maxSpeed, trafficFactor);
        this.fuelCapacity = 150.0;
        this.currentFuel = 150.0;
        this.waterCapacity = 3000.0;
        this.waterLevel = 3000.0;
    }

    @Override
    public String getCapability() {
        return "FIRE";
    }

    @Override
    public void refuel(double amount) throws InvalidOperationException {
        if (amount <= 0) {
            throw new InvalidOperationException("The amount of fuel must be more than 0");
        }
        if (currentFuel + amount > fuelCapacity){
            currentFuel = fuelCapacity;
        }
        else {
            currentFuel = currentFuel + amount;
        }
    }

    @Override
    public double getFuelLevel() { return currentFuel; }

    @Override
    public double getFuelCapacity() { return fuelCapacity; }

    @Override
    public double consumeFuel(double distance) throws InvalidOperationException, InsufficientResourceException{
        if (distance < 0){
            throw new InvalidOperationException("The distance to cover can not be less than 0");
        }
        double fuelConsumed = distance/FUEL_EFFICIENCY;
        if (fuelConsumed > currentFuel){
            throw new InsufficientResourceException("The total current fuel level is less than the fuel needed.");
        }
        currentFuel = currentFuel - fuelConsumed;
        return fuelConsumed;
    }

    @Override
    public boolean hasFuelFor(double distance) {
        double consumed = distance/FUEL_EFFICIENCY;
        if (consumed > currentFuel){
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void refillWater(double amount) throws InvalidOperationException {
        if (amount <= 0) {
            throw new InvalidOperationException("Refill amount must be positive");
        }
        if (waterLevel + amount > waterCapacity){
            waterLevel = waterCapacity;
        }
        else {
            waterLevel = waterLevel + amount;
        }
    }

    @Override
    public void useWater(double amount) throws InsufficientResourceException, InvalidOperationException {
        if (amount < 0) {
            throw new InvalidOperationException("Cannot use a negative amount of water");
        }
        if (amount > waterLevel) {
            throw new InsufficientResourceException("Insufficient water for fire engine ");
        }
        waterLevel = waterLevel - amount;
    }

    @Override
    public double getWaterLevel() { return waterLevel; }

    @Override
    public double getWaterCapacity() { return waterCapacity; }

    @Override
    public void move(double distance) throws InvalidOperationException, InsufficientResourceException {
        consumeFuel(distance);
        super.move(distance);
    }

    @Override
    public boolean hasResourcesFor(Incident incident) {
        if (incident.getRequiredCapability().equals("FIRE")) {
            if (waterLevel < incident.getWorkload()) {
                return false;
            }
            return hasFuelFor(2*incident.getDistanceFromBase());
        }
        return false;
    }

    @Override
    public double calculateDispatchScore(Incident incident, DispatchPolicy policy) {
        double score = super.calculateDispatchScore(incident, policy);

        if (currentFuel < policy.getLowFuelThreshold() * fuelCapacity) {
            score = score + policy.getLowFuelPenalty();
        }
        if (waterLevel < policy.getLowWaterThreshold() * waterCapacity) {
            score = score + policy.getLowWaterPenalty();
        }

        return score;
    }
}