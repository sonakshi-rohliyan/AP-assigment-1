package incidents;

import exceptions.*;

public class SearchIncident extends Incident {

    private int missingPersons;
    private double searchArea;

    public SearchIncident(String id, String description, double distanceFromBase, int severity,
                          int missingPersons, double searchArea) throws InvalidOperationException {
        super(id, description, distanceFromBase, severity);

        if (missingPersons < 0) {
            throw new InvalidOperationException("There must be some missing people");
        }

        this.missingPersons = missingPersons;
        this.searchArea = searchArea;
    }

    public int getMissingPersons() {return missingPersons; }
    public double getSearchArea() {return searchArea; }

    @Override
    public double getPriorityWeight() {
        int severity = getSeverity();
        return 4 * severity + 2 * missingPersons;
    }

    @Override
    public String getRequiredCapability() {
        return "SEARCH";
    }

    @Override
    public double getWorkload() {
        return 1.5 * searchArea;
    }
}