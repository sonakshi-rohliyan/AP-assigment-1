package unit;
import exceptions.*;
import incidents.Incident;
import dispatch.DispatchPolicy;

public class GroundResponseUnit extends ResponseUnit{
    private double trafficFactor;

    public GroundResponseUnit(String id, String name, double maxSpeed,double trafficFactor) throws InvalidOperationException{
        super(id,name,maxSpeed);

        if (trafficFactor < 1.0) {
            throw new InvalidOperationException("Traffic factor must be at least 1.0");
        }

        this.trafficFactor = trafficFactor;
    }
    public double getTrafficFactor() {return trafficFactor;}

    @Override
    public double estimateArrivalTime(double distance){
        double speed = getMaxSpeed();
        return distance*trafficFactor/getMaxSpeed();
    }

}
