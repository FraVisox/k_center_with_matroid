package it.unidp.dei;

import java.util.ArrayList;

//Conclusione: nessun grosso vantaggio, asintoticamente è peggio anzi
public class CAPPLinear implements Algorithm {

    public CAPPLinear(int[] _ki, double _epsilon, double _beta, double minDist, double maxDist) {
        //Calculate epsilon1 and then delta
        double epsilon1 = _epsilon/(1+2*alfa);
        double delta = epsilon1/(1+_beta);

        //Initiate the guesses array
        int first_i = (int)Math.floor(Math.log(minDist)/Math.log(1+_beta));
        int last_i = (int)Math.ceil(Math.log(maxDist)/Math.log(1+_beta));
        int number_of_guesses = last_i-first_i+1;
        guesses = new Guess[number_of_guesses];
        double gamma = Math.pow((1+_beta), first_i);
        for (int i = 0; i<number_of_guesses; i++) {
            guesses[i] = new Guess(gamma, delta, _ki);
            gamma *= (1+_beta);
        }
    }

    public void update(Point p, int time) {
        for (Guess g : guesses) {
            g.update(p, time);
        }
    }

    public ArrayList<Point> query() {
        for (Guess g : guesses) {
            if (g.isCorrect()) {
                return g.query();
            }
        }
        return new ArrayList<>();
    }

    public int getSize() {
        int size = 0;
        for (Guess g : guesses) {
            size += g.getSize();
        }
        return size;
    }

    //Array of guesses
    private final Guess[] guesses;

    //Approximation of sequential algorithm. In our case, CHEN gives a 3-approximation
    private static final int alfa = 3;

}
