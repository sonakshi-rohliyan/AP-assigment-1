package unit;

import exceptions.*;
import capabilities.FuelPowered;
import capabilities.ContainmentCarrier;
import incidents.Incident;
import incidents.HazmatIncident;
import dispatch.DispatchPolicy;

public class HazmatUnit extends GroundResponseUnit implements FuelPowered, ContainmentCarrier {

    private double currentFuel;
    private double fuelCapacity;
    private static final double FUEL_EFFICIENCY = 8.0; // km per litre
    private double currentContainment;
    private double containmentCapacity;

    public HazmatUnit(String id, String name, double maxSpeed, double trafficFactor) throws InvalidOperationException {
        super(id, name, maxSpeed, trafficFactor);
        this.fuelCapacity = 120.0;
        this.currentFuel = 120.0;
        this.containmentCapacity = 500.0;
        this.currentContainment = 500.0;
    }

    @Override
    public String getCapability() {
        return "HAZMAT";
    }

    @Override
    public void refuel(double amount) throws InvalidOperationException {
        if (amount <= 0) {
            throw new InvalidOperationException("Refuel amount must be positive");
        }
        if (currentFuel + amount > fuelCapacity) {
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
    public double consumeFuel(double distance) throws InvalidOperationException, InsufficientResourceException {
        if (distance < 0) {
            throw new InvalidOperationException("The distance to cover cannot be less than 0");
        }
        double fuelConsumed = distance / FUEL_EFFICIENCY;
        if (fuelConsumed > currentFuel) {
            throw new InsufficientResourceException("The total current fuel level is less than the fuel needed.");
        }
        currentFuel = currentFuel - fuelConsumed;
        return fuelConsumed;
    }

    @Override
    public boolean hasFuelFor(double distance) {
        double consumed = distance / FUEL_EFFICIENCY;
        return consumed <= currentFuel;
    }

    @Override
    public void loadContainmentAgent(double amount) throws InvalidOperationException {
        if (amount < 0) {
            throw new InvalidOperationException("Cannot load a negative amount of containment agent");
        }
        if (currentContainment + amount > containmentCapacity) {
            throw new InvalidOperationException("Exceeds containment agent capacity");
        }
        currentContainment = currentContainment + amount;
    }

    @Override
    public void useContainment(double amount) throws InvalidOperationException, InsufficientResourceException {
        if (amount < 0) {
            throw new InvalidOperationException("Cannot use a negative amount of containment agent");
        }
        if (amount > currentContainment) {
            throw new InsufficientResourceException("Insufficient containment agent for unit " + getId());
        }
        currentContainment = currentContainment - amount;
    }

    @Override
    public double getContainmentLevel() { return currentContainment; }

    @Override
    public double getContainmentCapacity() { return containmentCapacity; }

    @Override
    public void move(double distance) throws InvalidOperationException, InsufficientResourceException {
        consumeFuel(distance);
        super.move(distance);
    }

    @Override
    public boolean hasResourcesFor(Incident incident) {
        if (incident.getRequiredCapability().equals("HAZMAT")) {
            if (currentContainment < incident.getWorkload()) {
                return false;
            }
            return hasFuelFor(2 * incident.getDistanceFromBase());
        }
        return false;
    }

    @Override
    public double calculateDispatchScore(Incident incident, DispatchPolicy policy) {
        double base_score = super.calculateDispatchScore(incident, policy);

        if (currentFuel < policy.getLowFuelThreshold() * fuelCapacity) {
            base_score = base_score + policy.getLowFuelPenalty();
        }
        if (currentContainment < policy.getLowContainmentThreshold() * containmentCapacity) {
            base_score = base_score + policy.getLowContainmentPenalty();
        }

        return base_score;
    }
}