package it.unidp.dei.datasetReaders;

import it.unidp.dei.Point;

import java.io.FileNotFoundException;

public class PhonesReader implements DatasetReader {
    private InputFileReader reader;
    public static final int dimension = 3;
    public static final int firstIgnored = 3;
    public static final int secondIgnored = 3;
    private boolean first = true;

    public void setFile(String fileName) throws FileNotFoundException {
        if (reader == null) {
            reader = new InputFileReader(fileName);
        }
    }

    public Point nextPoint(int time, int wSize) {
        if (first) {
            reader.skipFirstLine();
            first = false;
        }

        //Index, Arrival_time, Creation_time: to be discarded
        for(int i=0; i<firstIgnored; i++) {
            reader.getWord();
        }

        //Coordinates to be saved (x,y,z)
        double[] coords = new double[dimension];
        for(int i=0; i<dimension; i++) {
            coords[i] = reader.getDouble();
        }

        //User, model, device: to be discarded
        for(int i=0; i<secondIgnored; i++) {
            reader.getWord();
        }

        //Category:
        int category = getCategory(reader.getWord());
        if (category == -1) {
            return null;
        }

        return new Point(coords, time, wSize, category);
    }

    @Override
    public boolean hasNext() {
        return reader.hasMoreTokens();
    }

    public void close() {
        reader.close();
    }

    //The activities are: bike, sit, stand, walk, stairsup, stairsdown and null
    private static int getCategory(String s) {
        return switch (s) {
            case "bike" -> 0;
            case "sit" -> 1;
            case "stand" -> 2;
            case "walk" -> 3;
            case "stairsup" -> 4;
            case "stairsdown" -> 5;
            case "null" -> 6;
            default -> -1;
        };
    }

}
