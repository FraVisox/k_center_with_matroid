package datasetUtils;

import it.unidp.dei.Point;
import it.unidp.dei.TestUtils;
import it.unidp.dei.datasetReaders.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;

public class FindNumberOfK {
    private static final String[] datasets = {"blobs5.csv", "blobs10.csv","blobs15.csv","blobs20.csv","blobs25.csv", "blobs30.csv", "blobs35.csv", "blobs40.csv", "blobs45.csv", "blobs50.csv","random20.csv", "HIGGS.csv", "Phones_accelerometer.csv", "covtype.dat", "normalizedcovtype.dat", "Phones_accelerometer.csv", "covtype.dat", "normalizedcovtype.dat"};
    private static final String[] outFiles = {"kiBlobs5.txt","kiBlobs10.txt","kiBlobs15.txt","kiBlobs20.txt","kiBlobs25.txt", "kiBlobs30.txt", "kiBlobs35.txt", "kiBlobs40.txt", "kiBlobs45.txt", "kiBlobs50.txt","kiRandom.txt", "kiHiggs.txt", "kiPhones_accelerometer.txt","kiCovtype.txt", "kiNormalizedCovtype.txt", "kiPhones_accelerometerORIGINAL.txt","kiCovtypeORIGINAL.txt", "kiNormalizedCovtypeORIGINAL.txt"};
    private static final DatasetReader[] readers = {new RandomReader(5),new RandomReader(10),new RandomReader(15),new RandomReader(20),new RandomReader(25), new RandomReader(30),new RandomReader(35),new RandomReader(40),new RandomReader(45),new RandomReader(50),new RandomReader(20), new HiggsReader(), new PhonesReader(), new CovertypeReader(), new CovertypeReader(), new PhonesReader(), new CovertypeReader(), new CovertypeReader()};
    private static final int[] outputTime = {500, 1000, 5000, 10000, 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000, 100000, 200000, 500000};
    private static final int[] numberOfKi = {7,7,7,7,7,7,7,7,7,7,6,2,7,7,7,7,7,7};

    public static void main(String[] args) {
        DatasetReader reader;
        PrintWriter writer;

        for (int i = 0; i<datasets.length; i++) {
            String set = datasets[i];
            try {
                //Create a file reader
                reader = readers[i];
                if (set.equals("HIGGS.csv") || i >= datasets.length-3) {
                    reader.setFile(TestUtils.inFolderOriginals + set);
                } else {
                    reader.setFile(TestUtils.inFolderRandomized + set);
                }
                writer = new PrintWriter(TestUtils.outFolder+outFiles[i]);
            } catch (FileNotFoundException e) {
                System.out.println("File " + set + " not found, skipping to next dataset");
                continue;
            }

            int[] ki = new int[numberOfKi[i]];
            Arrays.fill(ki, 0);
            int time;
            for (time = 1; reader.hasNext(); time++) {
                Point p = reader.nextPoint(time, 0);
                ki[p.getGroup()]++;
                for (int t : outputTime) {
                    if (time == t) {
                        writer.println("DISTRIBUTIONS at time "+time+":\n");
                        for (int j = 0; j<ki.length; j++) {
                            writer.println(j+" : "+ki[j]);
                        }
                        writer.println("\n\n");
                    }
                }
            }

            time--;
            writer.println("FINAL DISTRIBUTIONS at time "+time+":\n");
            for (int j = 0; j<ki.length; j++) {
                writer.println(j+" : "+ki[j]);
            }
            writer.println("\n\n");

            writer.flush();
            writer.close();
        }
    }
}
