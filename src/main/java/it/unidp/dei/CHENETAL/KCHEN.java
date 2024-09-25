package it.unidp.dei.CHENETAL;

import it.unidp.dei.Algorithm;
import it.unidp.dei.Point;

import java.util.*;

//This is only a wrapper of the CHEN class, that tries to return a set of centers of the maximum size possible (k, if possible)
public class KCHEN implements Algorithm {
    public KCHEN(int[] _ki) {
        realAlg = new CHEN(_ki);
        ki = _ki;
        k = Algorithm.calcK(_ki);
    }

    public KCHEN(LinkedList<Point> p, int[] _ki) {
        realAlg = new CHEN(p, _ki);
        ki = _ki;
        k = Algorithm.calcK(_ki);
    }

    @Override
    public void update(Point p, int time) {
        realAlg.update(p, time);
    }

    @Override
    public ArrayList<Point> query() {
        ArrayList<Point> centers = realAlg.query();

        //Now we can have less than k centers. Update if needed
        LinkedList<Point> pts = realAlg.getPoints();
        while (centers.size() < k && !insertPointAtMaxDistanceBetweenSets(pts, centers)) {
            //The loop updates the centers, but this is done in the condition
        }
        return centers;
    }
    @Override
    public int getSize() {
        return realAlg.getSize();
    }

    //CHIPLUNKAR uses an algorithm that only inserts a casual point in the set of centers,
    //while we use an algorithm that inserts the most distant point of the points in memory in the set of centers.
    private boolean insertPointAtMaxDistanceBetweenSets(Collection<Point> set, ArrayList<Point> centers){
        int[] kj = ki.clone();
        for (Point p : centers) {
            kj[p.getGroup()]--;
        }

        //Take the point at the maximum distance from the centers
        double maxD = 0;
        Point toInsert = null;
        for(Point p : set){
            double dd = p.getMinDistance(centers);
            if (dd > maxD && kj[p.getGroup()] > 0) {
                maxD = dd;
                toInsert = p;
            }
        }

        if (toInsert != null) {
            centers.add(toInsert);
        }
        return toInsert == null;
    }

    private final CHEN realAlg;
    private final int[] ki;
    private final int k;
}
