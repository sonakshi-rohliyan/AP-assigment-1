package unit;

import capabilities.FuelPowered;
import capabilities.PatientCarrier;
import exceptions.*;
import incidents.Incident;
import dispatch.DispatchPolicy;

public class Ambulance extends GroundResponseUnit implements FuelPowered, PatientCarrier {
    private double fuelCapacity;
    private double currentFuel;
    private static final double FUEL_EFFICIENCY = 12.0;
    private int patientCapacity;
    private int currentPatients;

    public Ambulance(String id, String name, double maxSpeed, double trafficFactor) throws InvalidOperationException {
        super(id, name, maxSpeed, trafficFactor);
        this.fuelCapacity = 80.0;
        this.currentFuel = 80.0;
        this.patientCapacity = 4;
        this.currentPatients = 0;
    }

    // overiding ground response unit method
    @Override
    public String getCapability(){
        return "MEDICAL";
    }

    // overriding fuelpowered interference methods
    @Override
    public void refuel(double amount) throws InvalidOperationException{
        if (amount < 0){
            throw new InvalidOperationException("The amount to refuel can not be less than 0");
        }
        if (currentFuel + amount > fuelCapacity){
            currentFuel = fuelCapacity;
        }
        else {
            currentFuel = currentFuel + amount;
        }
    }

    @Override
    public double getFuelLevel(){
        return currentFuel;
    }

    @Override
    public double getFuelCapacity(){
        return fuelCapacity;
    }

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

    // overriding patient carrier interference methods

    @Override
    public void boardPatients(int number) throws InsufficientResourceException{
        if (number + currentPatients > patientCapacity){
            throw new InsufficientResourceException("There isnt enough space for these many patients");
        }
        currentPatients = currentPatients + number;
    }

    @Override
    public void releasePatients(int number) throws InvalidOperationException{
        if (number > currentPatients){
            throw new InvalidOperationException("We can't release more patients than the total number of patients");
        }
        currentPatients = currentPatients - number;
    }

    @Override
    public int getPatientCapacity(){
        return currentPatients;
    }

    @Override
    public int getCurrentPatients(){
        return currentPatients;
    }

    @Override
    public void move(double distance) throws InvalidOperationException, InsufficientResourceException{
        consumeFuel(distance);
        super.move(distance);
    }

    @Override
    public boolean hasResourcesFor(Incident incident){
        if (incident.getRequiredCapability().equals("MEDICAL")){
            if (patientCapacity-currentPatients < incident.getWorkload()){
                return false;
            }
            return hasFuelFor(incident.getDistanceFromBase()*2);
        }
        return false;
    }

    @Override
    public double calculateDispatchScore(Incident incident, DispatchPolicy policy){
        double base_score = super.calculateDispatchScore(incident,policy);
        base_score = base_score + 3.0*currentPatients;
        if (currentFuel < fuelCapacity*policy.getLowFuelThreshold()){
            base_score = base_score + policy.getLowFuelPenalty();
        }
        return base_score;
    }
}
