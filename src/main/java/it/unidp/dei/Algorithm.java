package it.unidp.dei;

import java.util.ArrayList;

public interface Algorithm {
    void update(Point p, int time);
    ArrayList<Point> query();
    int getSize();

    //Utility function to calculate K
    static int calcK(int[] _ki) {
        int tmp = 0;
        for (int kj : _ki) {
            tmp += kj;
        }
        return tmp;
    }
}
