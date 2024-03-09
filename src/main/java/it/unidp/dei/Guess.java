package it.unidp.dei;

import java.util.*;

public class Guess {

    public Guess(double _gamma, double _delta, int[] _ki) {
        gamma = _gamma;
        delta = _delta;
        ki = _ki;
        int tmp = 0;
        for (int kj : _ki) {
            tmp += kj;
        }
        k = tmp;
    }

    public void update(Point p, int time) {
        //Removes expired points
        if (!RV.isEmpty() && RV.firstKey().hasExpired(time)) {
            OV.add(RV.remove(RV.firstKey()));
        }
        if (!R.isEmpty() && R.firstKey().hasExpired(time)) {
            List<Point>[] list = R.remove(R.firstKey());
            for (List<Point> l : list) {
                O.addAll(l);
            }
        }
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
        ArrayList<Point> E = new ArrayList<>();
        for (Point v : R.keySet()) {
            if (p.getDistance(v) <= delta*gamma/2) {
                E.add(v);
            }
        }

        //If there aren't attraction v-points near p
        if (EV.isEmpty()) {
            //Add it to AV
            RV.put(p, p);

            //If the size is greater than k+1, remove the oldest points
            if (RV.keySet().size() > k+1) {
                OV.add(RV.remove(RV.firstKey()));
            }
            //If the size is greater than k, remove points older from A, OV and O
            if (RV.size() > k) {
                Point vold = RV.firstKey();
                List<Point> ptsToDelete = new ArrayList<>();

                for (Point a : R.navigableKeySet()) {
                    if (a.compareTo(vold) <= 0) {
                        ptsToDelete.add(a);
                    }
                    else {
                        break;
                    }
                }
                for (Point a : ptsToDelete) {
                    LinkedList<Point>[] pts = R.remove(a);
                    for (LinkedList<Point> groupOfPts : pts) {
                        O.addAll(groupOfPts);
                    }
                }

                while (!OV.isEmpty() && OV.first().compareTo(vold) <= 0) {
                    OV.removeFirst();
                }

                while (!O.isEmpty() && O.first().compareTo(vold) <= 0 ) {
                    O.removeFirst();
                }
            }
        }
        else {
            //Else add this point as a representative in RV
            for (Point v : EV) {
                RV.put(v, p);
            }
        }

        //If there aren't attraction c-points near p
        if (E.isEmpty()) {
            //Add this point as a new attraction c-point
            LinkedList<Point>[] ptsGroups = new LinkedList[ki.length];
            for (int i = 0; i< ki.length; i++) {
                LinkedList<Point> pGroup = new LinkedList<>();
                if (i == p.getGroup()) {
                    pGroup.add(p);
                }
                ptsGroups[i] = pGroup;
            }
            R.put(p, ptsGroups);
        }
        else {
            //If there is a tie, the first point inserted in E gets this point as a representative
            int pGroup = p.getGroup();
            Point aadd = E.get(0);
            for (int i = 1; i<E.size(); i++) {
                Point a = E.get(i);
                if (R.get(a)[pGroup].size() < R.get(aadd)[pGroup].size()) {
                    aadd = a;
                }
            }
            LinkedList<Point>[] aaddRep = R.get(aadd);
            aaddRep[pGroup].addLast(p);
            if (aaddRep[pGroup].size() > ki[pGroup]) {
                aaddRep[pGroup].removeFirst();
            }
        }
    }

    public ArrayList<Point> query() {
        LinkedList<Point> union = new LinkedList<>(O);
        for (LinkedList<Point>[] list : R.values()) {
            for (LinkedList<Point> l : list) {
                union.addAll(l);
            }
        }
        CHEN chenEtAl = new CHEN(union, ki);
        return chenEtAl.query();
    }

    public boolean isCorrect() {
        if (RV.keySet().size() <= k) {
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
            //TODO: è corretto?
            for(Point p : RV.values())
            {
                if (p.getMinDistance(C) > 2*gamma) {
                    System.out.println("NOOOOOO NON PUO' SUCCEDEREEEE");
                    C.add(p);
                    if(C.size() > k) {
                        return false;
                    }
                }
            }
            //We don't check if any point in RV is at distance > 2*gamma from C because we know this isn't true
            return true;
        }
        return false;
    }

    public int getSize() {
        int size = O.size()+OV.size()+2*RV.size()+R.keySet().size();
        for (LinkedList<Point>[] list : R.values()) {
            for (LinkedList<Point> l : list) {
                size += l.size();
            }
        }
        return size;
    }

    //TODO: elimina se non oblivious
    public double getValue() {return gamma;}

    private final double gamma;
    private final double delta;
    private final int k;
    private final int[] ki;
    private final TreeSet<Point> O = new TreeSet<>();
    private final TreeSet<Point> OV = new TreeSet<>();
    private final TreeMap<Point, Point> RV = new TreeMap<>();
    private final TreeMap<Point, LinkedList<Point>[]> R = new TreeMap<>();
}
