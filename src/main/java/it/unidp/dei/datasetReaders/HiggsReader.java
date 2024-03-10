package it.unidp.dei.datasetReaders;

import it.unidp.dei.Point;

import java.io.FileNotFoundException;

public class HiggsReader implements DatasetReader {
    private InputFileReader reader;
    public static final int dimension = 7;
    public static final int ignored = 22;

    public void setFile(String fileName) throws FileNotFoundException {
        if (reader == null) {
            reader = new InputFileReader(fileName);
        }
    }

    public Point nextPoint(int time, int wSize) {
        //Coordinates to be discarded
        for(int i=1; i<=ignored; i++) {
            reader.getWord();
        }

        //Coordinates to be saved
        double[] coords = new double[dimension];
        for(int i=1; i<=dimension; i++) {
            coords[i] = reader.getDouble();
        }
        //TODO: assegna ad ogni punto una categoria in modo intelligente
        return new Point(coords, time, wSize, 0);
    }

    @Override
    public boolean hasNext() {
        return reader.hasMoreTokens();
    }

    public void close() {
        reader.close();
    }

}
