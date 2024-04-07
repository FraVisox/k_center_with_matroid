package it.unidp.dei.CAPPELLOTTO.Delta;

import it.unidp.dei.CAPPELLOTTO.Oblivious.COHENCAPPObl;
import it.unidp.dei.CHENETAL.CHEN;

public class COHENCAPPOblChooseDelta extends COHENCAPPObl
{
    public COHENCAPPOblChooseDelta(double _beta, double _delta, int[] _ki) {
        super(_beta, _delta*(1+_beta)*(1+2*CHEN.alfa), _ki);
    }
}

