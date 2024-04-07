package it.unidp.dei.CAPPELLOTTO.Utils.Diameter;

import it.unidp.dei.Point;

import java.util.ArrayList;
import java.util.TreeMap;

//This algorithm is the one presented in Cohen-Addad's paper
public class COHENDiameter extends Diameter
{
    public COHENDiameter(double _eps) {
        super(_eps);
    }

    @Override
    public double getDiameter()
    {
        if(!cnew.isEmpty()) {
            //For the first point that has cnew == null, we return 3 times the corresponding guess
            for (int i = cnew.firstKey(); i <= cnew.lastKey(); i++) {
                if (cnew.get(i) == null) {
                    //The diameter is at most 3*gamma
                    return 3 * Math.pow(1 + eps, i);
                }
            }
        }

        //If the number of points is not enough to have guesses:
        if (last != null) {
            return last.getDistance(secondLast);
        }
        return 0;
    }

    @Override
    public void update(Point p, int time)
    {
        //The first point is pt2 and the second one is pt1
        if(secondLast == null){
            secondLast = p;
            return;
        }
		if(last == null){
			last = p;
			return;
		}

        //rt is the minimum guess of the diameter. If the guess is zero, we substitute it with the minimum (as the log(0) is undefined)
        double rt = last.getDistance(p);
        if (rt == 0) {
            rt = Diameter.minimum;
        }

        //Delete the guesses less than minIndex from the possible guesses
        int minIndex = (int) Math.floor(Math.log(rt) / Math.log(1 + eps));

        ArrayList<Integer> toRemove = new ArrayList<>();
        for(Integer gam : q.keySet()){
            if (gam >= minIndex) {
                break;
            }
            toRemove.add(gam);
        }
        for (Integer gam : toRemove) {
            remove(gam);
        }

        //If we have guesses, and if the current minIndex is lower than the lower existing guess
        if(!q.isEmpty()){
            int low = q.firstKey() -1;
            while(low >= minIndex){
                insert(low, secondLast, last);
                low--;
            }
        }

        //First guess of M, it will be updated
        double Mt = 3*(1+eps)*rt;
        //Initial index
        int i = minIndex;

        //This is the algorithm described in Cohen-Addad: for every guess, it inserts p
        for(double gamma = Math.pow(1+eps, i); gamma <= Mt; gamma *= (1+eps)){

            //If there isn't a guess with this value (we are over the previous M), add it
            if(q.isEmpty() || i > q.lastKey()){
                insert(i, secondLast, null);
            }

            //Delete expired points
            if(cold.get(i).hasExpired(time)){
                if(cnew.get(i) != null){
                    cnew.put(i, null);
                    if(cold.get(i).equals(q.get(i))){
                        cold.put(i, r.get(i));
                    } else{
                        cold.put(i, q.get(i));
                    }
                } else{
                    cold.put(i, r.get(i));
                }
            }

            //Insert p
            if(p.getDistance(r.get(i)) > gamma){
                cold.put(i, r.get(i));
                q.put(i, r.get(i));
                cnew.put(i, p);
            } else if(cnew.get(i) == null){
                if(p.getDistance(cold.get(i)) > gamma){
                    q.put(i, r.get(i));
                    cnew.put(i, p);
                }
            } else {
                 if(p.getDistance(cnew.get(i)) > gamma){
                    cold.put(i, cnew.get(i));
                    q.put(i, r.get(i));
                    cnew.put(i, p);
                } else if(p.getDistance(q.get(i)) > gamma){
                    if(!cold.get(i).equals(q.get(i))){
                        cold.put(i, q.get(i));
                        q.put(i, r.get(i));
                        cnew.put(i, p);
                    }
                }
            }

            //Update r
            r.put(i, p);

            //If 3*gamma <= diameter, then Mt must be at least 3*next_gamma.
            //This is done only for the first guesses, so Mt is not updated infinitely
            if(cnew.get(i) != null){
                Mt = 3*gamma*(1+eps);
            }

            i++;
        }

        //Reached upper bound, clean all the remaining guesses
        while(i <= q.lastKey()){
            remove(i);
            i++;
        }

        //Update pt2 and pt1
        secondLast = last;
		last = p;
    }

    @Override
    public int getSize() {
        int size = cold.size()+q.size()+r.size()+2;
        for (Point p : cnew.values()) {
            if (p != null) {
                size++;
            }
        }
        return size;
    }

    private void remove(Integer gam) {
        cold.remove(gam);
        cnew.remove(gam);
        q.remove(gam);
        r.remove(gam);
    }

    private void insert(Integer i, Point _cold, Point _cnew) {
        cold.put(i, _cold);
        r.put(i, _cold);
        q.put(i, _cold);
        cnew.put(i, _cnew);
    }

    //For every guess, we keep one cold, one cnew, one q and one r points.
    private final TreeMap<Integer, Point> cold = new TreeMap<>();
    private final TreeMap<Integer, Point> cnew = new TreeMap<>();
    private final TreeMap<Integer, Point> q = new TreeMap<>();
    private final TreeMap<Integer, Point> r = new TreeMap<>();
    private Point secondLast;
    private Point last;
}
