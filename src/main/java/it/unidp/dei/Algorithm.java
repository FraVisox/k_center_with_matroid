package it.unidp.dei;

import java.util.ArrayList;

public interface Algorithm {
    void update(Point p, int time);

    ArrayList<Point> query();

    int getSize();
}
