package dispatch;

import java.io.*;

import capabilities.*;
import exceptions.*;
import unit.*;
import incidents.*;
import capabilities.*;

public class DispatchManager {
    private ResponseUnit[] units;
    private Incident[] incidents;
    private int unitCount;
    private int incidentCount;
    private DispatchPolicy policy;

    public DispatchManager(int maxUnits, int maxIncidents, DispatchPolicy policy) {
        this.units = new ResponseUnit[maxUnits];
        this.incidents = new Incident[maxIncidents];
        this.unitCount = 0;
        this.incidentCount = 0;
        this.policy = policy;
    }

    public int getUnitCount() { return unitCount; }
    public int getIncidentCount() { return incidentCount; }
    public ResponseUnit getUnitAt(int index) { return units[index]; }
    public Incident getIncidentAt(int index) { return incidents[index]; }


    public void addUnit(ResponseUnit unit) throws DuplicateIdException, InvalidOperationException {
        if (unit == null){
            throw new InvalidOperationException("Unit can not be null");
        }
        if (findUnit(unit.getId()) != null) {
            throw new DuplicateIdException("Duplicate unit ID " + unit.getId());
        }
        if (unitCount >= units.length) {
            throw new InvalidOperationException("No space remaining to add more units");
        }
        units[unitCount] = unit;
        unitCount++;
    }

    public void addIncident(Incident incident) throws InvalidOperationException, DuplicateIdException {
        if (incident == null) {
            throw new InvalidOperationException("Incident cannot be null");
        }
        if (incidentCount >= incidents.length) {
            throw new InvalidOperationException("No space remaining to add more incidents");
        }
        if (findIncident(incident.getId()) != null) {
            throw new DuplicateIdException("Duplicate incident ID: " + incident.getId());
        }
        incidents[incidentCount] = incident;
        incidentCount++;
    }

    public ResponseUnit findUnit(String id){
        for (int i = 0; i < unitCount; i++){
            if (units[i].getId().equals(id)){
                return units[i];
            }
        }
        return null;
    }

    public Incident findIncident(String id) {
        for (int i = 0; i < incidentCount; i++) {
            if (incidents[i].getId().equals(id)) {
                return incidents[i];
            }
        }
        return null;
    }

    public void removeUnit(String id) throws InvalidOperationException{
        int index = -1;
        for (int i = 0; i < unitCount; i++){
            if (units[i].getId().equals(id)){
                index = i;
                break;
            }
        }
        if (index == -1){
            throw new InvalidOperationException("There is no such id found.");
        }
        if (!units[index].isAvailable()){
            throw new InvalidOperationException("A unit that is assigned can not be removed");
        }
        for (int i = index; i< unitCount -1; i++){
            units[i] = units[i+1];
        }
        unitCount--;
    }

    public void removeIncident(String id) throws InvalidOperationException {
        int index = -1;
        for (int i = 0; i < incidentCount; i++) {
            if (incidents[i].getId().equals(id)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new InvalidOperationException("There is no such id found");
        }
        if (incidents[index].getStatus().equals("ASSIGNED")) {
            throw new InvalidOperationException("An incident that is assigned can not be removed");
        }

        for (int i = index; i < incidentCount - 1; i++) {
            incidents[i] = incidents[i + 1];
        }
        incidentCount--;
    }

    public ResponseUnit dispatchBestUnit(String incidentId) throws InvalidOperationException, NoSuitableUnitException, InsufficientResourceException{
        Incident incident = findIncident(incidentId);
        if (incident == null) {
            throw new InvalidOperationException("No such incident id exists ");
        }
        if (!incident.getStatus().equals("OPEN")){
            throw new InvalidOperationException("The incident must have an OPEN status.");
        }

        double minim = Double.MAX_VALUE;
        ResponseUnit best = null;

        for (int i = 0; i <unitCount; i++){
            ResponseUnit unit = units[i];
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

        best.assignIncident(incident.getId());
        incident.assigningUnit(best.getId());
        best.move(incident.getDistanceFromBase());

        return best;
    }

    public void displayDispatchCandidates(String incidentId)throws InvalidOperationException, NoSuitableUnitException{
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

        for (int i = 0; i <unitCount; i++){
            ResponseUnit unit = units[i];
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

    public void resolveIncident(String incidentId) throws InvalidOperationException, InsufficientResourceException {

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

    public void refuelAll(double amount)throws InvalidOperationException{
        for (int i = 0; i < unitCount; i++ ){
            if (units[i] instanceof FuelPowered){
                ((FuelPowered) units[i]).refuel(amount);
            }
        }
    }

    public void rechargeAll(double amount)throws InvalidOperationException{
        for (int i = 0; i < unitCount; i++ ){
            if (units[i] instanceof BatteryPowered){
                ((BatteryPowered) units[i]).recharge(amount);
            }
        }
    }

    public void refillAllWater(double amount)throws InvalidOperationException{
        for (int i = 0; i < unitCount; i++ ){
            if (units[i] instanceof WaterCarrier){
                ((WaterCarrier) units[i]).refillWater(amount);
            }
        }
    }

    public void restockAllSupplies(double amount)throws InvalidOperationException{
        for (int i = 0; i < unitCount; i++ ){
            if (units[i] instanceof SupplyCarrier){
                ((SupplyCarrier) units[i]).loadSupplies((int) amount);
            }
        }
    }

    public void restockAllContainment(double amount) throws  InvalidOperationException{
        for (int i = 0; i < unitCount; i++ ){
            if (units[i] instanceof ContainmentCarrier){
                ((ContainmentCarrier) units[i]).loadContainment((int) amount);
            }
        }
    }

    public String generateReport() {
        String report = "";
        report += "SYSTEM REPORT\n";
        report += "Total response units: " + unitCount + "\n";

        int count_available_responseUnit = 0;
        double total_dist_everyUnit = 0;
        int total_incidents_everyUnit = 0;
        int total_open_incident = 0;
        int total_resolved_incident = 0;
        int total_unresolved_severe = 0;
        ResponseUnit unit_w_greatest_completed = null;
        Incident curr_open_w_greated_priority = null;

        for (int i = 0; i < incidentCount; i++) {
            Incident incident = incidents[i];
            if (incident.getStatus().equals("OPEN")) {
                total_open_incident++;
                if (curr_open_w_greated_priority == null || incident.getPriorityWeight() > curr_open_w_greated_priority.getPriorityWeight()) {
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

        for (int i = 0; i < unitCount; i++) {
            ResponseUnit unit = units[i];
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
        report += "Total busy response units: " + (unitCount - count_available_responseUnit) + "\n";
        report += "Total distance travelled by all units: " + total_dist_everyUnit + " km\n";
        report += "Total incidents completed by all units: " + total_incidents_everyUnit + "\n";
        report += "Total open incidents: " + total_open_incident + "\n";
        report += "Total assigned incidents: " + (incidentCount - total_open_incident - total_resolved_incident) + "\n";
        report += "Total resolved incidents: " + total_resolved_incident + "\n";
        report += "Total number of unresolved cases with severity-4 or severity-5: " + total_unresolved_severe + "\n";
        report += "The response unit with the greatest number of completed incidents: "
                + (unit_w_greatest_completed == null ? "None" : unit_w_greatest_completed.getId()) + "\n";
        report += "The currently OPEN incident having the greatest priority weight: "
                + (curr_open_w_greated_priority == null ? "None" : curr_open_w_greated_priority.getId()) + "\n";

        int unserviceable = 0;
        for (int i = 0; i < incidentCount; i++) {
            boolean isIT = true;
            Incident incident = incidents[i];
            if (incident.getStatus().equals("OPEN")) {
                for (int j = 0; j < unitCount; j++) {
                    if (units[j].isAvailable() && units[j].canHandle(incident) && units[j].hasResourcesFor(incident)) {
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

    public void displayUnserviceableIncidents() {
        boolean foundAny = false;

        System.out.println("Currently Unserviceable Incidents");

        for (int i = 0; i < incidentCount; i++) {
            Incident incident = incidents[i];
            boolean isService = false;
            boolean hasResource = false;

            if (incident.getStatus().equals("OPEN")) {
                for (int j = 0; j < unitCount; j++) {
                    if (units[j].isAvailable() && units[j].canHandle(incident) && units[j].hasResourcesFor(incident)) {
                        isService = true;
                        break;
                    }
                    if (units[j].canHandle(incident) && units[j].hasResourcesFor(incident)) {
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
        }

        if (!foundAny) {
            System.out.println("All open incidents are currently serviceable.");
        }
    }

    public void saveState(String prefix) throws InvalidOperationException {
        try {
            PrintWriter unitWriter = new PrintWriter(new FileWriter(prefix + "_units.csv"));

            for (int i = 0; i < unitCount; i++) {
                ResponseUnit unit = units[i];
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

            for (int i = 0; i < incidentCount; i++) {
                Incident incident = incidents[i];

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


    public void loadState(String prefix) throws InvalidOperationException {
        this.unitCount = 0;
        this.incidentCount = 0;
        for (int i = 0; i < units.length; i++) units[i] = null;
        for (int i = 0; i < incidents.length; i++) incidents[i] = null;

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

                    ResponseUnit unit = null;

                    if (type.equals("AMBULANCE")) {
                        Ambulance a = new Ambulance(id, name, maxSpeed, trafficFactor);
                        a.setFuelLevel(fuel);
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
                    units[unitCount] = unit;
                    unitCount++;

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
                    incidents[incidentCount] = incident;
                    incidentCount++;

                } catch (Exception e) {
                    System.out.println("Skipping unknown incident record: " + line);
                }
            }
            incidentReader.close();

        } catch (IOException e) {
            throw new InvalidOperationException("Failed to load state: " + e.getMessage());
        }
    }

}
