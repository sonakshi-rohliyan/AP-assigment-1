package unit;

import exceptions.*;
import capabilities.BatteryPowered;
import incidents.Incident;
import dispatch.DispatchPolicy;

public class SearchDrone extends ResponseUnit implements BatteryPowered {

    private double currentBattery;
    private double batteryCapacity;
    private static final double TRAVEL_CONSUMPTION = 0.6;

    public SearchDrone(String id, String name, double maxSpeed) throws InvalidOperationException {
        super(id, name, maxSpeed);
        this.batteryCapacity = 100.0;
        this.currentBattery = 100.0;
    }

    @Override
    public String getCapability() {
        return "SEARCH";
    }

    @Override
    public double estimateArrivalTime(double distance) {
        double baseTime = distance / getMaxSpeed();
        return baseTime * 0.85;
    }

    @Override
    public void recharge(double amount) throws InvalidOperationException {
        if (amount <= 0) {
            throw new InvalidOperationException("Recharge amount must be positive");
        }
        if (currentBattery + amount > batteryCapacity) {
            currentBattery = batteryCapacity;
        }
        else {
            currentBattery = currentBattery + amount;
        }
    }

    @Override
    public double getBatteryLevel() { return currentBattery; }

    @Override
    public double getBatteryCapacity() { return batteryCapacity; }

    @Override
    public double consumeBattery(double amount) throws InvalidOperationException, InsufficientResourceException {
        if (amount < 0) {
            throw new InvalidOperationException("The battery amount to consume cannot be less than 0");
        }
        if (amount > currentBattery) {
            throw new InsufficientResourceException("The total current battery level is less than the battery needed.");
        }
        currentBattery = currentBattery - amount;
        return amount;
    }

    @Override
    public boolean hasBatteryFor(double amount) {
        if (amount > currentBattery) {
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void move(double distance) throws InvalidOperationException, InsufficientResourceException {
        double batteryNeeded = distance * TRAVEL_CONSUMPTION;
        consumeBattery(batteryNeeded);
        super.move(distance);
    }

    @Override
    public boolean hasResourcesFor(Incident incident) {
        if (incident.getRequiredCapability().equals("SEARCH")) {
            double outbound = incident.getDistanceFromBase() * TRAVEL_CONSUMPTION;
            double search = incident.getWorkload();
            double returnTrip = incident.getDistanceFromBase() * TRAVEL_CONSUMPTION;
            double totalNeeded = outbound + search + returnTrip;
            return hasBatteryFor(totalNeeded);
        }
        return false;
    }

    @Override
    public double calculateDispatchScore(Incident incident, DispatchPolicy policy) {
        double base_score = super.calculateDispatchScore(incident, policy);

        if (currentBattery < policy.getLowBatteryThreshold() * batteryCapacity) {
            base_score = base_score + policy.getLowBatteryPenalty();
        }

        return base_score;
    }
}