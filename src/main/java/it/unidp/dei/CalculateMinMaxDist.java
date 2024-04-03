package it.unidp.dei;

import it.unidp.dei.datasetReaders.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class CalculateMinMaxDist {
    private static final String[] datasets = {"random20.csv", "HIGGS.csv", "Phones_accelerometer.csv", "covtype.dat"};
    private static final String[] outFiles = {"distRandom.txt", "distHIGGS.txt", "distPhones_accelerometer.txt", "distCovtype.txt"};
    private static final DatasetReader[] readers = {new RandomReader(), new HiggsReader(), new PhonesReader(), new CovertypeReader()};
    private static final double INFINITE = 10e20;
    private static final int outputTime = 10000;
    public static void main(String[] args) {
        DatasetReader reader;
        PrintWriter writer;

        for (int i = 0; i<datasets.length; i++) {
            String set = datasets[i];
            try {
                //Create a file reader. We use the randomized datasets
                reader = readers[i];
                if (!set.equals("HIGGS.csv")) {
                    reader.setFile(Main.inFolderRandomized + set);
                } else {
                    reader.setFile(Main.inFolderOriginals + set);
                }
                writer = new PrintWriter(Main.outFolder+outFiles[i]);
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
                    System.out.println(set+": at time: " + time+"\nMin distance: " + minD+"\nMax distance: " + maxD + "\n");
                    writer.println("At time: " + time+"\nMin distance: " + minD+"\nMax distance: " + maxD + "\n");
                    writer.flush();
                }
            }
            System.out.println("FINAL DISTANCES at time: " + time+"\nMin distance: " + minD+"\nMax distance: " + maxD + "\n");
            writer.println("FINAL DISTANCES at time: " + time+"\nMin distance: " + minD+"\nMax distance: " + maxD + "\n");

            writer.flush();
            writer.close();
        }
    }
}
