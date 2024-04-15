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
import java.util.*;

public class Main {
    //Folders of input and output files
    public static final String inFolderOriginals = "data/originals/";
    public static final String inFolderRandomized = "data/randomized/";
    public static final String outFolder = "out/";

    //Some parameters that are the same for every dataset
    private static final double epsilon = 1;
    private static final double delta = 0.5;
    private static final double beta = 2;
    public static int wSize = 1000;
    public static int queryTime = 10000; //TODO: deve essere un punto standard da cui partire per fare i confronti dei raggi
    public static final double INF = 9000;
    //It tells how many times we will query the algorithms after having a complete window
    private static final int stride = 100;

    //For every dataset we save the name of the input file, the name of the output file, ki, minDist, maxDist and the object to read the file
    private static final String[] datasets = {"Phones_accelerometer.csv", "covtype.dat", "HIGGS.csv", "random20.csv", "normalizedcovtype.dat"};
    private static final String[] outFiles = {"TestPhones.csv", "TestCovtype.csv", "TestHiggs.csv", "TestRandom20.csv", "TestNormalizedCovtype.csv"};
    private static final Class[] readers = {PhonesReader.class, CovertypeReader.class, HiggsReader.class, RandomReader.class, CovertypeReader.class};
    private static final int[][] ki = {{1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}, {1, 1}, {1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}};

    //VALUES OF MAX AND MIN DISTANCES:
    //  PHONES: maxD = 53 and minD = 8e-5 (tested for 840000 points and 530000 randomized, and there are 13062475)
    //  COVTYPE: maxD = 13244 and minD = 3.87 (tested for 510000 points and there are 581012). Pellizzoni used 10000 and 0.1
    //  HIGGS: maxD = 29 and minD = 0.008 (tested for 620000 points and there are 11000000). Pellizzoni used 100 and 0.01
    //  RANDOM: maxD = 3.32 and minD = 0.35 (tested for 410000 points and there are 1000000)
    //  COVTYPE NORMALIZED: maxD = 2.9 and minD = 5e-4 (tested for 210000 points)

    //VALUES OF MAX AND MIN DISTANCES for randomized till time 100000
    //  PHONES: maxD = 47.6 and minD = 1.2e-4
    //  COVTYPE: maxD = 8834.2 and minD = 3.9
    //  HIGGS: maxD = 22.9 and minD = 0.011
    //  RANDOM: maxD = 3.22 and minD = 0.42
    //  COVTYPE NORMALIZED: maxD = 2.9 and minD = 0.02
    private static final double[] minDist = {1.2e-4, 3.9, 0.011, 0.42, 0.02};
    private static final double[] maxDist = {47.6, 8834.2, 22.9, 3.22, 2.9};

    public static void main(String[] args) {
        System.out.println("TESTS TO RUN:\n" +
                "- cambiare il delta con varie dimensioni di window per vedere la variazione dei parametri: wsize 1000 d\n" +
                "- aumento memoria/tempo in funzione di k: k\n" +
                "- aumento memoria/tempo in funzione di epsilon: e\n" +
                "- aumento memoria/tempo in funzione di beta: b\n");
        for (int i = 0; i<args.length; i++) {
            if (args[i].equalsIgnoreCase("wsize")) {
                wSize = Integer.parseInt(args[i+1]);
                break;
            }
        }
        //Tests to run
        if (Arrays.binarySearch(args, "r") >= 0) {
            System.out.println("\n----------------------\nSTART OF TEST OF RANDOMIZED DATASETS\n----------------------\n");
            testRandomized();
            System.out.println("\n----------------------\nTEST OF RANDOMIZED DATASETS FINISHED\n----------------------\n");
        }
        if (Arrays.binarySearch(args, "o") >= 0) {
            System.out.println("\n----------------------\nSTART OF TEST OF ORIGINAL DATASETS\n----------------------\n");
            testOriginals();
            System.out.println("\n----------------------\nTEST OF ORIGINAL DATASETS FINISHED\n----------------------\n");
        }
        if (Arrays.binarySearch(args, "w") >= 0) {
            System.out.println("\n----------------------\nSTART OF TEST OF WSIZE\n----------------------\n");
            testWSize();
            System.out.println("\n----------------------\nWSIZE TEST FINISHED\n----------------------\n");
        }
        if (Arrays.binarySearch(args, "e") >= 0) {
            System.out.println("\n----------------------\nSTART OF TEST OF EPSILON\n----------------------\n");
            testEpsilon();
            System.out.println("\n----------------------\nEPSILON TEST FINISHED\n----------------------\n");
        }
        if (Arrays.binarySearch(args, "b") >= 0) {
            System.out.println("\n----------------------\nSTART OF TEST OF BETA\n----------------------\n");
            testBeta();
            System.out.println("\n----------------------\nBETA TEST FINISHED\n----------------------\n");
        }
        if (Arrays.binarySearch(args, "k") >= 0) {
            System.out.println("\n----------------------\nSTART OF TEST OF KI\n----------------------\n");
            testKi();
            System.out.println("\n----------------------\nKI TEST FINISHED\n----------------------\n");
        }
        if (Arrays.binarySearch(args, "d") >= 0) {
            System.out.println("\n----------------------\nSTART OF TEST OF DELTAS\n----------------------\n");
            testDelta();
            System.out.println("\n----------------------\nDELTAS TEST FINISHED\n----------------------\n");
        }
        System.out.println("r = RANDOM\no = ORIGINALS\nw = WSIZE\ne = EPSILON\nb = BETA\nk = KI\nd = DELTAS");
    }

    //Test of algorithms on standard ki, wSize, epsilon and beta. The datasets used are the randomized ones
    private static void testRandomized() {
        DatasetReader reader;
        PrintWriter writer;

        for (int i = 0; i< datasets.length; i++) {
            String set = datasets[i];
            try {
                //Create a dataset reader
                reader = (DatasetReader) readers[i].newInstance();
                if (reader instanceof HiggsReader) {
                    reader.setFile(inFolderOriginals+set);
                } else {
                    reader.setFile(inFolderRandomized + set);
                }
                //Create a results writer
                writer = new PrintWriter(outFolder+"rand"+outFiles[i]);

            } catch (FileNotFoundException e) {
                System.out.println("File " + set + " not found, skipping to next dataset");
                continue;
            } catch (InstantiationException | IllegalAccessException e) {
                System.out.println("Problem with the search of the right reader for the "+set+"dataset");
                continue;
            }

            //TEST THINGS
            testAlgorithms(reader, writer, minDist[i], maxDist[i], ki[i], wSize, epsilon, beta);

            //CLOSE
            writer.close();

            reader.close();
            System.out.println(set+" finished");
        }
    }

    //Test of algorithms on standard ki, wSize, epsilon and beta. The datasets used are the originals
    private static void testOriginals() {
        DatasetReader reader;
        PrintWriter writer;

        for (int i = 0; i< datasets.length; i++) {
            String set = datasets[i];
            try {
                //Create a dataset reader
                reader = (DatasetReader) readers[i].newInstance();
                if (reader instanceof RandomReader) {
                    reader.setFile(inFolderRandomized+set);
                } else {
                    reader.setFile(inFolderOriginals + set);
                }
                //Create a results writer
                writer = new PrintWriter(outFolder+"orig"+outFiles[i]);

            } catch (FileNotFoundException e) {
                System.out.println("File " + set + " not found, skipping to next dataset");
                continue;
            } catch (InstantiationException | IllegalAccessException e) {
                System.out.println("Problem with the search of the right reader for the "+set+"dataset");
                continue;
            }

            //TEST THINGS
            testAlgorithms(reader, writer, minDist[i], maxDist[i], ki[i], wSize, epsilon, beta);

            //CLOSE
            writer.close();

            reader.close();
            System.out.println(set+" finished");
        }
    }

    //Test with different ki, with wSize, epsilon and beta standard
    private static void testKi() {
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

        DatasetReader reader;
        PrintWriter writer;

        for (int j = 0; j< ki.length; j++) {
            for (int i = 0; i < datasets.length; i++) {
                String set = datasets[i];
                try {
                    //Create a dataset reader
                    reader = (DatasetReader) readers[i].newInstance();
                    if (reader instanceof HiggsReader) {
                        reader.setFile(inFolderOriginals + set);
                    } else {
                        reader.setFile(inFolderRandomized + set);
                    }
                    //Create a results writer
                    writer = new PrintWriter(outFolder + "k" + ki[j][i][0] + outFiles[i]);

                } catch (FileNotFoundException e) {
                    System.out.println("File " + set + " not found, skipping to next dataset");
                    continue;
                } catch (InstantiationException | IllegalAccessException e) {
                    System.out.println("Problem with the search of the right reader for the "+set+"dataset");
                    continue;
                }

                //TEST THINGS
                testAlgorithms(reader, writer, minDist[i], maxDist[i], ki[j][i], wSize, epsilon, beta);

                //CLOSE
                writer.close();

                reader.close();
                System.out.println(set +" "+j+ " finished");
            }
        }
    }

    //Test with different epsilon, with ki, wSize and beta standard
    private static void testEpsilon() {
        double[] epsilon = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};

        DatasetReader reader;
        PrintWriter writer;

        for (int j = 0; j< epsilon.length; j++) {
            for (int i = 0; i < datasets.length; i++) {
                String set = datasets[i];
                try {
                    //Create a dataset reader
                    reader = (DatasetReader) readers[i].newInstance();
                    if (reader instanceof HiggsReader) {
                        reader.setFile(inFolderOriginals + set);
                    } else {
                        reader.setFile(inFolderRandomized + set);
                    }
                    //Create a results writer
                    writer = new PrintWriter(outFolder + "epsilon" + epsilon[j] + outFiles[i]);

                } catch (FileNotFoundException e) {
                    System.out.println("File " + set + " not found, skipping to next dataset");
                    continue;
                } catch (InstantiationException | IllegalAccessException e) {
                    System.out.println("Problem with the search of the right reader for the "+set+"dataset");
                    continue;
                }

                //TEST THINGS
                testAlgorithms(reader, writer, minDist[i], maxDist[i], ki[i], wSize, epsilon[j], beta);

                //CLOSE
                writer.close();

                reader.close();
                System.out.println(set +" "+j+ " finished");
            }
        }
    }

    //Test with different beta, with ki, epsilon and wSize standard
    private static void testBeta() {
        double[] beta = {0.1, 0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 8, 10, 15, 20, 30, 40, 50, 70, 100, 200, 500, 1000};

        DatasetReader reader;
        PrintWriter writer;

        for (int j = 0; j< beta.length; j++) {
            for (int i = 0; i < datasets.length; i++) {
                String set = datasets[i];
                try {
                    //Create a dataset reader
                    reader = (DatasetReader) readers[i].newInstance();
                    if (reader instanceof HiggsReader) {
                        reader.setFile(inFolderOriginals + set);
                    } else {
                        reader.setFile(inFolderRandomized + set);
                    }
                    //Create a results writer
                    writer = new PrintWriter(outFolder + "beta" + beta[j] + outFiles[i]);

                } catch (FileNotFoundException e) {
                    System.out.println("File " + set + " not found, skipping to next dataset");
                    continue;
                } catch (InstantiationException | IllegalAccessException e) {
                    System.out.println("Problem with the search of the right reader for the "+set+"dataset");
                    continue;
                }

                //TEST THINGS
                testAlgorithms(reader, writer, minDist[i], maxDist[i], ki[i], wSize, epsilon, beta[j]);

                //CLOSE
                writer.close();

                reader.close();
                System.out.println(set +" "+j+ " finished");
            }
        }
    }

    //Test with different wSize, with ki, epsilon and beta standard
    private static void testWSize() {
        int[] wSize = {500, 1000, 5000, 10000, 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000, 100000, 200000, 500000};

        DatasetReader reader;
        PrintWriter writer;

        for (int j = 0; j< wSize.length; j++) {
            for (int i = 0; i < datasets.length; i++) {
                String set = datasets[i];
                try {
                    //Create a dataset reader
                    reader = (DatasetReader) readers[i].newInstance();
                    if (reader instanceof HiggsReader) {
                        reader.setFile(inFolderOriginals + set);
                    } else {
                        reader.setFile(inFolderRandomized + set);
                    }
                    //Create a results writer
                    writer = new PrintWriter(outFolder + "wSize" + wSize[j] + outFiles[i]);

                } catch (FileNotFoundException e) {
                    System.out.println("File " + set + " not found, skipping to next dataset");
                    continue;
                } catch (InstantiationException | IllegalAccessException e) {
                    System.out.println("Problem with the search of the right reader for the "+set+"dataset");
                    continue;
                }

                //TEST THINGS
                testAlgorithms(reader, writer, minDist[i], maxDist[i], ki[i], wSize[j], epsilon, beta);

                //CLOSE
                writer.close();

                reader.close();
                System.out.println(set +" "+j+ " finished");
            }
        }
    }

    //Test only some algorithms with different delta
    private static void testDelta() {
        double[] deltas = {0.025, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 1.1, 1.2, 1.3, 1.4, 1.5, 2};

        DatasetReader reader;
        PrintWriter writer;

            for (int i = 0; i < datasets.length; i++) {
                String set = datasets[i];
                try {
                    //Create a dataset reader
                    reader = (DatasetReader) readers[i].newInstance();
                    if (reader instanceof HiggsReader) {
                        reader.setFile(inFolderOriginals + set);
                    } else {
                        reader.setFile(inFolderRandomized + set);
                    }
                    //Create a results writer
                    writer = new PrintWriter(outFolder + "deltas" + outFiles[i]);

                } catch (FileNotFoundException e) {
                    System.out.println("File " + set + " not found, skipping to next dataset");
                    continue;
                } catch (InstantiationException | IllegalAccessException e) {
                    System.out.println("Problem with the search of the right reader for the "+set+"dataset");
                    continue;
                }

                //TEST THINGS
                testDeltasAlgorithms(reader, writer, minDist[i], maxDist[i], ki[i], wSize, epsilon, beta, deltas);

                //CLOSE
                writer.close();

                reader.close();
                System.out.println(set +" finished");
            }
    }

    //In every line of the output file we will have a header:
    //updateTime;queryTime;radius;independence;memory
    private static void testAlgorithms(DatasetReader reader, PrintWriter writer, double min, double max, int[] kiSet, int wSize, double epsilon, double beta) {

        //Testing LinkedList, contains all the window
        LinkedList<Point> window = new LinkedList<>();

        //Initialize the algorithms
        Algorithm[] algorithms = new Algorithm[10];
        algorithms[0] = new CHEN(kiSet);
        algorithms[1] = new CAPP(kiSet, epsilon, beta, min, max);
        algorithms[2] = new COHENCAPPObl(beta, epsilon, kiSet);
        algorithms[3] = new PELLCAPPObl(beta, epsilon, kiSet);
        algorithms[4] = new CAPPDelta(kiSet, delta, beta, min, max);
        algorithms[5] = new COHENCAPPOblDelta(beta, delta, kiSet);
        algorithms[6] = new PELLCAPPOblDelta(beta, delta, kiSet);
        algorithms[7] = new CAPPValidation(kiSet, beta, min, max);
        algorithms[8] = new COHENCAPPOblValidation(beta, delta, kiSet);
        algorithms[9] = new PELLCAPPOblValidation(beta, delta, kiSet);

        writer.println("CHEN;;;;;CAPP;;;;;COHENCAPPOBL;;;;;PELLCAPPOBL;;;;;CAPPDELTA;;;;;COHENCAPPDELTA;;;;;PELLCAPPDELTA;;;;;CAPPVALIDATION;;;;;COHENCAPPVALIDATION;;;;;PELLCAPPVALIDATION;");

        String header = "Update Time;Query Time;Radius;Memory";
        for (int i = 0; i<algorithms.length; i++) {
            writer.print(header);
            writer.print(";;");
        }
        writer.println();


        for (int time = 1; time <= queryTime+stride && reader.hasNext(); time++) {
            Point p = reader.nextPoint(time, wSize);

            if (p == null) {
                System.out.println("NULL POINT");
                continue;
            }

            //If window is not full, we don't query
            if (time <= queryTime) {
                window.addLast(p);
                for (Algorithm alg : algorithms) {
                    alg.update(p, time);
                }
                continue;
            }

            //Check of passing of time
            System.out.println(time);

            //Update the window
            window.addLast(p);
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
    private static void testDeltasAlgorithms(DatasetReader reader, PrintWriter writer, double min, double max, int[] kiSet, int wSize, double epsilon, double beta, double[] deltas) {

        //Testing LinkedList, contains all the window
        LinkedList<Point> window = new LinkedList<>();

        //Initialize the algorithms
        Algorithm[] algorithms = new Algorithm[2+deltas.length];
        algorithms[0] = new CHEN(kiSet);
        algorithms[1] = new PELLCAPPObl(beta, epsilon, kiSet);
        for (int i = 2; i<algorithms.length; i++) {
            algorithms[i] = new PELLCAPPOblDelta(beta, deltas[i-2], kiSet);
        }

        writer.println("CHEN;;;;;CAPP;;;;;DELTAS;");

        String header = "Update Time;Query Time;Radius;Memory";
        for (int i = 0; i<algorithms.length; i++) {
            writer.print(header);
            writer.print(";;");
        }
        writer.println();


        for (int time = 1; time <= queryTime+stride && reader.hasNext(); time++) {
            Point p = reader.nextPoint(time, wSize);

            if (p == null) {
                System.out.println("NULL POINT");
                continue;
            }

            //If window is not full, we don't query
            if (time <= queryTime) {
                window.addLast(p);
                for (Algorithm alg : algorithms) {
                    alg.update(p, time);
                }
                continue;
            }

            //Check of passing of time
            System.out.println(time);

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
            System.out.println("Max or min distances are not correct. There isn't a valid guess");
            throw new IllegalArgumentException();
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
