package it.unidp.dei.CAPPELLOTTO.PELL;

import it.unidp.dei.Algorithm;
import it.unidp.dei.Point;
import it.unidp.dei.TestUtils;

import java.util.*;

//GuessVAL with CHEN instead of JONES
public class PELLGuess {

    public PELLGuess(double _gamma, double _delta, int[] _ki) {
        gamma = _gamma;
        delta = _delta;
        ki = _ki;
        k = Algorithm.calcK(_ki);
        RV = new TreeMap<>();
        R = new TreeMap<>();
    }

    public PELLGuess(double _gamma, double _delta, int[] _ki, TreeMap<Point, Point> _RV, TreeMap<Point, Point> _R) {
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
            O.add(R.remove(R.firstKey()));
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
                    O.add(R.remove(a));
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
            R.put(p, p);
        } else {
            for (Point v : E) {
                R.put(v, p);
            }
        }
    }

    //To be used only after the call to isCorrect() returns true
    public ArrayList<Point> query() {
        ArrayList<Point> union = new ArrayList<>(O);
        union.addAll(R.values());
        return gonKCenter(union);
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
        return O.size()+OV.size()+2*RV.keySet().size()+2*R.keySet().size();
    }

    private ArrayList<Point> gonKCenter(ArrayList<Point> points){
        int n = points.size();
        ArrayList<Point> sol = new ArrayList<>();
        double[] distances = new double[n];
        Arrays.fill(distances, TestUtils.INF + 1);
        int maxi = 0;
        for(int i=0; i < k; i++){
            sol.add(points.get(maxi));
            double max = 0;
            for(int j=0; j < n; j++){
                distances[j] = Math.min(distances[j], sol.get(i).getDistance(points.get(j)));
                if(distances[j] > max){
                    max = distances[j];
                    maxi = j;
                }
            }
        }
        return sol;
    }

    private final double gamma;
    private final double delta;
    private final int k;
    protected final int[] ki;
    protected final TreeSet<Point> O = new TreeSet<>();
    private final TreeSet<Point> OV = new TreeSet<>();
    private final TreeMap<Point, Point> RV;
    private final TreeMap<Point, Point> R;
}
