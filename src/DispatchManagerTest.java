import dispatch.*;
import exceptions.*;
import incidents.*;
import  gui.*;
import unit.*;
import java.io.File;


public class DispatchManagerTest {
    private interface Step { void run() throws Exception; }

    public static void main(String[] args) throws Exception {

        String prefix = "scenario_test_state";

        DispatchManager manager = new DispatchManager(new DispatchPolicy());
        Controller gui = new Controller(manager);
        // 1. Add two different response-unit types
        gui.addUnit("AMBULANCE", "A1", "Central Ambulance", 60, 1.2);
        gui.addUnit("FIRE_ENGINE", "F1", "Main Fire Engine", 70, 1.3);
        check(gui.listAllUnits().contains("A1") && gui.listAllUnits().contains("F1"), "1: both units listed");

        // 2. Invalid input: duplicate ID, bad values (the app must not stop)
        expectFailure("2a: duplicate unit ID", () -> gui.addUnit("AMBULANCE", "A1", "Copy", 60, 1.2));
        expectFailure("2b: speed of zero", () -> gui.addUnit("AMBULANCE", "A9", "Slow", 0, 1.2));
        expectFailure("2c: traffic factor below 1", () -> gui.addUnit("AMBULANCE", "A9", "Fast", 60, 0.5));

        // 3. Report two different incident types
        gui.addIncident("MEDICAL", "I1", "Road accident", 12.5, 4, 2, 1, false);
        gui.addIncident("FIRE", "I2", "Warehouse fire", 5, 2, 100, 0, false);
        expectFailure("3a: duplicate incident ID", () -> gui.addIncident("MEDICAL", "I1", "Again", 5, 3, 1, 0, false));
        expectFailure("3b: critical patients exceed patients", () -> gui.addIncident("MEDICAL", "I8", "Bad", 5, 3, 1, 5, false));
        expectFailure("3c: fractional patient count", () -> gui.addIncident("MEDICAL", "I8", "Bad", 5, 3, 2.5, 0, false));

        // 4. Status filter and available units
        String open = gui.listIncidentsByStatus("OPEN");
        check(open.contains("I1") && open.contains("I2"), "4a: both incidents are OPEN");
        check(gui.listIncidentsByStatus("ASSIGNED").contains("none"), "4b: nothing ASSIGNED yet");
        check(gui.listAvailableUnits().contains("A1"), "4c: A1 is available");

        // 5. Preview must not dispatch
        String preview = gui.previewDispatch("I1");
        check(preview.contains("A1") && preview.contains("ELIGIBLE"), "5a: A1 is an eligible candidate");
        check(preview.contains("F1") && preview.contains("REJECTED"), "5b: F1 is rejected for a medical incident");
        check(gui.listIncidentsByStatus("ASSIGNED").contains("none"), "5c: preview did not assign anything");

        // 6. Dispatch (success) and an unsuccessful dispatch
        check(gui.dispatch("I1").contains("Dispatched A1"), "6a: A1 dispatched to I1");
        String state = gui.stateSummary();
        check(state.contains("Incident I1: ASSIGNED") && state.contains("Unit A1: BUSY"), "6b: state updated after dispatch");
        gui.addIncident("SEARCH", "I3", "Missing hiker", 10, 3, 1, 5, false);
        expectFailure("6c: no search unit exists", () -> gui.dispatch("I3"));
        expectFailure("6d: dispatching an ASSIGNED incident", () -> gui.dispatch("I1"));
        expectFailure("6e: unknown incident ID", () -> gui.dispatch("NOPE"));

        // 7. Resolve
        expectFailure("7a: resolving an OPEN incident", () -> gui.resolve("I2"));
        check(gui.resolve("I1").contains("resolved"), "7b: I1 resolved");
        state = gui.stateSummary();
        check(state.contains("Incident I1: RESOLVED") && state.contains("Unit A1: AVAILABLE"), "7c: unit free, incident resolved");

        // 8. Report
        String report = gui.report();
        check(report.contains("Distinct response-unit capabilities") && report.contains("Total incidents: 3"), "8: report contents");

        // 9. Save, start with a fresh model, load, compare
        gui.save(prefix);
        DispatchManager freshManager = new DispatchManager(new DispatchPolicy());
        Controller freshGui = new Controller(freshManager);
        check(freshGui.stateSummary().equals("--- Current state ---\n"), "9a: fresh model is empty");
        freshGui.load(prefix);
        check(sorted(freshGui.stateSummary()).equals(sorted(gui.stateSummary())), "9b: loaded state matches saved state");
        check(freshGui.listIncidentsByStatus("RESOLVED").contains("I1"), "9c: resolved incident restored");

        // 10. Loading a missing file fails cleanly and leaves the data alone
        expectFailure("10a: missing file", () -> freshGui.load("no_such_prefix"));
        check(sorted(freshGui.stateSummary()).equals(sorted(gui.stateSummary())), "10b: state unchanged after failed load");

        new File(prefix + "_units.csv").delete();
        new File(prefix + "_incidents.csv").delete();
        System.out.println("All GUI scenario checks passed.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError("FAILED " + message);
        System.out.println("ok  " + message);
    }

    private static void expectFailure(String message, Step step) {
        try {
            step.run();
        } catch (Exception expected) {
            System.out.println("ok  " + message + " -> " + expected.getMessage());
            return;
        }
        throw new AssertionError("FAILED " + message + " (expected an error)");
    }

    // HashMap order is not fixed, so compare the lines in sorted order
    private static String sorted(String text) {
        String[] lines = text.split("\n");
        java.util.Arrays.sort(lines);
        return String.join("\n", lines);
    }
}
