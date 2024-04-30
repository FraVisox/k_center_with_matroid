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
    private static final int[] blobsDatasetsDimensions = {5, 10, 15, 20, 25, 50};

    //Ki of the blobs
    private static final int[] blobsKi = {3,3,3,3,3,3,3};

    //VALUES OF MAX AND MIN DISTANCES (measured with CalculateMinMaxDist). The first two are:
    private static final double[] blobsMinDist = {0.04, 0.65, 1.71, 2.81, 4, 8.9};
    private static final double[] blobsMaxDist = {163.1, 208.7, 235.4, 276.9, 278.6, 365.5};

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
            testAlgorithms(reader, writer, blobsMinDist[i], blobsMaxDist[i], blobsKi, defaultWSize, defaultEpsilon, defaultBeta);

            //CLOSE
            writer.close();

            reader.close();
            System.out.println("blobs"+dim+" finished");
        }
    }
}
