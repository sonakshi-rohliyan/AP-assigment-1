package dispatch;

import java.util.Comparator;
import unit.ResponseUnit;

public class UnitDistanceComparator implements Comparator<ResponseUnit>{
    @Override
    public int compare(ResponseUnit a, ResponseUnit b){
        int result = Double.compare(b.getTotalDistanceTravelled(), a.getTotalDistanceTravelled());
        if (result != 0 ){
            return result;
        }
        return a.getId().compareTo(b.getId());
    }
}
