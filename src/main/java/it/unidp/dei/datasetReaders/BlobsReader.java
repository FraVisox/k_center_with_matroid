package it.unidp.dei.datasetReaders;

import it.unidp.dei.Point;

public class BlobsReader extends RandomReader {
    public BlobsReader(int dimension) {
        super(dimension);
    }
    @Override
    public Point nextPoint(int time, int wSize) {
        double[] coords = new double[doublingDimension];
        for (int i = 0; i<doublingDimension; i++) {
            coords[i] = reader.getDouble();
        }
        return new Point(coords, time, wSize, reader.getInt()-1);
    }
}
