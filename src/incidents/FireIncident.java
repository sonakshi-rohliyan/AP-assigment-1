package incidents;
import exceptions.*;

public class FireIncident extends Incident{
    private double affectedArea;
    private boolean hazardousMaterial;

    public FireIncident(String id, String description, double distanceFromBase, int severity, double affectedArea, boolean hazardousMaterial) throws InvalidOperationException {
        super(id, description, distanceFromBase, severity);

        if (affectedArea < 0) {
            throw new InvalidOperationException("There must be some affected area");
        }

        this.affectedArea = affectedArea;
        this.hazardousMaterial = hazardousMaterial;
    }

    public double getAffectedArea(){return affectedArea;}
    public boolean getHazardousMaterial(){return hazardousMaterial;}

    @Override
    public double getPriorityWeight() {
        int severity = getSeverity();
        if(hazardousMaterial){
            return severity*7 + 15;
        }
        else{
            return severity*7;
        }
    }

    @Override
    public String getRequiredCapability(){
        return "FIRE";
    }

    @Override
    public double getWorkload(){
        int severity = getSeverity();
        return 300*severity + 0.2*affectedArea;
        // requires water
    }
}
