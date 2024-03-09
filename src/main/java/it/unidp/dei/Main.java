package it.unidp.dei;

import it.unidp.dei.datasetReaders.DatasetReader;
import it.unidp.dei.datasetReaders.PhonesReader;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    public static final String inFolderOriginals = "src/main/java/it/unidp/dei/data/originals/";
    public static final String inFolderRandomized = "src/main/java/it/unidp/dei/data/randomized/";
    private static final String[] datasets = {"Phones_accelerometer.csv"};

    //TODO: testa CHEN per bene
    private static final int[] ki = {5, 5, 5, 5, 5, 5, 5};
    private static final double epsilon = 1;
    private static final double beta = 1;

    //PHONES: maxD = 30 and minD = 5e-4 (tested for 840000 points)
    //HIGGS: maxD = 29 and minD = 0.008 (tested for 620000 points). Pellizzoni used 100 and 0.01
    //COVTYPE: maxD = 13244 and minD = 4.8 (tested for 510000 points). Pellizzoni used 10000 and 0.1
    private static final double minDist = 5e-4;
    private static final double maxDist = 30;
    private static final int wSize = 5000;
    public static final double INF = maxDist+1;

    //It tells how many times we will query the algorithms after having a complete window
    private static final int stride = 10;

    public static void main(String[] args) {
        DatasetReader reader;
        PrintWriter writerChen;
        PrintWriter writerCapp;
        PrintWriter writerChenWithK;
        PrintWriter writerCappWihtK;
        for (String set : datasets) {

            try {
                //Create a dataset reader
                reader = new PhonesReader(inFolderRandomized+set);
                //Create a results writer
                writerChen = new PrintWriter("out/testPhonesCHEN.csv");
                writerCapp = new PrintWriter("out/testPhonesCAPP.csv");
                writerChenWithK = new PrintWriter("out/testPhonesCHENWithK.csv");
                writerCappWihtK = new PrintWriter("out/testPhonesCAPPWithK.csv");

            } catch (FileNotFoundException e) {
                System.out.println("File " + set + " not found, skipping to next dataset");
                continue;
            }

            //TEST THINGS
            testAlgorithms(reader, writerChen, writerCapp, writerChenWithK, writerCappWihtK);

            //FLUSH AND CLOSE
            writerChen.flush();
            writerCapp.flush();

            writerChenWithK.flush();
            writerCappWihtK.flush();

            writerChen.close();
            writerCapp.close();

            writerChenWithK.close();
            writerCappWihtK.close();

            reader.close();
        }
    }

    //In every line of the output file we will have an header:
    //updateTime;queryTime;radius;independence;memory
    private static void testAlgorithms(DatasetReader reader, PrintWriter writerChen, PrintWriter writerCapp, PrintWriter writerChenWithK, PrintWriter writerCappWithK) {
        long startTime, endTime;

        String header = "Update Time;Query Time;Radius;Independence;Memory";

        writerChen.println(header);
        writerChenWithK.println(header);
        writerCapp.println(header);
        writerCappWithK.println(header);

        //Testing LinkedList, contains all the window
        LinkedList<Point> window = new LinkedList<>();

        //Initialize the algorithm
        CAPP capp = new CAPP(ki, epsilon, beta, minDist, maxDist);
        CAPPWithK cappWithK = new CAPPWithK(ki, epsilon, beta, minDist, maxDist);
        CHEN chen = new CHEN(ki);
        CHENWithK chenWithK = new CHENWithK(ki);

        for (int time = 1; time <= wSize+stride; time++) {

            //Check of passing of time, only used for debug purposes
            if (time > wSize) {
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
                chenWithK.update(p, time);
                cappWithK.update(p, time);
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

            //CHEN
            System.gc();
            startTime = System.nanoTime();
            chenWithK.update(p, time);
            endTime = System.nanoTime();
            //Write on file the time of update
            writerChenWithK.print((endTime-startTime)+";");

            //CAPP
            System.gc();
            startTime = System.nanoTime();
            cappWithK.update(p, time);
            endTime = System.nanoTime();
            //Write on file the time of update
            writerCappWithK.print((endTime-startTime)+";");


            //1b. TIME TEST OF QUERY
            ArrayList<Point> centersChen;
            ArrayList<Point> centersCapp;
            ArrayList<Point> centersChenWithK;
            ArrayList<Point> centersCappWithK;

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

            //CHEN WITH K
            System.gc();
            startTime = System.nanoTime();
            centersChenWithK = chenWithK.query();
            endTime = System.nanoTime();
            //Write on file the time of query
            writerChenWithK.print((endTime-startTime)+";");

            //CAPP WITH K
            System.gc();
            startTime = System.nanoTime();
            centersCappWithK = cappWithK.query();
            endTime = System.nanoTime();
            //Write on file the time of query
            writerCappWithK.print((endTime-startTime)+";");


            //2. QUALITY TEST: Check of the radius of the centers returned and the independence of the set
            double radius = maxDistanceBetweenSets(window, centersChen);
            boolean independence = isIndependent(centersChen);
            //Write on file
            writerChen.print(String.format(Locale.ITALIAN, "%.20f", radius)+";"+(independence ? "SI": "NO")+";");

            radius = maxDistanceBetweenSets(window, centersCapp);
            independence = isIndependent(centersCapp);
            writerCapp.print(String.format(Locale.ITALIAN, "%.20f", radius)+";"+(independence ? "SI": "NO")+";");

            radius = maxDistanceBetweenSets(window, centersChenWithK);
            independence = isIndependent(centersChenWithK);
            writerChenWithK.print(String.format(Locale.ITALIAN, "%.20f", radius)+";"+(independence ? "SI": "NO")+";");

            radius = maxDistanceBetweenSets(window, centersCappWithK);
            independence = isIndependent(centersCappWithK);
            writerCappWithK.print(String.format(Locale.ITALIAN, "%.20f", radius)+";"+(independence ? "SI": "NO")+";");


            //3. MEMORY TEST: we only sum the number of points for every algorithm
            int memory = chen.getSize();
            //Write on file
            writerChen.println(memory);

            memory = capp.getSize();
            //Write on file
            writerCapp.println(memory);

            memory = chenWithK.getSize();
            writerChenWithK.println(memory);

            memory = cappWithK.getSize();
            writerCappWithK.println(memory);
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

    public static boolean isIndependent(Collection<Point> set) {
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
            minD = Math.min(minD, p.getMinDistanceWithoutZeroes(window));

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
