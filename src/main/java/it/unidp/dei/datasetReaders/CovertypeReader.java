package it.unidp.dei.datasetReaders;

import it.unidp.dei.Point;

import java.io.FileNotFoundException;

public class CovertypeReader implements DatasetReader {
    private final InputFileReader reader;
    public static final int dimension = 54;
    public CovertypeReader(String fileName) throws FileNotFoundException {
        reader = new InputFileReader(fileName);
    }
    @Override
    public boolean hasNext() {
        return reader.hasMoreTokens();
    }

    public Point nextPoint(int time, int wSize) {
        double[] coords = new double[dimension];
        for(int i=0; i<dimension; i++){
            coords[i] = reader.getDouble();
        }

        //TODO: assegna ad ogni punto una categoria in modo intelligente
        return new Point(coords, time, wSize, 0);
    }

    public void close() {
        reader.close();
    }

}
