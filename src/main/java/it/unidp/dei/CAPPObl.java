package it.unidp.dei;

import java.util.*;

public class CAPPObl {
/*
    public CAPPObl(int[] _ki, double _epsilon, double _beta, double minDist, double maxDist) {
        //Calculate epsilon1 and then delta
        double epsilon1 = _epsilon/(1+2*alfa);
        double delta = epsilon1/(1+_beta);

        //Initiate the guesses array. We don't use the definition, but an equivalent form
        int number_of_guesses = (int)Math.ceil(Math.log(maxDist/minDist)/Math.log(1+_beta))+1;

        guesses = new Guess[number_of_guesses];
        double gamma = minDist;
        for (int i = 0; i<number_of_guesses; i++) {
            guesses[i] = new Guess(gamma, delta, _ki);
            gamma *= (1+_beta);
        }
        assert gamma >= maxDist;
    }

    private static double minPairwiseDistance(LinkedList<Point> set) {
        if (set.isEmpty()) {
            return 0;
        }
        LinkedList<Point> thisSet = (LinkedList<Point>)set.clone();
        double minD = (thisSet.removeFirst()).getMinDistance(thisSet);
        for (int i = 1; i<set.size(); i++) {
            minD = Math.min(minD, (thisSet.removeFirst()).getMinDistance(thisSet));
        }
        return minD;
    }

    public void update(Point p, int time) {
        //TODO: in realtà devo controllare se quelli della sua categoria eccedono il kj associato
        if(last_k_points.size() > k){
            last_k_points.removeFirst();
        }
        last_k_points.addLast(p);

        //UPDATE DI r_t E M_t. Per questo serve diameter
        double r_t = minPairwiseDistance(last_k_points);
        double M_t = 12*diameter.query();

        // update sets (elimina quelli che non servono)
        for(int i = 0; i<guesses.length && (guesses[i] == null || guesses[i].getValue() < Math.floor(Math.log(r_t/2)/Math.log(1+beta))); i++){
            guesses[i] = null;
        }

        for(int i = guesses.length-1; i>=0 && (guesses[i] == null || guesses[i] >Math.ceil(Math.log(2*M_t/delta)/Math.log(1+beta))); i--){
            guesses[i] = null;
        }

        //Mette i nuovi RV, OV e R, O in quelle nuove
        if(!RV.isEmpty()){
            int low = RV.firstKey() - 1;
            int high = RV.lastKey() + 1;
            while(low >= Math.floor(Math.log(r_t/2)/Math.log(1+beta)) ){
                TreeMap<Point, Point> RVi = new TreeMap<>((aa, bb)-> aa.exitTime-bb.exitTime);
                RVi.put(old_last, old_last);
                for(Point pp : last_points){
                    if(pp != p)
                        RVi.put(pp, pp);
                }
                RV.put(low, RVi);

                TreeMap<Point, Point> Ri = new TreeMap<>((aa, bb)-> aa.exitTime-bb.exitTime);
                Ri.put(old_last, old_last);
                for(Point pp : last_points){
                    if(pp != p)
                        Ri.put(pp, pp);
                }
                R.put(low, Ri);

                OV.put(low, new TreeSet<>((aa, bb)-> aa.exitTime-bb.exitTime));
                O.put(low, new TreeSet<>((aa, bb)-> aa.exitTime-bb.exitTime));

                low--;
            }

            while(high <= Math.ceil(Math.log(2*M_t/delta)/Math.log(1+beta))){
                TreeMap<Point, Point> RVi = new TreeMap<>((aa, bb)-> aa.exitTime-bb.exitTime);
                RVi.put(old_new, old_new);
                RV.put(high, RVi);

                TreeMap<Point, Point> Ri = new TreeMap<>((aa, bb)-> aa.exitTime-bb.exitTime    );
                Ri.put(old_new, old_new);
                R.put(high, Ri);

                OV.put(high, new TreeSet<>((aa, bb)-> aa.exitTime-bb.exitTime));
                O.put(high, new TreeSet<>((aa, bb)-> aa.exitTime-bb.exitTime));

                high++;
            }
        }
        //Se ho svuotato tutto RV:
        else{
            int i = (int)Math.floor(Math.log(r_t/2)/Math.log(1+beta));
            while(i <= Math.ceil(Math.log(2*M_t/delta)/Math.log(1+beta))){
                TreeMap<Point, Point> RVi = new TreeMap<>((aa, bb)-> aa.exitTime-bb.exitTime);
                RVi.put(old_new, old_new);
                RV.put(i, RVi);

                TreeMap<Point, Point> Ri = new TreeMap<>((aa, bb)-> aa.exitTime-bb.exitTime);
                Ri.put(old_new, old_new);
                R.put(i, Ri);

                OV.put(i, new TreeSet<>((aa, bb)-> aa.exitTime-bb.exitTime));
                O.put(i, new TreeSet<>((aa, bb)-> aa.exitTime-bb.exitTime));
                i++;
            }
        }
        // end updating
        for (Guess g : guesses) {
            g.update(p, time);
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
    private final Guess[] guesses;
    private final LinkedList<Point> last_k_points = new LinkedList<>();

    //Approximation of sequential algorithm. In our case, CHEN gives a 3-approximation
    private static final int alfa = 3;


 */
}
