package it.unidp.dei.CAPPELLOTTO.Delta;

import it.unidp.dei.CAPPELLOTTO.Originals.CAPP;
import it.unidp.dei.CHENETAL.CHEN;

public class CAPPDelta extends CAPP {
    public CAPPDelta(int[] _ki, double _delta, double _beta, double _minDist, double _maxDist) {
        super(_ki, _delta*(1+_beta)*(1+2*CHEN.alfa), _beta, _minDist, _maxDist);
    }
}
