package it.unidp.dei.CAPPELLOTTO;

import it.unidp.dei.Algorithm;
import it.unidp.dei.CHENETAL.CHEN;
import it.unidp.dei.Point;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

public class KCAPPObl implements Algorithm
{
    public KCAPPObl(double _beta, double _eps, int[] _ki) {
        beta = _beta;
        double epsilon1 = _eps/(1+2*CHEN.alfa);
        delta = epsilon1/(1+_beta);
        ki = _ki;
        int tmp = 0;
        for (int kj : _ki) {
            tmp += kj;
        }
        k = tmp;
        diameter = new DiameterEstimation(beta);
    }

    //This function is the same as the one in CAPP
    public ArrayList<Point> query()
    {
        //Binary search on guesses
        Integer valid = binarySearchGuess();

        //If there isn't a valid guess, it returns an empty ArrayList
        if (valid == null) {
            return new ArrayList<>();
        }
        return guesses.get(valid).query();
    }

    private Integer binarySearchGuess() {
        Integer valid = null;
        int low = guesses.firstKey();
        int high = guesses.lastKey();
        while (low <= high) {
            int mid = (high + low) / 2;
            if (guesses.get(mid).isCorrect()) {
                valid = mid;
                high = mid - 1;
            } else  {
                low = mid + 1;
            }
        }
        return valid;
    }

    @Override
    public int getSize() {
        int size = diameter.getSize()+last_points.size();
        for (KGuess g : guesses.values()) {
            size += g.getSize();
        }
        return size;
    }

    public void update(Point p, int time)
    {
        diameter.update(p, time);
        // update r and M

        //Aggiunge il punto alla lista di punti
        if(last_points.isEmpty()){
            last_points.addLast(p);
            return;
        }
        Point old_last = last_points.getFirst();
        Point old_new = last_points.getLast();
        //La rimuove se i last_points sono più di k
        if(last_points.size() > k || old_last.hasExpired(time)){
            last_points.removeFirst();
        }
        last_points.add(p);

        //UPDATE DI R_T E M_T. Per questo serve diameter
        double r_t = minPairwiseDistance(last_points);
        double m_t = diameter.getDiameter();
        // end updating


        //Mette i nuovi RV, OV e R, O in quelle nuove
        double minDist = Math.floor(Math.log(r_t / 2) / Math.log(1 + beta));
        double maxDist = Math.ceil(Math.log(2 * m_t / delta) / Math.log(1 + beta));

        if (guesses.isEmpty()) {
            int i = (int) minDist;
            while (i <= maxDist) {
                TreeMap<Point, Point> RV = new TreeMap<>();
                RV.put(old_new, old_new);

                TreeMap<Point, LinkedList<Point>[]> R = new TreeMap<>();
                LinkedList<Point>[] list = new LinkedList[ki.length];
                for (int j = 0; j < ki.length; j++) {
                    list[j] = new LinkedList<>();
                }
                list[old_new.getGroup()].add(old_new);
                R.put(old_new, list);

                guesses.put(i, new KGuess(Math.pow((1 + beta), i), delta, ki, RV, R));
                i++;
            }
        } else {
            // update sets (elimina quelli che non servono)
            for(int i = guesses.firstKey(); i<=guesses.lastKey(); i++){
                if(i >= minDist){
                    break;
                }
                guesses.remove(i);
            }

            for(int i = guesses.firstKey(); i<=guesses.lastKey(); i++){
                if(i <= maxDist){
                    break;
                }
                guesses.remove(i);
            }

            int i = guesses.firstKey() - 1;
            while(i >= minDist){
                TreeMap<Point, Point> RV = new TreeMap<>();
                RV.put(old_last, old_last);
                for(Point pp : last_points){ //TODO: MIGLIORA LA LOGICA DEL IF (PP!=P)
                    if(pp != p) {
                        RV.put(pp, pp);
                    }
                }

                TreeMap<Point, LinkedList<Point>[]> R = new TreeMap<>();
                for (Point pp : last_points) {
                    if (pp != p) {
                        LinkedList<Point>[] list = new LinkedList[ki.length];
                        for (int j = 0; j < ki.length; j++) {
                            list[j] = new LinkedList<>();
                        }
                        list[pp.getGroup()].add(pp);
                        R.put(pp, list);
                    }
                }

                LinkedList<Point>[] list = new LinkedList[ki.length];
                for (int j = 0; j < ki.length; j++) {
                    list[j] = new LinkedList<>();
                }
                list[old_last.getGroup()].add(old_last);
                R.put(old_last, list);

                guesses.put(i, new KGuess(Math.pow((1+beta), i), delta, ki, RV, R));

                i--;
            }

            i = guesses.lastKey() + 1;

            while(i <= maxDist){
                TreeMap<Point, Point> RV = new TreeMap<>();
                RV.put(old_new, old_new);

                TreeMap<Point, LinkedList<Point>[]> R = new TreeMap<>();
                LinkedList<Point>[] list = new LinkedList[ki.length];
                for (int j = 0; j < ki.length; j++) {
                    list[j] = new LinkedList<>();
                }
                list[old_new.getGroup()].add(old_new);
                R.put(old_new, list);

                guesses.put(i, new KGuess(Math.pow((1+beta), i), delta, ki, RV, R));

                i++;
            }
        }
        // end updating

        //E QUA INIZIA IL VERO E PROPRIO UPDATE
        for(KGuess g : guesses.values()) {
            g.update(p, time);
        }
    }

    private double minPairwiseDistance(Iterable<Point> points){
        double ans = Double.POSITIVE_INFINITY;
        for(Point p1 : points){
            for(Point p2 : points){
                if(p1 != p2)
                    ans = Math.min(ans, p1.getDistance(p2));
            }
        }
        //TODO: fix
        if (ans == 0) {
            ans = 1e-9;
        }
        return ans;
    }


    //RV, R, OV e O sono indicizzati dall'esponente che devo mettere a (1+beta) per ottenere gamma
    private final TreeMap<Integer, KGuess> guesses = new TreeMap<>();

    //Ultimi punti messi dentro
    private final LinkedList<Point> last_points = new LinkedList<>();
    private int k;
    private final double beta;
    private final double delta;
    private final int[] ki;
    private final DiameterEstimation diameter;
}
