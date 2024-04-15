package it.unidp.dei;

import it.unidp.dei.datasetReaders.CovertypeReader;
import it.unidp.dei.datasetReaders.InputFileReader;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

//In this case, we only normalize covtype
public class NormalizeDataset {
    private static final String outFolder = Main.inFolderOriginals;
    private static final String inFolder = Main.inFolderOriginals;
    private static final String dataset = "covtype.dat";
    public static void main(String[] args) {
        InputFileReader reader;
        PrintWriter writer;
        try {
            //Create a file reader
            reader = new InputFileReader(inFolder+dataset);
            //Create a file writer
            writer = new PrintWriter(outFolder+"normalizedCovtype.dat");
        } catch (FileNotFoundException e) {
            System.out.println("File " + inFolder + dataset + " not found, skipping to next dataset");
            return;
        }

        double[] maxs = new double[CovertypeReader.dimension];
        double[] mins = new double[CovertypeReader.dimension];
        ArrayList<Double>[] coordinates = new ArrayList[CovertypeReader.dimension+1];
        for (int i = 0; i<coordinates.length; i++) {
            coordinates[i] = new ArrayList<>();
        }
        while (reader.hasMoreTokens()) {
            for (int i = 0; i<CovertypeReader.dimension; i++) {
                double d = reader.getDouble();
                if (d > maxs[i]) {
                    maxs[i] = d;
                } else if (d < mins[i]) {
                    mins[i] = d;
                }
                coordinates[i].add(d);
            }
            //Ignore the category
            coordinates[CovertypeReader.dimension].add(reader.getDouble());
        }
        reader.close();

        for (int i = 0; i<coordinates[0].size(); i++) {
            int j;
            for (j = 0; j<coordinates.length-1; j++) {
                writer.print((coordinates[j].get(i)-mins[j])/(maxs[j]-mins[j])+";");
            }
            writer.println((int)Math.ceil(coordinates[j].get(i))+";");
        }

        writer.flush();
        writer.close();

        //As a check:
        System.out.println(Arrays.toString(maxs));
        System.out.println(Arrays.toString(mins));
    }
}
