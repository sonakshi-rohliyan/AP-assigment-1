package unit;
import exceptions.*;
import incidents.Incident;
import dispatch.DispatchPolicy;

public class ResponseUnit {
    private String id;
    private String name;
    private double maxSpeed;
    private double totalDistanceTravelled;
    private boolean available;
    private String assignedIncidentId;
    private int completedIncidents;

    public ResponseUnit(String id, String name, double maxSpeed) throws InvalidOperationException{
        if (id == null || id.isEmpty()){
            throw new InvalidOperationException("ID can not be null or empty");
        }
        if (name == null || name.isEmpty()){
            throw new InvalidOperationException("Name can not be null or empty");
        }
        if (maxSpeed <= 0){
            throw new InvalidOperationException("Max speed has to be more than");
        }

        this.id = id;
        this.name = name;
        this.maxSpeed = maxSpeed;
        this.totalDistanceTravelled = 0;
        this.available = true;
        this.assignedIncidentId = null;
        this.completedIncidents = 0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getMaxSpeed() { return maxSpeed; }
    public double getTotalDistanceTravelled() { return totalDistanceTravelled; }
    public boolean isAvailable() { return available; }
    public String getAssignedIncidentId() { return assignedIncidentId; }
    public int getCompletedIncidents() { return completedIncidents; }

    public void move(double distance) throws InvalidOperationException{
        if (distance < 0){
            throw new InvalidOperationException("Distance can't be negative");
        }
        totalDistanceTravelled = distance + totalDistanceTravelled;

        System.out.println("Unit" + id + "travelled" + distance);
    }

    public double estimateArrivalTime(double distance){
        return distance/maxSpeed;
    }

    public String getCapability(){
        return "GENERAL";
    }

    public boolean canHandle(Incident incident){
        String capability1 = getCapability();
        if (capability1.equals("GENERAL")){
            return true;
        }
        String capability2 = incident.getRequiredCapability();

        if (capability1.equals(capability2)){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean hasResourcesFor(Incident incident){
        return true;
    }

    public double calculateDispatchScore(Incident incident, DispatchPolicy policy){
        double priority_weight = incident.getPriorityWeight();
        double time = estimateArrivalTime(incident.getDistanceFromBase());
        return time*60 - priority_weight;
    }

    public void assignIncident(String id){
        this.assignedIncidentId = id;
        this.available = false;
    }

    public void releaseIncident(){
        this.assignedIncidentId = null;
        this.available = true;
    }

    public void incrimentIncident(){
        completedIncidents++;
    }

    public void display() {
        System.out.println("Unit ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Max speed: " + maxSpeed);
        System.out.println("Capability: " + getCapability());
        System.out.println("Total distance travelled: " + totalDistanceTravelled);
        System.out.println("Available: " + available);
        System.out.println("Assigned incident: " + (assignedIncidentId == null ? "None" : assignedIncidentId));
        System.out.println("Completed incidents: " + completedIncidents);
    }
}
