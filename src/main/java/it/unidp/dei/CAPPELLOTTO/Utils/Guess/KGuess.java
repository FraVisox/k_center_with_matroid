package it.unidp.dei.CAPPELLOTTO.Utils.Guess;

import it.unidp.dei.CHENETAL.KCHEN;
import it.unidp.dei.Point;

import java.util.*;

//Guess used for K-Algorithms (it simply uses KCHEN instead of CHEN)
public class KGuess extends Guess {
    public KGuess(double _gamma, double _delta, int[] _ki) {
        super(_gamma, _delta, _ki);
    }

    public KGuess(double _gamma, double _delta, int[] _ki, TreeMap<Point, Point> _RV, TreeMap<Point, LinkedList<Point>[]> _R) {
        super(_gamma, _delta, _ki, _RV, _R);
    }

    @Override
    public ArrayList<Point> query() {
        LinkedList<Point> union = new LinkedList<>(O);
        for (LinkedList<Point>[] list : R.values()) {
            for (LinkedList<Point> l : list) {
                union.addAll(l);
            }
        }
        KCHEN chenEtAl = new KCHEN(union, ki);
        return chenEtAl.query();
    }
}
