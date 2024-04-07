package it.unidp.dei.CAPPELLOTTO.Delta;

import it.unidp.dei.CAPPELLOTTO.Originals.KPELLCAPPObl;
import it.unidp.dei.CHENETAL.CHEN;

public class KPELLCAPPOblDelta extends KPELLCAPPObl
{
    public KPELLCAPPOblDelta(double _beta, double _delta, int[] _ki) {
        super(_beta, _delta*(1+_beta)*(1+2*CHEN.alfa), _ki);
    }
}

