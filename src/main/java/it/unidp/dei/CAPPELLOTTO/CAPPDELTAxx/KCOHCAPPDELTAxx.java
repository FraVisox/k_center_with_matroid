package it.unidp.dei.CAPPELLOTTO.CAPPDELTAxx;

import it.unidp.dei.CAPPELLOTTO.CAPP.KCOHCAPP;
import it.unidp.dei.CHENETAL.CHEN;

//It is a wrapper around KCOHCAPP, as it only selects epsilon based on delta.
public class KCOHCAPPDELTAxx extends KCOHCAPP
{
    public KCOHCAPPDELTAxx(double _beta, double _delta, int[] _ki) {
        super(_beta, _delta*(1+_beta)*(1+2*CHEN.alfa), _ki);
    }
}

