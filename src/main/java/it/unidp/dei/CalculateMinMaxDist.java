package it.unidp.dei;

import it.unidp.dei.datasetReaders.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class CalculateMinMaxDist {
    private static final String[] datasets = {"covtype.dat", "HIGGS.csv", "Phones_accelerometer.csv"};
    private static final DatasetReader[] readers = { new CovertypeReader(), new HiggsReader(), new PhonesReader()};
    private static final double INFINITE = 10e20;
    private static final int outputTime = 10000;
    public static void main(String[] args) {
        DatasetReader reader;
        for (int i = 0; i<datasets.length; i++) {
            String set = datasets[i];
            try {
                //Create a file reader
                reader = readers[i];
                if (!set.equals("HIGGS.csv")) {
                    reader.setFile(Main.inFolderRandomized + set);
                } else {
                    reader.setFile(Main.inFolderOriginals + set);
                }
            } catch (FileNotFoundException e) {
                System.out.println("File " + set + " not found, skipping to next dataset");
                continue;
            }

            double maxD = 0, minD = INFINITE;
            ArrayList<Point> window = new ArrayList<>();
            int time;
            for (time = 0; reader.hasNext(); time++) {
                Point p = reader.nextPoint(time, 0);
                window.add(p);

                maxD = Math.max(maxD, p.getMaxDistance(window));

                //We don't want zeroes as log(0) is undefined
                minD = Math.min(minD, p.getMinDistanceWithoutZeroes(window, INFINITE));

                if (time % outputTime == 0) {
                    System.out.println(set+": at time: " + time);
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
