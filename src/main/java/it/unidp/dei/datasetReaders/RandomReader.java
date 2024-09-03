package it.unidp.dei.datasetReaders;

import it.unidp.dei.Point;

import java.io.FileNotFoundException;
import java.util.Random;

//Reader of RANDOM and all the BLOBS datasets
public class RandomReader extends DatasetReader {
    public RandomReader(int dimension) {
        doublingDimension = dimension;
    }
    @Override
    public Point nextPoint(int time, int wSize) {
        double[] coords = new double[doublingDimension];
        for (int i = 0; i<doublingDimension; i++) {
            coords[i] = reader.getDouble();
        }
        return new Point(coords, time, wSize, reader.getInt());
    }
    protected final int doublingDimension;
}
