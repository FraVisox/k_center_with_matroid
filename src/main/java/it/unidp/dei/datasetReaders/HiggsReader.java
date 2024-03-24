package it.unidp.dei.datasetReaders;

import it.unidp.dei.Point;

public class HiggsReader extends DatasetReader {
    @Override
    public Point nextPoint(int time, int wSize) {
        //The first coordinate gives the group: 0 or 1
        int pGroup = (int) reader.getDouble();

        //Coordinates to be discarded: the first 21
        for(int i=0; i<ignored; i++) {
            reader.getWord();
        }

        //Coordinates to be saved: the last 7
        double[] coords = new double[dimension];
        for(int i=0; i<dimension; i++) {
            coords[i] = reader.getDouble();
        }
        return new Point(coords, time, wSize, pGroup);
    }
    private static final int dimension = 7;
    private static final int ignored = 21;
}
