package it.unidp.dei.CAPPELLOTTO;

import it.unidp.dei.Algorithm;
import it.unidp.dei.Point;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

public class PELLDiameter extends Diameter
{
    public PELLDiameter(double _eps)
    {
        super(_eps);
        k = 1;
        RV = new TreeMap<>();
        OV = new TreeMap<>();
        last_points = new LinkedList<>();
    }

    public double getDiameter()
    {
        return M_t;
    }

    public int getSize()
    {
        int size = last_points.size()+1;
        for (TreeSet<Point> o : OV.values()) {
            size += o.size();
        }
        for (TreeMap<Point, Point> r : RV.values()) {
            size += 2*r.size();
        }
        return size;
    }

    public void update(Point p, int time)
    {
        // update r
        if(first_point == null){
            first_point = p;
            last_points.add(p);
            step++;
            return;
        }
        Point old_last = last_points.getFirst();
        Point old_new = last_points.getLast();
        if(last_points.size() > k){
            last_points.remove(old_last);
        }
        last_points.add(p);
        r_t = Algorithm.minPairwiseDistance(last_points, p)+1e-9;
        // end updating


        // update sets
        ArrayList<Integer> gams = new ArrayList<>();
        for(Integer gam : RV.keySet())
            gams.add(gam);
        for(Integer gam : gams){
            if(gam < Math.floor(Math.log(r_t/2)/Math.log(1+eps)) ){
                RV.remove(gam);
                OV.remove(gam);
            }
        }
        if(!RV.isEmpty()){
            int low = RV.firstKey() - 1;
            while(low >= Math.floor(Math.log(r_t/2)/Math.log(1+eps)) ){
                TreeMap<Point, Point> RVi = new TreeMap<>();
                RVi.put(old_last, old_last);
                for(Point pp : last_points){
                    if(pp != p)
                        RVi.put(pp, pp);
                }
                RV.put(low, RVi);
                OV.put(low, new TreeSet<>());

                low--;
            }
        }
        // end updating

        // add p for each gamma
        int i = (int)Math.floor(Math.log(r_t/2)/Math.log(1+eps));
        M_t = Double.POSITIVE_INFINITY;
        while(true){
            double gamma = Math.pow(1+eps, i);
            if (i < (int)Math.floor(Math.log(r_t/2)/Math.log(1+eps))) {
                break;
            }
            // reached upper bound, cleanup and exit
            if(gamma > M_t){
                for(int j = i; j <= RV.lastKey(); j++){
                    RV.remove(j);
                    OV.remove(j);
                }
                break;
            }
            if(RV.isEmpty() || i > RV.lastKey()){
                TreeMap<Point, Point> RVi = new TreeMap<>();
                RVi.put(old_new, old_new);
                RV.put(i, RVi);

                OV.put(i, new TreeSet<>());
            }


            // REMOVE OLD POINTS
            ArrayList<Point> ptsToDel = new ArrayList<>();

            if(!RV.get(i).isEmpty() && RV.get(i).firstKey().hasExpired(time)){
                OV.get(i).add(RV.get(i).get(RV.get(i).firstKey()));
                RV.get(i).remove(RV.get(i).firstKey());
            }
            while(!OV.get(i).isEmpty() && OV.get(i).first().hasExpired(time))
                OV.get(i).remove(OV.get(i).first());

            // INSERT NEW POINT p
            ArrayList<Point> E = new ArrayList<>(); // within radius of a validation pt
            for(Point q : RV.get(i).keySet()){
                if( p.getDistance(q) <= gamma*2 ){
                    E.add(q);
                }
            }
            if(E.isEmpty()){
                RV.get(i).put(p, p);
                if(RV.get(i).size() > k+1){ // keep size <= k+1
                    Point vOld = RV.get(i).firstKey();
                    OV.get(i).add(RV.get(i).get(vOld));
                    RV.get(i).remove(vOld);

                }
                if(RV.get(i).size() > k){ // surely can't find a k cluster, so delete
                    while(!OV.get(i).isEmpty() && OV.get(i).first().compareTo(RV.get(i).firstKey()) <= 0)
                        OV.get(i).remove(OV.get(i).first());
                 }
            } else {
                for(Point a : E){
                    //RV.get(i).remove(a);
                    RV.get(i).put(a, p);
                }
            }

            // ENDED INSERTING

            if(M_t == Double.POSITIVE_INFINITY){
                ArrayList<Point> C = new ArrayList<>();
                for(Point pp : RV.get(i).keySet())
                {
                    C.add(pp);
                }
                for(Point pp : OV.get(i))
                {
                    if(C.size() > k)
                        break;
                    double mind = Double.POSITIVE_INFINITY;
                    for(Point q : C)
                        mind = Math.min(mind, pp.getDistance(q));
                    if(mind > 2*gamma)
                        C.add(pp);
                }
                if(C.size()<=k)
                    M_t = 12*gamma;
            }

            i++;

        }
        step++; //next step
    }

    public TreeMap<Integer, TreeMap<Point, Point>> RV;
    public TreeMap<Integer, TreeSet<Point>> OV;
    LinkedList<Point> last_points;
    Point first_point;
    public double r_t, M_t;
    int step = 1, k;
}
