package datasetUtils;

import it.unidp.dei.Point;
import it.unidp.dei.TestUtils;
import it.unidp.dei.datasetReaders.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;

public class FindNumberOfK {
    private static final String[] datasets = {"random20.csv", "HIGGS.csv", "Phones_accelerometer.csv", "covtype.dat", "normalizedcovtype.dat"};
    private static final String[] outFiles = {"distr2KofRandom.txt", "distr2KofHIGGS.txt", "distr2KofPhones_accelerometer.txt","distr2KofCovtype.txt", "distr2KofNormalizedCovtype.txt"};
    private static final DatasetReader[] readers = {new RandomReader(20), new HiggsReader(), new PhonesReader(), new CovertypeReader(), new CovertypeReader()};
    private static final int outputTime = 500000;
    private static final int[] numberOfKi = {6,2,7,7,7};

    public static void main(String[] args) {
        DatasetReader reader;
        PrintWriter writer;

        for (int i = 0; i<datasets.length; i++) {
            String set = datasets[i];
            try {
                //Create a file reader
                reader = readers[i];
                if (!set.equals("HIGGS.csv")) {
                    reader.setFile(TestUtils.inFolderRandomized + set);
                } else {
                    reader.setFile(TestUtils.inFolderOriginals + set);
                }
                writer = new PrintWriter(TestUtils.outFolder+outFiles[i]);
            } catch (FileNotFoundException e) {
                System.out.println("File " + set + " not found, skipping to next dataset");
                continue;
            }

            int[] ki = new int[numberOfKi[i]];
            Arrays.fill(ki, 0);
            for (int time = 0; reader.hasNext() && time<outputTime; time++) {
                Point p = reader.nextPoint(time, 0);
                ki[p.getGroup()]++;
            }
            writer.println("FINAL DISTRIBUTIONS");
            for (int j = 0; j<ki.length; j++) {
                writer.println(j+" : "+ki[j]);
            }

            writer.flush();
            writer.close();
        }
    }
}
