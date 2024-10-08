package it.unidp.dei;

import it.unidp.dei.CAPPELLOTTO.Utils.Diameter.Diameter;

import java.util.ArrayList;
import java.util.LinkedList;

//Interface for all the algorithms that resolve the problem of FKC
public interface Algorithm {

    //The function called when a new point is added to the window
    void update(Point p, int time);

    //Function to calculate a solution fo the FKC problem at time step t
    ArrayList<Point> query();

    //Function to get the number of points maintained in memory
    int getSize();

    //Utility function to calculate K given a set of ki
    static int calcK(int[] _ki) {
        int tmp = 0;
        for (int kj : _ki) {
            tmp += kj;
        }
        return tmp;
    }

    //Utility function to calculate the minimum distance between all the points in a list and a point p
    static double minPairwiseDistance(LinkedList<Point> points, Point p){
        double ans = p.getMinDistanceWithoutItself(points, TestUtils.INF);
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
