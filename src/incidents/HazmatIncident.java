package incidents;

import exceptions.InvalidOperationException;

public class HazmatIncident extends Incident {

    private double contaminantSpread;
    private int populationAtRisk;

    public HazmatIncident(String id, String description, double distanceFromBase, int severity, double contaminantSpread, int populationAtRisk) throws InvalidOperationException {
        super(id, description, distanceFromBase, severity);

        if (contaminantSpread < 0) {
            throw new InvalidOperationException("Contaminant spread cannot be negative");
        }
        if (populationAtRisk < 0) {
            throw new InvalidOperationException("Population at risk cannot be negative");
        }

        this.contaminantSpread = contaminantSpread;
        this.populationAtRisk = populationAtRisk;
    }

    public double getContaminantSpread() { return contaminantSpread; }
    public int getPopulationAtRisk() { return populationAtRisk; }

    @Override
    public String getRequiredCapability() {
        return "HAZMAT";
    }

    @Override
    public double getPriorityWeight() {
        double weight = 8 * getSeverity();

        double populationComponent = populationAtRisk / 50.0;
        if (populationComponent > 10) {
            populationComponent = 10;
        }
        weight += populationComponent;

        return weight;
    }

    @Override
    public double getWorkload() {
        return 50 * getSeverity() + 0.5 * contaminantSpread;
    }
}