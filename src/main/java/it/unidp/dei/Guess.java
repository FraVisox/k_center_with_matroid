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
        if (!AV.isEmpty() && AV.getFirst().hasExpired(time)) {
            OV.add(RV.remove(AV.removeFirst()));
        }
        if (!R.isEmpty() && R.firstKey().hasExpired(time)) {
            List<Point>[] list = R.remove(R.firstKey());
            for (List<Point> l : list) {
                O.addAll(l);
            }
        }
        if (!OV.isEmpty() && OV.getFirst().hasExpired(time)) {
            OV.remove(OV.getFirst());
        }
        if (!O.isEmpty() && O.getFirst().hasExpired(time)) {
            O.remove(O.getFirst());
        }

        //Selects the attraction points near to the new point
        ArrayList<Point> EV = new ArrayList<>();
        for (Point v : AV) {
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
            AV.addLast(p);
            RV.put(p, p);

            //If the size is greater than k+1, remove the oldest points
            if (AV.size() > k+1) {
                OV.add(RV.remove(AV.removeFirst()));
            }
            //If the size is greater than k, remove points older from A, OV and O
            if (AV.size() > k) {
                Point vold = AV.getFirst();
                List<Point> ptsToDelete = new ArrayList<>();

                //TODO: e' corretto? E' fatto solo per andare in ordine e risparmiare tempo
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

                while (!OV.isEmpty() && OV.getFirst().compareTo(vold) <= 0) {
                    OV.removeFirst();
                }

                while (!O.isEmpty() && O.getFirst().compareTo(vold) <= 0 ) {
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
            //TODO: questo modifica la struttura dati interna o no? In teoria si, perche' e' un oggetto
        }
    }

    public ArrayList<Point> query() {
        LinkedList<Point> union = new LinkedList<>(O);
        for (LinkedList<Point>[] list : R.values()) {
            for (LinkedList<Point> l : list) {
                union.addAll(l);
            }
        }
        //TODO: devo passargli la guess? In teoria no
        CHEN chenEtAl = new CHEN(union, ki);
        return chenEtAl.query();
    }

    public boolean isCorrect() {
        if (AV.size() <= k) {
            ArrayList<Point> C = new ArrayList<>(AV);
            if(C.size() > k) {
                return false;
            }
            for(Point p : OV)
            {
                if (p.getMinDistance(C) > 2*gamma) {
                    C.add(p);
                    if(C.size() > k) {
                        return false;
                    }
                }
            }
            for(Point p : RV.values())
            {
                if(p.getMinDistance(C) > 2*gamma) {
                    C.add(p);
                    if(C.size() > k) {
                        return false;
                    }
                }
            }
            return C.size() <= k;
        }
        return false;
    }

    public int getSize() {
        int size = 2*AV.size()+O.size()+OV.size()+2*RV.size()+R.keySet().size();
        //TODO: e' da contare anche la dimensione di AV 2 volte? Se si, vedi se togliendolo cambia il tempo/memoria in maniera significativa
        for (LinkedList<Point>[] list : R.values()) {
            for (LinkedList<Point> l : list) {
                size += l.size();
            }
        }
        return size;
    }

    //TODO: FINISCI DI FARE CHECK

    private final double gamma;

    private final double delta;
    private final int k;
    private final int[] ki;
    private final LinkedList<Point> AV = new LinkedList<>();
    private final TreeSet<Point> O = new TreeSet<>();
    private final TreeSet<Point> OV = new TreeSet<>();
    private final TreeMap<Point, Point> RV = new TreeMap<>();
    private final TreeMap<Point, LinkedList<Point>[]> R = new TreeMap<>();
}
