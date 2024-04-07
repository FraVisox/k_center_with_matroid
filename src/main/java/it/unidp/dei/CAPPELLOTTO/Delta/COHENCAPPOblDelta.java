package it.unidp.dei.CAPPELLOTTO.Delta;

import it.unidp.dei.CAPPELLOTTO.Originals.COHENCAPPObl;
import it.unidp.dei.CHENETAL.CHEN;

public class COHENCAPPOblDelta extends COHENCAPPObl
{
    public COHENCAPPOblDelta(double _beta, double _delta, int[] _ki) {
        super(_beta, _delta*(1+_beta)*(1+2*CHEN.alfa), _ki);
    }
}

