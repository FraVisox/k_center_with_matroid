package it.unidp.dei;

import it.unidp.dei.CAPPELLOTTO.Delta.*;
import it.unidp.dei.CAPPELLOTTO.Originals.*;
import it.unidp.dei.CAPPELLOTTO.Validation.*;
import it.unidp.dei.CHENETAL.CHEN;
import it.unidp.dei.CHENETAL.KCHEN;
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
    private static final int stride = 200;

    //Datasets: input files and output files, plus the DatasetReaders to read them
    private static final String[] datasets = {"Phones_accelerometer.csv", "covtype.dat", "HIGGS.csv", "random20.csv", "normalizedcovtype.dat"};
    private static final String[] outFiles = {"TestPhones.csv", "TestCovtype.csv", "TestHiggs.csv", "TestRandom20.csv", "TestNormalizedCovtype.csv"};
    private static final Class[] readers = {PhonesReader.class, CovertypeReader.class, HiggsReader.class, RandomReader.class, CovertypeReader.class};

    //Some default parameters that are the same for every dataset
    public static final double defaultEpsilon = 0.9;
    private static final double[] defaultDeltas = {0.5, 1.0, 1.5, 2.0};
    public static final double defaultBeta = 2;
    public static int defaultWSize = 10000;
    private static final int[][] defaultKi = {{2, 2, 2, 2, 2, 2, 2}, {5, 7, 1, 0, 0, 0, 1}, {2, 2}, {2, 2, 2, 2, 2, 2}, {5, 7, 1, 0, 0, 0, 1}};
    public static final double INF = 8900;

    //VALUES OF MAX AND MIN DISTANCES (measured with CalculateMinMaxDist):
    //  PHONES: maxD = 52.6 and minD = 8.1e-5 (tested for 600000 points, and there are 13062475)
    //  COVTYPE: maxD = 8853.4 and minD = 2.82 (tested for all 581012 points)
    //  HIGGS: maxD = 26.7 and minD = 0.008 (tested for 600000 points and there are 11000000)
    //  RANDOM: maxD = 3.32 and minD = 0.35 (tested for 600000 points and there are 1000000)
    //  COVTYPE NORMALIZED: maxD = 2.9 and minD = 5.8e-4 (tested for all 581012 points)
    private static final double[] minDist = {8.1e-5, 2.82, 0.008, 0.35, 5.8e-4};
    private static final double[] maxDist = {52.6, 8853.4, 26.7, 3.32, 2.9};

    //VALUES OF RIGHT MAX AND MIN DISTANCES for 10.000 points:
    private static final double[] realMinDist = {0.002, 8.12, 0.02, 0.56, 0.007};
    private static final double[] realMaxDist = {33.7, 8693, 15.4, 3.09, 2.7};

    //Test of algorithms with standard parameters on randomized datasets
    public static void testRandomized() {
        testDatasets(true, null, defaultKi, defaultWSize, defaultBeta, null);
    }

    //Test of algorithms with standard parameters on originals datasets
    public static void testOriginals() {
        testDatasets(false, null, defaultKi, defaultWSize, defaultBeta, null);
    }

    //Test with different ki of standard datasets
    public static void testKi() {
        int[][][] ki = {
                //PHONES, COVTYPE, HIGGS, RANDOM, NORMALIZED
                {{1, 1, 1, 1, 1, 1, 1}, {3, 4, 0, 0, 0, 0, 0}, {1, 1}, {1, 1, 1, 1, 1, 1}, {3, 4, 0, 0, 0, 0, 0}},
                //{{2, 2, 2, 2, 2, 2, 2}, {5, 7, 1, 0, 0, 0, 1}, {2, 2}, {2, 2, 2, 2, 2, 2}, {5, 7, 1, 0, 0, 0, 1}}, DEFAULT
                {{5, 6, 5, 6, 4, 4, 5}, {13, 17, 2, 0, 1, 1, 1}, {9, 11}, {5, 5, 5, 5, 5, 5}, {13, 17, 2, 0, 1, 1, 1}},
                {{10, 11, 9, 12, 9, 9, 10}, {25, 35, 4, 0, 1, 2, 3}, {14, 16}, {10, 10, 10, 10, 10, 10}, {25, 35, 4, 0, 1, 2, 3}},
                {{15, 16, 14, 19, 13, 13, 15}, {37, 52, 6, 1, 2, 3, 4}, {19, 21}, {15, 15, 15, 15, 15, 15}, {37, 52, 6, 1, 2, 3, 4}},
                {{20, 22, 19, 25, 18, 17, 19}, {50, 69, 9, 1, 2, 4, 5}, {24, 26}, {20, 20, 20, 20, 20, 20}, {50, 69, 9, 1, 2, 4, 5}},
                {{25, 27, 24, 31, 22, 22, 24}, {62, 87, 11, 1, 3, 5, 6}, {28, 32}, {25, 25, 25, 25, 25, 25}, {62, 87, 11, 1, 3, 5, 6}},
                {{50, 55, 48, 62, 44, 43, 48}, {125, 173, 21, 2, 6, 11, 12}, {47, 53}, {50, 50, 50, 51, 50, 49}, {125, 173, 21, 2, 6, 11, 12}},
                {{99, 110, 95, 125, 88, 86, 97}, {250, 346, 43, 3, 12, 22, 24}, {94, 106}, {100, 100, 100, 102, 100, 98},  {250, 346, 43, 3, 12, 22, 24}},
        };
        for (int[][] ints : ki) {
            int k = Algorithm.calcK(ints[0]);
            testDatasets(true, "k" + k, ints, defaultWSize, defaultBeta, null);
        }
    }


    /* EPSILON TEST
    //Test with different epsilon on standard datasets
    public static void testEpsilon() {
        double[] epsilon = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8}; //, 0.9 DEFAULT
        for (double e : epsilon) {
            testDatasets(true, "eps" + e, defaultKi, defaultWSize, e, defaultBeta, null);
        }
    }
    */

    //Test with different beta on standard datasets
    public static void testBeta() {
        double[] beta = {0.5, 1, 1.5, /*,2 DEFAULT*/ 2.5, 3, 3.5, 4, 5, 10, 15, 20, 30, 40, 50, 70, 100, 200, 500, 1000};
        for (double b : beta) {
            testDatasets(true, "beta" + b, defaultKi, defaultWSize, b, null);
        }
    }

    //Test with different wSize on standard datasets
    public static void testWSize() {
        int[] wSize = {500, 1000, 5000, /* 10000 DEFAULT*/ 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000, 100000, 200000, 500000};
        for (int w : wSize) {
            testDatasets(true, "w" + w, defaultKi, w, defaultBeta, null);
            System.gc();
        }
    }

    //Test of K algorithms different wSize on standard datasets
    public static void testKAlg() {
        testDatasets(true, "kalg", defaultKi, defaultWSize, defaultBeta, new double[] {});
    }

    //TEST DATASETS, called in every test with different parameters
    private static void testDatasets(boolean rand, String name, int[][] ki, int wSize, double beta, double[] delta) {
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

                //IF RANDOM
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

                } else { //IF ORIGINAL

                    //Instantiate the file
                    if (reader instanceof RandomReader || reader instanceof HiggsReader) {
                        //Random and Higgs are already tested in randomized
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

            //Depending on deltas, call the testings
            if (delta == null) {
                if (name == null) {
                    //THIS IS ONLY FOR RANDOM AND ORIGINAL
                    System.out.println("Test differences\n\n");
                    testDiffAlgorithms(reader, writer, minDist[i], maxDist[i], realMinDist[i], realMaxDist[i], ki[i], wSize, TestUtils.defaultEpsilon, beta);
                } else {
                    System.out.println("Test algorithms\n\n");
                    testAlgorithms(reader, writer, ki[i], wSize, TestUtils.defaultEpsilon, beta);
                }
            } else if (delta.length == 0) {
                System.out.println("Test K-algorithms\n\n");
                testKAlgorithms(reader, writer, minDist[i], maxDist[i], ki[i], wSize, TestUtils.defaultEpsilon, beta);
            }

            //CLOSE
            writer.close();

            reader.close();
            System.out.println(set+" finished");
        }
    }

    //In every line of the output file we will have a header:
    //updateTime;queryTime;radius;ratio;memory
    public static void testAlgorithms(DatasetReader reader, PrintWriter writer, int[] kiSet, int wSize, double epsilon, double beta) {

        //Testing LinkedList, contains all the window
        LinkedList<Point> window = new LinkedList<>();

        //Initialize the algorithms
        Algorithm[] algorithms;
        if (((reader instanceof PhonesReader || reader instanceof HiggsReader) && wSize > 30000 && wSize < 50000)
            ||
            (reader instanceof CovertypeReader && wSize > 30000 && wSize <= 40000))    {
            //NO CHEN
            algorithms = new Algorithm[5];

            algorithms[0] = new PELLCAPPObl(beta, epsilon, kiSet);

            int i = 1;
            for (double dd : defaultDeltas) {
                algorithms[i] = new PELLCAPPOblDelta(beta, dd, kiSet);
                i++;
            }

            writer.println("PELLCAPP;;;;;;PELLCAPPDELTA05;;;;;;PELLCAPPDELTA10;;;;;;PELLCAPPDELTA15;;;;;;PELLCAPPDELTA20;;;;;;");

        } else if (((reader instanceof PhonesReader || reader instanceof HiggsReader) && wSize >= 50000)
            ||
            (reader instanceof CovertypeReader && wSize > 40000)){

            //NO CAPP
            algorithms = new Algorithm[4];

            int i = 0;
            for (;i<defaultDeltas.length;i++) {
                algorithms[i] = new PELLCAPPOblDelta(beta, defaultDeltas[i], kiSet);
            }

            writer.println("PELLCAPPDELTA05;;;;;;PELLCAPPDELTA10;;;;;;PELLCAPPDELTA15;;;;;;PELLCAPPDELTA20;;;;;;");
        } else if (reader instanceof RandomReader && wSize >= 40000) {
            //NO CAPPDELTA 0.5 or CAPP
            algorithms = new Algorithm[3];

            for (int i = 1; i < defaultDeltas.length; i++) {
                algorithms[i-1] = new PELLCAPPOblDelta(beta, defaultDeltas[i], kiSet);
            }

            writer.println("PELLCAPPDELTA10;;;;;;PELLCAPPDELTA15;;;;;;PELLCAPPDELTA20;;;;;;");
        } else {
            //DEFAULT, with everything
            algorithms = new Algorithm[6];
            algorithms[0] = new CHEN(kiSet);

            algorithms[1] = new PELLCAPPObl(beta, epsilon, kiSet);

            int i = 2;
            for (double dd : defaultDeltas) {
                algorithms[i] = new PELLCAPPOblDelta(beta, dd, kiSet);
                i++;
            }

            writer.println("CHEN;;;;;;PELLCAPP;;;;;;PELLCAPPDELTA05;;;;;;PELLCAPPDELTA10;;;;;;PELLCAPPDELTA15;;;;;;PELLCAPPDELTA20;;;;;;");
        }

        String header = "Update Time;Query Time;Radius;Ratio;Memory";
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

            if (time % 50 == 0) {
                System.out.println(time);
            }

            window.removeFirst();

            double minR = -1;

            //Tests
            int i = 0;
            for (Algorithm algorithm : algorithms) {
                calcUpdateTime(algorithm, p, time, writer);
                if (i == 0) {
                    minR = calcQuery(algorithm, writer, window, kiSet, -1);
                }
                else {
                    calcQuery(algorithm, writer, window, kiSet, minR);
                }
                calcMemory(algorithm, writer);
                writer.print(";;");
                i++;
            }
            writer.println();

            //FLUSH
            writer.flush();
        }
    }

    //In every line of the output file we will have a header:
    //updateTime;queryTime;radius;ratio;memory
    public static void testDiffAlgorithms(DatasetReader reader, PrintWriter writer, double min, double max, double realMin, double realMax, int[] kiSet, int wSize, double epsilon, double beta) {

        //Testing LinkedList, contains all the window
        LinkedList<Point> window = new LinkedList<>();

        //Initialize the algorithms
        Algorithm[] algorithms = new Algorithm[25];
        algorithms[0] = new CHEN(kiSet);

        algorithms[1] = new CAPP(kiSet, epsilon, beta, min, max);
        algorithms[2] = new CAPP(kiSet, epsilon, beta, realMin, realMax);
        algorithms[3] = new COHENCAPPObl(beta, epsilon, kiSet);
        algorithms[4] = new PELLCAPPObl(beta, epsilon, kiSet);

        int i = 5;

        for (double dd : defaultDeltas) {
            algorithms[i] = new CAPPDelta(kiSet, dd, beta, min, max);
            algorithms[i+1] = new CAPPDelta(kiSet, dd, beta, realMin, realMax);
            algorithms[i+2] = new COHENCAPPOblDelta(beta, dd, kiSet);
            algorithms[i+3] = new PELLCAPPOblDelta(beta, dd, kiSet);
            i += 4;
        }

        algorithms[i] = new CAPPValidation(kiSet, beta, min, max);
        algorithms[i+1] = new CAPPValidation(kiSet, beta, realMin, realMax);
        algorithms[i+2] = new COHENCAPPOblValidation(beta, kiSet);
        algorithms[i+3] = new PELLCAPPOblValidation(beta, kiSet);


        writer.println("CHEN;;;;;;CAPP;;;;;;REALCAPP;;;;;;COHCAPP;;;;;;PELLCAPP;;;;;;CAPPDELTA05;;;;;;REALCAPPDELTA05;;;;;;COHCAPPDELTA05;;;;;;PELLCAPPDELTA05;;;;;;CAPPDELTA10;;;;;;REALCAPPDELTA10;;;;;;COHCAPPDELTA10;;;;;;PELLCAPPDELTA10;;;;;;CAPPDELTA15;;;;;;REALCAPPDELTA15;;;;;;COHCAPPDELTA15;;;;;;PELLCAPPDELTA15;;;;;;CAPPDELTA20;;;;;;REALCAPPDELTA20;;;;;;COHCAPPDELTA20;;;;;;PELLCAPPDELTA20;;;;;;CAPPVAL;;;;;;REALCAPPVAL;;;;;;COHCAPPVAL;;;;;;PELLCAPPVAL;;;;;;;");

        String header = "Update Time;Query Time;Radius;Ratio;Memory";
        for (i = 0; i<algorithms.length; i++) {
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

            if (time % 50 == 0) {
                System.out.println(time);
            }

            window.removeFirst();


            double minR = -1;

            //Tests
            i = 0;
            for (Algorithm algorithm : algorithms) {
                calcUpdateTime(algorithm, p, time, writer);
                if (i == 0) {
                    minR = calcQuery(algorithm, writer, window, kiSet, -1);
                }
                else {
                    calcQuery(algorithm, writer, window, kiSet, minR);
                }
                calcMemory(algorithm, writer);
                writer.print(";;");
                i++;
            }
            writer.println();

            //FLUSH
            writer.flush();
        }
    }

    //In every line of the output file we will have a header:
    //updateTime;queryTime;radius;ratio;memory
    public static void testKAlgorithms(DatasetReader reader, PrintWriter writer, double min, double max, int[] kiSet, int wSize, double epsilon, double beta) {

        //Testing LinkedList, contains all the window
        LinkedList<Point> window = new LinkedList<>();

        //Initialize the algorithms
        Algorithm[] algorithms = new Algorithm[14];

        algorithms[0] = new CHEN(kiSet);
        algorithms[1] = new KCHEN(kiSet);

        algorithms[2] = new CAPP(kiSet, epsilon, beta, min, max);
        algorithms[3] = new KCAPP(kiSet, epsilon, beta, min, max);

        algorithms[4] = new PELLCAPPObl(beta, epsilon, kiSet);
        algorithms[5] = new KPELLCAPPObl(beta, epsilon, kiSet);

        algorithms[6] = new CAPPDelta(kiSet, defaultDeltas[0], beta, min, max);
        algorithms[7] = new KCAPPDelta(kiSet, defaultDeltas[0], beta, min, max);

        algorithms[8] = new PELLCAPPOblDelta(beta, defaultDeltas[0], kiSet);
        algorithms[9] = new KPELLCAPPOblDelta(beta, defaultDeltas[0], kiSet);

        algorithms[10] = new CAPPValidation(kiSet, beta, min, max);
        algorithms[11] = new KCAPPValidation(kiSet, beta, min, max);

        algorithms[12] = new PELLCAPPOblValidation(beta, kiSet);
        algorithms[13] = new KPELLCAPPOblValidation(beta, kiSet);


        writer.println("CHEN;;;;;;KCHEN;;;;;;CAPP;;;;;;KCAPP;;;;;;PELLCAPP;;;;;;KPELLCAPP;;;;;;CAPPDELTA;;;;;;KCAPPDELTA;;;;;;PELLCAPPDELTA;;;;;;KPELLCAPPDELTA;;;;;;CAPPVAL;;;;;;KCAPPVAL;;;;;;PELLCAPPVAL;;;;;;KPELLCAPPVAL;");

        String header = "Update Time;Query Time;Radius;Ratio;Memory";
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

            if (time % 50 == 0) {
                System.out.println(time);
            }

            window.removeFirst();

            double minR = -1;

            //Tests
            for (int i = 0; i<algorithms.length; i++) {
                calcUpdateTime(algorithms[i], p, time, writer);
                if (i == 0) {
                    minR = calcQuery(algorithms[i], writer, window, kiSet, -1);
                }
                else {
                    calcQuery(algorithms[i], writer, window, kiSet, minR);
                }
                calcMemory(algorithms[i], writer);
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

    private static double calcQuery(Algorithm alg, PrintWriter writer, LinkedList<Point> window, int[] kiSet, double minRadius) {
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

        if (radius == 0) {
            writer.print("0,0;");
        } else if (minRadius != -1) {
            writer.print(String.format(Locale.ITALIAN, "%.16f", radius / minRadius) + ";");
        } else {
            writer.print("1,0;");
        }
        return radius;
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
