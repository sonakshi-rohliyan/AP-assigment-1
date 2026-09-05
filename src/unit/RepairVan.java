package unit;

import exceptions.*;
import capabilities.FuelPowered;
import capabilities.SupplyCarrier;
import incidents.Incident;
import dispatch.DispatchPolicy;

public class RepairVan extends GroundResponseUnit implements FuelPowered, SupplyCarrier {

    private double currentFuel;
    private double fuelCapacity;
    private static final double FUEL_EFFICIENCY = 10.0; // km per litre
    private int currentSupplies;
    private int supplyCapacity;

    public RepairVan(String id, String name, double maxSpeed, double trafficFactor) throws InvalidOperationException {
        super(id, name, maxSpeed, trafficFactor);
        this.fuelCapacity = 100.0;
        this.currentFuel = 100.0;
        this.supplyCapacity = 30;
        this.currentSupplies = 30;
    }

    @Override
    public String getCapability() {
        return "REPAIR";
    }

    @Override
    public void refuel(double amount) throws InvalidOperationException {
        if (amount <= 0) {
            throw new InvalidOperationException("Refuel amount must be positive");
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
    public void loadSupplies(int number) throws InvalidOperationException {
        if (number < 0) {
            throw new InvalidOperationException("Cannot load a negative number of supplies");
        }
        if (currentSupplies + number > supplyCapacity) {
            throw new InvalidOperationException("The total current supply exceeds the maximum capacity");
        }
        currentSupplies = currentSupplies + number;
    }

    @Override
    public void useSupplies(int number) throws InvalidOperationException, InsufficientResourceException {
        if (number < 0) {
            throw new InvalidOperationException("Cannot use a negative number of supplies");
        }
        if (number > currentSupplies) {
            throw new InsufficientResourceException("Insufficient supplies for repair van ");
        }
        currentSupplies = currentSupplies - number;
    }

    @Override
    public int getSupplyLevel() { return currentSupplies; }

    @Override
    public int getSupplyCapacity() { return supplyCapacity; }


    @Override
    public void move(double distance) throws InvalidOperationException, InsufficientResourceException {
        consumeFuel(distance);
        super.move(distance);
    }

    @Override
    public boolean hasResourcesFor(Incident incident) {
        if (incident.getRequiredCapability().equals("REPAIR")) {
            if (currentSupplies < incident.getWorkload()) {
                return false;
            }
            return hasFuelFor(2*incident.getDistanceFromBase());
        }
        return false;
    }

    @Override
    public double calculateDispatchScore(Incident incident, DispatchPolicy policy) {
        double base_score = super.calculateDispatchScore(incident, policy);

        if (currentFuel < policy.getLowFuelThreshold() * fuelCapacity) {
            base_score = base_score + policy.getLowFuelPenalty();
        }
        if (currentSupplies < policy.getLowSupplyThreshold() * supplyCapacity) {
            base_score = base_score + policy.getLowSupplyPenalty();
        }

        return base_score;
    }
}