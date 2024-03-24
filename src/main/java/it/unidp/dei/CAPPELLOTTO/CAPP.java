package it.unidp.dei.CAPPELLOTTO;

import it.unidp.dei.Algorithm;
import it.unidp.dei.CHENETAL.CHEN;
import it.unidp.dei.Point;

import java.util.ArrayList;

public class CAPP implements Algorithm {

    public CAPP(int[] _ki, double _epsilon, double _beta, double _minDist, double _maxDist) {
        //Calculate epsilon1 and then delta
        double epsilon1 = _epsilon/(1+2*CHEN.alfa);
        double delta = epsilon1/(1+_beta);

        //Initiate the guesses array. We don't use the definition, but an equivalent form
        int number_of_guesses = (int)Math.ceil(Math.log(_maxDist/_minDist)/Math.log(1+_beta))+1;

        /* This is how to use the definition to obtain the number of guesses
        int last_i = (int)Math.ceil(Math.log(maxDist)/Math.log(1+_beta));
        int number_of_guesses = last_i-first_i+1;
         */
        guesses = new Guess[number_of_guesses];

        int first_i = (int)Math.floor(Math.log(_minDist)/Math.log(1+_beta));

        //We use the definition: we start from (1+beta)^first_i, and don't start from minDist
        double gamma = Math.pow((1+_beta), first_i);

        int i;
        for (i = 0; i<number_of_guesses; i++) {
            guesses[i] = new Guess(gamma, delta, _ki);
            gamma *= (1+_beta);
        }

        //Check
        int last_i = (int)Math.ceil(Math.log(_maxDist)/Math.log(1+_beta));
        if (i+1 < last_i) {
            throw new RuntimeException("Error in the initialization of guesses of CAPP");
        }
    }

    @Override
    public void update(Point p, int time) {
        for (Guess g : guesses) {
            g.update(p, time);
        }
    }

    @Override
    public ArrayList<Point> query() {
        //Binary search on guesses
        int valid = binarySearchGuess();

        //If there isn't a valid guess, it returns an empty ArrayList
        if (valid == -1) {
            return new ArrayList<>();
        }
        return guesses[valid].query();
    }

    @Override
    public int getSize() {
        int size = 0;
        for (Guess g : guesses) {
            size += g.getSize();
        }
        return size;
    }

    private int binarySearchGuess() {
        int valid = -1;
        int low = 0;
        int high = guesses.length-1;
        while (low <= high) {
            int mid = (high + low) / 2;
            if (guesses[mid].isCorrect()) {
                valid = mid;
                high = mid - 1;
            } else  {
                low = mid + 1;
            }
        }
        return valid;
    }

    //Array of guesses
    private final Guess[] guesses;
}
