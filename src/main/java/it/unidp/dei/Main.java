package it.unidp.dei;

import it.unidp.dei.datasetReaders.DatasetReader;
import it.unidp.dei.datasetReaders.PhonesReader;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    private static final String[] datasets = {"src/main/java/it/unidp/dei/data/Phones/Phones_accelerometer.csv"};

    //TODO: il numero di centri puo' essere minore di 8 in questo caso? Per come è ora l'algoritmo si. Volendo si potrebbe aumentare in modo da ottenere 8 aggiungendo punti a caso della categoria giusta
    private static final int[] ki = {5, 5, 5, 5, 5, 5, 5};
    private static final double epsilon = 1;
    private static final double beta = 0.2;

    //TODO: per phones al tempo 840000 sembra maxD = 30 e minD = 5e-4
    private static final double minDist = 0.1;
    private static final double maxDist = 10e90;
    private static final int wSize = 10000;
    public static final double INF = maxDist+1;

    //Da un'indicazione di quante volte andremo a fare update e query dopo aver raggiunto wSize
    private static final int stride = 2000;

    public static void main(String[] args) {
        DatasetReader reader;
        PrintWriter writerChen;
        PrintWriter writerCapp;
        for (String set : datasets) {

            try {
                //Create a dataset reader
                reader = new PhonesReader(set);
                //Create a results writer
                writerChen = new PrintWriter("out/testPhonesCHEN.csv");
                writerCapp = new PrintWriter("out/testPhonesCAPP.csv");

            } catch (FileNotFoundException e) {
                System.out.println("File " + set + " not found, skipping to next dataset");
                continue;
            }

            //TEST THINGS
            calculateMinMaxDist(reader);


            //FLUSH AND CLOSE
            writerChen.flush();
            writerCapp.flush();

            writerChen.close();
            writerCapp.close();

            reader.close();
        }
    }

    private static void testAlgorithms(DatasetReader reader) {
        long startTime, endTime;

        //Testing LinkedList, contains all the window
        LinkedList<Point> window = new LinkedList<>();

        //Initialize the algorithm
        CAPP capp = new CAPP(ki, epsilon, beta, minDist, maxDist);
        CHEN chen = new CHEN(ki);

        for (int time = 0; time <= wSize+stride; time++) {

            //Check of passing of time, only used for debug
            if (time % wSize == 0) {
                System.out.println(time);
            }

            Point p = reader.nextPoint(time, wSize);

            if (p == null) {
                continue;
            }

            //If window is not full, we don't query
            if (time < wSize) {
                window.addLast(p);
                capp.update(p, time);
                chen.update(p, time);
                continue;
            }

            //Update the window
            window.addLast(p);
            window.removeFirst();

            //1. TIME TEST: we call explicitly the garbage collector to allow our algorithm
            //              to run without having to wait for the garbage collector

            //1a. TIME TEST OF UPDATE
            System.gc();
            startTime = System.nanoTime();
            chen.update(p, time);
            endTime = System.nanoTime();
            System.out.println("TEMPO UPDATE CHEN: "+(endTime-startTime)+"\n");


            System.gc();
            startTime = System.nanoTime();
            capp.update(p, time);
            endTime = System.nanoTime();
            System.out.println("TEMPO UPDATE CAPP: "+(endTime-startTime)+"\n");

            //TODO: write on file the time of update

            //1b. TIME TEST OF QUERY
            ArrayList<Point> centersChen;
            ArrayList<Point> centersCapp;

            System.out.println("CHEN:");
            System.gc();
            startTime = System.nanoTime();
            centersChen = chen.query();
            endTime = System.nanoTime();
            System.out.println("TEMPO QUERY CHEN: "+(endTime-startTime)+"\n");

            System.out.println("CAPP:");
            System.gc();
            startTime = System.nanoTime();
            centersCapp = capp.query();
            endTime = System.nanoTime();
            if (centersCapp.isEmpty()) {
                System.out.println("Max or min distances are not correct. There isn't a valid guess");
                return;
            }
            System.out.println("TEMPO QUERY CAPP: "+(endTime-startTime)+"\n");

            //TODO: write on file

            //2. QUALITY TEST: Check of the radius of the centers returned and the independence of the set
            System.out.println("RAGGIO CHEN: "+maxDistanceBetweenSets(window, centersChen));
            System.out.println("INDIPENDENZA CHEN: "+(isIndependent(centersChen) ? "Verificata" : "NON SUSSISTE"));
            System.out.println("RAGGIO CAPP: "+maxDistanceBetweenSets(window, centersCapp));
            System.out.println("INDIPENDENZA CAPP: "+(isIndependent(centersCapp) ? "Verificata" : "NON SUSSISTE"));

            //TODO: write on file

            //3. MEMORY TEST: we only sum the number of points for every algorithm
            int memory = capp.getSize();
            System.out.println("MEMORIA CAPP: "+memory);
            memory = chen.getSize();
            System.out.println("MEMORIA CHEN: "+memory);

            //TODO: write on file
        }
    }

    private static double maxDistanceBetweenSets(Collection<Point> set, Collection<Point> centers){
        double ans = 0;
        for(Point p : set){
            double dd = INF+1;
            for(Point q : centers){
                dd = Math.min(dd, p.getDistance(q));
            }
            ans = Math.max(dd, ans);
        }
        return ans;
    }

    private static boolean isIndependent(Collection<Point> set) {
        int[] kj = ki.clone();
        for (Point p : set) {
            kj[p.getGroup()] -= 1;
            if (kj[p.getGroup()] < 0) {
                return false;
            }
        }
        return true;
    }

    private static void calculateMinMaxDist(DatasetReader reader) {
        double maxD = 0, minD = INF;
        ArrayList<Point> window = new ArrayList<>(wSize);

        for (int time = 0; reader.hasNext(); time++) {
            Point p = reader.nextPoint(time, wSize);
            window.add(p);

            maxD = Math.max(maxD, p.getMaxDistance(window));
            minD = Math.min(minD, p.getMinDistance(window));

            if (time % 10000 == 0) {
                System.out.println("Al tempo: " + time);
                System.out.println("Minima distanza: " + minD);
                System.out.println("Massima distanza: " + maxD+"\n");
            }
        }
        System.out.println("Distanze finali:");
        System.out.println("Minima distanza: "+minD);
        System.out.println("Massima distanza: "+maxD);
    }
}
