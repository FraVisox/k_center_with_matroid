package it.unidp.dei.CAPPELLOTTO.Diameter;

import it.unidp.dei.Point;

public abstract class Diameter {
    //The minimum, if the distance is 0. It's empirical (and less than the minimum of the distances)
    public static final double minimum = 1e-5;
    protected final double eps;

    public Diameter(double _eps) {
        eps = _eps;
    }

    public abstract double getDiameter();

    public abstract void update(Point p, int time);

    public abstract int getSize();
}
