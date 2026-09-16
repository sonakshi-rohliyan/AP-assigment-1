import java.util.Scanner;
import dispatch.*;
import unit.*;
import incidents.*;
import exceptions.*;

public class Main {

    static Scanner scanner = new Scanner(System.in);
    static DispatchManager manager = new DispatchManager(50, 100, new DispatchPolicy());

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ");

            try {
                switch (choice) {
                    case 1: addUnit(); break;
                    case 2: removeUnit(); break;
                    case 3: addIncident(); break;
                    case 4: removeIncident(); break;
                    case 5: findUnitMenu(); break;
                    case 6: findIncidentMenu(); break;
                    case 7: listUnits(); break;
                    case 8: listIncidents(); break;
                    case 9: previewDispatch(); break;
                    case 10: dispatchUnit(); break;
                    case 11: resolveIncidentMenu(); break;
                    case 12: refuelAll(); break;
                    case 13: rechargeAll(); break;
                    case 14: refillWaterAll(); break;
                    case 15: restockSuppliesAll(); break;
                    case 16: System.out.println(manager.generateReport()); break;
                    case 17: manager.displayUnserviceableIncidents(); break;
                    case 18: saveState(); break;
                    case 19: loadState(); break;
                    case 20: running = false; System.out.println("Exiting..."); break;
                    default: System.out.println("Invalid choice. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    static void printMenu() {
        System.out.println("\n================ EMERGENCY DISPATCH SYSTEM ================");
        System.out.println("1. Add Response Unit");
        System.out.println("2. Remove Response Unit");
        System.out.println("3. Report New Incident");
        System.out.println("4. Remove Incident");
        System.out.println("5. Find Response Unit");
        System.out.println("6. Find Incident");
        System.out.println("7. List All Response Units");
        System.out.println("8. List All Incidents");
        System.out.println("9. Preview Dispatch Candidates");
        System.out.println("10. Dispatch Best Unit");
        System.out.println("11. Resolve Incident");
        System.out.println("12. Refuel Fuel-Powered Units");
        System.out.println("13. Recharge Battery-Powered Units");
        System.out.println("14. Refill Water Carriers");
        System.out.println("15. Restock Supply Carriers");
        System.out.println("16. Generate System Report");
        System.out.println("17. Display Unserviceable Incidents");
        System.out.println("18. Save State");
        System.out.println("19. Load State");
        System.out.println("20. Exit");
        System.out.println("===========================================================");
    }

    // ---------- input helpers ----------
    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return Double.parseDouble(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    static boolean readBoolean(String prompt) {
        while (true) {
            System.out.print(prompt + " (true/false): ");
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("true")) return true;
            if (line.equalsIgnoreCase("false")) return false;
            System.out.println("Please enter true or false.");
        }
    }

    // ---------- menu actions ----------
    static void addUnit() throws Exception {
        String type = readString("Type (AMBULANCE/FIRE_ENGINE/REPAIR_VAN/SEARCH_DRONE/HAZMAT_UNIT): ").toUpperCase();
        String id = readString("ID: ");
        String name = readString("Name: ");
        double speed = readDouble("Max speed: ");

        ResponseUnit unit;
        switch (type) {
            case "AMBULANCE":
                unit = new Ambulance(id, name, speed, readDouble("Traffic factor: "));
                break;
            case "FIRE_ENGINE":
                unit = new FireEngine(id, name, speed, readDouble("Traffic factor: "));
                break;
            case "REPAIR_VAN":
                unit = new RepairVan(id, name, speed, readDouble("Traffic factor: "));
                break;
            case "SEARCH_DRONE":
                unit = new SearchDrone(id, name, speed);
                break;
            case "HAZMAT_UNIT":
                unit = new HazmatUnit(id, name, speed, readDouble("Traffic factor: "));
                break;
            default:
                System.out.println("Unknown unit type.");
                return;
        }
        manager.addUnit(unit);
        System.out.println("Unit added successfully.");
    }

    static void removeUnit() throws Exception {
        String id = readString("Unit ID to remove: ");
        manager.removeUnit(id);
        System.out.println("Unit removed.");
    }

    static void addIncident() throws Exception {
        String type = readString("Type (MEDICAL/FIRE/INFRASTRUCTURE/SEARCH/HAZMAT): ").toUpperCase();
        String id = readString("ID: ");
        String description = readString("Description: ");
        double distance = readDouble("Distance from base: ");
        int severity = readInt("Severity (1-5): ");

        Incident incident;
        switch (type) {
            case "MEDICAL":
                incident = new MedicalIncident(id, description, distance, severity,
                        readInt("Patient count: "), readInt("Critical patients: "));
                break;
            case "FIRE":
                incident = new FireIncident(id, description, distance, severity,
                        readDouble("Affected area: "), readBoolean("Hazardous material?"));
                break;
            case "INFRASTRUCTURE":
                incident = new InfrastructureIncident(id, description, distance, severity,
                        readInt("Affected users: "), readBoolean("Critical service?"));
                break;
            case "SEARCH":
                incident = new SearchIncident(id, description, distance, severity,
                        readInt("Missing persons: "), readDouble("Search area: "));
                break;
            case "HAZMAT":
                incident = new HazmatIncident(id, description, distance, severity,
                        readDouble("Contaminant spread: "), readInt("Population at risk: "));
                break;
            default:
                System.out.println("Unknown incident type.");
                return;
        }
        manager.addIncident(incident);
        System.out.println("Incident reported successfully.");
    }

    static void removeIncident() throws Exception {
        String id = readString("Incident ID to remove: ");
        manager.removeIncident(id);
        System.out.println("Incident removed.");
    }

    static void findUnitMenu() {
        String id = readString("Unit ID: ");
        ResponseUnit unit = manager.findUnit(id);
        if (unit == null) {
            System.out.println("No unit found with that ID.");
        } else {
            unit.display();
        }
    }

    static void findIncidentMenu() {
        String id = readString("Incident ID: ");
        Incident incident = manager.findIncident(id);
        if (incident == null) {
            System.out.println("No incident found with that ID.");
        } else {
            incident.displayIncident();
        }
    }

    static void listUnits() {
        for (int i = 0; i < manager.getUnitCount(); i++) {
            manager.getUnitAt(i).display();
            System.out.println("---");
        }
    }

    static void listIncidents() {
        for (int i = 0; i < manager.getIncidentCount(); i++) {
            manager.getIncidentAt(i).displayIncident();
            System.out.println("---");
        }
    }

    static void previewDispatch() throws Exception {
        String id = readString("Incident ID: ");
        manager.displayDispatchCandidates(id);
    }

    static void dispatchUnit() throws Exception {
        String id = readString("Incident ID: ");
        ResponseUnit unit = manager.dispatchBestUnit(id);
        System.out.println("Dispatched: " + unit.getId());
    }

    static void resolveIncidentMenu() throws Exception {
        String id = readString("Incident ID: ");
        manager.resolveIncident(id);
        System.out.println("Incident resolved.");
    }

    static void refuelAll() throws Exception {
        manager.refuelAll(readDouble("Refuel amount: "));
        System.out.println("Refuelled all fuel-powered units.");
    }

    static void rechargeAll() throws Exception {
        manager.rechargeAll(readDouble("Recharge amount: "));
        System.out.println("Recharged all battery-powered units.");
    }

    static void refillWaterAll() throws Exception {
        manager.refillAllWater(readDouble("Water amount: "));
        System.out.println("Refilled all water carriers.");
    }

    static void restockSuppliesAll() throws Exception {
        manager.restockAllSupplies(readInt("Supply amount: "));
        System.out.println("Restocked all supply carriers.");
    }

    static void saveState() throws Exception {
        String prefix = readString("Save file prefix: ");
        manager.saveState(prefix);
        System.out.println("State saved.");
    }

    static void loadState() throws Exception {
        String prefix = readString("Load file prefix: ");
        manager.loadState(prefix);
        System.out.println("State loaded.");
    }
}