package it.unidp.dei;

import it.unidp.dei.CAPPELLOTTO.Delta.CAPPDelta;
import it.unidp.dei.CAPPELLOTTO.Delta.COHENCAPPOblDelta;
import it.unidp.dei.CAPPELLOTTO.Delta.PELLCAPPOblDelta;
import it.unidp.dei.CAPPELLOTTO.Originals.CAPP;
import it.unidp.dei.CAPPELLOTTO.Originals.COHENCAPPObl;
import it.unidp.dei.CAPPELLOTTO.Originals.PELLCAPPObl;
import it.unidp.dei.CAPPELLOTTO.Validation.CAPPValidation;
import it.unidp.dei.CAPPELLOTTO.Validation.COHENCAPPOblValidation;
import it.unidp.dei.CAPPELLOTTO.Validation.PELLCAPPOblValidation;
import it.unidp.dei.CHENETAL.CHEN;
import it.unidp.dei.datasetReaders.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

public class TestUtils {
    //Folders of input and output files
    public static final String inFolderOriginals = "data/originals/";
    public static final String inFolderRandomized = "data/randomized/";
    public static final String outFolder = "out/";

    //It tells how many times we will query the algorithms after wSize
    private static final int stride = 500;

    //Datasets: input files and output files, plus the DatasetReaders to read them
    private static final String[] datasets = {"Phones_accelerometer.csv", "covtype.dat", "HIGGS.csv", "random20.csv", "normalizedcovtype.dat"};
    private static final String[] outFiles = {"TestPhones.csv", "TestCovtype.csv", "TestHiggs.csv", "TestRandom20.csv", "TestNormalizedCovtype.csv"};
    private static final Class[] readers = {PhonesReader.class, CovertypeReader.class, HiggsReader.class, RandomReader.class, CovertypeReader.class};

    //Some default parameters that are the same for every dataset
    public static final double defaultEpsilon = 0.9;
    private static final double defaultDelta = 0.5;
    public static final double defaultBeta = 2;
    public static int defaultWSize = 10000;
    private static final int[][] defaultKi = {{2, 2, 2, 2, 2, 2, 2}, {2, 2, 2, 2, 2, 2, 2}, {2, 2}, {2, 2, 2, 2, 2, 2, 2}, {2, 2, 2, 2, 2, 2, 2}};
    public static final double INF = 8900;

    //VALUES OF MAX AND MIN DISTANCES (measured with CalculateMinMaxDist):
    //  PHONES: maxD = 52.6 and minD = 8.1e-5 (tested for 600000 points, and there are 13062475)
    //  COVTYPE: maxD = 8853.4 and minD = 2.82 (tested for all 581012 points)
    //  HIGGS: maxD = 26.7 and minD = 0.008 (tested for 600000 points and there are 11000000)
    //  RANDOM: maxD = 3.32 and minD = 0.35 (tested for 600000 points and there are 1000000)
    //  COVTYPE NORMALIZED: maxD = 2.9 and minD = 5.8e-4 (tested for all 581012 points)
    private static final double[] minDist = {8.1e-5, 2.82, 0.008, 0.35, 5.8e-4};
    private static final double[] maxDist = {52.6, 8853.4, 26.7, 3.32, 2.9};

    //Test of algorithms with standard parameters on randomized datasets
    public static void testRandomized() {
        testDatasets(true, null, defaultKi, defaultWSize, defaultEpsilon, defaultBeta, null);
    }

    //Test of algorithms with standard parameters on originals datasets
    public static void testOriginals() {
        testDatasets(false, null, defaultKi, defaultWSize, defaultEpsilon, defaultBeta, null);
    }

    //Test with different ki of standard datasets
    public static void testKi() {
        int[][][] ki = {
                {{1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}, {1, 1}, {1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}},
                {{2, 2, 2, 2, 2, 2, 2}, {2, 2, 2, 2, 2, 2, 2}, {5, 5}, {2, 2, 2, 2, 2, 2, 2}, {2, 2, 2, 2, 2, 2, 2}},
                {{5, 5, 5, 5, 5, 5, 5}, {5, 5, 5, 5, 5, 5, 5}, {10, 10}, {5, 5, 5, 5, 5, 5, 5}, {5, 5, 5, 5, 5, 5, 5}},
                {{10, 10, 10, 10, 10, 10, 10}, {10, 10, 10, 10, 10, 10, 10}, {15, 15}, {10, 10, 10, 10, 10, 10, 10}, {10, 10, 10, 10, 10, 10, 10}},
                {{15, 15, 15, 15, 15, 15, 15}, {15, 15, 15, 15, 15, 15, 15}, {20, 20}, {15, 15, 15, 15, 15, 15, 15}, {15, 15, 15, 15, 15, 15, 15}},
                {{20, 20, 20, 20, 20, 20, 20}, {20, 20, 20, 20, 20, 20, 20}, {25, 25}, {20, 20, 20, 20, 20, 20, 20}, {20, 20, 20, 20, 20, 20, 20}},
                {{25, 25, 25, 25, 25, 25, 25}, {25, 25, 25, 25, 25, 25, 25}, {30, 30}, {25, 25, 25, 25, 25, 25, 25}, {25, 25, 25, 25, 25, 25, 25}},
                {{50, 50, 50, 50, 50, 50, 50}, {50, 50, 50, 50, 50, 50, 50}, {50, 50}, {50, 50, 50, 50, 50, 50, 50}, {50, 50, 50, 50, 50, 50, 50}},
                {{100, 100, 100, 100, 100, 100, 100}, {100, 100, 100, 100, 100, 100, 100}, {100, 100}, {100, 100, 100, 100, 100, 100, 100}, {100, 100, 100, 100, 100, 100, 100}},
                {{500, 500, 500, 500, 500, 500, 500}, {500, 500, 500, 500, 500, 500, 500}, {500, 500}, {500, 500, 500, 500, 500, 500, 500}, {500, 500, 500, 500, 500, 500, 500}}
        };
        for (int[][] ints : ki) {
            testDatasets(true, "k" + ints[0][0], ints, defaultWSize, defaultEpsilon, defaultBeta, null);
        }
    }

    //Test with different epsilon on standard datasets
    public static void testEpsilon() {
        double[] epsilon = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        for (double e : epsilon) {
            testDatasets(true, "eps" + e, defaultKi, defaultWSize, e, defaultBeta, null);
        }
    }

    //Test with different beta on standard datasets
    public static void testBeta() {
        double[] beta = {0.1, 0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 8, 10, 15, 20, 30, 40, 50, 70, 100, 200, 500, 1000};
        for (double b : beta) {
            testDatasets(true, "beta" + b, defaultKi, defaultWSize, defaultEpsilon, b, null);
        }
    }

    //Test with different wSize on standard datasets
    public static void testWSize() {
        int[] wSize = {500, 1000, 5000, 10000, 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000, 100000, 200000, 500000};
        for (int w : wSize) {
            testDatasets(true, "w" + w, defaultKi, w, defaultEpsilon, defaultBeta, null);
        }
    }

    //Test only some algorithms (CHEN, CAPP, DELTA) with different delta
    public static void testDeltaW() {
        int[] wSizes = {500, 1000, 5000, 10000, 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000, 100000, 200000, 500000};
        double[] deltas = {0.025, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 1.1, 1.2, 1.3, 1.4, 1.5, 2};
        for (int ww : wSizes) {
            testDatasets(true, "deltas"+ww, defaultKi, ww, defaultEpsilon, defaultBeta, deltas);
        }
    }

    private static void testDatasets(boolean rand, String name, int[][] ki, int wSize, double epsilon, double beta, double[] delta) {
        DatasetReader reader;
        PrintWriter writer;
        for (int i = 0; i< datasets.length; i++) {
            String set = datasets[i];
            try {
                //Create a dataset reader
                if (readers[i] == RandomReader.class) {
                    //Dimension of random20
                    reader = new RandomReader(20);
                } else {
                    reader = (DatasetReader) readers[i].newInstance();
                }

                if (rand) {

                    //Instantiate the file
                    if (reader instanceof HiggsReader) {
                        reader.setFile(inFolderOriginals + set);
                    } else {
                        reader.setFile(inFolderRandomized + set);
                    }

                    //Create a results writer
                    if (name != null) {
                        writer = new PrintWriter(outFolder + "rand" + name + outFiles[i]);
                    } else {
                        writer = new PrintWriter(outFolder + "rand" + outFiles[i]);
                    }

                } else {

                    //Instantiate the file
                    if (reader instanceof RandomReader || reader instanceof HiggsReader) {
                        continue;
                    }
                    reader.setFile(inFolderOriginals + set);

                    //Create a results writer
                    if (name != null) {
                        writer = new PrintWriter(outFolder + "orig" + name + outFiles[i]);
                    } else {
                        writer = new PrintWriter(outFolder + "orig" + outFiles[i]);
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("File " + set + " not found, skipping to next dataset");
                continue;
            } catch (InstantiationException | IllegalAccessException e) {
                System.out.println("Problem with the search of the right reader for the "+set+"dataset");
                continue;
            }

            if (delta == null) {
                testAlgorithms(reader, writer, minDist[i], maxDist[i], ki[i], wSize, epsilon, beta);
            } else {
                testDeltasAlgorithms(reader, writer, ki[i], wSize, epsilon, beta, delta);
            }

            //CLOSE
            writer.close();

            reader.close();
            System.out.println(set+" finished");
        }
    }

    //In every line of the output file we will have a header:
    //updateTime;queryTime;radius;independence;memory
    public static void testAlgorithms(DatasetReader reader, PrintWriter writer, double min, double max, int[] kiSet, int wSize, double epsilon, double beta) {

        //Testing LinkedList, contains all the window
        LinkedList<Point> window = new LinkedList<>();

        //Initialize the algorithms
        Algorithm[] algorithms = new Algorithm[10];
        algorithms[0] = new CHEN(kiSet);
        algorithms[1] = new CAPP(kiSet, epsilon, beta, min, max);
        algorithms[2] = new COHENCAPPObl(beta, epsilon, kiSet);
        algorithms[3] = new PELLCAPPObl(beta, epsilon, kiSet);
        algorithms[4] = new CAPPDelta(kiSet, defaultDelta, beta, min, max);
        algorithms[5] = new COHENCAPPOblDelta(beta, defaultDelta, kiSet);
        algorithms[6] = new PELLCAPPOblDelta(beta, defaultDelta, kiSet);
        algorithms[7] = new CAPPValidation(kiSet, beta, min, max);
        algorithms[8] = new COHENCAPPOblValidation(beta, defaultDelta, kiSet);
        algorithms[9] = new PELLCAPPOblValidation(beta, defaultDelta, kiSet);

        writer.println("CHEN;;;;;CAPP;;;;;COHENCAPPOBL;;;;;PELLCAPPOBL;;;;;CAPPDELTA;;;;;COHENCAPPDELTA;;;;;PELLCAPPDELTA;;;;;CAPPVALIDATION;;;;;COHENCAPPVALIDATION;;;;;PELLCAPPVALIDATION;");

        String header = "Update Time;Query Time;Radius;Memory";
        for (int i = 0; i<algorithms.length; i++) {
            writer.print(header);
            writer.print(";;");
        }
        writer.println();

        for (int time = 1; time <= wSize+stride && reader.hasNext(); time++) {
            Point p = reader.nextPoint(time, wSize);

            if (p == null) {
                System.out.println("NULL POINT");
                continue;
            }

            //Update the window
            window.addLast(p);

            //If window is not full, we don't query
            if (time <= wSize) {
                for (Algorithm alg : algorithms) {
                    alg.update(p, time);
                }
                continue;
            }

            if (time % 100 == 0) {
                System.out.println(time);
            }

            window.removeFirst();

            ArrayList<Point>[] centers = new ArrayList[algorithms.length];
            //Tests
            for (int i = 0; i<algorithms.length; i++) {
                calcUpdateTime(algorithms[i], p, time, writer);
                centers[i] = calcQuery(algorithms[i], writer, window, kiSet);
                calcMemory(algorithms[i], writer);
                writer.print(";;");
            }
            writer.println();

            //FLUSH
            writer.flush();

            //TESTS REGARDING CENTERS
            if (!centers[1].equals(centers[2]) || !centers[1].equals(centers[3]) || !centers[4].equals(centers[5]) || !centers[4].equals(centers[6]) || !centers[7].equals(centers[8]) || !centers[7].equals(centers[9])) {
                throw new RuntimeException("Error in the implementation of the oblivious versions");
            }
        }
    }

    //In every line of the output file we will have a header:
    //updateTime;queryTime;radius;independence;memory
    private static void testDeltasAlgorithms(DatasetReader reader, PrintWriter writer, int[] kiSet, int wSize, double epsilon, double beta, double[] deltas) {

        //Testing LinkedList, contains all the window
        LinkedList<Point> window = new LinkedList<>();

        //Initialize the algorithms
        Algorithm[] algorithms = new Algorithm[2+deltas.length];
        algorithms[0] = new CHEN(kiSet);
        algorithms[1] = new PELLCAPPObl(beta, epsilon, kiSet);
        writer.print("CHEN;;;;;CAPP");
        for (int i = 2; i<algorithms.length; i++) {
            algorithms[i] = new PELLCAPPOblDelta(beta, deltas[i-2], kiSet);
            writer.print(";;;;;DELTA"+deltas[i-2]);
        }
        writer.println(";");

        String header = "Update Time;Query Time;Radius;Memory";
        for (int i = 0; i<algorithms.length; i++) {
            writer.print(header);
            writer.print(";;");
        }
        writer.println();

        for (int time = 1; time <= wSize+stride && reader.hasNext(); time++) {
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

            if (time % 100 == 0) {
                System.out.println(time);
            }

            //Update the window
            window.addLast(p);
            window.removeFirst();

            //Tests
            for (Algorithm algorithm : algorithms) {
                calcUpdateTime(algorithm, p, time, writer);
                calcQuery(algorithm, writer, window, kiSet);
                calcMemory(algorithm, writer);
                writer.print(";;");
            }
            writer.println();

            //FLUSH
            writer.flush();
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

        if (centers == null) {
            throw new RuntimeException("Max or min distances are not correct. There isn't a valid guess");
        }

        //2. QUALITY TEST: Check of the radius of the centers returned and the independence of the set
        double radius = maxDistanceBetweenSets(window, centers);
        if (!isIndependent(centers, kiSet)) {
            throw new RuntimeException(alg.getClass()+" did not solve the problem correctly");
        }
        writer.print(String.format(Locale.ITALIAN, "%.16f", radius)+";");
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
