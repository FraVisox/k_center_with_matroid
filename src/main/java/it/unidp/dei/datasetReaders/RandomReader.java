package it.unidp.dei.datasetReaders;

import it.unidp.dei.Point;

import java.io.FileNotFoundException;
import java.util.Random;

public class RandomReader extends DatasetReader {
    @Override
    public Point nextPoint(int time, int wSize) {
        double[] coords = new double[doublingDimension];
        for (int i = 0; i<notZeroes; i++) {
            coords[i] = reader.getDouble();
        }
        for (int i = notZeroes; i<doublingDimension; i++) {
            reader.getWord();
        }
        return new Point(coords, time, wSize, reader.getInt());
    }
    public static final int doublingDimension = 20;
    public static final int notZeroes = 20;
}
