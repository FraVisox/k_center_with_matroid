package it.unidp.dei;

public class Distance implements Comparable<Distance> {
    private double dist;
    public static final double epsilon = 10e-6;
    public Distance(double d) {dist=d;}
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Distance)) {
            return false;
        }
        return dist - ((Distance) other).dist <= epsilon && dist - ((Distance) other).dist >= -epsilon;
    }
    @Override
    public int compareTo(Distance other) {
        if (this.equals(other)) {
            return 0;
        }
        if (dist-other.dist < 0) {
            return -1;
        }
        return 1;
    }

    public double toDouble() {
        return dist;
    }
}
