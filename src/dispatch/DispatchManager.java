package dispatch;

import capabilities.*;
import exceptions.*;
import incidents.Incident;
import unit.ResponseUnit;

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
            else if (!unit.hasResourcesFor(incident)) {
                System.out.println(unit.getId() + "    REJECTED    Insufficient resources ");
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
        report += "The response unit with the greatest number of completed incidents: " + unit_w_greatest_completed.getId() + "\n";
        report += "The currently OPEN incident having the greatest priority weight: " + curr_open_w_greated_priority.getId() + "\n";

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

        report += "The number of currently unserviceable incidents: " +unserviceable;
        return report
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
}
