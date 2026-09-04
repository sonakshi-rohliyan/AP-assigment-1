package incidents;

import exceptions.*;

public class InfrastructureIncident extends Incident {

    private int affectedUsers;
    private boolean criticalService;

    public InfrastructureIncident(String id, String description, double distanceFromBase, int severity,
                                  int affectedUsers, boolean criticalService) throws InvalidOperationException {
        super(id, description, distanceFromBase, severity);

        if (affectedUsers < 0) {
            throw new InvalidOperationException("Affected users cannot be negative");
        }

        this.affectedUsers = affectedUsers;
        this.criticalService = criticalService;
    }

    public int getAffectedUsers() {return affectedUsers; }
    public boolean getCriticalService() {return criticalService; }



    @Override
    public double getPriorityWeight() {
        double weight = getSeverity() * 5;

        if (criticalService) {
            weight = weight + 10;
        }

        double extra = affectedUsers / 100.0;
        if (extra > 10) {
            extra = 10;
        }
        weight = weight + extra;

        return weight;
    }

    @Override
    public String getRequiredCapability() {
        return "REPAIR";
    }

    @Override
    public double getWorkload() {
        int severity = getSeverity();
        double curr = severity + (affectedUsers / 500.0);
        return Math.ceil(curr);
        // units of supply
    }
}