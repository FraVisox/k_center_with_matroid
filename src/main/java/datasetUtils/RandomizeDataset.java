package datasetUtils;

import it.unidp.dei.datasetReaders.InputFileReader;
import it.unidp.dei.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Random;

//Randomize the dataset by simply swapping the lines
public class RandomizeDataset {
    private static final String outFolder = TestUtils.inFolderRandomized;
    private static final String inFolder = TestUtils.inFolderOriginals;

    //HIGGS does not need to be randomized, as it is already randomized. NORMALIZED is taken from the randomized covtype
    private static final String[] datasets = {"perfect_dataset.csv"};//{"Phones_accelerometer.csv", "covtype.dat"};

    //Tells if there is the header in the csv file
    private static final boolean[] isThereFirst = {false, false};

    public static void main(String[] args) {
        InputFileReader reader;
        PrintWriter writer;
        for (int i = 0; i<datasets.length; i++) {
            String set = datasets[i];
            try {
                //Create a file reader
                reader = new InputFileReader(inFolder+set);
                //Create a file writer
                writer = new PrintWriter(outFolder+set);
            } catch (FileNotFoundException e) {
                System.out.println("File " + inFolder + set + " not found, skipping to next dataset");
                continue;
            }

            ArrayList<String> strings = new ArrayList<>();
            ArrayList<String> centers = new ArrayList<>();

            //Takes all the lines (except the first if we are in Phones_accelerometer.csv)
            String first = null;

            int jj = 0;
            String p = reader.getLine();
            while (p != null) {
                if (jj < 22) {
                    centers.add(p);
                    jj++;
                } else {
                    strings.add(p);
                }
                p = reader.getLine();
            }

            reader.close();

            //Shuffle everything
            Collections.shuffle(strings);

            Collections.shuffle(centers);

            //Decide which are the indexes of the special 22 points
            int[] special_indexes = new int[22];
            Random rand = new Random();
            for (int kk = 0; kk<22; kk++) {
                special_indexes[kk] = 250+rand.nextInt(9999-250);
            }

            //Write lines
            int kk = 0;
            for (String s : strings) {
                boolean written = false;
                for (int kkk = 0; kkk < 22; kkk++) {
                    if (kk == special_indexes[kkk]) {
                        writer.write(centers.get(kkk));
                        written = true;
                    }
                }
                if (!written) {
                    writer.write(s);
                }
                kk++;
            }

            writer.flush();
            writer.close();
        }
    }
}
