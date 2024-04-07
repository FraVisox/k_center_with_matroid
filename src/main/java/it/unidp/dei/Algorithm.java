package it.unidp.dei;

import it.unidp.dei.CAPPELLOTTO.Utils.Diameter.Diameter;

import java.util.ArrayList;
import java.util.LinkedList;

public interface Algorithm {
    void update(Point p, int time);
    ArrayList<Point> query();
    int getSize();

    //Utility function to calculate K given a set of ki
    static int calcK(int[] _ki) {
        int tmp = 0;
        for (int kj : _ki) {
            tmp += kj;
        }
        return tmp;
    }

    //Utility function to calculate the minimum distance between all the points in points and p:
    static double minPairwiseDistance(LinkedList<Point> points, Point p){
        double ans = p.getMinDistanceWithoutItself(points, Main.INF);
        LinkedList<Point> thesePts = (LinkedList<Point>)points.clone();
        for(Point q : points){
            thesePts.remove(q);
            ans = q.getMinDistanceWithoutItself(thesePts, ans);
        }
        if (ans == 0) {
            ans = Diameter.minimum;
        }
        return ans;
    }
}
