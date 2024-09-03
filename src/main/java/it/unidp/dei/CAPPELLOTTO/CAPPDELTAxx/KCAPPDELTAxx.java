package it.unidp.dei.CAPPELLOTTO.CAPPDELTAxx;

import it.unidp.dei.CAPPELLOTTO.CAPP.KCAPP;
import it.unidp.dei.CHENETAL.CHEN;

//It is a wrapper around KCAPP, as it only selects epsilon based on delta.
public class KCAPPDELTAxx extends KCAPP {
    public KCAPPDELTAxx(int[] _ki, double _delta, double _beta, double _minDist, double _maxDist) {
        super(_ki, _delta*(1+_beta)*(1+2*CHEN.alfa), _beta, _minDist, _maxDist);
    }
}
