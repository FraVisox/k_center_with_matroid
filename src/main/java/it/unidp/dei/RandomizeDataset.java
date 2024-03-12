package it.unidp.dei;

import it.unidp.dei.datasetReaders.InputFileReader;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

public class RandomizeDataset {
    private static final String outFolder = Main.inFolderRandomized;
    private static final String inFolder = Main.inFolderOriginals;

    //HIGGS does not need to be randomized, as it is already randomized
    private static final String[] datasets = {"Phones_accelerometer.csv", "covtype.dat"};
    private static final boolean[] isThereFirst = {true, false};

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

            //Takes all the lines (except the first if we are in Phones_accelerometer.csv)
            String first = null;
            if (isThereFirst[i]) {
                first = reader.getLine();
            }

            String p = reader.getLine();
            int j = 0;
            while (p != null) {
                j++;
                strings.add(p);
                p = reader.getLine();
            }
            System.out.println(j);
            
            reader.close();

            Collections.shuffle(strings);

            if (isThereFirst[i]) {
                writer.write(first+"\n");
            }

            for (String s : strings) {
                writer.write(s+"\n");
            }

            writer.flush();
            writer.close();
        }
    }
}
