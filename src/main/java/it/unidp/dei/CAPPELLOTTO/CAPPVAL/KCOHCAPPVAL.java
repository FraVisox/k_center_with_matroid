package it.unidp.dei.CAPPELLOTTO.CAPPVAL;

import it.unidp.dei.Algorithm;
import it.unidp.dei.CAPPELLOTTO.Utils.Diameter.COHENDiameter;
import it.unidp.dei.CAPPELLOTTO.Utils.Guess.GuessVAL;
import it.unidp.dei.CAPPELLOTTO.Utils.Guess.KCGuessVAL;
import it.unidp.dei.Point;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

//It is the same as KCOHCAPP, but uses GuessVAL instead of Guess
public class KCOHCAPPVAL implements Algorithm
{
    public KCOHCAPPVAL(double _beta, int[] _ki) {
        beta = _beta;
        ki = _ki;
        k = Algorithm.calcK(_ki);
        diameter = new COHENDiameter(beta);
    }

    @Override
    public void update(Point p, int time)
    {
        //Update of the diameter: is always done, even if it's the first point
        diameter.update(p, time);

        //If this is the first point, there is no need to create guesses
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

        //UPDATE of r_t and M_t: r_t is the minimum distance between the last k+1 points,
        //while M_t is a guess of the diameter of the entire window.
        double r_t = Algorithm.minPairwiseDistance(last_points, p);
        double M_t = diameter.getDiameter();

        //Create first and last indexes
        int firstIndex = (int) Math.floor(Math.log(r_t / 2) / Math.log(1 + beta));
        int lastIndex = (int) Math.ceil(Math.log( M_t ) / Math.log(1 + beta));

        if (guesses.isEmpty()) {
            for (int i = firstIndex; i <= lastIndex; i++) {
                //RV only contains the point t-1
                TreeMap<Point, LinkedList<Point>[]> RV = new TreeMap<>();
                RV.put(last_points.getLast(), createR(last_points.getLast()));

                guesses.put(i, new KCGuessVAL(Math.pow((1 + beta), i), ki, RV));
            }
        } else {
            // Delete the sets that are under the first index or over the last.
            for(int i = guesses.firstKey(); i <= guesses.lastKey() && i < firstIndex; i++){
                guesses.remove(i);
            }
            for(int i = guesses.lastKey(); i >= guesses.firstKey() && i > lastIndex; i--){
                guesses.remove(i);
            }

            //Creates the new guesses
            for(int i = guesses.firstKey() - 1; i >= firstIndex; i--){
                //RV contains all the last points (even oldest, if present)
                TreeMap<Point, LinkedList<Point>[]> RV = new TreeMap<>();
                for (Point pp : last_points) {
                    RV.put(pp, createR(pp));
                }
                if (oldest != null) {
                    RV.put(oldest, createR(oldest));
                }

                guesses.put(i, new KCGuessVAL(Math.pow((1 + beta), i), ki, RV));
            }


            for(int i = guesses.lastKey() + 1; i <= lastIndex; i++){
                //RV contains only the last point
                TreeMap<Point, LinkedList<Point>[]> RV = new TreeMap<>();
                RV.put(last_points.getLast(), createR(last_points.getLast()));

                guesses.put(i, new KCGuessVAL(Math.pow((1+beta), i), ki, RV));
            }
        }
        //Insert the point p in the last points
        last_points.add(p);

        //Update all the guesses
        for(GuessVAL g : guesses.values()) {
            g.update(p, time);
        }
    }

    @Override
    //This function is the same as the one in CAPP
    public ArrayList<Point> query()
    {
        //Binary search on guesses
        Integer valid = binarySearchGuess();

        //If there isn't a valid guess, it returns null
        if (valid == null) {
            return null;
        }
        return guesses.get(valid).query();
    }

    @Override
    public int getSize() {
        int size = diameter.getSize()+last_points.size();
        for (GuessVAL g : guesses.values()) {
            size += g.getSize();
        }
        return size;
    }

    private LinkedList<Point>[] createR(Point p) {
        LinkedList<Point>[] list = new LinkedList[ki.length];
        for (int j = 0; j < ki.length; j++) {
            list[j] = new LinkedList<>();
        }
        list[p.getGroup()].add(p);
        return list;
    }

    //Binary search on guesses, as in CAPP. It returns null if there aren't correct guesses (as -1 is a valid return value)
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

    //Guesses, the key is the exponent to give to (1+beta) to get that guess
    private final TreeMap<Integer, KCGuessVAL> guesses = new TreeMap<>();
    //Used to estimate the diameter
    private final COHENDiameter diameter;
    //Last k+1 points
    private final LinkedList<Point> last_points = new LinkedList<>();
    private final int k;
    private final double beta;
    private final int[] ki;
}

