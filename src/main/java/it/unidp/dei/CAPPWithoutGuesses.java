package it.unidp.dei;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CAPPWithoutGuesses implements Algorithm {

    public CAPPWithoutGuesses(int[] _ki, double _epsilon, double _beta, double minDist, double maxDist) {
        //Calculate epsilon1 and then delta
        double epsilon1 = _epsilon/(1+2*alfa);
        double delta = epsilon1/(1+_beta);

        //Initiate the guesses array
        //TODO: questo usa la definizione fatta, ma in effetti si puo' iniziare da minDist?
        int first_i = (int)Math.floor(Math.log(minDist)/Math.log(1+_beta));
        int last_i = (int)Math.ceil(Math.log(maxDist)/Math.log(1+_beta));
        number_of_guesses = last_i-first_i+1;
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
            //TODO: questo modifica la struttura dati interna o no? In teoria si, perche' e' un oggetto
        }
    }

    public ArrayList<Point> query() {
        //Binary search on guesses
        int valid = binarySearchGuess();

        //If there isn't a valid guess, it returns an empty ArrayList
        if (valid == -1) {
            return new ArrayList<>();
        }
        return guesses[valid].query();
    }

    public int getSize() {
        int size = 0;
        for (Guess g : guesses) {
            size += g.getSize();
        }
        return size;
    }

    private int binarySearchGuess() {
        int valid = -1;
        int low = 0;
        int high = guesses.length-1;
        while (low <= high) {
            int mid = (high + low) / 2;
            if (guesses[mid].isCorrect()) {
                valid = mid;
                high = mid - 1;
            } else  {
                low = mid + 1;
            }
        }
        return valid;
    }

    //Array of guesses
    private final int number_of_guesses;

    //Approximation of sequential algorithm. In our case, CHEN gives a 3-approximation
    private static final int alfa = 3;

}
