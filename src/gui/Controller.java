package gui;

import dispatch.DispatchManager;
import incidents.*;
import unit.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class Controller {
    private final DispatchManager manager;

    public Controller(DispatchManager manager){
        this.manager = manager;
    }

    public String addUnit(String type, String id, String name, double speed, double trafficFactor) throws Exception{
        ResponseUnit unit;
        switch (type){
            case "AMBULANCE": unit = new Ambulance(id, name, speed, trafficFactor); break;
            case "FIRE_ENGINE":  unit = new FireEngine(id, name, speed, trafficFactor); break;
            case "REPAIR_VAN":   unit = new RepairVan(id, name, speed, trafficFactor); break;
            case "SEARCH_DRONE": unit = new SearchDrone(id, name, speed); break;
            case "HAZMAT_UNIT":  unit = new HazmatUnit(id, name, speed, trafficFactor); break;
            default: throw new IllegalArgumentException("Unkown unit type");
        }
        manager.addUnit(unit);
        return type + " unit with id " + id+" added.";
    }

    public String addIncident(String type, String id, String description, double distance,
                              int severity, double field1, double field2, boolean flag) throws Exception{
        Incident incident;
        switch (type) {
            case "MEDICAL":
                incident = new MedicalIncident(id, description, distance, severity,
                        wholeNumber(field1, "Patient count"), wholeNumber(field2, "Critical patients"));
                break;
            case "FIRE":
                incident = new FireIncident(id, description, distance, severity, field1, flag);
                break;
            case "INFRASTRUCTURE":
                incident = new InfrastructureIncident(id, description, distance, severity,
                        wholeNumber(field1, "Affected users"), flag);
                break;
            case "SEARCH":
                incident = new SearchIncident(id, description, distance, severity,
                        wholeNumber(field1, "Missing persons"), field2);
                break;
            case "HAZMAT":
                incident = new HazmatIncident(id, description, distance, severity,
                        field1, wholeNumber(field2, "Population at risk"));
                break;
            default:
                throw new IllegalArgumentException("Unknown incident type" );
        }
        manager.addIncident(incident);
        return "Incident " + id + " (" + type + ") reported.";
    }
    private int wholeNumber(double value, String name) {
        if (value != Math.rint(value)) {
            throw new IllegalArgumentException(name + " must be a whole number.");
        }
        return (int) value;
    }

     public String listAllUnits() {
        return formatUnits("ALL RESPONSE UNITS", manager.getAllUnits());
    }

    public String listAvailableUnits() {
        return formatUnits("AVAILABLE RESPONSE UNITS", manager.getAvailableUnits());
    }

    public String listAllIncidents() {
        return formatIncidents("ALL INCIDENTS", manager.getAllIncidents());
    }

    public String listIncidentsByStatus(String status) {
        return formatIncidents(status + " INCIDENTS", manager.getIncidentsByStatus(status));
    }

    private String formatUnits(String heading, ArrayList<ResponseUnit> units) {
        if (units.isEmpty()) {
            return heading + ": none\n";
        }
        return heading + " (" + units.size() + ")\n\n" + capture(() -> {
            for (ResponseUnit unit : units) {
                unit.display();          // polymorphic: each subtype shows its own details
                System.out.println();
            }
        });
    }

    private String formatIncidents(String heading, ArrayList<Incident> incidents) {
        if (incidents.isEmpty()) {
            return heading + ": none\n";
        }
        return heading + " (" + incidents.size() + ")\n\n" + capture(() -> {
            for (Incident incident : incidents) {
                incident.displayIncident();
                System.out.println();
            }
        });
    }

    private String capture(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            action.run();
        } finally {
            System.out.flush();
            System.setOut(original);     // always restore the console
        }
        return buffer.toString();
    }
    private interface ModelAction {
        void run() throws Exception;
    }

    private String captureOutput(ModelAction action) throws Exception{
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            action.run();
        }
        finally {
            System.out.flush();
            System.setOut(original);
        }
        return buffer.toString();
    }


    public String previewDispatch(String incidentID) throws Exception{
        return captureOutput(()->manager.displayDispatchCandidates(incidentID));
    }

    public String dispatch(String incidentId) throws Exception {
        ResponseUnit[] chosen = new ResponseUnit[1];
        String printed = captureOutput(() -> chosen[0] = manager.dispatchBestUnit(incidentId));
        return printed + "Dispatched " + chosen[0].getId() + " (" + chosen[0].getName()
                + ") to incident " + incidentId + ".\n";
    }

    public String resolve(String incidentId) throws Exception {
        String printed = captureOutput(() -> manager.resolveIncident(incidentId));
        return printed + "Incident " + incidentId + " resolved.\n";
    }

    public String stateSummary() {
        StringBuilder text = new StringBuilder("--- Current state ---\n");
        for (Incident incident : manager.getAllIncidents()) {
            text.append("Incident ").append(incident.getId())
                    .append(": ").append(incident.getStatus()).append("\n");
        }
        for (ResponseUnit unit : manager.getAllUnits()) {
            text.append("Unit ").append(unit.getId())
                    .append(": ").append(unit.isAvailable() ? "AVAILABLE" : "BUSY").append("\n");
        }
        return text.toString();
    }

    public String report() {
        return manager.generateReport();
    }

    public String save(String prefix) throws Exception {
        String printed = captureOutput(() -> manager.saveState(prefix));
        return printed + "State saved to " + prefix + "_units.csv and " + prefix + "_incidents.csv.\n";
    }

    public String load(String prefix) throws Exception {
        String printed = captureOutput(() -> manager.loadState(prefix));
        return printed + "State loaded from prefix \"" + prefix + "\".\n";
    }
}
