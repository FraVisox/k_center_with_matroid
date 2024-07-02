package it.unidp.dei;

import it.unidp.dei.datasetReaders.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import static it.unidp.dei.TestUtils.*;

public class BlobsTestUtils {
    //Folders of input and output files
    private static final String inFolder = "data/randomized/";
    private static final String outFolder = "out/";

    //For the blobs datasets, we only have the dimensions
    private static final int[] blobsDatasetsDimensions = {5, 10, 15, 20, 25, 30, 35, 40, 45, 50};

    //Ki of the blobs
    private static final int[] blobsKi = {3,3,3,3,3,3,3};

    //VALUES OF MAX AND MIN DISTANCES (measured with CalculateMinMaxDist)
    private static final double[] blobsMinDist = {0.04, 0.65, 1.71, 2.81, 4, 4.36, 5.83, 6.84, 7.4, 8.9};
    private static final double[] blobsMaxDist = {163.1, 208.7, 235.4, 276.9, 278.6, 323.1, 333.4, 340.7, 366.2, 365.5};

    //REAL
    private static final double[] realBlobsMinDist = {0.10, 0.65, 2.1, 3, 4.5, 5.5, 6.5, 7.4, 7.4, 9.6};
    private static final double[] realBlobsMaxDist = {160.6, 207.6, 234.5, 275.6, 276.5, 321.6, 332.2, 338.3, 364, 365.3};

    //Test of blobs datasets
    public static void testBlobs() {
        DatasetReader reader;
        PrintWriter writer;

        for (int i = 0; i < blobsDatasetsDimensions.length; i++) {
            int dim = blobsDatasetsDimensions[i];
            try {
                //Create a dataset reader
                reader = new RandomReader(dim);
                reader.setFile(inFolder+"blobs"+dim+".csv");
                //Create a results writer
                writer = new PrintWriter(outFolder+"testBlobs"+dim+".csv");
            } catch (FileNotFoundException e) {
                System.out.println("File blobs"+dim+".csv not found, skipping to next dataset");
                continue;
            }

            //TEST THINGS
            testAlgorithms(reader, writer, blobsMinDist[i], blobsMaxDist[i],realBlobsMinDist[i],realBlobsMaxDist[i], blobsKi, defaultWSize, defaultEpsilon, defaultBeta);

            //CLOSE
            writer.close();

            reader.close();
            System.out.println("blobs"+dim+" finished");
        }
    }
}
