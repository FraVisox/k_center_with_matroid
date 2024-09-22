package it.unidp.dei.CAPPELLOTTO.CAPPVAL;

import it.unidp.dei.Algorithm;
import it.unidp.dei.CAPPELLOTTO.Utils.Guess.GuessVAL;
import it.unidp.dei.CAPPELLOTTO.Utils.Guess.KCGuessVAL;
import it.unidp.dei.Point;

import java.util.ArrayList;

//It is the same as KCAPP, but uses GuessVAL instead of Guess
public class KCAPPVAL implements Algorithm {

    public KCAPPVAL(int[] _ki, double _beta, double _minDist, double _maxDist) {

        //We use the definition to obtain the number of guesses
        int first_i = (int)Math.floor(Math.log(_minDist)/Math.log(1+_beta));
        int last_i = (int)Math.ceil(Math.log(_maxDist)/Math.log(1+_beta));
        int number_of_guesses = last_i-first_i+1;
        guesses = new KCGuessVAL[number_of_guesses];

        //We use the definition: we start from (1+beta)^first_i, and don't start from minDist as in Pellizzoni
        double gamma = Math.pow((1+_beta), first_i);
        for (int i = 0; i<number_of_guesses; i++) {
            guesses[i] = new KCGuessVAL(gamma, _ki);
            gamma *= (1+_beta);
        }
    }

    @Override
    public void update(Point p, int time) {
        for (GuessVAL g : guesses) {
            g.update(p, time);
        }
    }

    @Override
    public ArrayList<Point> query() {
        //Binary search on guesses
        int valid = binarySearchGuess();

        //If there isn't a valid guess, it returns null
        if (valid == -1) {
            return null;
        }
        return guesses[valid].query();
    }

    @Override
    public int getSize() {
        int size = 0;
        for (KCGuessVAL g : guesses) {
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
    private final KCGuessVAL[] guesses;
}
