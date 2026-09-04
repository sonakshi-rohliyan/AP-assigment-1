package incidents;

import exceptions.*;

public class MedicalIncident extends Incident{
    private int patientCount;
    private int criticalPatients;

    public MedicalIncident(String id, String description, double distanceFromBase, int severity, int patientCount, int criticalPatients) throws InvalidOperationException {
        super(id, description, distanceFromBase, severity);

        if (criticalPatients > patientCount) {
            throw new InvalidOperationException("Critical patients cannot exceed total patient count");
        }

        this.patientCount = patientCount;
        this.criticalPatients = criticalPatients;
    }

    public int getPatientCount(){return patientCount;}
    public int getCriticalPatients(){return criticalPatients;}

    @Override
    public double getPriorityWeight() {
        int severity = getSeverity();
        return (severity * 6) + (3*criticalPatients);
    }

    @Override
    public String getRequiredCapability(){
        return "MEDICAL";
    }

    @Override
    public double getWorkload(){
        return patientCount;
    }

}
