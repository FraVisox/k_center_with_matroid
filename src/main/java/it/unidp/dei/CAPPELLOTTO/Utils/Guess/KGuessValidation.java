package it.unidp.dei.CAPPELLOTTO.Utils.Guess;

import it.unidp.dei.CHENETAL.KCHEN;
import it.unidp.dei.Point;

import java.util.*;

public class KGuessValidation extends GuessValidation {
    public KGuessValidation(double _gamma, int[] _ki) {
        super(_gamma, _ki);
    }

    public KGuessValidation(double _gamma, int[] _ki, TreeMap<Point, LinkedList<Point>[]> _R) {
        super(_gamma, _ki, _R);
    }

    @Override
    public ArrayList<Point> query() {
        LinkedList<Point> union = new LinkedList<>(OV);
        for (LinkedList<Point>[] list : RV.values()) {
            for (LinkedList<Point> l : list) {
                union.addAll(l);
            }
        }
        KCHEN chenEtAl = new KCHEN(union, ki);
        return chenEtAl.query();
    }
}
