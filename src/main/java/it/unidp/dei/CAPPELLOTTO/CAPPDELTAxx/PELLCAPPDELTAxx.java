package it.unidp.dei.CAPPELLOTTO.CAPPDELTAxx;

import it.unidp.dei.CAPPELLOTTO.CAPP.PELLCAPP;
import it.unidp.dei.CHENETAL.CHEN;

//It is a wrapper around PELLCAPP, as it only selects epsilon based on delta.
public class PELLCAPPDELTAxx extends PELLCAPP
{
    public PELLCAPPDELTAxx(double _beta, double _delta, int[] _ki) {
        super(_beta, _delta*(1+_beta)*(1+2*CHEN.alfa), _ki);
    }
}

