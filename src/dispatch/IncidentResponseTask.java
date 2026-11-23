package dispatch;

import java.util.function.Consumer;

public class IncidentResponseTask implements Runnable {
    private DispatchManager dispatchManager;
    private String incidentId;
    private Consumer<String> afterTaskFinished;

    public IncidentResponseTask(DispatchManager dispatchManager, String incidentId, Consumer<String> afterTaskFinished){
        this.dispatchManager = dispatchManager;
        this.incidentId = incidentId;
        this.afterTaskFinished = afterTaskFinished;
    }

    @Override
    public void run() {
        StringBuilder output = new StringBuilder();
        try {
            Thread.sleep(5000);
            dispatchManager.resolveIncident(incidentId);
            output.append("The incident ").append(incidentId).append(" is resolved\n");
        }
        catch (InterruptedException e){
            output.append("The response for").append(incidentId).append(" was interrupted");
            Thread.currentThread().interrupt();
        }
        catch (Exception e){
            output.append("Error ").append(e.getMessage()).append("\n" );
        }
        finally {
            afterTaskFinished.accept(output.toString());
        }
    }
}
