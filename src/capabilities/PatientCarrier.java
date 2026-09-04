package capabilities;

public interface PatientCarrier {
    void boardPatients(int number);
    void releasePatients(int number);
    int getPatientCapacity();
    int getCurrentPatients();
}
