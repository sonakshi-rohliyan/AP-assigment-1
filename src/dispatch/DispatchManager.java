package dispatch;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;

import capabilities.*;
import exceptions.*;
import unit.*;
import incidents.*;

public class DispatchManager {
    private HashMap<String, ResponseUnit> units;
    private HashMap<String, Incident> incidents;
    private DispatchPolicy policy;

    public DispatchManager(DispatchPolicy policy) {
        this.units = new HashMap<>();
        this.incidents = new HashMap<>();
        this.policy = policy;
    }

    public int getUnitCount() { return units.size(); }
    public int getIncidentCount() { return incidents.size(); }

    public HashSet<String> getDistinctCapabilities() {
        HashSet<String> capabilities = new HashSet<>();
        for (ResponseUnit unit : units.values()) {
            capabilities.add(unit.getCapability());
        }
        return capabilities;
    }

    public synchronized void addUnit(ResponseUnit unit) throws DuplicateIdException, InvalidOperationException {
        if (unit == null){
            throw new InvalidOperationException("Unit can not be null");
        }
        if (units.containsKey(unit.getId())) {
            throw new DuplicateIdException("Duplicate unit ID " + unit.getId());
        }
        units.put(unit.getId(), unit);
    }

    public synchronized void addIncident(Incident incident) throws InvalidOperationException, DuplicateIdException {
        if (incident == null) {
            throw new InvalidOperationException("Incident cannot be null");
        }
        if (incidents.containsKey(incident.getId())) {
            throw new DuplicateIdException("Duplicate incident ID: " + incident.getId());
        }
        incidents.put(incident.getId(), incident);
    }

    public ResponseUnit findUnit(String id){
        return units.get(id);
    }

    public Incident findIncident(String id) {
        return incidents.get(id);
    }

    public void removeUnit(String id) throws InvalidOperationException{
        ResponseUnit unit = units.get(id);

        if (unit == null){
            throw new InvalidOperationException("There is no such id found.");
        }
        if (!unit.isAvailable()){
            throw new InvalidOperationException("A unit that is assigned can not be removed");
        }
        units.remove(id);
    }

    public void removeIncident(String id) throws InvalidOperationException {
        Incident incident = incidents.get(id);
        if (incident == null) {
            throw new InvalidOperationException("There is no such id found");
        }
        if (incident.getStatus().equals("ASSIGNED")) {
            throw new InvalidOperationException("An incident that is assigned can not be removed");
        }

        incidents.remove(id);
    }

    public synchronized ArrayList<ResponseUnit> getAllUnits() {
        return new ArrayList<>(units.values());
    }

    public synchronized ArrayList<Incident> getAllIncidents() {
        return new ArrayList<>(incidents.values());
    }

    public synchronized ArrayList<ResponseUnit> getUnitsSortedByDistance() {
        ArrayList<ResponseUnit> sorted = new ArrayList<>(units.values());
        Collections.sort(sorted, new UnitDistanceComparator());
        return sorted;
    }

    public synchronized ArrayList<Incident> getIncidentsSortedByPriority() {
        ArrayList<Incident> sorted = new ArrayList<>(incidents.values());
        Collections.sort(sorted, new IncidentPriorityComparator());
        return sorted;
    }

    public synchronized ArrayList<ResponseUnit> getAvailableUnits(){
        ArrayList<ResponseUnit> ans = new ArrayList<>();
        for (ResponseUnit unit : units.values()){
            if (unit.isAvailable()){
                ans.add(unit);
            }
        }
        return ans;
    }

    public synchronized ArrayList<Incident> getIncidentsByStatus(String status) {
        ArrayList<Incident> ans = new ArrayList<>();
        for (Incident incident : incidents.values()) {
            if (incident.getStatus().equals(status)) {
                ans.add(incident);
            }
        }
        return ans;
    }


    public synchronized ResponseUnit dispatchBestUnit(String incidentId) throws InvalidOperationException, NoSuitableUnitException, InsufficientResourceException{
        Incident incident = findIncident(incidentId);
        if (incident == null) {
            throw new InvalidOperationException("No such incident id exists ");
        }
        if (!incident.getStatus().equals("OPEN")){
            throw new InvalidOperationException("The incident must have an OPEN status.");
        }

        double minim = Double.MAX_VALUE;
        ResponseUnit best = null;

        for (ResponseUnit unit : units.values()){
            if (unit.isAvailable() && unit.canHandle(incident) && unit.hasResourcesFor(incident)) {
                double score = unit.calculateDispatchScore(incident, policy);
                if (score < minim) {
                    minim = score;
                    best = unit;
                } else if (score == minim && best != null) {
                    if (unit.getId().compareTo(best.getId()) < 0) {
                        best = unit;
                        minim = score;
                    }
                }
            }
        }
        if (best == null) {
            throw new NoSuitableUnitException("No suitable unit found for incident " + incidentId);
        }

        best.move(incident.getDistanceFromBase());
        best.assignIncident(incident.getId());
        incident.assigningUnit(best.getId());

        return best;
    }


    public synchronized void displayDispatchCandidates(String incidentId)throws InvalidOperationException, NoSuitableUnitException{
        Incident incident = findIncident(incidentId);
        if (incident == null) {
            throw new InvalidOperationException("No such incident id exists ");
        }
        if (!incident.getStatus().equals("OPEN")){
            throw new InvalidOperationException("The incident must have an OPEN status.");
        }

        System.out.println("Incident: " + incidentId);
        System.out.println("Description: " + incident.getDescription());
        System.out.println("Severity: " + incident.getSeverity());

        double minim = Double.MAX_VALUE;
        ResponseUnit best = null;

        for (ResponseUnit unit : units.values()){
            if (unit.isAvailable() && unit.canHandle(incident) && unit.hasResourcesFor(incident)) {
                double score = unit.calculateDispatchScore(incident, policy);
                if (score < minim) {
                    minim = score;
                    best = unit;
                } else if (score == minim && best != null) {
                    if (unit.getId().compareTo(best.getId()) < 0) {
                        best = unit;
                        minim = score;
                    }
                }
                System.out.println(unit.getId() + "    ELIGIBLE    Score = " + score);
            }
            else if(!unit.isAvailable()){
                System.out.println(unit.getId() + "    REJECTED    Currently assigned ");
            }
            else if(!unit.canHandle(incident)){
                System.out.println(unit.getId() + "    REJECTED    Capability mismatch ");
            }
            else if (unit instanceof GroundResponseUnit && !unit.hasExtraResource(incident)) {
                System.out.println(unit.getId() + "    REJECTED    Insufficient resources ");
            }
            else if (unit instanceof FuelPowered && !unit.hasResourcesFor(incident)){
                System.out.println(unit.getId() + "    REJECTED    Insufficient fuel ");
            }
            else if (unit instanceof BatteryPowered && !unit.hasResourcesFor(incident)){
                System.out.println(unit.getId() + "    REJECTED    Insufficient battery ");
            }
        }
        if (best == null){
            System.out.println("NO UNIT IS ELIGIBLE");
        }
        else{
            System.out.println("Best Candidate: " + best.getId());
        }
    }

    public synchronized void resolveIncident(String incidentId) throws InvalidOperationException, InsufficientResourceException {

        Incident incident = findIncident(incidentId);
        if (incident == null) {
            throw new InvalidOperationException("Incident not found: " + incidentId);
        }
        if (!incident.getStatus().equals("ASSIGNED")) {
            throw new InvalidOperationException("Incident must be ASSIGNED to resolve: " + incidentId);
        }

        ResponseUnit unit = findUnit(incident.getAssignedUnitId());
        if (unit == null) {
            throw new InvalidOperationException("Assigned unit not found for incident: " + incidentId);
        }

        unit.useResources(incident);
        unit.move(incident.getDistanceFromBase());

        unit.releaseIncident();
        unit.incrementIncident();
        incident.resolveIncident();
    }

    public synchronized void refuelAll(double amount) throws InvalidOperationException {
        for (ResponseUnit unit : units.values()) {
            if (unit instanceof FuelPowered) {
                ((FuelPowered) unit).refuel(amount);
            }
        }
    }

    public synchronized void rechargeAll(double amount) throws InvalidOperationException {
        for (ResponseUnit unit : units.values()) {
            if (unit instanceof BatteryPowered) {
                ((BatteryPowered) unit).recharge(amount);
            }
        }
    }

    public synchronized void refillAllWater(double amount) throws InvalidOperationException {
        for (ResponseUnit unit : units.values()) {
            if (unit instanceof WaterCarrier) {
                ((WaterCarrier) unit).refillWater(amount);
            }
        }
    }

    public synchronized void restockAllSupplies(double amount) throws InvalidOperationException {
        for (ResponseUnit unit : units.values()) {
            if (unit instanceof SupplyCarrier) {
                ((SupplyCarrier) unit).loadSupplies((int) amount);
            }
        }
    }

    public synchronized void restockAllContainment(double amount) throws InvalidOperationException {
        for (ResponseUnit unit : units.values()) {
            if (unit instanceof ContainmentCarrier) {
                ((ContainmentCarrier) unit).loadContainment((int) amount);
            }
        }
    }

    public synchronized String generateReport() {
        String report = "";
        report += "SYSTEM REPORT\n";
        report += "Total response units: " + units.size() + "\n";

        int count_available_responseUnit = 0;
        double total_dist_everyUnit = 0;
        int total_incidents_everyUnit = 0;
        int total_open_incident = 0;
        int total_resolved_incident = 0;
        int total_unresolved_severe = 0;
        ResponseUnit unit_w_greatest_completed = null;
        Incident curr_open_w_greated_priority = null;
        ResponseUnit unit_w_greatest_distance = null;

        IncidentPriorityComparator priorityCompare = new IncidentPriorityComparator();
        UnitDistanceComparator distanceCompare = new UnitDistanceComparator();

        for (Incident incident : incidents.values()) {
            if (incident.getStatus().equals("OPEN")) {
                total_open_incident++;
                if (curr_open_w_greated_priority == null || priorityCompare.compare(incident, curr_open_w_greated_priority) < 0) {
                    curr_open_w_greated_priority = incident;
                }
            }
            if (incident.getStatus().equals("RESOLVED")) {
                total_resolved_incident++;
            } else {
                if (incident.getSeverity() >= 4) {
                    total_unresolved_severe++;
                }
            }
        }

        for (ResponseUnit unit : units.values()) {
            if (unit_w_greatest_distance == null || distanceCompare.compare(unit, unit_w_greatest_distance) < 0){
                unit_w_greatest_distance = unit;
            }
            if (unit.isAvailable()) {
                count_available_responseUnit++;
            }
            total_dist_everyUnit += unit.getTotalDistanceTravelled();
            total_incidents_everyUnit += unit.getCompletedIncidents();
            if (unit_w_greatest_completed == null || unit.getCompletedIncidents() > unit_w_greatest_completed.getCompletedIncidents()) {
                unit_w_greatest_completed = unit;
            }
        }

        report += "Total available response units: " + count_available_responseUnit + "\n";
        report += "Total busy response units: " + (units.size() - count_available_responseUnit) + "\n";
        report += "Total distance travelled by all units: " + total_dist_everyUnit + " km\n";
        report += "Total incidents completed by all units: " + total_incidents_everyUnit + "\n";
        report += "Total incidents: " + incidents.size() + "\n";
        report += "Total open incidents: " + total_open_incident + "\n";
        report += "Total assigned incidents: " + (incidents.size() - total_open_incident - total_resolved_incident) + "\n";
        report += "Total resolved incidents: " + total_resolved_incident + "\n";
        report += "Total number of unresolved cases with severity-4 or severity-5: " + total_unresolved_severe + "\n";
        report += "Distinct response-unit capabilities: " + getDistinctCapabilities() + "\n";
        report += "The response unit with the greatest number of completed incidents: "
                + (unit_w_greatest_completed == null ? "None" : unit_w_greatest_completed.getId()) + "\n";
        report += "The response unit with the greatest distance travelled: " + (unit_w_greatest_distance == null ? "None"
                : unit_w_greatest_distance.getId() + " (" + unit_w_greatest_distance.getTotalDistanceTravelled() + " km)\n");
        report += "The currently OPEN incident having the greatest priority weight: "
                + (curr_open_w_greated_priority == null ? "None" : curr_open_w_greated_priority.getId()) + "\n";

        int unserviceable = 0;
        for (Incident incident : incidents.values()) {
            boolean isIT = true;
            if (incident.getStatus().equals("OPEN")) {
                for (ResponseUnit unit : units.values()) {
                    if (unit.isAvailable() && unit.canHandle(incident) && unit.hasResourcesFor(incident)) {
                        isIT = false;
                        break;
                    }
                }
                if (isIT){
                    unserviceable++;
                }
            }
        }

        report += "The number of currently unserviceable incidents: " + unserviceable;
        return report;
    }

    public synchronized void displayUnserviceableIncidents() {
        boolean foundAny = false;

        System.out.println("Currently Unserviceable Incidents");

        for (Incident incident : incidents.values()) {
            if (!incident.getStatus().equals("OPEN")) {
                continue;
            }
            boolean isService = false;
            boolean hasResource = false;

            for (ResponseUnit unit : units.values()) {
                if (unit.isAvailable() && unit.canHandle(incident) && unit.hasResourcesFor(incident)) {
                    isService = true;
                    break;
                }
                if (unit.canHandle(incident) && unit.hasResourcesFor(incident)) {
                    hasResource = true;
                }
            }

            if (!isService) {
                foundAny = true;
                if (hasResource) {
                    System.out.println(incident.getId() + "  " + incident.getRequiredCapability() + "  No suitable available unit");
                } else {
                    System.out.println(incident.getId() + "  " + incident.getRequiredCapability() + "  Insufficient resources");
                }
            }
        }
        if (!foundAny) {
            System.out.println("All open incidents are currently serviceable.");
        }
    }


    public synchronized void saveState(String prefix) throws InvalidOperationException {
        try {
            PrintWriter unitWriter = new PrintWriter(new FileWriter(prefix + "_units.csv"));

            for (ResponseUnit unit : units.values()) {
                String unitType = "UNKNOWN";
                String trafficFactor = "0";
                if (unit instanceof GroundResponseUnit) {
                    trafficFactor = String.valueOf(((GroundResponseUnit) unit).getTrafficFactor());
                }

                String fuel = "0";
                if (unit instanceof FuelPowered) {
                    fuel = String.valueOf(((FuelPowered) unit).getFuelLevel());
                }
                String battery = "0";
                if (unit instanceof BatteryPowered) {
                    unitType = "SEARCH_DRONE";
                    battery = String.valueOf(((BatteryPowered) unit).getBatteryLevel());
                }
                String water = "0";
                if (unit instanceof WaterCarrier) {
                    unitType = "FIRE_ENGINE";
                    water = String.valueOf(((WaterCarrier) unit).getWaterLevel());
                }
                String supplies = "0";
                if (unit instanceof SupplyCarrier) {
                    unitType = "REPAIR_VAN";
                    supplies = String.valueOf(((SupplyCarrier) unit).getSupplyLevel());
                }
                String containment = "0";
                if (unit instanceof ContainmentCarrier) {
                    unitType = "HAZMAT_UNIT";
                    containment = String.valueOf(((ContainmentCarrier) unit).getContainmentLevel());
                }
                String currentPatients = "0";
                if (unit instanceof PatientCarrier) {
                    unitType = "AMBULANCE";
                    currentPatients = String.valueOf(((PatientCarrier) unit).getCurrentPatients());
                }

                String line = unitType + "," + unit.getId() + "," + unit.getName() + "," + unit.getMaxSpeed() + ","
                        + trafficFactor + "," + unit.getTotalDistanceTravelled() + "," + unit.isAvailable() + ","
                        + (unit.getAssignedIncidentId() == null ? "NONE" : unit.getAssignedIncidentId()) + ","
                        + unit.getCompletedIncidents() + "," + fuel + "," + battery + "," + water + ","
                        + supplies + "," + containment + "," + currentPatients;

                unitWriter.println(line);
            }
            unitWriter.close();

            PrintWriter incidentWriter = new PrintWriter(new FileWriter(prefix + "_incidents.csv"));

            for (Incident incident : incidents.values()) {

                String extra = "";
                String incidentType = "UNKNOWN";

                if (incident instanceof MedicalIncident) {
                    incidentType = "MEDICAL";
                    MedicalIncident m = (MedicalIncident) incident;
                    extra = m.getPatientCount() + "," + m.getCriticalPatients();
                }
                else if (incident instanceof FireIncident) {
                    incidentType = "FIRE";
                    FireIncident f = (FireIncident) incident;
                    extra = f.getAffectedArea() + "," + f.getHazardousMaterial();
                }
                else if (incident instanceof InfrastructureIncident) {
                    incidentType = "INFRASTRUCTURE";
                    InfrastructureIncident inf = (InfrastructureIncident) incident;
                    extra = inf.getAffectedUsers() + "," + inf.getCriticalService();
                }
                else if (incident instanceof SearchIncident) {
                    incidentType = "SEARCH";
                    SearchIncident s = (SearchIncident) incident;
                    extra = s.getMissingPersons() + "," + s.getSearchArea();
                }
                else if (incident instanceof HazmatIncident) {
                    incidentType = "HAZMAT";
                    HazmatIncident h = (HazmatIncident) incident;
                    extra = h.getContaminantSpread() + "," + h.getPopulationAtRisk();
                }

                String line = incidentType + "," + incident.getId() + "," + incident.getDescription() + ","
                        + incident.getDistanceFromBase() + "," + incident.getSeverity() + "," + incident.getStatus() + ","
                        + (incident.getAssignedUnitId() == null ? "NONE" : incident.getAssignedUnitId()) + "," + extra;

                incidentWriter.println(line);
            }
            incidentWriter.close();

        } catch (IOException e) {
            throw new InvalidOperationException("Failed to save state: " + e.getMessage());
        }
    }


    public synchronized void loadState(String prefix) throws InvalidOperationException {
        HashMap<String, ResponseUnit> newUnits = new HashMap<>();
        HashMap<String, Incident> newIncidents = new HashMap<>();

        try {
            BufferedReader unitReader = new BufferedReader(new FileReader(prefix + "_units.csv"));
            String line;
            while ((line = unitReader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    String[] p = line.split(",");

                    String type = p[0];
                    String id = p[1];
                    String name = p[2];
                    double maxSpeed = Double.parseDouble(p[3]);
                    double trafficFactor = Double.parseDouble(p[4]);
                    double totalDistance = Double.parseDouble(p[5]);
                    boolean available = Boolean.parseBoolean(p[6]);
                    String assignedIncidentId = p[7].equals("NONE") ? null : p[7];
                    int completed = Integer.parseInt(p[8]);
                    double fuel = Double.parseDouble(p[9]);
                    double battery = Double.parseDouble(p[10]);
                    double water = Double.parseDouble(p[11]);
                    int supplies = Integer.parseInt(p[12]);
                    double containment = Double.parseDouble(p[13]);
                    int patients = Integer.parseInt(p[14]);

                    ResponseUnit unit = null;

                    if (type.equals("AMBULANCE")) {
                        Ambulance a = new Ambulance(id, name, maxSpeed, trafficFactor);
                        a.setFuelLevel(fuel);
                        a.boardPatients(patients);
                        unit = a;
                    }
                    else if (type.equals("FIRE_ENGINE")) {
                        FireEngine f = new FireEngine(id, name, maxSpeed, trafficFactor);
                        f.setFuelLevel(fuel);
                        f.setWaterLevel(water);
                        unit = f;
                    }
                    else if (type.equals("REPAIR_VAN")) {
                        RepairVan r = new RepairVan(id, name, maxSpeed, trafficFactor);
                        r.setFuelLevel(fuel);
                        r.setSupplyLevel(supplies);
                        unit = r;
                    }
                    else if (type.equals("SEARCH_DRONE")) {
                        SearchDrone d = new SearchDrone(id, name, maxSpeed);
                        d.setBatteryLevel(battery);
                        unit = d;
                    }
                    else if (type.equals("HAZMAT_UNIT")) {
                        HazmatUnit h = new HazmatUnit(id, name, maxSpeed, trafficFactor);
                        h.setFuelLevel(fuel);
                        h.setContainmentLevel(containment);
                        unit = h;
                    }
                    else {
                        System.out.println("Skipping unknown type " + type);
                        continue;
                    }

                    // restoreState is an extra method added in response class as an alternative to set methods
                    unit.restoreState(totalDistance, available, assignedIncidentId, completed);

                    if (newUnits.containsKey(id)) {
                        System.out.println("Duplicate unit ID, skipping: " + id);
                        continue;
                    }

                    newUnits.put(id, unit);

                } catch (Exception e) {
                    System.out.println("Skipping unknown unit record: " + line);
                }
            }
            unitReader.close();

            BufferedReader incidentReader = new BufferedReader(new FileReader(prefix + "_incidents.csv"));
            while ((line = incidentReader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    String[] p = line.split(",");

                    String type = p[0];
                    String id = p[1];
                    String description = p[2];
                    double distance = Double.parseDouble(p[3]);
                    int severity = Integer.parseInt(p[4]);
                    String status = p[5];
                    String assignedUnitId = p[6].equals("NONE") ? null : p[6];

                    Incident incident = null;

                    if (type.equals("MEDICAL")) {
                        incident = new MedicalIncident(id, description, distance, severity, Integer.parseInt(p[7]), Integer.parseInt(p[8]));
                    }
                    else if (type.equals("FIRE")) {
                        incident = new FireIncident(id, description, distance, severity, Double.parseDouble(p[7]), Boolean.parseBoolean(p[8]));
                    }
                    else if (type.equals("INFRASTRUCTURE")) {
                        incident = new InfrastructureIncident(id, description, distance, severity, Integer.parseInt(p[7]), Boolean.parseBoolean(p[8]));
                    }
                    else if (type.equals("SEARCH")) {
                        incident = new SearchIncident(id, description, distance, severity, Integer.parseInt(p[7]), Double.parseDouble(p[8]));
                    }
                    else if (type.equals("HAZMAT")) {
                        incident = new HazmatIncident(id, description, distance, severity, Double.parseDouble(p[7]), Integer.parseInt(p[8]));
                    }
                    else {
                        System.out.println("Skipping unknown incident record type  " + type);
                        continue;
                    }

                    incident.restoreState(status, assignedUnitId);
                    if (newIncidents.containsKey(id)) {
                        System.out.println("Duplicate incident ID, skipping: " + id);
                        continue;
                    }
                    newIncidents.put(id, incident);

                } catch (Exception e) {
                    System.out.println("Skipping unknown incident record: " + line);
                }
            }
            incidentReader.close();
            units.clear();
            units.putAll(newUnits);
            incidents.clear();
            incidents.putAll(newIncidents);

        } catch (IOException e) {
            throw new InvalidOperationException("Failed to load state: " + e.getMessage());
        }
    }

}
