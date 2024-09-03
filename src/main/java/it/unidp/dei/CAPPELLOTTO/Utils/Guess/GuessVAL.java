package it.unidp.dei.CAPPELLOTTO.Utils.Guess;

import it.unidp.dei.Algorithm;
import it.unidp.dei.CHENETAL.CHEN;
import it.unidp.dei.Point;

import java.util.*;

//The same as Guess, but without the coreset sets
public class GuessVAL {

    public GuessVAL(double _gamma, int[] _ki) {
        gamma = _gamma;
        ki = _ki;
        k = Algorithm.calcK(_ki);
        RV = new TreeMap<>();
    }

    public GuessVAL(double _gamma, int[] _ki, TreeMap<Point, LinkedList<Point>[]> _RV) {
        gamma = _gamma;
        ki = _ki;
        k = Algorithm.calcK(_ki);
        RV = _RV;
    }

    public void update(Point p, int time) {
        //Removes expired points
        if (!RV.isEmpty() && RV.firstKey().hasExpired(time)) {
            List<Point>[] list = RV.remove(RV.firstKey());
            for (List<Point> l : list) {
                OV.addAll(l);
            }
        }

        //Removes the expired points from OV: even here, only one point can be expired per time.
        //In fact, when we add the representatives of expired attraction points, these representative
        //could contain only points of exitTime >= the attraction point that has just expired, thus only
        //one could be expired.
        if (!OV.isEmpty() && OV.first().hasExpired(time)) {
            OV.remove(OV.first());
        }

        //Selects the attraction points near to the new point
        ArrayList<Point> EV = new ArrayList<>();
        for (Point v : RV.keySet()) {
            if (p.getDistance(v) <= 2*gamma) {
                EV.add(v);
            }
        }

        //If there aren't attraction c-points near p
        if (EV.isEmpty()) {
            //Add this point as a new attraction c-point
            LinkedList<Point>[] ptsGroups = new LinkedList[ki.length];
            for (int i = 0; i< ki.length; i++) {
                ptsGroups[i] = new LinkedList<>();
            }
            ptsGroups[p.getGroup()].add(p);
            RV.put(p, ptsGroups);

            //If the size is greater than k+1, remove the oldest point
            if (RV.size() > k+1) {
                LinkedList<Point>[] pts = RV.remove(RV.firstKey());
                for (LinkedList<Point> groupOfPts : pts) {
                    OV.addAll(groupOfPts);
                }
            }
            //If the size is greater than k, remove points older than the oldest of AV from A, OV and O
            if (RV.size() > k) {
                while (!OV.isEmpty() && OV.first().compareTo(RV.firstKey()) < 0) {
                    OV.remove(OV.first());
                }
            }
        } else {
            //If there is a tie, the first point inserted in E gets this point as a representative
            int pGroup = p.getGroup();
            Point aAdd = EV.get(0);
            for (int i = 1; i<EV.size(); i++) {
                Point a = EV.get(i);
                if (RV.get(a)[pGroup].size() < RV.get(aAdd)[pGroup].size()) {
                    aAdd = a;
                }
            }
            LinkedList<Point>[] aAddRep = RV.get(aAdd);
            aAddRep[pGroup].addLast(p);
            if (aAddRep[pGroup].size() > ki[pGroup]) {
                aAddRep[pGroup].removeFirst();
            }
        }
    }

    //To be used only after the call to isCorrect() returns true
    public ArrayList<Point> query() {
        LinkedList<Point> union = new LinkedList<>(OV);
        for (LinkedList<Point>[] list : RV.values()) {
            for (LinkedList<Point> l : list) {
                union.addAll(l);
            }
        }
        CHEN chenEtAl = new CHEN(union, ki);
        return chenEtAl.query();
    }

    //Returns true if and only if the union of points of AV, RV and OV which are at distance
    //greater than 2*gamma has cardinality <= k
    public boolean isCorrect() {
        if (RV.size() > k) {
            return false;
        }
        ArrayList<Point> C = new ArrayList<>(RV.keySet());
        for(Point p : OV)
        {
            if (p.getMinDistance(C) > 2*gamma) {
                C.add(p);
                if(C.size() > k) {
                    return false;
                }
            }
        }
        //We don't check if any point in RV is at distance > 2*gamma from C because we know this isn't true
        return true;
    }

    public int getSize() {
        int size = OV.size()+RV.keySet().size();
        for (LinkedList<Point>[] list : RV.values()) {
            for (LinkedList<Point> l : list) {
                size += l.size();
            }
        }
        return size;
    }

    private final double gamma;
    private final int k;
    protected final int[] ki;
    protected final TreeSet<Point> OV = new TreeSet<>();
    protected final TreeMap<Point, LinkedList<Point>[]> RV;
}
