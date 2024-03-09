package it.unidp.dei;

import java.util.Arrays;
import java.util.Collection;

public class Point implements Comparable<Point> {
    public Point(double[] _coords, int _time, int _windowSize, int _group) {
        coords = _coords;
        exitTime = _time+_windowSize;
        group = _group;
    }

    public boolean hasExpired(int currentTime) {
        return exitTime <= currentTime;
    }

    public int getGroup() {
        return group;
    }

    public double getDistance(Point p) {
        double quad_dist = 0;
        for (int i = 0; i<coords.length; i++) {
            //It's faster to multiply than to use Math.pow
            quad_dist += (this.coords[i]-p.coords[i])*(this.coords[i]-p.coords[i]);
        }
        return Math.sqrt(quad_dist);
    }

    public double getMinDistance(Collection<Point> set) {
        double mind = Main.INF;
        for(Point q : set) {
            mind = Math.min(mind, this.getDistance(q));
        }
        return mind;
    }

    //It returns the minimum distance of this point from a Collection of points. If this
    //point is in that collection, that distance is skipped
    public double getMinDistanceWithoutZeroes(Collection<Point> set) {
        double mind = Main.INF;
        for(Point q : set) {
            if (!this.equals(q)) {
                mind = Math.min(mind, this.getDistance(q));
            }
        }
        return mind;
    }

    public double getMaxDistance(Collection<Point> set) {
        double maxd = 0;
        for(Point q : set) {
            maxd = Math.max(maxd, this.getDistance(q));
        }
        return maxd;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Point)) {
            return false;
        }
        return this.exitTime == ((Point)o).exitTime;
    }

    @Override
    public int compareTo(Point q) {
        return (this.exitTime-q.exitTime);
    }

    //This method isn't needed in our code, but it is a good practice to always make
    //hashCode() method match equals() method
    @Override
    public int hashCode() {
        return exitTime;
    }

    @Override
    public String toString() {
        return Integer.toString(exitTime);
    }

    private final double[] coords;
    private final int exitTime;
    private final int group;
}
