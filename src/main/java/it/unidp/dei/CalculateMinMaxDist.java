package it.unidp.dei;

import it.unidp.dei.datasetReaders.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

public class CalculateMinMaxDist {
    private static final String outFolder = Main.inFolderRandomized;
    private static final String inFolder = Main.inFolderOriginals;
    private static final String[] datasets = {"Phones_accelerometer.csv", "covtype.dat"};
    private static final boolean[] isThereFirst = {true, false};
    private static final DatasetReader[] readers = {new PhonesReader(), new CovertypeReader(), new HiggsReader()};

    private static final double INFINITE = 10e20;

    public static void main(String[] args) {
        DatasetReader reader;
        for (int i = 0; i<datasets.length; i++) {
            String set = datasets[i];
            try {
                //Create a file reader
                reader = readers[i];
                reader.setFile(inFolder + set);
            } catch (FileNotFoundException e) {
                System.out.println("File " + inFolder + set + " not found, skipping to next dataset");
                continue;
            }

            double maxD = 0, minD = INFINITE;
            ArrayList<Point> window = new ArrayList<>();
            int time;
            for (time = 0; reader.hasNext(); time++) {
                Point p = reader.nextPoint(time, 0);
                window.add(p);

                maxD = Math.max(maxD, p.getMaxDistance(window));
                minD = Math.min(minD, p.getMinDistanceWithoutZeroes(window));

                if (time % 10000 == 0) {
                    System.out.println("At time: " + time);
                    System.out.println("Min distance: " + minD);
                    System.out.println("Max distance: " + maxD + "\n");
                }
            }
            System.out.println("FINAL DISTANCES at time: " + time);
            System.out.println("Min distance: " + minD);
            System.out.println("Max distance: " + maxD + "\n");
        }
    }
}
