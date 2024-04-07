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
    public static final String inFolderOriginals = "src/main/java/it/unidp/dei/data/originals/";
    public static final String inFolderRandomized = "src/main/java/it/unidp/dei/data/randomized/";
    public static final String outFolder = "out/";

    //Some parameters that are the same for every dataset
    private static final double epsilon = 1;
    private static final double delta = 0.5;
    private static final double beta = 20;
    public static final int wSize = 5000;
    public static final double INF = 9000;
    //It tells how many times we will query the algorithms after having a complete window
    private static final int stride = 30;

    //For every dataset we save the name of the input file, the name of the output file, ki, minDist, maxDist and the object to read the file
    private static final String[] datasets = {"Phones_accelerometer.csv", "covtype.dat", "HIGGS.csv", "random20.csv"};
    private static final String[] outFiles = {"TestPhones.csv", "TestCovtype.csv", "TestHiggs.csv", "TestRandom20.csv"};
    private static final Class[] readers = {PhonesReader.class, CovertypeReader.class, HiggsReader.class, RandomReader.class};
    private static final int[][] ki = {{1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}, {1, 1}, {1, 1, 1, 1, 1, 1, 1}};

    //VALUES OF MAX AND MIN DISTANCES:
    //  PHONES: maxD = 53 and minD = 8e-5 (tested for 840000 points and 530000 randomized, and there are 13062475)
    //  COVTYPE: maxD = 13244 and minD = 3.87 (tested for 510000 points and there are 581012). Pellizzoni used 10000 and 0.1
    //  HIGGS: maxD = 29 and minD = 0.008 (tested for 620000 points and there are 11000000). Pellizzoni used 100 and 0.01
    //  RANDOM: maxD = 3.32 and minD = 0.35 (tested for 410000 points and there are 1000000)

    //VALUES OF MAX AND MIN DISTANCES for randomized till time 100000
    //  PHONES: maxD = 47.6 and minD = 1.2e-4
    //  COVTYPE: maxD = 8834.2 and minD = 3.9
    //  HIGGS: maxD = 22.9 and minD = 0.011
    //  RANDOM: maxD = 3.22 and minD = 0.42
    private static final double[] minDist = {1.2e-4, 3.9, 0.011, 0.42};
    private static final double[] maxDist = {47.6, 8834.2, 22.9, 3.22};

    public static void main(String[] args) {
        //Tests to run
        testRandomized();
        /*
        testWSize();
        System.out.println("\n----------------------\nWSIZE TEST FINISHED\n----------------------\n");
        testEpsilon();
        System.out.println("\n----------------------\nEPSILON TEST FINISHED\n----------------------\n");
        testBeta();
        System.out.println("\n----------------------\nBETA TEST FINISHED\n----------------------\n");
        testKi();
        System.out.println("\n----------------------\nKI TEST FINISHED\n----------------------\n");
        */
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
                {{1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}, {1, 1}, {1, 1, 1, 1, 1, 1, 1}},
                {{2, 2, 2, 2, 2, 2, 2}, {2, 2, 2, 2, 2, 2, 2}, {5, 5}, {2, 2, 2, 2, 2, 2, 2}},
                {{5, 5, 5, 5, 5, 5, 5}, {5, 5, 5, 5, 5, 5, 5}, {10, 10}, {5, 5, 5, 5, 5, 5, 5}},
                {{10, 10, 10, 10, 10, 10, 10}, {10, 10, 10, 10, 10, 10, 10}, {15, 15}, {10, 10, 10, 10, 10, 10, 10}},
                {{15, 15, 15, 15, 15, 15, 15}, {15, 15, 15, 15, 15, 15, 15}, {20, 20}, {15, 15, 15, 15, 15, 15, 15}},
                {{20, 20, 20, 20, 20, 20, 20}, {20, 20, 20, 20, 20, 20, 20}, {25, 25}, {20, 20, 20, 20, 20, 20, 20}},
                {{25, 25, 25, 25, 25, 25, 25}, {25, 25, 25, 25, 25, 25, 25}, {30, 30}, {25, 25, 25, 25, 25, 25, 25}},
                {{50, 50, 50, 50, 50, 50, 50}, {50, 50, 50, 50, 50, 50, 50}, {50, 50}, {50, 50, 50, 50, 50, 50, 50}},
                {{100, 100, 100, 100, 100, 100, 100}, {100, 100, 100, 100, 100, 100, 100}, {100, 100}, {100, 100, 100, 100, 100, 100, 100}},
                {{500, 500, 500, 500, 500, 500, 500}, {500, 500, 500, 500, 500, 500, 500}, {500, 500}, {500, 500, 500, 500, 500, 500, 500}}
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
        int[] wSize = {10, 100, 500, 1000, 5000, 10000};

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


        writer.println("CHEN;;;;;;CAPP;;;;;;COHENCAPPOBL;;;;;;PELLCAPPOBL;;;;;;CAPPDELTA;;;;;;COHENCAPPDELTA;;;;;;PELLCAPPDELTA;;;;;;CAPPVALIDATION;;;;;;COHENCAPPVALIDATION;;;;;;PELLCAPPVALIDATION;;");

        String header = "Update Time;Query Time;Radius;Independence;Memory";
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

            //Check of passing of time
            System.out.println(time);

            //Update the window
            window.addLast(p);
            window.removeFirst();

            //Tests
            for (int i = 0; i<algorithms.length; i++) {
                calcUpdateTime(algorithms[i], p, time, writer);
                calcQuery(algorithms[i], writer, window, kiSet);
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

    private static void calcQuery(Algorithm alg, PrintWriter writer, LinkedList<Point> window, int[] kiSet) {
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

        //5002, 5004, 5207, 5295, 5176

        //2. QUALITY TEST: Check of the radius of the centers returned and the independence of the set
        double radius = maxDistanceBetweenSets(window, centers);
        if (!isIndependent(centers, kiSet)) {
            throw new RuntimeException(alg.getClass()+" did not solve the problem correctly");
        }
        writer.print(String.format(Locale.ITALIAN, "%.16f", radius)+";");
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
