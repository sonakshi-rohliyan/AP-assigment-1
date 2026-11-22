package dispatch;

import java.util.Comparator;
import incidents.Incident;

public class IncidentPriorityComparator implements Comparator<Incident>{
    @Override
    public int compare(Incident a, Incident b) {
        int result = Double.compare(b.getPriorityWeight(), a.getPriorityWeight());
        if (result != 0){
            return result;
        }
        result = Double.compare(a.getDistanceFromBase(), b.getDistanceFromBase());
        if (result != 0){
            return  result;
        }
        return a.getId().compareTo(b.getId());
    }
}
