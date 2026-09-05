package incidents;

import exceptions.*;

public class Incident {
    private String id;
    private String description;
    private double distanceFromBase;
    private int severity;
    private String status;
    private String assignedUnitId;

    public Incident(String id, String description, double distanceFromBase, int severity) throws InvalidOperationException{
        if (severity <= 0 || severity > 5){
            throw new InvalidOperationException("Severity must be an integer between 1 and 5");
        }
        this.id = id;
        this.description = description;
        this.distanceFromBase = distanceFromBase;
        this.severity = severity;
        this.status = "OPEN";
        this.assignedUnitId = null;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public double getDistanceFromBase() { return distanceFromBase; }
    public int getSeverity() { return severity; }
    public String getStatus() { return status; }
    public String getAssignedUnitId() { return assignedUnitId; }

    public double getPriorityWeight() {
        return severity * 5;
    }
    public String getRequiredCapability(){
        return "GENERAL";
    }

    public double getWorkload(){
        return severity;
    }
    public void resolveIncident() throws InvalidOperationException{
        if (!status.equals("ASSIGNED")){
            throw new InvalidOperationException("You can not RESOLVE a incident that is not ASSIGNED.");
        }
        this.status = "RESOLVED";
    }
    public void assigningUnit(String unitId) throws InvalidOperationException{
        if (status.equals("RESOLVED")){
            throw new InvalidOperationException("An incident with RESOLVED status can not be assigned.");
        }
        this.assignedUnitId = unitId;
        this.status = "ASSIGNED";
    }
    public void displayIncident(){
        System.out.println("Incident ID: " + id);
        System.out.println("Description: " + description);
        System.out.println("Distance from base: " + distanceFromBase);
        System.out.println("Severity: " + severity);
        System.out.println("Status: " + status);
        System.out.println("Required capability: " + getRequiredCapability());
        System.out.println("Priority weight: " + getPriorityWeight());
        System.out.println("Workload: " + getWorkload());

    }
}
