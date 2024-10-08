package it.unidp.dei.CAPPELLOTTO.Utils.Guess;

import it.unidp.dei.Algorithm;
import it.unidp.dei.JONES.JONES;
import it.unidp.dei.Point;

import java.util.*;

//GuessVAL with CHEN instead of JONES
public class Guess {

    public Guess(double _gamma, double _delta, int[] _ki) {
        gamma = _gamma;
        delta = _delta;
        ki = _ki;
        k = Algorithm.calcK(_ki);
        RV = new TreeMap<>();
        R = new TreeMap<>();
    }

    public Guess(double _gamma, double _delta, int[] _ki, TreeMap<Point, Point> _RV, TreeMap<Point, LinkedList<Point>[]> _R) {
        gamma = _gamma;
        delta = _delta;
        ki = _ki;
        k = Algorithm.calcK(_ki);
        RV = _RV;
        R = _R;
    }

    public void update(Point p, int time) {
        //Removes expired points: only one point per time can be expired
        if (!RV.isEmpty() && RV.firstKey().hasExpired(time)) {
            OV.add(RV.remove(RV.firstKey()));
        }
        if (!R.isEmpty() && R.firstKey().hasExpired(time)) {
            List<Point>[] list = R.remove(R.firstKey());
            for (List<Point> l : list) {
                O.addAll(l);
            }
        }

        //Removes the expired points from OV and O: even here, only one point can be expired per time.
        //In fact, when we add the representatives of expired attraction points, these representative
        //could contain only points of exitTime >= the attraction point that has just expired, thus only
        //one could be expired.
        if (!OV.isEmpty() && OV.first().hasExpired(time)) {
            OV.remove(OV.first());
        }
        if (!O.isEmpty() && O.first().hasExpired(time)) {
            O.remove(O.first());
        }

        //Selects the attraction points near to the new point
        ArrayList<Point> EV = new ArrayList<>();
        for (Point v : RV.keySet()) {
            if (p.getDistance(v) <= 2*gamma) {
                EV.add(v);
            }
        }

        //If there aren't attraction v-points near p
        if (EV.isEmpty()) {
            //Add it to AV
            RV.put(p, p);

            //If the size is greater than k+1, remove the oldest point
            if (RV.size() > k+1) {
                OV.add(RV.remove(RV.firstKey()));
            }
            //If the size is greater than k, remove points older than the oldest of AV from A, OV and O
            if (RV.size() > k) {
                Point vOld = RV.firstKey();

                List<Point> ptsToDelete = new ArrayList<>();
                for (Point a : R.keySet()) {
                    if (a.compareTo(vOld) >= 0) {
                        break;
                    }
                    ptsToDelete.add(a);
                }
                for (Point a : ptsToDelete) {
                    LinkedList<Point>[] pts = R.remove(a);
                    for (LinkedList<Point> groupOfPts : pts) {
                        O.addAll(groupOfPts);
                    }
                }

                while (!OV.isEmpty() && OV.first().compareTo(vOld) < 0) {
                    OV.remove(OV.first());
                }

                while (!O.isEmpty() && O.first().compareTo(vOld) < 0) {
                    O.remove(O.first());
                }
            }
        }
        else {
            //Else add this point as a representative in RV
            for (Point v : EV) {
                RV.put(v, p);
            }
        }

        ArrayList<Point> E = new ArrayList<>();
        for (Point c : R.keySet()) {
            if (p.getDistance(c) <= delta*gamma/2) {
                E.add(c);
            }
        }

        //If there aren't attraction c-points near p
        if (E.isEmpty()) {
            //Add this point as a new attraction c-point
            LinkedList<Point>[] ptsGroups = new LinkedList[ki.length];
            for (int i = 0; i< ki.length; i++) {
                ptsGroups[i] = new LinkedList<>();
            }
            ptsGroups[p.getGroup()].add(p);
            R.put(p, ptsGroups);
        } else {
            //If there is a tie, the first point inserted in E gets this point as a representative
            int pGroup = p.getGroup();
            Point aAdd = E.get(0);
            for (int i = 1; i<E.size(); i++) {
                Point a = E.get(i);
                if (R.get(a)[pGroup].size() < R.get(aAdd)[pGroup].size()) {
                    aAdd = a;
                }
            }
            LinkedList<Point>[] aAddRep = R.get(aAdd);
            aAddRep[pGroup].addLast(p);
            if (aAddRep[pGroup].size() > ki[pGroup]) {
                aAddRep[pGroup].removeFirst();
            }
        }
    }

    //To be used only after the call to isCorrect() returns true
    public ArrayList<Point> query() {
        LinkedList<Point> union = new LinkedList<>(O);
        for (LinkedList<Point>[] list : R.values()) {
            for (LinkedList<Point> l : list) {
                union.addAll(l);
            }
        }
        JONES jones = new JONES(union, ki); //here we can use JONES or CHEN, we choose JONES as it performs better
        return jones.query();
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
        int size = O.size()+OV.size()+2*RV.keySet().size()+R.keySet().size();
        for (LinkedList<Point>[] list : R.values()) {
            for (LinkedList<Point> l : list) {
                size += l.size();
            }
        }
        return size;
    }

    private final double gamma;
    private final double delta;
    private final int k;
    protected final int[] ki;
    protected final TreeSet<Point> O = new TreeSet<>();
    private final TreeSet<Point> OV = new TreeSet<>();
    private final TreeMap<Point, Point> RV;
    protected final TreeMap<Point, LinkedList<Point>[]> R;
}
