package it.unidp.dei.JONES;

import it.unidp.dei.Point;

//Utility class that stores one point and a distance or two points and their distance
public class Pair implements Comparable<Pair> {
    public Pair(double dd, Point pp, Point vvj) {
        p = pp;
        d = dd;
        vj = vvj;
    }

    public Pair(double dd, Point pp) {
        p = pp;
        d = dd;
        vj = null;
    }

    //Getters and setters
    public Point getP() {
        return p;
    }

    public double getD() {
        return d;
    }

    public void setD(double dd) {
        d = dd;
    }

    public Point getVj() {
        return vj;
    }

    @Override
    public int compareTo(Pair o) {
        return Double.compare(this.d, o.d);
    }

    private final Point vj;
    private final Point p;
    private double d;
}
