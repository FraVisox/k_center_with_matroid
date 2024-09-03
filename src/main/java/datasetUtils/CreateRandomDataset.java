package datasetUtils;

import it.unidp.dei.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

//Create the RANDOM dataset
public class CreateRandomDataset {
    private static final String file = "random20.csv";
    private static final int numOfPoints = 1000000;

    //Number of categories
    private static final int numOfGroups = 6;

    //Number of dimensions
    private static final int doublingDimension = 20;

    //Number of dimensions that are not zero
    private static final int notZeroes = 20;
    public static void main(String[] args) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(TestUtils.inFolderRandomized+file);
        } catch (FileNotFoundException e) {
            System.out.println("File can't be found");
            return;
        }
        Random randomizer = new Random();
        for (int i = 0; i<numOfPoints; i++) {
            for (int j = 0; j<notZeroes; j++) {
                writer.print(randomizer.nextDouble()+";");
            }
            for (int j = notZeroes; j<doublingDimension; j++) {
                writer.print(randomizer.nextDouble()+";");
            }
            writer.println(randomizer.nextInt(numOfGroups));
        }
        writer.flush();
        writer.close();
    }
}
