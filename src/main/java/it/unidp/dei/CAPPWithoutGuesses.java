package it.unidp.dei;

import java.util.*;

//Conclusione: non fa nulla in più né in meno, e non ha grossi vantaggi

public class CAPPWithoutGuesses implements Algorithm {

    public CAPPWithoutGuesses(int[] _ki, double _epsilon, double _beta, double _minDist, double _maxDist) {
        //Calculate epsilon1 and then delta
        double epsilon1 = _epsilon/(1+2*alfa);
        delta = epsilon1/(1+_beta);

        beta = _beta;

        ki = _ki;
        int tmp = 0;
        for (int kj : ki) {
            tmp += kj;
        }
        k = tmp;

        //Initiate the guesses array
        first_i = (int)Math.floor(Math.log(_minDist)/Math.log(1+_beta));
        last_i = (int)Math.ceil(Math.log(_maxDist)/Math.log(1+_beta));
        int number_of_guesses = last_i-first_i+1;
        RV = new TreeMap[number_of_guesses];
        for (int i = 0; i<number_of_guesses; i++) {
            RV[i] = new TreeMap<>();
        }
        R = new TreeMap[number_of_guesses];
        for (int i = 0; i<number_of_guesses; i++) {
            R[i] = new TreeMap<>();
        }
        OV = new TreeSet[number_of_guesses];
        for (int i = 0; i<number_of_guesses; i++) {
            OV[i] = new TreeSet<>();
        }
        O = new TreeSet[number_of_guesses];
        for (int i = 0; i<number_of_guesses; i++) {
            O[i] = new TreeSet<>();
        }
    }

    public void update(Point p, int time) {
        double gamma = Math.pow((1+beta), first_i);
        for (int i = 0; i<RV.length; i++) {
            //Removes expired points
            if (!RV[i].isEmpty() && RV[i].firstKey().hasExpired(time)) {
                OV[i].add(RV[i].remove(RV[i].firstKey()));
            }
            if (!R[i].isEmpty() && R[i].firstKey().hasExpired(time)) {
                List<Point>[] list = R[i].remove(R[i].firstKey());
                for (List<Point> l : list) {
                    O[i].addAll(l);
                }
            }
            if (!OV[i].isEmpty() && OV[i].first().hasExpired(time)) {
                OV[i].remove(OV[i].first());
            }
            if (!O[i].isEmpty() && O[i].first().hasExpired(time)) {
                O[i].remove(O[i].first());
            }

            //Selects the attraction points near to the new point
            ArrayList<Point> EV = new ArrayList<>();
            for (Point v : RV[i].keySet()) {
                if (p.getDistance(v) <= 2 * gamma) {
                    EV.add(v);
                }
            }
            ArrayList<Point> E = new ArrayList<>();
            for (Point v : R[i].keySet()) {
                if (p.getDistance(v) <= delta * gamma / 2) {
                    E.add(v);
                }
            }

            //If there aren't attraction v-points near p
            if (EV.isEmpty()) {
                //Add it to AV
                RV[i].put(p, p);

                //If the size is greater than k+1, remove the oldest points
                if (RV[i].keySet().size() > k + 1) {
                    OV[i].add(RV[i].remove(RV[i].firstKey()));
                }
                //If the size is greater than k, remove points older from A, OV and O
                if (RV[i].size() > k) {
                    Point vold = RV[i].firstKey();
                    List<Point> ptsToDelete = new ArrayList<>();

                    //TODO: e' corretto? E' fatto solo per andare in ordine e risparmiare tempo
                    for (Point a : R[i].navigableKeySet()) {
                        if (a.compareTo(vold) <= 0) {
                            ptsToDelete.add(a);
                        } else {
                            break;
                        }
                    }
                    for (Point a : ptsToDelete) {
                        LinkedList<Point>[] pts = R[i].remove(a);
                        for (LinkedList<Point> groupOfPts : pts) {
                            O[i].addAll(groupOfPts);
                        }
                    }

                    while (!OV[i].isEmpty() && OV[i].first().compareTo(vold) <= 0) {
                        OV[i].removeFirst();
                    }

                    while (!O[i].isEmpty() && O[i].first().compareTo(vold) <= 0) {
                        O[i].removeFirst();
                    }
                }
            } else {
                //Else add this point as a representative in RV
                for (Point v : EV) {
                    RV[i].put(v, p);
                }
            }

            //If there aren't attraction c-points near p
            if (E.isEmpty()) {
                //Add this point as a new attraction c-point
                LinkedList<Point>[] ptsGroups = new LinkedList[ki.length];
                for (int j = 0; j < ki.length; j++) {
                    LinkedList<Point> pGroup = new LinkedList<>();
                    if (j == p.getGroup()) {
                        pGroup.add(p);
                    }
                    ptsGroups[j] = pGroup;
                }
                R[i].put(p, ptsGroups);
            } else {
                //If there is a tie, the first point inserted in E gets this point as a representative
                int pGroup = p.getGroup();
                Point aadd = E.get(0);
                for (int j = 1; j < E.size(); j++) {
                    Point a = E.get(j);
                    if (R[i].get(a)[pGroup].size() < R[i].get(aadd)[pGroup].size()) {
                        aadd = a;
                    }
                }
                LinkedList<Point>[] aaddRep = R[i].get(aadd);
                aaddRep[pGroup].addLast(p);
                if (aaddRep[pGroup].size() > ki[pGroup]) {
                    aaddRep[pGroup].removeFirst();
                }
            }
            gamma *= (1+beta);
        }
    }

    public ArrayList<Point> query() {
        double gamma = Math.pow(1+beta, first_i);
        for (int mid = 0; mid < RV.length; mid++) {
            if (RV[mid].keySet().size() <= k) {
                ArrayList<Point> C = new ArrayList<>(RV[mid].keySet());
                for(Point p : OV[mid])
                {
                    if(C.size() > k) {
                        break;
                    }
                    if (p.getMinDistance(C) > 2*gamma) {
                        C.add(p);
                    }
                }
                for(Point p : RV[mid].values())
                {
                    if(C.size() > k) {
                        break;
                    }
                    if(p.getMinDistance(C) > 2*gamma) {
                        C.add(p);
                    }
                }
                if(C.size() <= k) {
                    LinkedList<Point> union = new LinkedList<>(O[mid]);
                    for (LinkedList<Point>[] list : R[mid].values()) {
                        for (LinkedList<Point> l : list) {
                            union.addAll(l);
                        }
                    }
                    CHEN chenEtAl = new CHEN(union, ki);
                    return chenEtAl.query();
                }
            }
            gamma *= (1+beta);
        }
        return null;
    }

    public int getSize() {
        int size = 0;
        for (TreeSet<Point> set : OV) {
            size += set.size();
        }
        for (TreeSet<Point> set : O) {
            size += set.size();
        }
        for (TreeMap<Point, Point> set : RV) {
            size += 2*set.size();
        }
        for (TreeMap<Point, LinkedList<Point>[]> set : R) {
            size += set.keySet().size();
            for (LinkedList<Point>[] list : set.values()) {
                for (LinkedList<Point> l : list) {
                    size += l.size();
                }
            }
        }
        return size;
    }


    //Approximation of sequential algorithm. In our case, CHEN gives a 3-approximation
    private static final int alfa = 3;
    private final double delta;
    private final int k;
    private final int[] ki;
    private final int first_i;
    private final int last_i;
    private final double beta;

    private final TreeSet<Point>[] O;
    private final TreeSet<Point>[] OV;
    private final TreeMap<Point, Point>[] RV;
    private final TreeMap<Point, LinkedList<Point>[]>[] R;

}
