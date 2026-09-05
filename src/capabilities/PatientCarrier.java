package capabilities;

import exceptions.InsufficientResourceException;
import exceptions.InvalidOperationException;

public interface PatientCarrier {
    void boardPatients(int number) throws InsufficientResourceException;
    void releasePatients(int number) throws InvalidOperationException;
    int getPatientCapacity();
    int getCurrentPatients();
}
