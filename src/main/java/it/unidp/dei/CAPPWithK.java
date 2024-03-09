package it.unidp.dei;

import java.util.ArrayList;

public class CAPPWithK implements Algorithm {

    public CAPPWithK(int[] _ki, double _epsilon, double _beta, double minDist, double maxDist) {
        //Calculate epsilon1 and then delta
        double epsilon1 = _epsilon/(1+2*alfa);
        double delta = epsilon1/(1+_beta);

        //Initiate the guesses array. We don't use the definition, but an equivalent form
        int number_of_guesses = (int)Math.ceil(Math.log(maxDist/minDist)/Math.log(1+_beta))+1;

        /* This is how to use the definition

        int first_i = (int)Math.floor(Math.log(minDist)/Math.log(1+_beta));
        int last_i = (int)Math.ceil(Math.log(maxDist)/Math.log(1+_beta));
        int number_of_guesses = last_i-first_i+1;
        double gamma = Math.pow((1+_beta), first_i);

         */

        guesses = new GuessWithK[number_of_guesses];
        double gamma = minDist;
        for (int i = 0; i<number_of_guesses; i++) {
            guesses[i] = new GuessWithK(gamma, delta, _ki);
            gamma *= (1+_beta);
        }
        assert gamma >= maxDist;
    }

    public void update(Point p, int time) {
        for (GuessWithK g : guesses) {
            g.update(p, time);
        }
    }

    public ArrayList<Point> query() {
        //Binary search on guesses
        int valid = binarySearchGuess();

        //If there isn't a valid guess, it returns an empty ArrayList
        if (valid == -1) {
            return new ArrayList<>();
        }
        return guesses[valid].query();
    }

    public int getSize() {
        int size = 0;
        for (GuessWithK g : guesses) {
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
    private final GuessWithK[] guesses;

    //Approximation of sequential algorithm. In our case, CHEN gives a 3-approximation
    private static final int alfa = 3;

}
