package it.unidp.dei;

import it.unidp.dei.CAPPELLOTTO.CAPP;
import it.unidp.dei.CAPPELLOTTO.CAPPObl;
import it.unidp.dei.CAPPELLOTTO.KCAPP;
import it.unidp.dei.CAPPELLOTTO.KCAPPObl;
import it.unidp.dei.CHENETAL.CHEN;
import it.unidp.dei.CHENETAL.KCHEN;
import it.unidp.dei.datasetReaders.CovertypeReader;
import it.unidp.dei.datasetReaders.DatasetReader;
import it.unidp.dei.datasetReaders.HiggsReader;
import it.unidp.dei.datasetReaders.PhonesReader;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    //Some parameters that are the same for every dataset
    private static final double epsilon = 0.1;
    private static final double beta = 1;
    //private static final int[] wSize = {10, 100, 1000, 10000, 100000};
    private static final int wSize = 1000;
    public static final double INF = 14000;
    //It tells how many times we will query the algorithms after having a complete window
    private static final int stride = 20;

    //Folders of input and output files
    public static final String inFolderOriginals = "src/main/java/it/unidp/dei/data/originals/";
    public static final String inFolderRandomized = "src/main/java/it/unidp/dei/data/randomized/";
    private static final String outFolder = "out/";

    //For every dataset we save the name of the input file, the name of the output file, ki, minDist, maxDist and the object to read the file
    private static final String[] datasets = {"Phones_accelerometer.csv", "covtype.dat", "HIGGS.csv"};
    private static final String[] outFiles = {"testPhones.csv", "testCovtype.csv", "testHiggs.csv"};
    //private static final int[][] ki = {{5, 5, 5, 5, 5, 5, 5}, {5, 5, 5, 5, 5, 5, 5}, {10, 10}}; TODO: decidi i ki
    private static final int[][] ki = {{5, 5, 5, 5, 5, 5, 5}, {10, 10, 10, 10, 10, 10, 10}, {10, 10}};
    //PHONES: maxD = 53 and minD = 8e-5 (tested for 840000 points and 530000 randomized, and there are 13062475)
    //COVTYPE: maxD = 13244 and minD = 4.8 (tested for 510000 points and there are 581012). Pellizzoni used 10000 and 0.1 TODO: ritestalo
    //HIGGS: maxD = 29 and minD = 0.008 (tested for 620000 points and there are 11000000). Pellizzoni used 100 and 0.01
    private static final double[] minDist = {8e-5, 4.8, 0.008};
    private static final double[] maxDist = {53, 13244, 29};
    private static final DatasetReader[] readers = {new PhonesReader(), new CovertypeReader(), new HiggsReader()};

    public static void main(String[] args) {
        DatasetReader reader;
        PrintWriter writer;

        for (int i = 0; i< datasets.length; i++) {
            String set = datasets[i];
            reader = readers[i];
            try {
                //Create a dataset reader
                if (reader instanceof HiggsReader) {
                    readers[i].setFile(inFolderOriginals+set);
                } else {
                    readers[i].setFile(inFolderRandomized + set);
                }
                //Create a results writer
                writer = new PrintWriter(outFolder+outFiles[i]);

            } catch (FileNotFoundException e) {
                System.out.println("File " + set + " not found, skipping to next dataset");
                continue;
            }

            //TEST THINGS
            int[] kiSet = ki[i];
            double max = maxDist[i];
            double min = minDist[i];

            testAlgorithms(reader, kiSet, min, max, writer);

            //FLUSH AND CLOSE
            writer.flush();

            writer.close();

            reader.close();
            System.out.println(set+" finished");
        }
    }

    //In every line of the output file we will have an header:
    //updateTime;queryTime;radius;independence;memory
    private static void testAlgorithms(DatasetReader reader, int[] kiSet, double min, double max, PrintWriter writer) {

        //Testing LinkedList, contains all the window
        LinkedList<Point> window = new LinkedList<>();

        //Initialize the algorithms
        Algorithm[] algorithms = new Algorithm[6];
        algorithms[0] = new CHEN(kiSet);
        algorithms[1] = new KCHEN(kiSet);
        algorithms[2] = new CAPP(kiSet, epsilon, beta, min, max);
        algorithms[3] = new KCAPP(kiSet, epsilon, beta, min, max);
        algorithms[4] = new CAPPObl(beta, epsilon, kiSet);
        algorithms[5] = new KCAPPObl(beta, epsilon, kiSet);
        writer.println("CHEN;;;;;;KCHEN;;;;;;CAPP;;;;;;KCAPP;;;;;;CAPPOBL;;;;;;KCAPPOBL;;");

        String header = "Update Time;Query Time;Radius;Independence;Memory";
        for (int i = 0; i<algorithms.length; i++) {
            writer.print(header);
            writer.print(";;");
        }
        writer.println();


        for (int time = 1; time <= wSize+stride && reader.hasNext(); time++) {

            //Check of passing of time, only used for debug purposes
            if (time > wSize) {
                System.out.println(time);
            }

            Point p = reader.nextPoint(time, wSize);

            if (p == null) {
                System.out.println("NULL POINT");
                continue;
            }

            //If window is not full, we don't query
            if (time <= wSize) {
                window.addLast(p);
                for (Algorithm alg : algorithms) {
                    alg.update(p, time);
                }
                continue;
            }

            //Update the window
            window.addLast(p);
            window.removeFirst();

            //Tests
            ArrayList<Point>[] centers = new ArrayList[algorithms.length];

            for (int i = 0; i<algorithms.length; i++) {
                calcUpdateTime(algorithms[i], p, time, writer);
                ArrayList<Point> cent = calcQuery(algorithms[i], writer, window, kiSet);
                centers[i] = cent;
                calcMemory(algorithms[i], writer);
                writer.print(";;");
            }
            writer.println();

            if (!centers[2].equals(centers[4]) || !centers[3].equals(centers[5])) {
                throw new RuntimeException();
            }
        }
    }

    private static void calcUpdateTime(Algorithm alg, Point p, int time, PrintWriter writer) {
        //TIME TEST: we call explicitly the garbage collector to allow our algorithm
        //           to run without having to wait for the garbage collector
        long startTime, endTime;
        System.gc();
        startTime = System.nanoTime();
        alg.update(p, time);
        endTime = System.nanoTime();
        //Write on file the time of update
        writer.print((endTime-startTime)+";");
    }

    private static ArrayList<Point> calcQuery(Algorithm alg, PrintWriter writer, LinkedList<Point> window, int[] kiSet) {
        ArrayList<Point> centers;
        long startTime, endTime;

        //1. TIME TEST: we call explicitly the garbage collector to allow our algorithm
        //              to run without having to wait for the garbage collector
        System.gc();
        startTime = System.nanoTime();
        centers = alg.query();
        endTime = System.nanoTime();
        writer.print((endTime-startTime)+";");

        if (centers.isEmpty()) {
            System.out.println("Max or min distances are not correct. There isn't a valid guess");
            throw new IllegalArgumentException();
        }

        //2. QUALITY TEST: Check of the radius of the centers returned and the independence of the set
        double radius = maxDistanceBetweenSets(window, centers);
        boolean independence = isIndependent(centers, kiSet);
        writer.print(String.format(Locale.ITALIAN, "%.16f", radius)+";"+(independence ? "T": "F")+";");
        return centers;
    }

    private static void calcMemory(Algorithm alg, PrintWriter writer) {
        writer.print(alg.getSize());
    }

    private static double maxDistanceBetweenSets(Collection<Point> set, Collection<Point> centers){
        double ans = 0;
        for(Point p : set){
            ans = Math.max(p.getMinDistance(centers), ans);
        }
        return ans;
    }

    private static boolean isIndependent(Collection<Point> set, int[] kiSet) {
        int[] kj = kiSet.clone();
        for (Point p : set) {
            kj[p.getGroup()] -= 1;
            if (kj[p.getGroup()] < 0) {
                return false;
            }
        }
        return true;
    }
}
