package datasetUtils;

import it.unidp.dei.datasetReaders.*;
import it.unidp.dei.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

//Find min and max distances in the dataset at the specified output times
public class CalculateMinMaxDist {
    private static final String[] datasets = {"blobs5.csv", "blobs10.csv","blobs15.csv","blobs20.csv","blobs25.csv", "blobs30.csv", "blobs35.csv", "blobs40.csv", "blobs45.csv", "blobs50.csv","random20.csv", "HIGGS.csv", "Phones_accelerometer.csv", "covtype.dat", "normalizedcovtype.dat", "Phones_accelerometer.csv", "covtype.dat", "normalizedcovtype.dat"};
    private static final String[] outFiles = {"distBlobs5.txt","distBlobs10.txt","distBlobs15.txt","distBlobs20.txt","distBlobs25.txt", "distBlobs30.txt", "distBlobs35.txt", "distBlobs40.txt", "distBlobs45.txt", "distBlobs50.txt","distRandom.txt", "distHiggs.txt","distPhones_accelerometer.txt","distCovtype.txt", "distNormalizedCovtype.txt", "distPhones_accelerometerORIGINAL.txt","distCovtypeORIGINAL.txt", "distNormalizedCovtypeORIGINAL.txt"};
    private static final DatasetReader[] readers = {new RandomReader(5),new RandomReader(10),new RandomReader(15),new RandomReader(20),new RandomReader(25), new RandomReader(30),new RandomReader(35),new RandomReader(40),new RandomReader(45),new RandomReader(50),new RandomReader(20), new HiggsReader(), new PhonesReader(), new CovertypeReader(), new CovertypeReader(), new PhonesReader(), new CovertypeReader(), new CovertypeReader()};

    //Upper bound to the max distance (in the case of our datasets)
    private static final double INFINITE = 10e20;
    private static final int[] outputTime = {500, 1000, 5000, 10000, 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000, 100000, 200000, 500000};
    public static void main(String[] args) {
        DatasetReader reader;
        PrintWriter writer;

        for (int i = 0; i<datasets.length; i++) {
            String set = datasets[i];
            try {
                //Create a file reader. We use the randomized datasets
                reader = readers[i];
                if (set.equals("HIGGS.csv") || i >= datasets.length-3) {
                    reader.setSource(TestUtils.inFolderOriginals + set);
                } else {
                    reader.setSource(TestUtils.inFolderRandomized + set);
                }
                writer = new PrintWriter(TestUtils.outFolder+outFiles[i]);
            } catch (FileNotFoundException e) {
                System.out.println("File " + set + " not found, skipping to next dataset");
                continue;
            }

            double maxD = 0, minD = INFINITE;
            ArrayList<Point> window = new ArrayList<>();
            int time;
            for (time = 1; reader.hasNext() && time <= 500000; time++) {
                Point p = reader.nextPoint(time, 0);
                window.add(p);

                maxD = Math.max(maxD, p.getMaxDistance(window));

                //We don't want zeroes as log(0) is undefined
                minD = Math.min(minD, p.getMinDistanceWithoutZeroes(window, INFINITE));

                for (int t : outputTime) {
                    if (time == t) {
                        writer.println("At time: " + time + "\nMin distance: " + minD + "\nMax distance: " + maxD + "\n");
                        writer.flush();
                    }
                }

            }

            writer.println("FINAL: " + time + "\nMin distance: " + minD + "\nMax distance: " + maxD + "\n");
            writer.flush();

            writer.flush();
            writer.close();
        }
    }
}
