package it.unidp.dei.CAPPELLOTTO.Delta;

import it.unidp.dei.CAPPELLOTTO.Originals.KCAPP;
import it.unidp.dei.CHENETAL.CHEN;

public class KCAPPDelta extends KCAPP {
    public KCAPPDelta(int[] _ki, double _delta, double _beta, double _minDist, double _maxDist) {
        super(_ki, _delta*(1+_beta)*(1+2*CHEN.alfa), _beta, _minDist, _maxDist);
    }
}
