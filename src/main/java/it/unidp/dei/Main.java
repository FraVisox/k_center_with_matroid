package it.unidp.dei;

import it.unidp.dei.datasetReaders.DatasetReader;
import it.unidp.dei.datasetReaders.PhonesReader;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    private static final String[] datasets = {"src/main/java/it/unidp/dei/data/Phones/Phones_accelerometer.csv"};

    //TODO: il numero di centri puo' essere minore di k? Per come è ora l'algoritmo si. Volendo si potrebbe aumentare in modo da ottenere k aggiungendo punti a caso della categoria giusta
    //TODO: testa CHEN per bene
    private static final int[] ki = {5, 5, 5, 5, 5, 5, 5};
    private static final double epsilon = 1;
    private static final double beta = 1;

    //PHONES: maxD = 30 and minD = 5e-4 (tested for 840000 points)
    //HIGGS: maxD = 29 and minD = 0.008 (tested for 620000 points). Pellizzoni used 100 and 0.01
    private static final double minDist = 5e-4;
    private static final double maxDist = 30;
    private static final int wSize = 1000;
    public static final double INF = maxDist+1;

    //It tells how many times we will query the algorithms after having a complete window
    private static final int stride = 500;

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
            testAlgorithms(reader, writerChen, writerCapp);

            //FLUSH AND CLOSE
            writerChen.flush();
            writerCapp.flush();

            writerChen.close();
            writerCapp.close();

            reader.close();
        }
    }

    //In every line of the output file we will have:
    //updateTime;queryTime;radius;independence;memory
    private static void testAlgorithms(DatasetReader reader, PrintWriter writerChen, PrintWriter writerCapp) {
        long startTime, endTime;

        writerChen.println("Update Time;Query Time;Radius;Independence;Memory");
        writerCapp.println("Update Time;Query Time;Radius;Independence;Memory");

        //Testing LinkedList, contains all the window
        LinkedList<Point> window = new LinkedList<>();

        //Initialize the algorithm
        CAPP capp = new CAPP(ki, epsilon, beta, minDist, maxDist);
        CHEN chen = new CHEN(ki);

        for (int time = 1; time <= wSize+stride; time++) {

            //Check of passing of time, only used for debug
            if (time % wSize == 0) {
                System.out.println(time);
            }

            Point p = reader.nextPoint(time, wSize);

            if (p == null) {
                continue;
            }

            //If window is not full, we don't query
            if (time <= wSize) {
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
            //CHEN
            System.gc();
            startTime = System.nanoTime();
            chen.update(p, time);
            endTime = System.nanoTime();
            //Write on file the time of update
            writerChen.print((endTime-startTime)+";");

            //CAPP
            System.gc();
            startTime = System.nanoTime();
            capp.update(p, time);
            endTime = System.nanoTime();
            //Write on file the time of update
            writerCapp.print((endTime-startTime)+";");


            //1b. TIME TEST OF QUERY
            ArrayList<Point> centersChen;
            ArrayList<Point> centersCapp;

            //CHEN
            System.gc();
            startTime = System.nanoTime();
            centersChen = chen.query();
            endTime = System.nanoTime();
            //Write on file the time of query
            writerChen.print((endTime-startTime)+";");

            //CAPP
            System.gc();
            startTime = System.nanoTime();
            centersCapp = capp.query();
            endTime = System.nanoTime();
            if (centersCapp.isEmpty()) {
                System.out.println("Max or min distances are not correct. There isn't a valid guess");
                return;
            }
            //Write on file the time of query
            writerCapp.print((endTime-startTime)+";");


            //2. QUALITY TEST: Check of the radius of the centers returned and the independence of the set
            double radius = maxDistanceBetweenSets(window, centersChen);
            boolean independence = isIndependent(centersChen);
            //Write on file
            writerChen.print(radius+";"+(independence ? "": "NO")+";");

            radius = maxDistanceBetweenSets(window, centersCapp);
            independence = isIndependent(centersCapp);
            writerCapp.print(radius+";"+(independence ? "": "NO")+";");


            //3. MEMORY TEST: we only sum the number of points for every algorithm
            int memory = chen.getSize();
            //Write on file
            writerChen.println(memory);

            memory = capp.getSize();
            //Write on file
            writerCapp.println(memory);
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
