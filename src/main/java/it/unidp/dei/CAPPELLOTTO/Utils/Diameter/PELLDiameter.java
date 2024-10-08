package it.unidp.dei.CAPPELLOTTO.Utils.Diameter;

import it.unidp.dei.Algorithm;
import it.unidp.dei.Point;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

//This algorithm is the one presented in Pellizzoni's paper
public class PELLDiameter extends Diameter {

    public PELLDiameter(double _eps) {
        super(_eps);
    }

    @Override
    public double getDiameter() {
        return Mt;
    }

    @Override
    public void update(Point p, int time) {
        ///If this is the first point, there is no need to create guesses
        if(last_points.isEmpty()){
            last_points.addLast(p);
            return;
        }

        //If this is not the first point, update last_points:
        //it removes the last point if it has expired or the size is major than k
        Point oldest = null;
        if(last_points.size() > k || last_points.getFirst().hasExpired(time)){
            oldest = last_points.removeFirst();
        }

        //UPDATE of rt: it is the minimum distance between the last k+1 points
        double rt = Algorithm.minPairwiseDistance(last_points, p);

        //Create first index
        int minIndex = (int) Math.floor(Math.log(rt) / Math.log(1 + eps));

        //Delete sets that are not needed
        ArrayList<Integer> toRemove = new ArrayList<>();
        for(Integer gam : RV.keySet()) {
            if(gam >= minIndex){
                break;
            }
            toRemove.add(gam);
        }
        for (Integer gam : toRemove) {
            RV.remove(gam);
            OV.remove(gam);
        }

        if (!RV.isEmpty()) {
            //Creates the new guesses under the ones present
            for (int i = RV.firstKey() - 1; i >= minIndex; i--) {
                //RV and R contain all the last points (even oldest, if present)
                TreeMap<Point, Point> RVi = new TreeMap<>();
                for (Point pp : last_points) {
                    RVi.put(pp, pp);
                }
                if (oldest != null) {
                    RVi.put(oldest, oldest);
                }

                RV.put(i, RVi);
                OV.put(i, new TreeSet<>());
            }
        }

        //Add p for each guess using Pellizzoni algorithm
        int i = minIndex;
        Mt = -1;
        for(double gamma = Math.pow(1+eps, i); Mt == -1 || gamma <= Mt; gamma *= (1+eps), i++){

            //Add if there isn't such a guess
            if(RV.isEmpty() || i > RV.lastKey()){
                TreeMap<Point, Point> RVi = new TreeMap<>();
                RVi.put(last_points.getLast(), last_points.getLast());
                RV.put(i, RVi);
                OV.put(i, new TreeSet<>());
            }

            //This is the procedure UPDATE of Pellizzoni
            if(!RV.get(i).isEmpty() && RV.get(i).firstKey().hasExpired(time)){
                OV.get(i).add(RV.get(i).remove(RV.get(i).firstKey()));
            }
            if(!OV.get(i).isEmpty() && OV.get(i).first().hasExpired(time)) {
                OV.get(i).remove(OV.get(i).first());
            }

            //Near validation points
            ArrayList<Point> EV = new ArrayList<>();
            for(Point q : RV.get(i).keySet()){
                if( p.getDistance(q) <= 2*gamma ){
                    EV.add(q);
                }
            }
            if(EV.isEmpty()){
                RV.get(i).put(p, p);
                if(RV.get(i).size() > k+1){
                    OV.get(i).add(RV.get(i).remove(RV.get(i).firstKey()));
                }
                if(RV.get(i).size() > k){
                    while(!OV.get(i).isEmpty() && OV.get(i).first().compareTo(RV.get(i).firstKey()) <= 0) {
                        OV.get(i).remove(OV.get(i).first());
                    }
                }
            } else {
                for(Point a : EV){
                    RV.get(i).put(a, p);
                }
            }
            //Ended the update of the sets of the guess

            //Update Mt if it is a valid guess and it hasn't been updated
            if(Mt == -1 && RV.get(i).keySet().size() == k){
                Point validation = RV.get(i).firstKey();
                if (validation.getMaxDistance(OV.get(i)) <= 2*gamma) {
                    Mt = 12 * gamma;
                }
            }
        }

        //Reached upper bound, clean all the remaining guesses
        while(i <= RV.lastKey()){
            RV.remove(i);
            OV.remove(i);
            i++;
        }

        //Add the current point to the list of last points
        last_points.add(p);
    }

    @Override
    public int getSize() {
        int size = last_points.size();
        for (TreeSet<Point> o : OV.values()) {
            size += o.size();
        }
        for (TreeMap<Point, Point> r : RV.values()) {
            size += 2*r.size();
        }
        return size;
    }

    private final TreeMap<Integer, TreeMap<Point, Point>> RV = new TreeMap<>();
    private final TreeMap<Integer, TreeSet<Point>> OV = new TreeMap<>();
    private final LinkedList<Point> last_points = new LinkedList<>();
    private double Mt;
    private static final int k = 1;
}
