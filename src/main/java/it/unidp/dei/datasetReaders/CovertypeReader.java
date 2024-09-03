package it.unidp.dei.datasetReaders;

import it.unidp.dei.Point;

//Reader of COVERTYPE (and also NORMALIZED)
public class CovertypeReader extends DatasetReader {
    @Override
    public Point nextPoint(int time, int wSize) {
        double[] coords = new double[dimension];
        for(int i=0; i<dimension; i++){
            coords[i] = reader.getDouble();
        }

        return new Point(coords, time, wSize, reader.getInt()-1);
    }
    public static final int dimension = 54;
}
